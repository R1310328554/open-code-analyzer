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

package com.alibaba.nacos.naming.core.v2.event.client;

import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.naming.core.v2.client.Client;

/**
 * 客户端相关事件基类，通过 {@link NotifyCenter} 异步分发。
 *
 * @author xiweng.yy
 */
public class ClientEvent extends Event {
    
    private static final long serialVersionUID = -8211818115593181708L;
    
    /** 事件关联的客户端实例。 */
    private final Client client;
    
    public ClientEvent(Client client) {
        this.client = client;
    }
    
    public Client getClient() {
        return client;
    }
    
    /** 客户端服务集合变更事件（注册/注销实例或订阅变化时触发）。 */
    /**
     * Client changed event. Happened when {@code Client} add or remove service.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    public static class ClientChangedEvent extends ClientEvent {
        
        private static final long serialVersionUID = 6440402443724824673L;
        
        public ClientChangedEvent(Client client) {
            super(client);
        }
        
    }
    
    /** 客户端与服务器断开连接事件，携带是否为本机负责标志。 */
    /**
     * Client disconnect event. Happened when {@code Client} disconnect with server.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    public static class ClientDisconnectEvent extends ClientEvent {
        
        private static final long serialVersionUID = 370348024867174101L;
        
        /** 断开的是否为本机直连（负责）客户端。 */
        private boolean isNative;
        
        public boolean isNative() {
            return isNative;
        }
        
        public ClientDisconnectEvent(Client client, boolean isNative) {
            super(client);
            this.isNative = isNative;
        }
        
    }
    
    /** Distro 校验失败事件，通知目标节点清理无效同步客户端。 */
    /**
     * Client add event. Happened when verify failed.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    public static class ClientVerifyFailedEvent extends ClientEvent {
        
        private static final long serialVersionUID = 2023951686223780851L;
        
        private final String clientId;
        
        private final String targetServer;
        
        public ClientVerifyFailedEvent(String clientId, String targetServer) {
            super(null);
            this.clientId = clientId;
            this.targetServer = targetServer;
        }
        
        public String getClientId() {
            return clientId;
        }
        
        public String getTargetServer() {
            return targetServer;
        }
    }
}
