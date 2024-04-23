package com.lcl.lclregistry.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @Author conglongli
 * @date 2024/4/22 21:33
 */
@Data
@ConfigurationProperties(prefix = "lclregistry")
public class LclRegistryConfigProperties {

    private List<String> serverList;
}
