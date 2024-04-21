package com.lcl.lclregistry;

import com.lcl.lclregistry.model.InstanceMeta;
import com.lcl.lclregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author conglongli
 * @date 2024/4/21 20:28
 */
@RestController
@Slf4j
public class LclRegistryController {

    @Autowired
    RegistryService registryService;

    @RequestMapping("/reg")
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance){
        log.info(" =======>>>>>>  register service {} @ {} ", service, instance);
        return registryService.register(service, instance);
    }

    @RequestMapping("/unreg")
    public InstanceMeta unRegister(@RequestParam String service, @RequestBody InstanceMeta instance){
        log.info(" =======>>>>>>  unregister service {} @ {} ", service, instance);
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
}
