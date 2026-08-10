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

package com.alibaba.nacos.console.handler.core;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.response.Namespace;
import com.alibaba.nacos.core.namespace.model.form.NamespaceForm;

import java.util.List;

/**
 * 命名空间控制台处理器接口：列举、创建、更新、删除及存在性校验。
 * Interface for handling namespace-related operations.
 *
 * @author zhangyukun
 */
public interface NamespaceHandler {
    
    /**
      * 获取命名空间列表。
     * Get a list of namespaces.
     *
     * @return list of namespaces
     * @throws NacosException if there is an issue fetching the namespaces
     */
    List<Namespace> getNamespaceList() throws NacosException;
    
    /**
      * 获取指定命名空间详情。
     * Get details of a specific namespace.
     *
     * @param namespaceId 命名空间 ID
     * @return namespace details
     * @throws NacosException if there is an issue fetching the namespace
     */
    Namespace getNamespaceDetail(String namespaceId) throws NacosException;
    
    /**
      * 创建新命名空间。
     * Create a new namespace.
     *
     * @param namespaceId   命名空间 ID
     * @param namespaceName 命名空间名称
     * @param namespaceDesc 命名空间描述
     * @return true if the namespace was successfully created, otherwise false
     * @throws NacosException if there is an issue creating the namespace
     */
    Boolean createNamespace(String namespaceId, String namespaceName, String namespaceDesc)
        throws NacosException;
    
    /**
      * 更新已有命名空间。
     * Update an existing namespace.
     *
     * @param namespaceForm 命名空间更新表单
     * @return true if the namespace was successfully updated, otherwise false
     * @throws NacosException if there is an issue updating the namespace
     */
    Boolean updateNamespace(NamespaceForm namespaceForm) throws NacosException;
    
    /**
      * 按 ID 删除命名空间。
     * Delete a namespace by its ID.
     *
     * @param namespaceId 命名空间 ID
     * @return true if the namespace was successfully deleted, otherwise false
     * @throws NacosException if there is an issue deleting the namespace
     */
    Boolean deleteNamespace(String namespaceId) throws NacosException;
    
    /**
      * 校验命名空间 ID 是否已存在。
     * Check if a namespace ID exists.
     *
     * @param namespaceId 命名空间 ID to check
     * @return true if the namespace exists, otherwise false
     * @throws NacosException if there is an issue checking the namespace
     */
    Boolean checkNamespaceIdExist(String namespaceId) throws NacosException;
}
