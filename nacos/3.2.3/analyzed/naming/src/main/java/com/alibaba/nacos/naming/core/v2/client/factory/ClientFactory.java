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

package com.alibaba.nacos.naming.core.v2.client.factory;

import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.client.ClientAttributes;

/**
 * 客户端工厂接口。
 *
 * <p>按客户端类型创建本地新建或从其他节点同步而来的 {@link Client} 实例。</p>
 *
 * @author xiweng.yy
 */
public interface ClientFactory<C extends Client> {
    
    /**
     * 返回本工厂支持的客户端类型标识。
     *
     * @return 客户端类型字符串
     */
    String getType();
    
    /**
     * 创建本地新建的 {@link Client} 实例。
     *
     * @param clientId   客户端 ID
     * @param attributes 客户端扩展属性
     * @return 新的客户端实现
     */
    C newClient(String clientId, ClientAttributes attributes);
    
    /**
     * 创建从其他集群节点同步而来的 {@link Client} 实例。
     *
     * @param clientId   客户端 ID
     * @param attributes 客户端扩展属性
     * @return 同步型客户端实现
     */
    C newSyncedClient(String clientId, ClientAttributes attributes);
}
