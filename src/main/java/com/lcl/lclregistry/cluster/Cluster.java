package com.lcl.lclregistry.cluster;

import com.lcl.lclregistry.LclRegistryConfigProperties;
import com.lcl.lclregistry.http.HttpInvoker;
import com.lcl.lclregistry.service.LclRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 集群行为
 * @Author conglongli
 * @date 2024/4/22 21:28
 */
@Slf4j
public class Cluster {

    String host;

    @Value("${server.port}")
    String port;

    Server MYSELF;

    LclRegistryConfigProperties registryConfigProperties;

    public Cluster(LclRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    int interval = 5_000;

    @Getter
    private List<Server> servers;

    public void init(){
        // host = InetAddress.getLocalHost().getHostAddress(); // 有可能拿到127.0.0.1
        // 使用 Spring Cloud Commons 的 InetUtils 获取真实 IP
        try {
            host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
            log.debug(" ======>>>>> findFirstNonLoopbackHostInfo: {}", host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }

        // 标记当前服务
        MYSELF = new Server("http://"+host+":"+port, true, false, -1L);
        log.debug(" ======>>>>> MYSELF: {}", MYSELF);

        List<Server> servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerList()) {
            Server server = new Server();
            // 替换本机地址为 host，防止配置的是回环地址，与获取的host不一致
            if(url.contains("localhost")){
                url = url.replace("localhost", host);
            } else if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }
            // 保证集合中的当前节点为 MYSELF 对象，在探活后更新的就是 MYSELF 对象
            if(url.equals(self().getUrl())){
                servers.add(self());
            } else {
                server.setUrl(url);
                server.setStatus(false);
                server.setLeader(false);
                server.setVersion(-1L);
                servers.add(server);
            }
        }
        this.servers = servers;
        executorService.scheduleWithFixedDelay(() -> {
            try {
                log.debug(" =====>>>> Cluster running...");
                // 集群探活
                updateServers();
                // 集群选主
                electLeader();
                // 数据同步
                syncSnapshotFromLeader();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * 从leader节点同步数据
     */
    private void syncSnapshotFromLeader() {
        log.info(" syncSnapshotFromLeader ===============================>>>> ");
        // 如果当前节点不是leader，且版本比leader小，则需要从leader节点同步数据
        if(!self().isLeader() && self().getVersion() < leader().getVersion()){
            // 从leader节点获取快照数据
            Snapshot snapshot = HttpInvoker.httpGet(leader().getUrl() + "/snapshot", Snapshot.class);
            log.info(" =====>>>> leader version：{}, my version：{}, snapshot: {}", leader().getVersion(), self().getVersion(), snapshot);
            // 更新当前节点数据
            LclRegistryService.restore(snapshot);
        }
    }

    /**
     * 集群选主
     */
    private void electLeader() {
        log.info(" electLeader ===============================>>>> ");
        // 获取所有存活且认为自己是master的节点
        List<Server> masters = this.servers.stream().filter(Server::isStatus).filter(Server::isLeader).collect(Collectors.toList());
        if(masters.isEmpty()){
            log.info("  =====>>>> elect for no leader：{}", servers);
            elect();
        } else if(masters.size() > 1) {
            log.info(" =====>>>> elect for more than one leader: {}", masters);
            elect();
        } else {
            log.info(" =====>>>> no need election for leader: {}", masters.get(0));
        }
    }

    /**
     * 集群选主：使用第一种方式
     * 常用的选举：
     *      1、各个节点选自己为leader，算法保证各个节点最终选出的leader是一致的
     *      2、外部有一个分布式锁，谁拿到锁谁就是leader
     *      3、分布式一致性算法，比如 paxos、raft，很复杂
     */
    private void elect() {
        Server candidate = null;
        for (Server server : servers) {
            // 选举前先清空所有节点的leader标识，防止脑裂恢复后出现多leader情况
            server.setLeader(false);
            // 选出存活的节点中 hashCode(url的hashCode)最小的，这种算法简单，不需要各个节点同步谁是leader
            if(server.isStatus()){
                if(candidate == null){
                    candidate = server;
                } else {
                    if(server.hashCode() < candidate.hashCode()){
                        candidate = server;
                    }
                }
            }
        }

        // 选出leader
        if(candidate != null){
            candidate.setLeader(true);
            log.info(" =====>>>> elect new leader is: {}", candidate);
        } else {
            log.info(" =====>>>> elect failed for no leader");
        }

    }

    /**
     * 集群探活
     */
    private void updateServers() {
        log.info(" updateServers ===============================>>>> ");
        // 并发探活
        servers.stream().parallel().forEach(server -> {
            try {
                // 自身不需要探活
                if(server.equals(self())){
                    return;
                }
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.info(" =====>>>> health check success for: {}", serverInfo);
                if(serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                } else {
                    server.setStatus(false);
                }
            } catch (Exception e){
                log.error(" =====>>>> health check failed for: {}", servers);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    public Server self(){
        // 更新版本号，为了保证版本号是最新的，所有使用 MYSELF 对象的地方都需要调用此方法获取
        MYSELF.setVersion(LclRegistryService.VERSION.get());
        return MYSELF;
    }

    public Server leader(){
        return servers.stream().filter(Server::isLeader).findFirst().orElse(null);
    }

}
