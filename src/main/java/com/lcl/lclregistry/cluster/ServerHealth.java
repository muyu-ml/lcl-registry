package com.lcl.lclregistry.cluster;

import com.lcl.lclregistry.http.HttpInvoker;
import com.lcl.lclregistry.service.LclRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * check the health of server
 * @Author conglongli
 * @date 2024/4/23 23:30
 */
@Slf4j
public class ServerHealth {

    final Cluster cluster;

    public ServerHealth(Cluster cluster){
        this.cluster = cluster;
    }

    final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    int interval = 5_000;


    public void checkServerHealth(){
        executorService.scheduleWithFixedDelay(() -> {
            try {
                log.debug(" =====>>>> Cluster running...");
                // 集群探活
                updateServers();
                // 集群选主
                doElect();
                // 数据同步
                syncSnapshotFromLeader();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    private void doElect() {
        new Election(cluster.getServers()).electLeader();
    }


    /**
     * 从leader节点同步数据
     */
    private void syncSnapshotFromLeader() {
        Server self = cluster.self();
        Server leader = cluster.leader();
        log.debug(" syncSnapshotFromLeader ===============================>>>> ");
        // 如果当前节点不是leader，且版本比leader小，则需要从leader节点同步数据
        if(!self.isLeader() && self.getVersion() < leader.getVersion()){
            // 从leader节点获取快照数据
            Snapshot snapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);
            log.info(" =====>>>> leader version：{}, my version：{}, snapshot: {}", leader.getVersion(), self.getVersion(), snapshot);
            // 更新当前节点数据
            LclRegistryService.restore(snapshot);
        }
    }



    /**
     * 集群探活
     */
    private void updateServers() {
        log.debug(" updateServers ===============================>>>> ");
        // 并发探活
        cluster.getServers().stream().parallel().forEach(server -> {
            try {
                // 自身不需要探活
                if(server.equals(cluster.self())){
                    return;
                }
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.debug(" =====>>>> health check success for: {}", serverInfo);
                if(serverInfo != null) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                } else {
                    server.setStatus(false);
                }
            } catch (Exception e){
                log.error(" =====>>>> health check failed for: {}", cluster.getServers());
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }
}
