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
import com.alibaba.nacos.naming.core.v2.pojo.Service;

import java.util.Set;

/**
 * 客户端操作事件基类，描述注册、订阅、模糊监听等命名行为。
 *
 * @author xiweng.yy
 */
public class ClientOperationEvent extends Event {
    
    private static final long serialVersionUID = -4582413232902517619L;
    
    /** 触发操作的客户端 ID。 */
    private final String clientId;
    
    /** 操作涉及的服务（模糊监听事件可为 null）。 */
    private final Service service;
    
    public ClientOperationEvent(String clientId, Service service) {
        this.clientId = clientId;
        this.service = service;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public Service getService() {
        return service;
    }
    
    /** 客户端向服务注册实例事件。 */
    /**
     * Client register service event.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    public static class ClientRegisterServiceEvent extends ClientOperationEvent {
        
        private static final long serialVersionUID = 3412396514440368087L;
        
        public ClientRegisterServiceEvent(Service service, String clientId) {
            super(clientId, service);
        }
    }
    
    /** 客户端从服务注销实例事件。 */
    /**
     * Client deregister service event.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    public static class ClientDeregisterServiceEvent extends ClientOperationEvent {
        
        private static final long serialVersionUID = -4518919987813223120L;
        
        public ClientDeregisterServiceEvent(Service service, String clientId) {
            super(clientId, service);
        }
    }
    
    /** 客户端订阅服务变更事件。 */
    /**
     * Client subscribe service event.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    public static class ClientSubscribeServiceEvent extends ClientOperationEvent {
        
        private static final long serialVersionUID = -4518919987813223120L;
        
        public ClientSubscribeServiceEvent(Service service, String clientId) {
            super(clientId, service);
        }
    }
    
    /** 客户端取消订阅服务事件。 */
    /**
     * Client unsubscribe service event.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    public static class ClientUnsubscribeServiceEvent extends ClientOperationEvent {
        
        private static final long serialVersionUID = -4518919987813223120L;
        
        public ClientUnsubscribeServiceEvent(Service service, String clientId) {
            super(clientId, service);
        }
    }
    
    /** 客户端模糊监听（按 group 模式）事件。 */
    /**
     * Client fuzzy watch service event.
      * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
     */
    public static class ClientFuzzyWatchEvent extends ClientOperationEvent {
        
        private static final long serialVersionUID = -4518919987813223119L;
        
        /** 客户端监听的 group 键模式。 */
        /**
         * client watched pattern.
          * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
         */
        private final String groupKeyPattern;
        
        /** 客户端侧已收到的 group 键集合。 */
        /**
         * client side received group keys.
          * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
         */
        private Set<String> clientReceivedServiceKeys;
        
        /** 是否处于模糊监听初始化阶段。 */
        /**
         * is fuzzy watch initializing.
          * <p>Nacos 命名 V2 客户端工厂、管理器与事件模型；详见上方类/接口说明。</p>
         */
        private boolean isInitializing;
        
        public ClientFuzzyWatchEvent(String groupKeyPattern, String clientId,
            Set<String> clientReceivedServiceKeys,
            boolean isInitializing) {
            super(clientId, null);
            this.groupKeyPattern = groupKeyPattern;
            this.clientReceivedServiceKeys = clientReceivedServiceKeys;
            this.isInitializing = isInitializing;
        }
        
        public String getGroupKeyPattern() {
            return groupKeyPattern;
        }
        
        public Set<String> getClientReceivedServiceKeys() {
            return clientReceivedServiceKeys;
        }
        
        public boolean isInitializing() {
            return isInitializing;
        }
    }
    
    /** 客户端释放事件：断开连接后通知索引与推送模块清理。 */
    public static class ClientReleaseEvent extends ClientOperationEvent {
        
        private static final long serialVersionUID = -281486927726245701L;
        
        private final Client client;
        
        private final boolean isNative;
        
        public ClientReleaseEvent(Client client, boolean isNative) {
            super(client.getClientId(), null);
            this.client = client;
            this.isNative = isNative;
        }
        
        public Client getClient() {
            return client;
        }
        
        public boolean isNative() {
            return isNative;
        }
    }
}
