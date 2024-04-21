package com.lcl.lclregistry.service;

import com.lcl.lclregistry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * 注册中心服务
 * @Author conglongli
 * @date 2024/4/21 20:08
 */
public interface RegistryService {

    // 最基础的3个方法
    /**
    * 服务注册
    * @param serviceName 服务名称
    * @param instanceMeta 服务实例
    * @return 服务地址
    */
    InstanceMeta register(String serviceName, InstanceMeta instanceMeta);

    /**
    * 服务发现
    * @param serviceName 服务名称
    * @param instanceMeta 服务实例
    * @return 服务地址
    */
    InstanceMeta unRegister(String serviceName, InstanceMeta instanceMeta);

    /**
     * 服务发现
     * @param serviceName
     * @return
     */
    List<InstanceMeta> findAllInstances(String serviceName);


    /**
     * 服务续约
     * @param services
     * @param instance
     * @return
     */
    long renew(InstanceMeta instance, String... services);

    /**
     * 获取服务版本
     * @param service
     * @return
     */
    Long version(String service);

    /**
     * 获取服务版本
     * @param services
     * @return
     */
    Map<String, Long> getVersions(String... services);
}
