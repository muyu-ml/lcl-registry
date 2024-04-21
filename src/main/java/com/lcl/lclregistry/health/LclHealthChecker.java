package com.lcl.lclregistry.health;

import com.lcl.lclregistry.model.InstanceMeta;
import com.lcl.lclregistry.service.LclRegistryService;
import com.lcl.lclregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of HealthChecker
 * @Author conglongli
 * @date 2024/4/21 22:45
 */

@Slf4j
public class LclHealthChecker implements HealthChecker {

    final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    long timeout = 20_000;

    RegistryService registryService;

    public LclHealthChecker(RegistryService registryService){
        this.registryService = registryService;
    }

    @Override
    public void start() {
        executorService.scheduleWithFixedDelay(() -> {
            System.out.println(" =====>>>> Health checker running...");
            long now = System.currentTimeMillis();
            LclRegistryService.TIMESTAMPS.keySet().forEach(serviceAndInstance -> {
                Long timestamp = LclRegistryService.TIMESTAMPS.get(serviceAndInstance);
                if(now - timestamp > timeout){
                    log.info(" =======>>>>>>  service {} is down", serviceAndInstance);
                    int index = serviceAndInstance.indexOf("@");
                    String service = serviceAndInstance.substring(0, index);
                    String url = serviceAndInstance.substring(index + 1);
                    InstanceMeta instance = InstanceMeta.from(url);
                    registryService.unRegister(service, instance);
                    LclRegistryService.TIMESTAMPS.remove(serviceAndInstance);
                }
            });
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }
}
