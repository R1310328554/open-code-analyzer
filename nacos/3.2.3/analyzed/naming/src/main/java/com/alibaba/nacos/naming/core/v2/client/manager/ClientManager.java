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

package com.alibaba.nacos.naming.core.v2.client.manager;

import com.alibaba.nacos.naming.consistency.ephemeral.distro.v2.DistroClientVerifyInfo;
import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.client.ClientAttributes;

import java.util.Collection;

/**
 * Nacos 命名 {@link Client} 生命周期管理接口。
 *
 * <p>负责连接建立、Distro 同步、断开清理与负责节点判定。</p>
 *
 * @author xiweng.yy
 */
public interface ClientManager {
    
    /**
     * 新客户端连接：按属性创建并注册客户端。
     *
     * @param clientId new client id
     * @param attributes client attributes, which can help create client
     * @return true if add successfully, otherwise false
     */
    boolean clientConnected(String clientId, ClientAttributes attributes);
    
    /**
     * 注册已构造的客户端实例。
     *
     * @param client new client
     * @return true if add successfully, otherwise false
     */
    boolean clientConnected(Client client);
    
    /**
     * 注册从 Distro 同步的客户端。
     *
     * @param clientId   synced client id
     * @param attributes client sync attributes, which can help create sync client
     * @return true if add successfully, otherwise false
     */
    boolean syncClientConnected(String clientId, ClientAttributes attributes);
    
    /**
     * 客户端断开：移除并释放资源、发布断开事件。
     *
     * @param clientId client id
     * @return true if remove successfully, otherwise false
     */
    boolean clientDisconnected(String clientId);
    
    /**
     * 按 ID 查询客户端。
     *
     * @param clientId client id
     * @return client
     */
    Client getClient(String clientId);
    
    /**
     * 判断客户端 ID 是否已注册。
     *
     * @param clientId client id
     * @return client
     */
    boolean contains(final String clientId);
    
    /**
     * 返回当前管理的全部客户端 ID。
     *
     * @return collection of client id
     */
    Collection<String> allClientId();
    
    /**
     * 判断当前节点是否为该客户端的负责节点。
     *
     * @param client client
     * @return true if responsible, otherwise false
     */
    boolean isResponsibleClient(Client client);
    
    /**
     * Distro 校验客户端版本与续期状态。
     *
     * @param verifyData verify data from remote responsible server
     * @return true if client is valid, otherwise is false.
     */
    boolean verifyClient(DistroClientVerifyInfo verifyData);
}
