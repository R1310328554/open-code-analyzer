/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.core.v2.event.metadata;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.naming.core.v2.pojo.Service;

/**
 * 服务/实例信息变更事件基类。
 *
 * <p>用于触发元数据索引与推送链路更新。</p>
 *
 * @author RocketEngine26
 * @date 2022/9/14 下午6:12
 */
public class InfoChangeEvent extends Event {
    
    private static final long serialVersionUID = 2222222222222L;
    
    /** 变更涉及的服务对象。 */
    private final Service service;
    
    public InfoChangeEvent(Service service) {
        this.service = service;
    }
    
    public Service getService() {
        return service;
    }
    
    /** 服务级信息变更事件，构造时刷新服务更新时间。 */
    public static class ServiceInfoChangeEvent extends InfoChangeEvent {
        
        private static final long serialVersionUID = 3333333333333L;
        
        public ServiceInfoChangeEvent(Service service) {
            super(service);
            service.renewUpdateTime();
        }
    }
    
    /** 实例级信息变更事件，携带具体 {@link Instance} 快照。 */
    public static class InstanceInfoChangeEvent extends InfoChangeEvent {
        
        private static final long serialVersionUID = 4444444444444L;
        
        /** 发生变更的实例对象。 */
        private final Instance instance;
        
        public InstanceInfoChangeEvent(Service service, Instance instance) {
            super(service);
            this.instance = instance;
        }
        
        public Instance getInstance() {
            return instance;
        }
    }
}
