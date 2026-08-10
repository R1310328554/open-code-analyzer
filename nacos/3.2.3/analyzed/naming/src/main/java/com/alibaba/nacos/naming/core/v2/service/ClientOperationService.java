/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.core.v2.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.naming.pojo.Subscriber;
import com.alibaba.nacos.naming.constants.Constants;

import java.util.List;
import java.util.Map;

/**
 * 客户端命名操作服务接口：注册/注销实例与订阅服务。
 *
 * <p>临时与持久实例由不同实现处理，并提供 {@link Instance} → {@link InstancePublishInfo} 转换。</p>
 *
 * @author xiweng.yy
 */
public interface ClientOperationService {
    
    /**
     * 向服务注册单个实例。
     *
     * @param service  service
     * @param instance instance
     * @param clientId id of client
     * @throws NacosException throws NacosException
     */
    void registerInstance(Service service, Instance instance, String clientId)
        throws NacosException;
    
    /**
     * 批量向服务注册多个实例。
     *
     * @param service  service
     * @param instances instances
     * @param clientId id of client
     */
    void batchRegisterInstance(Service service, List<Instance> instances, String clientId);
    
    /**
     * 从服务注销实例。
     *
     * @param service  service
     * @param instance instance
     * @param clientId id of client
     */
    void deregisterInstance(Service service, Instance instance, String clientId);
    
    /**
     * 订阅服务变更（默认空实现，临时客户端实现覆盖）。
     *
     * @param service    service
     * @param subscriber subscribe
     * @param clientId   id of client
     */
    default void subscribeService(Service service, Subscriber subscriber, String clientId) {
        
    }
    
    /**
     * 取消订阅服务。
     *
     * @param service    service
     * @param subscriber subscribe
     * @param clientId   id of client
     */
    default void unsubscribeService(Service service, Subscriber subscriber, String clientId) {
        
    }
    
    double EPSILON = 1e-10;
    
    /**
     * 将 API {@link Instance} 转换为内部 {@link InstancePublishInfo}。
     *
     * <p>提取 metadata、权重、启用状态与集群名等扩展字段。</p>
     *
     * @param instance {@link Instance}
     * @return {@link InstancePublishInfo}
     */
    default InstancePublishInfo getPublishInfo(Instance instance) {
        InstancePublishInfo result = new InstancePublishInfo(instance.getIp(), instance.getPort());
        Map<String, Object> extendDatum = result.getExtendDatum();
        if (null != instance.getMetadata() && !instance.getMetadata().isEmpty()) {
            extendDatum.putAll(instance.getMetadata());
        }
        if (StringUtils.isNotEmpty(instance.getInstanceId())) {
            extendDatum.put(Constants.CUSTOM_INSTANCE_ID, instance.getInstanceId());
        }
        if (Math.abs(Constants.DEFAULT_INSTANCE_WEIGHT - instance.getWeight()) >= EPSILON) {
            extendDatum.put(Constants.PUBLISH_INSTANCE_WEIGHT, instance.getWeight());
        }
        if (!instance.isEnabled()) {
            extendDatum.put(Constants.PUBLISH_INSTANCE_ENABLE, instance.isEnabled());
        }
        String clusterName =
            StringUtils.isBlank(instance.getClusterName()) ? UtilsAndCommons.DEFAULT_CLUSTER_NAME
                : instance.getClusterName();
        result.setHealthy(instance.isHealthy());
        result.setCluster(clusterName);
        return result;
    }
}
