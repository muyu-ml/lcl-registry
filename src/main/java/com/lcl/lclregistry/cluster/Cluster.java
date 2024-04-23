package com.lcl.lclregistry.cluster;

import com.lcl.lclregistry.config.LclRegistryConfigProperties;
import com.lcl.lclregistry.service.LclRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

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

    @Getter
    private List<Server> servers;

    public void init(){
        // host = InetAddress.getLocalHost().getHostAddress(); // 有可能拿到127.0.0.1
        // 使用 Spring Cloud Commons 的 InetUtils 获取真实 IP
        try {
            host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
            log.info(" ======>>>>> findFirstNonLoopbackHostInfo: {}", host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }

        // 标记当前服务
        MYSELF = new Server("http://"+host+":"+port, true, false, -1L);
        log.info(" ======>>>>> MYSELF: {}", MYSELF);
        // 初始化集群节点
        initServers();
        // 集群探活
        ServerHealth serverHealth = new ServerHealth(this);
        serverHealth.checkServerHealth();
    }

    /**
     * 初始化集群节点
     */
    private void initServers() {
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
    }


    /**
     * 获取当前节点
     * @return
     */
    public Server self(){
        // 更新版本号，为了保证版本号是最新的，所有使用 MYSELF 对象的地方都需要调用此方法获取
        MYSELF.setVersion(LclRegistryService.VERSION.get());
        return MYSELF;
    }

    /**
     * 获取集群leader节点
     * @return
     */
    public Server leader(){
        return servers.stream().filter(Server::isLeader).findFirst().orElse(null);
    }

}
