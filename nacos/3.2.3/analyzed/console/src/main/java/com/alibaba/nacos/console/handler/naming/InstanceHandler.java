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
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.naming.model.form.InstanceForm;

/**
 * 服务实例操作接口：定义实例分页列表、更新与注销能力，由 inner/remote 实现按部署模式委托底层服务。
 * Interface for handling instance-related operations.
 *
 * @author zhangyukun
 */
public interface InstanceHandler {
    
    /**
     * 分页查询指定服务下的实例列表。
     * Retrieve a list of instances for a specific service and returns as an ObjectNode.
     *
     * @param namespaceId           命名空间 ID
     * @param serviceNameWithoutGroup 不含分组前缀的服务名
     * @param groupName             分组名
     * @param clusterName             集群名
     * @param page                  页码
     * @param pageSize              每页条数
     * @return {@link Instance} 分页结果
     * @throws NacosException 列表查询失败时抛出
     */
    Page<? extends Instance> listInstances(String namespaceId, String serviceNameWithoutGroup,
        String groupName, String clusterName,
        int page, int pageSize) throws NacosException;
    
    /**
     * 更新实例元数据（权重、健康状态等）。
     * Update an instance.
     *
     * @param instanceForm 包含实例定位信息的表单
     * @param instance     待更新的实例对象
     * @throws NacosException 更新失败时抛出
     */
    void updateInstance(InstanceForm instanceForm, Instance instance) throws NacosException;
    
    /**
     * 从服务中注销指定实例。
     * Remove an instance.
     *
     * @param instanceForm 包含实例定位信息的表单
     * @param instance     待注销的实例对象
     * @throws NacosException 注销失败时抛出
     */
    void removeInstance(InstanceForm instanceForm, Instance instance) throws NacosException;
}
