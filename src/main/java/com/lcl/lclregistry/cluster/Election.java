package com.lcl.lclregistry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 集群选举
 * @Author conglongli
 * @date 2024/4/23 23:44
 */

@Slf4j
public class Election {

    List<Server> servers;

    public Election(List<Server> servers){
        this.servers = servers;
    }

    /**
     * 集群选主
     */
    public void electLeader() {
        log.debug(" electLeader ===============================>>>> ");
        // 获取所有存活且认为自己是master的节点
        List<Server> masters = servers.stream().filter(Server::isStatus).filter(Server::isLeader).collect(Collectors.toList());
        if(masters.isEmpty()){
            log.warn("  =====>>>> [ELECT] elect for no leader：{}", servers);
            elect();
        } else if(masters.size() > 1) {
            log.warn(" =====>>>> [ELECT] elect for more than one leader: {}", masters);
            elect();
        } else {
            log.debug(" =====>>>> [ELECT] no need election for leader: {}", masters.get(0));
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
}
