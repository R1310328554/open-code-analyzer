/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.naming.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;
import com.alibaba.nacos.api.naming.utils.NamingUtils;

import java.util.Map;

/**
 * 持久实例健康状态运维接口，支持手动改健康态与查询可用健康检查器。
 *
 * @author xiweng.yy
 */
public interface HealthOperator {
    
    /**
     * Manually update healthy status for persistent instance.
     *
     * <p>Only {@code HealthCheckType.NONE} can be manually update status.
     *
     * @param namespace       namespace of service
     * @param fullServiceName full service name like `groupName@@serviceName`
     * @param clusterName     cluster of instance
     * @param ip              ip of instance
     * @param port            port of instance
     * @param healthy         health status of instance
     * @throws NacosException any exception during updating
     * @deprecated use {@link #updateHealthStatusForPersistentInstance(String, String, String, String, String, int, boolean)} replaced
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    @Deprecated
    default void updateHealthStatusForPersistentInstance(String namespace, String fullServiceName,
        String clusterName,
        String ip, int port, boolean healthy) throws NacosException {
        String groupName = NamingUtils.getGroupName(fullServiceName);
        String serviceName = NamingUtils.getServiceName(fullServiceName);
        updateHealthStatusForPersistentInstance(namespace, groupName, serviceName, clusterName, ip,
            port, healthy);
    }
    
    /**
     * Manually update healthy status for persistent instance.
     *
     * <p>Only {@code HealthCheckType.NONE} can be manually update status.
     *
     * @param namespace       namespace of service
     * @param groupName       groupName of service
     * @param serviceName     service name
     * @param clusterName     cluster of instance
     * @param ip              ip of instance
     * @param port            port of instance
     * @param healthy         health status of instance
     * @throws NacosException any exception during updating
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    /**
     * 手动更新持久实例健康状态（仅 NONE 健康检查类型允许）。
     *
     * @param namespace       namespace of service
     * @param groupName       groupName of service
     * @param serviceName     service name
     * @param clusterName     cluster of instance
     * @param ip              ip of instance
     * @param port            port of instance
     * @param healthy         health status of instance
     * @throws NacosException any exception during updating
     */
    void updateHealthStatusForPersistentInstance(String namespace, String groupName,
        String serviceName,
        String clusterName, String ip, int port, boolean healthy) throws NacosException;
    
    /**
     * Retrieves a map of available health checkers.
     *
     * <p>Each key in the map represents the type of health checker, and the corresponding value is an instance
     * of {@link AbstractHealthChecker} that implements the specific health check logic.
     *
     * @return a map of health checkers, where the key is the health checker type and the value is the
     *         corresponding {@link AbstractHealthChecker} instance
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    /**
     * 返回已加载的健康检查器类型映射。
     *
     * @return a map of health checkers, where the key is the health checker type and the value is the
     *         corresponding {@link AbstractHealthChecker} instance
     */
    Map<String, AbstractHealthChecker> checkers();
}
