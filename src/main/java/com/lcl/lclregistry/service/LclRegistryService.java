package com.lcl.lclregistry.service;

import com.lcl.lclregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * default implementation of registry service
 *
 * @Author conglongli
 * @date 2024/4/21 20:11
 */
@Slf4j
public class LclRegistryService implements RegistryService{

    MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap();
    final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    final static AtomicLong VERSION = new AtomicLong(0);

    @Override
    public InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if(metas != null && !metas.isEmpty()){
            if(metas.contains(instance)){
                log.info(" =======>>>>>>  instance {} already exists", instance.toUrl());
                instance.setStatus(true);
                return instance;
            }
        }
        log.info(" =======>>>>>>  register instance {} ", instance.toUrl());
        REGISTRY.add(service, instance);
        instance.setStatus(true);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public InstanceMeta unRegister(String service, InstanceMeta instance) {
        List<InstanceMeta> metas = REGISTRY.get(service);
        if(metas == null || metas.isEmpty()){
            return null;
        }
        log.info(" =======>>>>>>  unregister instance {} ", instance.toUrl());
        metas.removeIf(m -> m.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public List<InstanceMeta> findAllInstances(String service) {
        return REGISTRY.get(service);
    }

    @Override
    public long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for(String service : services){
            TIMESTAMPS.put(service + "@" + instance.toUrl(), now);
        }
        return now;
    }

    @Override
    public Long version(String service){
        return VERSIONS.get(service);
    }

    @Override
    public Map<String, Long> getVersions(String... services){
        return Arrays.stream(services).collect(Collectors.toMap(s -> s, this::version));
    }
}
