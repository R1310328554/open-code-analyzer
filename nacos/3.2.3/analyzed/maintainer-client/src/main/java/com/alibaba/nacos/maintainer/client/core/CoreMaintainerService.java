/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.maintainer.client.core;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.response.ConnectionInfo;
import com.alibaba.nacos.api.model.response.IdGeneratorInfo;
import com.alibaba.nacos.api.model.response.NacosMember;
import com.alibaba.nacos.api.model.response.Namespace;
import com.alibaba.nacos.api.model.response.ServerLoaderMetrics;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 核心模块维护服务接口：集群状态、命名空间、连接负载、插件与 Raft 运维等。
 *
 * <p>继承 {@link Closeable}，使用完毕应调用 {@link #shutdown()} 释放资源。</p>
 *
 * @author Nacos
 */
public interface CoreMaintainerService extends Closeable {
    
    /**
     * 获取 Nacos 服务端状态键值对。
     *
     * @return the states key-value map
     * @throws NacosException if the operation fails.
     */
    @Since("3.0.0")
    Map<String, String> getServerState() throws NacosException;
    
    /**
     * 探测服务端存活（liveness）。
     *
     * @return {@code true} detect successfully, {@code false} otherwise.
     * @throws NacosException if the operation fails.
     */
    @Since("3.0.0")
    Boolean liveness() throws NacosException;
    
    /**
     * 探测服务端就绪（readiness）。
     *
     * @return {@code true} detect successfully, {@code false} otherwise.
     * @throws NacosException if the operation fails.
     */
    @Since("3.0.0")
    Boolean readiness() throws NacosException;
    
    /**
     * 执行 Raft 运维命令。
     *
     * @param command the command to execute.
     * @param value   the value associated with the command.
     * @param groupId the group ID for the operation.
     * @return the result of the Raft operation.
     * @throws NacosException if the operation fails.
     */
    @Since("3.0.0")
    String raftOps(String command, String value, String groupId) throws NacosException;
    
    /**
     * 获取 ID 生成器健康状态列表。
     *
     * @return a list of ID generator status objects.
     * @throws NacosException if the operation fails.
     */
    @Since("3.0.0")
    List<IdGeneratorInfo> getIdGenerators() throws NacosException;
    
    /**
     * 更新指定 Logger 的日志级别。
     *
     * @param logName  the name of the logger to update.
     * @param logLevel the new log level to set.
     * @throws NacosException if the operation fails.
     */
    @Since("3.0.0")
    void updateLogLevel(String logName, String logLevel) throws NacosException;
    
    /**
     * 按地址与状态筛选列出集群节点。
     *
     * @param address the address to filter nodes by.
     * @param state   the state to filter nodes by.
     * @return a collection of matching nodes.
     * @throws NacosException if an error occurs during the operation.
     */
    @Since("3.0.0")
    Collection<NacosMember> listClusterNodes(String address, String state) throws NacosException;
    
    /**
     * Update the lookup mode for the cluster.
     *
     * @param type the type of lookup mode to set.
     * @return true if the operation was successful, false otherwise.
     * @throws NacosException if an error occurs during the operation.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.0.0")
    Boolean updateLookupMode(String type) throws NacosException;
    
    /**
     * 获取当前客户端连接映射。
     *
     * @return a map of current client connections.
     * @throws NacosException if the operation fails.
     */
    @Since("3.0.0")
    Map<String, ConnectionInfo> getCurrentClients() throws NacosException;
    
    /**
     * Reload the number of SDK connections on the current server.
     *
     * @param count           the number of connections to reload.
     * @param redirectAddress the address to redirect connections to.
     * @return the result of the operation.
     * @throws NacosException if the operation fails.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.0.0")
    String reloadConnectionCount(Integer count, String redirectAddress) throws NacosException;
    
    /**
     * Smartly reload the cluster based on the specified loader factor.
     *
     * @param loaderFactorStr the loader factor string.
     * @return the result of the operation.
     * @throws NacosException if the operation fails.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.0.0")
    String smartReloadCluster(String loaderFactorStr) throws NacosException;
    
    /**
     * Reload a single client connection.
     *
     * @param connectionId    the ID of the connection to reload.
     * @param redirectAddress the address to redirect the connection to.
     * @return the result of the operation.
     * @throws NacosException if the operation fails.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.0.0")
    String reloadSingleClient(String connectionId, String redirectAddress) throws NacosException;
    
    /**
     * Retrieve the current cluster loader metrics.
     *
     * @return the loader metrics for the cluster.
     * @throws NacosException if the operation fails.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.0.0")
    ServerLoaderMetrics getClusterLoaderMetrics() throws NacosException;
    
    /**
     * 获取全部命名空间列表。
     *
     * @return A list of {@link Namespace} objects representing all available namespaces.
     * @throws NacosException Thrown if any error occurs during the retrieval.
     */
    @Since("3.0.0")
    List<Namespace> getNamespaceList() throws NacosException;
    
    /**
     * Get detailed information of a specific namespace by its ID.
     *
     * @param namespaceId The unique identifier of the namespace.
     * @return A {@link Namespace} object containing all details of the specified namespace.
     * @throws NacosException Thrown if any error occurs during the retrieval.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.0.0")
    Namespace getNamespace(String namespaceId) throws NacosException;
    
    /**
     * Create a new namespace with the provided details and auto-generate UUID.
     *
     * @param namespaceName The name of the new namespace.
     * @param namespaceDesc The description of the new namespace.
     * @return {@code true} if the namespace is created successfully, {@code false} otherwise.
     * @throws NacosException Thrown if any error occurs during the creation.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.0.0")
    default Boolean createNamespace(String namespaceName, String namespaceDesc)
        throws NacosException {
        return createNamespace(StringUtils.EMPTY, namespaceName, namespaceDesc);
    }
    
    /**
     * 创建新命名空间（可指定 ID，空则自动生成 UUID）。
     *
     * @param namespaceId   The unique identifier for the new namespace, if null or empty will use auto-generate UUID.
     * @param namespaceName The name of the new namespace.
     * @param namespaceDesc The description of the new namespace.
     * @return {@code true} if the namespace is created successfully, {@code false} otherwise.
     * @throws NacosException Thrown if any error occurs during the creation.
     */
    @Since("3.0.0")
    Boolean createNamespace(String namespaceId, String namespaceName, String namespaceDesc)
        throws NacosException;
    
    /**
     * Update an existing namespace with the provided details.
     *
     * @param namespaceId   The unique identifier of the namespace to be updated.
     * @param namespaceName The new name for the namespace (can be null).
     * @param namespaceDesc The new description for the namespace.
     * @return {@code true} if the namespace is updated successfully, {@code false} otherwise.
     * @throws NacosException Thrown if any error occurs during the update.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.0.0")
    Boolean updateNamespace(String namespaceId, String namespaceName, String namespaceDesc)
        throws NacosException;
    
    /**
     * 按 ID 删除命名空间。
     *
     * @param namespaceId The unique identifier of the namespace to be deleted.
     * @return {@code true} if the namespace is deleted successfully, {@code false} otherwise.
     * @throws NacosException Thrown if any error occurs during the deletion.
     */
    @Since("3.0.0")
    Boolean deleteNamespace(String namespaceId) throws NacosException;
    
    /**
     * Check if a namespace with the specified ID exists.
     *
     * @param namespaceId The unique identifier of the namespace to check.
     * @return {@code true} if the namespace exists, {@code false} otherwise.
     * @throws NacosException Thrown if any error occurs during the check.
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.0.0")
    Boolean checkNamespaceIdExist(String namespaceId) throws NacosException;
    
    /**
     * 列出插件（可选按类型过滤）。
     *
     * @param pluginType optional plugin type filter, null or empty to list all
     * @return list of plugin information
     * @throws NacosException if the operation fails
     */
    @Since("3.2.0")
    List<Map<String, Object>> listPlugins(String pluginType) throws NacosException;
    
    /**
     * Get plugin detail by type and name.
     *
     * @param pluginType plugin type
     * @param pluginName plugin name
     * @return plugin detail information
     * @throws NacosException if the operation fails
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    Map<String, Object> getPluginDetail(String pluginType, String pluginName) throws NacosException;
    
    /**
     * Update plugin enabled/disabled status.
     *
     * @param pluginType plugin type
     * @param pluginName plugin name
     * @param enabled    whether to enable
     * @throws NacosException if the operation fails
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    default void updatePluginStatus(String pluginType, String pluginName, boolean enabled)
        throws NacosException {
        updatePluginStatus(pluginType, pluginName, enabled, false);
    }
    
    /**
     * Update plugin enabled/disabled status.
     *
     * @param pluginType plugin type
     * @param pluginName plugin name
     * @param enabled    whether to enable
     * @param localOnly  whether only apply to local node
     * @throws NacosException if the operation fails
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    void updatePluginStatus(String pluginType, String pluginName, boolean enabled,
        boolean localOnly)
        throws NacosException;
    
    /**
     * Update plugin configuration.
     *
     * @param pluginType plugin type
     * @param pluginName plugin name
     * @param config     configuration map
     * @throws NacosException if the operation fails
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    default void updatePluginConfig(String pluginType, String pluginName,
        Map<String, String> config)
        throws NacosException {
        updatePluginConfig(pluginType, pluginName, config, false);
    }
    
    /**
     * Update plugin configuration.
     *
     * @param pluginType plugin type
     * @param pluginName plugin name
     * @param config     configuration map
     * @param localOnly  whether only apply to local node
     * @throws NacosException if the operation fails
      * <p>Nacos 维护客户端模块；详见上方类/接口说明。</p>
     */
    @Since("3.2.0")
    void updatePluginConfig(String pluginType, String pluginName, Map<String, String> config,
        boolean localOnly)
        throws NacosException;
    
    /**
     * 查询插件在各集群节点上的可用性。
     *
     * @param pluginType plugin type
     * @param pluginName plugin name
     * @return node availability map (node address to availability)
     * @throws NacosException if the operation fails
     */
    @Since("3.2.0")
    Map<String, Boolean> getPluginAvailability(String pluginType, String pluginName)
        throws NacosException;
}
