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
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.naming.pojo.maintainer.SubscriberInfo;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collection;

/**
 * 服务元数据运维接口。
 *
 * <p>定义服务的创建、更新、删除、查询及订阅者列表获取等操作，由 V2 实现类 {@link ServiceOperatorV2Impl} 提供具体逻辑。</p>
 *
 * @author xiweng.yy
 */
public interface ServiceOperator {
    
    /**
     * 创建新服务。
     *
     * @param namespaceId 服务所属命名空间 ID
     * @param serviceName 分组服务名，格式为 groupName@@serviceName
     * @param metadata    服务元数据
     * @throws NacosException 创建过程中发生的异常
     */
    void create(String namespaceId, String serviceName, ServiceMetadata metadata)
        throws NacosException;
    
    /**
     * 更新服务元数据。
     *
     * <p>服务基础信息不可变更，仅允许更新元数据部分。</p>
     *
     * @param service  待更新的服务
     * @param metadata 新的服务元数据
     * @throws NacosException 更新过程中发生的异常
     */
    void update(Service service, ServiceMetadata metadata) throws NacosException;
    
    /**
     * 删除服务。
     *
     * @param namespaceId 服务所属命名空间 ID
     * @param serviceName 分组服务名，格式为 groupName@@serviceName
     * @throws NacosException 删除过程中发生的异常
     */
    void delete(String namespaceId, String serviceName) throws NacosException;
    
    /**
     * 查询服务详情。
     *
     * @param namespaceId 服务所属命名空间 ID
     * @param serviceName 分组服务名，格式为 groupName@@serviceName
     * @return 包含集群信息的服务详情 JSON
     * @throws NacosException 查询过程中发生的异常
     */
    ObjectNode queryService(String namespaceId, String serviceName) throws NacosException;
    
    /**
     * 列出命名空间下符合分组条件的服务名。
     *
     * @param namespaceId 命名空间 ID
     * @param groupName   服务分组名
     * @param selector    服务选择器表达式
     * @return 服务名列表
     * @throws NacosException 查询过程中发生的异常
     */
    Collection<String> listService(String namespaceId, String groupName, String selector)
        throws NacosException;
    
    /**
     * 列出所有存在服务的命名空间。
     *
     * @return 命名空间 ID 集合
     */
    Collection<String> listAllNamespace();
    
    /**
     * 在命名空间内按表达式模糊搜索服务名。
     *
     * @param namespaceId 命名空间 ID
     * @param expr        搜索表达式
     * @return 匹配的服务名集合
     * @throws NacosException 查询过程中发生的异常
     */
    Collection<String> searchServiceName(String namespaceId, String expr) throws NacosException;
    
    /**
     * 分页获取服务的订阅者列表。
     *
     * @param namespaceId 命名空间 ID
     * @param serviceName 服务名
     * @param groupName   分组名
     * @param aggregation 是否聚合集群内所有节点的订阅者
     * @param pageNo      页码
     * @param pageSize    每页大小
     * @return 订阅者信息分页结果
     * @throws NacosException 查询订阅者时发生的异常
     */
    Page<SubscriberInfo> getSubscribers(String namespaceId, String serviceName, String groupName,
        boolean aggregation,
        int pageNo, int pageSize) throws NacosException;
}
