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

package com.alibaba.nacos.naming.core.v2.event.service;

import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.naming.core.v2.pojo.Service;

/**
 * 服务相关事件基类。
 *
 * <p>封装发生变更或订阅行为的 {@link Service} 对象，供 {@link NotifyCenter} 异步广播。</p>
 *
 * @author xiweng.yy
 */
public class ServiceEvent extends Event {
    
    private static final long serialVersionUID = -9173247502346692418L;
    
    /** 事件关联的服务对象。 */
    private final Service service;
    
    public ServiceEvent(Service service) {
        this.service = service;
    }
    
    /** 返回事件关联的服务。 */
    public Service getService() {
        return service;
    }
    
    /**
     * 服务数据变更事件。
     *
     * <p>实例增删或服务级变更时发布，携带变更类型标识。</p>
     */
    public static class ServiceChangedEvent extends ServiceEvent {
        
        private static final long serialVersionUID = 2123694271992630822L;
        
        /** 变更类型，见 {@link com.alibaba.nacos.api.common.Constants.ServiceChangedType}。 */
        private final String changedType;
        
        public ServiceChangedEvent(Service service, String changedType) {
            this(service, changedType, false);
        }
        
        public ServiceChangedEvent(Service service, String changedType, boolean incrementRevision) {
            super(service);
            this.changedType = changedType;
            service.renewUpdateTime();
            if (incrementRevision) {
                service.incrementRevision();
            }
        }
        
        /** 返回服务变更类型。 */
        public String getChangedType() {
            return changedType;
        }
        
    }
    
    /**
     * 客户端首次订阅服务事件。
     *
     * <p>某客户端首次订阅某服务时触发，用于推送初始数据。</p>
     */
    public static class ServiceSubscribedEvent extends ServiceEvent {
        
        private static final long serialVersionUID = -2645441445867337345L;
        
        /** 发起订阅的客户端 ID。 */
        private final String clientId;
        
        public ServiceSubscribedEvent(Service service, String clientId) {
            super(service);
            this.clientId = clientId;
        }
        
        /** 返回订阅客户端 ID。 */
        public String getClientId() {
            return clientId;
        }
    }
    
}
