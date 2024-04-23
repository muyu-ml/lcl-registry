package com.lcl.lclregistry;

import com.lcl.lclregistry.config.LclRegistryConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LclRegistryConfigProperties.class)
public class LclRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LclRegistryApplication.class, args);
    }

}
