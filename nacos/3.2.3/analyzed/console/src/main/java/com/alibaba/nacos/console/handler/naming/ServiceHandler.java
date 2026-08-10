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

package com.alibaba.nacos.console.handler.naming;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceDetailInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceView;
import com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo;
import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.model.form.ServiceForm;

import java.util.List;

/**
 * 服务治理操作接口：定义创建、删除、更新、查询服务及订阅者、集群元数据等能力，由 inner/remote 实现按部署模式委托底层 naming 服务。
 * Interface for handling service-related operations.
 *
 * @author zhangyukun
 */
public interface ServiceHandler {
    
    /**
     * 创建新服务。
     * Create a new service.
     *
     * @param serviceForm     包含服务详情的表单
     * @param serviceMetadata 由 serviceForm 构建的服务元数据
     * @throws Exception 创建失败时抛出
     */
    void createService(ServiceForm serviceForm, ServiceMetadata serviceMetadata) throws Exception;
    
    /**
     * 删除已有服务。
     * Delete an existing service.
     *
     * @param namespaceId 命名空间 ID
     * @param serviceName 服务名
     * @param groupName   分组名
     * @throws Exception 删除失败时抛出
     */
    void deleteService(String namespaceId, String serviceName, String groupName) throws Exception;
    
    /**
     * 更新已有服务。
     * Update an existing service.
     *
     * @param serviceForm     包含服务详情的表单
     * @param serviceMetadata 由 serviceForm 构建的服务元数据
     * @throws Exception 更新失败时抛出
     */
    void updateService(ServiceForm serviceForm, ServiceMetadata serviceMetadata) throws Exception;
    
    /**
     * 获取全部负载均衡选择器类型列表。
     * Get all selector types.
     *
     * @return 选择器类型列表
     * @throws NacosException 查询失败时抛出
     */
    List<String> getSelectorTypeList() throws NacosException;
    
    /**
     * 分页查询指定服务的订阅者列表。
     * Get the list of subscribers for a service.
     *
     * @param pageNo      页码
     * @param pageSize    每页条数
     * @param namespaceId 命名空间 ID
     * @param serviceName 服务名
     * @param groupName   分组名
     * @param aggregation 是否聚合结果
     * @return 订阅者分页结果
     * @throws Exception 查询失败时抛出
     */
    Page<SubscriberInfo> getSubscribers(int pageNo, int pageSize, String namespaceId,
        String serviceName,
        String groupName, boolean aggregation) throws Exception;
    
    /**
     * 分页列出服务详情。
     * List service detail information.
     *
     * @param withInstances         是否包含实例信息
     * @param namespaceId           命名空间 ID
     * @param pageNo                页码
     * @param pageSize              每页条数
     * @param serviceName           服务名
     * @param groupName             分组名
     * @param ignoreEmptyService    是否过滤无实例的空服务
     * @return 若 withInstances 为 {@code true} 返回 {@link ServiceDetailInfo} 分页，否则返回 {@link ServiceView} 分页
     * @throws NacosException 查询失败时抛出
     */
    Object getServiceList(boolean withInstances, String namespaceId, int pageNo, int pageSize,
        String serviceName,
        String groupName, boolean ignoreEmptyService) throws NacosException;
    
    /**
     * 获取指定服务的详细信息。
     * Get the detail of a specific service.
     *
     * @param namespaceId 命名空间 ID
     * @param serviceName 不含分组前缀的服务名
     * @param groupName   分组名
     * @return 服务详情
     * @throws NacosException 查询失败时抛出
     */
    ServiceDetailInfo getServiceDetail(String namespaceId, String serviceName, String groupName)
        throws NacosException;
    
    /**
     * 更新集群元数据。
     * Update the metadata of a cluster.
     *
     * @param namespaceId     命名空间 ID
     * @param groupName       分组名
     * @param serviceName     服务名
     * @param clusterName     集群名
     * @param clusterMetadata 集群元数据
     * @throws Exception 更新失败时抛出
     */
    void updateClusterMetadata(String namespaceId, String groupName, String serviceName,
        String clusterName,
        ClusterMetadata clusterMetadata) throws Exception;
}
