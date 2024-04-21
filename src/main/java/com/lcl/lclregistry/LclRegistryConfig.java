package com.lcl.lclregistry;

import com.lcl.lclregistry.health.HealthChecker;
import com.lcl.lclregistry.health.LclHealthChecker;
import com.lcl.lclregistry.service.LclRegistryService;
import com.lcl.lclregistry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * configration for all beans
 * @Author conglongli
 * @date 2024/4/21 20:29
 */
@Configuration
public class LclRegistryConfig {

    @Bean
    RegistryService registryService(){
        return new LclRegistryService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    HealthChecker healthChecker(@Autowired RegistryService registryService){
        return new LclHealthChecker(registryService);
    }
}
