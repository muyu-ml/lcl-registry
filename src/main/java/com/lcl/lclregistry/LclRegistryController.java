package com.lcl.lclregistry;

import com.lcl.lclregistry.cluster.Cluster;
import com.lcl.lclregistry.cluster.Server;
import com.lcl.lclregistry.cluster.Snapshot;
import com.lcl.lclregistry.model.InstanceMeta;
import com.lcl.lclregistry.service.LclRegistryService;
import com.lcl.lclregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Author conglongli
 * @date 2024/4/21 20:28
 */
@RestController
@Slf4j
public class LclRegistryController {

    @Autowired
    RegistryService registryService;

    @Autowired
    Cluster cluster;

    @RequestMapping("/reg")
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance){
        log.info(" =======>>>>>>  register service {} @ {} ", service, instance);
        checkLeader();
        return registryService.register(service, instance);
    }

    /**
     * 非leader节点不允许注册、注销、续约
     */
    private void checkLeader() {
        if(!cluster.self().isLeader()){
            throw new RuntimeException("current server is not a leader, the leader is " + cluster.leader().getUrl() + ", please send request to leader");
        }
    }

    @RequestMapping("/unreg")
    public InstanceMeta unRegister(@RequestParam String service, @RequestBody InstanceMeta instance){
        log.info(" =======>>>>>>  unregister service {} @ {} ", service, instance);
        checkLeader();
        return registryService.unRegister(service, instance);
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta> findAllInstances(@RequestParam String service){
        log.info(" =======>>>>>>  get all instances of service {} ", service);
        return registryService.findAllInstances(service);
    }

    @RequestMapping("/renew")
    public long renew(@RequestBody InstanceMeta instance, @RequestParam String services){
        log.info(" =======>>>>>>  renew instance {} ", instance);
        checkLeader();
        return registryService.renew(instance, services.split(","));
    }

    @RequestMapping("/version")
    public Long version(@RequestParam String service){
        log.info(" =======>>>>>>  get version of service {} ", service);
        return registryService.version(service);
    }

    @RequestMapping("/versions")
    public Map<String, Long> getVersions(@RequestParam String services){
        log.info(" =======>>>>>>  get versions of services {} ", services);
        return registryService.getVersions(services.split(","));
    }

    @RequestMapping("/info")
    public Server info(){
//        log.info(" =======>>>>>>  get info：{} ", cluster.self());
        return cluster.self();
    }

    @RequestMapping("/cluster")
    public List<Server> cluster(){
        log.info(" =======>>>>>>  get cluster：{} ", cluster.getServers());
        return cluster.getServers();
    }

    @RequestMapping("/leader")
    public Server leader(){
        log.info(" =======>>>>>>  get leader：{} ", cluster.leader());
        return cluster.leader();
    }

    @RequestMapping("/sl")
    public String setLeader(){
        log.info(" =======>>>>>>  set leader ");
        cluster.self().setLeader(true);
        return "ok";
    }

    @RequestMapping("/snapshot")
    public Snapshot snapshot(){
        log.info(" =======>>>>>>  get snapshot ");
        return LclRegistryService.snapshot();
    }
}
