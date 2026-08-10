/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClientPublisherInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClientServiceInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClientSubscriberInfo;
import com.alibaba.nacos.api.naming.pojo.maintainer.ClientSummaryInfo;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * 客户端连接查询接口，供运维 API 列举、检索已连接客户端及其发布/订阅关系。
 *
 * <p>实现类 {@link ClientServiceImpl} 基于 V2 {@link com.alibaba.nacos.naming.core.v2.client.manager.ClientManager}。</p>
 *
 * @author Nacos
 */

public interface ClientService {
    
    /**
     * 获取当前所有已连接客户端 ID 列表。
     *
     * @return A list of client identifiers.
     */
    List<String> getClientList();
    
    /**
     * 查询指定客户端的摘要信息（类型、IP、版本等）。
     *
     * @param clientId The unique identifier of the client.
     * @return Detailed information about the client in JSON format.
     * @throws NacosApiException If an error occurs while retrieving the client information.
     */
    ClientSummaryInfo getClientDetail(String clientId) throws NacosApiException;
    
    /**
     * Retrieves a list of services published by a specific client. For 2.x http api.
     *
     * @param clientId The unique identifier of the client.
     * @return A list of published services in JSON format.
     * @deprecated with removing 2.x http api. use {@link #getPublishedServiceList(String)} replaced
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    @Deprecated
    List<ObjectNode> getPublishedServiceListAdapt(String clientId);
    
    /**
     * Retrieves a list of services published by a specific client. For 2.x http api.
     *
     * @param clientId The unique identifier of the client.
     * @return A list of {@link ClientServiceInfo} with {@link ClientPublisherInfo}
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    List<ClientServiceInfo> getPublishedServiceList(String clientId);
    
    /**
     * Retrieves a list of services subscribed to by a specific client. For 2.x http api.
     *
     * @param clientId The unique identifier of the client.
     * @return A list of subscribed services in JSON format.
     * @deprecated with removing 2.x http api. use {@link #getSubscribeServiceList(String)} replaced
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    @Deprecated
    List<ObjectNode> getSubscribeServiceListAdapt(String clientId);
    
    /**
     * Retrieves a list of services subscribed to by a specific client.
     *
     * @param clientId The unique identifier of the client.
     * @return A list of {@link ClientServiceInfo} with {@link ClientSubscriberInfo}
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    List<ClientServiceInfo> getSubscribeServiceList(String clientId);
    
    /**
     * Retrieves a list of clients that have published a specific service. For 2.x http api.
     *
     * @param namespaceId The namespace of the service.
     * @param groupName   The group name of the service.
     * @param serviceName The name of the service.
     * @param ephemeral   Whether the service is ephemeral (temporary).
     * @param ip          The IP address of the client (optional filter).
     * @param port        The port number of the client (optional filter).
     * @return A list of clients that published the service in JSON format.
     * @deprecated with removing 2.x http api. use {@link #getPublishedClientList(String, String, String, String, Integer)} replaced
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    @Deprecated
    List<ObjectNode> getPublishedClientList(String namespaceId, String groupName,
        String serviceName, boolean ephemeral,
        String ip, Integer port);
    
    /**
     * Retrieves a list of clients that have published a specific service.
     *
     * @param namespaceId The namespace of the service.
     * @param groupName   The group name of the service.
     * @param serviceName The name of the service.
     * @param ip          The IP address of the client (optional filter).
     * @param port        The port number of the client (optional filter).
     * @return A list of {@link ClientPublisherInfo} with clientId
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    /**
     * 查询向指定服务发布实例的客户端列表。
     *
     * @param namespaceId The namespace of the service.
     * @param groupName   The group name of the service.
     * @param serviceName The name of the service.
     * @param ip          The IP address of the client (optional filter).
     * @param port        The port number of the client (optional filter).
     * @return A list of {@link ClientPublisherInfo} with clientId
     */
    List<ClientPublisherInfo> getPublishedClientList(String namespaceId, String groupName,
        String serviceName,
        String ip, Integer port);
    
    /**
     * Retrieves a list of clients that have subscribed to a specific service. For 2.x http api.
     *
     * @param namespaceId The namespace of the service.
     * @param groupName   The group name of the service.
     * @param serviceName The name of the service.
     * @param ephemeral   Whether the service is ephemeral (temporary).
     * @param ip          The IP address of the client (optional filter).
     * @param port        The port number of the client (optional filter).
     * @return A list of clients that subscribed to the service in JSON format.
     * @deprecated @deprecated with removing 2.x http api. use {@link #getSubscribeClientList(String, String, String, String, Integer)} replaced
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    @Deprecated
    List<ObjectNode> getSubscribeClientList(String namespaceId, String groupName,
        String serviceName, boolean ephemeral,
        String ip, Integer port);
    
    /**
     * Retrieves a list of clients that have subscribed to a specific service.
     *
     * @param namespaceId The namespace of the service.
     * @param groupName   The group name of the service.
     * @param serviceName The name of the service.
     * @param ip          The IP address of the client (optional filter).
     * @param port        The port number of the client (optional filter).
     * @return A list of {@link ClientSubscriberInfo} with clientId
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    List<ClientSubscriberInfo> getSubscribeClientList(String namespaceId, String groupName,
        String serviceName,
        String ip, Integer port);
    
    /**
     * Determines the responsible server for handling requests from a specific client based on its IP and port.
     *
     * @param ip   The IP address of the client.
     * @param port The port number of the client.
     * @return The responsible server information in JSON format.
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    /**
     * 根据客户端 IP:Port 计算 Distro 负责节点。
     *
     * @param ip   The IP address of the client.
     * @param port The port number of the client.
     * @return The responsible server information in JSON format.
     */
    ObjectNode getResponsibleServer4Client(String ip, String port);
}
