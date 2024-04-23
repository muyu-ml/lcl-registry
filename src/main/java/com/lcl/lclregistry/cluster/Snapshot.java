package com.lcl.lclregistry.cluster;

import com.lcl.lclregistry.model.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Map;

/**
 * snapshot of registry
 * @Author conglongli
 * @date 2024/4/23 21:37
 */
@Data
@AllArgsConstructor
public class Snapshot {
    LinkedMultiValueMap<String, InstanceMeta> REGISTRY;
    Map<String, Long> VERSIONS;
    Map<String, Long> TIMESTAMPS;
    long version;
}
