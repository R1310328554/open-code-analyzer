/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.console.proxy.naming;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceDetailInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceView;
import com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo;
import com.alibaba.nacos.console.handler.naming.ServiceHandler;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.model.form.ServiceForm;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务管理代理：封装服务 CRUD、订阅者查询、列表检索与集群元数据维护，统一委派至 {@link ServiceHandler}。
 * Proxy class for handling service-related operations.
 *
 * @author zhangyukun
 */
@Service
public class ServiceProxy {
    
    /** 服务 Handler，对接 naming 服务维护实现 */
    private final ServiceHandler serviceHandler;
    
    /**
     * 注入服务 Handler 并构造代理，Handler 按部署类型路由。
     * Constructs a new ServiceProxy with the given ServiceInnerHandler and ConsoleConfig. The handler is mapped to a
     * deployment type key.
     *
     * @param serviceHandler 默认的 {@link ServiceHandler} 实现
     */
    public ServiceProxy(ServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }
    
    /**
     * Creates a new service by delegating the operation to the appropriate handler.
     *
     * @param serviceForm the service form containing the service details
     * @throws Exception if an error occurs during service creation
      * <p>服务管理代理；详见类级说明。</p>
     */
    /** 创建新服务并附带元数据。 */
    public void createService(ServiceForm serviceForm, ServiceMetadata serviceMetadata)
        throws Exception {
        serviceHandler.createService(serviceForm, serviceMetadata);
    }
    
    /**
     * Deletes an existing service by delegating the operation to the appropriate handler.
     *
     * @param namespaceId the namespace ID
     * @param serviceName the service name
     * @param groupName   the group name
     * @throws Exception if an error occurs during service deletion
      * <p>服务管理代理；详见类级说明。</p>
     */
    /** 删除命名空间下的指定服务。 */
    public void deleteService(String namespaceId, String serviceName, String groupName)
        throws Exception {
        serviceHandler.deleteService(namespaceId, serviceName, groupName);
    }
    
    /**
     * Updates an existing service by delegating the operation to the appropriate handler.
     *
     * @param serviceForm     the service form containing the service details
     * @param serviceMetadata the service metadata created from serviceForm
     * @throws Exception if an error occurs during service update
      * <p>服务管理代理；详见类级说明。</p>
     */
    /** 更新服务定义与元数据。 */
    public void updateService(ServiceForm serviceForm, ServiceMetadata serviceMetadata)
        throws Exception {
        serviceHandler.updateService(serviceForm, serviceMetadata);
    }
    
    /**
     * Retrieves all selector types by delegating the operation to the appropriate handler.
     *
     * @return a list of selector types
      * <p>服务管理代理；详见类级说明。</p>
     */
    /** 获取当前支持的路由选择器类型列表。 */
    public List<String> getSelectorTypeList() throws NacosException {
        return serviceHandler.getSelectorTypeList();
    }
    
    /**
     * Retrieves the list of subscribers for a service by delegating the operation to the appropriate handler.
     *
     * @param pageNo      the page number
     * @param pageSize    the size of the page
     * @param namespaceId the namespace ID
     * @param serviceName the service name
     * @param groupName   the group name
     * @param aggregation whether to aggregate the results
     * @return a JSON node containing the list of subscribers
     * @throws Exception if an error occurs during fetching subscribers
      * <p>服务管理代理；详见类级说明。</p>
     */
    /** 分页查询服务的订阅者列表，可选聚合模式。 */
    public Page<SubscriberInfo> getSubscribers(int pageNo, int pageSize, String namespaceId,
        String serviceName,
        String groupName, boolean aggregation) throws Exception {
        return serviceHandler.getSubscribers(pageNo, pageSize, namespaceId, serviceName, groupName,
            aggregation);
    }
    
    /**
     * Retrieves the list of services and their details by delegating the operation to the appropriate handler.
     *
     * @param withInstances whether to include instances
     * @param namespaceId   the namespace ID
     * @param pageNo        the page number
     * @param pageSize      the size of the page
     * @param serviceName   the service name
     * @param groupName     the group name
     * @param hasIpCount    whether to filter services with empty instances
     * @return if withInstances is {@code true}, return List of {@link ServiceDetailInfo}, otherwise return List of {@link ServiceView}
     * @throws NacosException if an error occurs during fetching service details
      * <p>服务管理代理；详见类级说明。</p>
     */
    /** 分页检索服务列表，可按是否含实例及 IP 计数过滤。 */
    public Object getServiceList(boolean withInstances, String namespaceId, int pageNo,
        int pageSize,
        String serviceName, String groupName, boolean hasIpCount) throws NacosException {
        return serviceHandler.getServiceList(withInstances, namespaceId, pageNo, pageSize,
            serviceName, groupName,
            hasIpCount);
    }
    
    /**
     * Retrieves the details of a specific service by delegating the operation to the appropriate handler.
     *
     * @param namespaceId             the namespace ID
     * @param serviceName the service name without group
     * @param groupName               the group name
     * @return service detail information
     * @throws NacosException if an error occurs during fetching service details
      * <p>服务管理代理；详见类级说明。</p>
     */
    /** 查询单个服务的完整详情。 */
    public ServiceDetailInfo getServiceDetail(String namespaceId, String serviceName,
        String groupName)
        throws NacosException {
        return serviceHandler.getServiceDetail(namespaceId, serviceName, groupName);
    }
    
    /**
     * Updates the metadata of a cluster.
     *
     * @param namespaceId     the namespace ID
     * @param groupName       the group name
     * @param serviceName     the service name
     * @param clusterName     the cluster name
     * @param clusterMetadata the metadata for the cluster
     * @throws Exception                if the update operation fails
     * @throws IllegalArgumentException if the deployment type is invalid
      * <p>服务管理代理；详见类级说明。</p>
     */
    /** 更新服务下指定集群的元数据。 */
    public void updateClusterMetadata(String namespaceId, String groupName, String serviceName,
        String clusterName,
        ClusterMetadata clusterMetadata) throws Exception {
        serviceHandler.updateClusterMetadata(namespaceId, groupName, serviceName, clusterName,
            clusterMetadata);
    }
}
