package com.lcl.lclregistry.service;

import com.lcl.lclregistry.cluster.Snapshot;
import com.lcl.lclregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
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

    final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap();
    final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    public static AtomicLong VERSION = new AtomicLong(0);

    @Override
    public synchronized InstanceMeta register(String service, InstanceMeta instance) {
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
    public synchronized InstanceMeta unRegister(String service, InstanceMeta instance) {
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


    /**
     * snapshot of registry
     * @return
     */
    public static synchronized Snapshot snapshot(){
        LinkedMultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        Map<String, Long> versions = new HashMap<>();
        Map<String, Long> timestamps = new HashMap<>();
        registry.addAll(REGISTRY);
        versions.putAll(VERSIONS);
        timestamps.putAll(TIMESTAMPS);
        return new Snapshot(registry, versions, timestamps, VERSION.get());
    }

    /**
     * restore registry from snapshot
     * @param snapshot
     * @return
     */
    public static synchronized long restore(Snapshot snapshot){
        REGISTRY.clear();
        REGISTRY.addAll(snapshot.getREGISTRY());
        VERSIONS.clear();
        VERSIONS.putAll(snapshot.getVERSIONS());
        TIMESTAMPS.clear();
        TIMESTAMPS.putAll(snapshot.getTIMESTAMPS());
        VERSION.set(snapshot.getVersion());
        return VERSION.get();
    }
}
