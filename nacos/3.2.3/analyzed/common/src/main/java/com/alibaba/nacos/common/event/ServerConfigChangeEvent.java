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

package com.alibaba.nacos.common.event;

import com.alibaba.nacos.common.notify.Event;

/**
 * Server configuration changed event.
 *
 * <p>
 * When nacos server configuration file (default nacos/conf/application.properties) changed, The event should be notify
 * to all subscriber.
 * </p>
 * <p>服务端配置文件（默认 {@code nacos/conf/application.properties}）变更时发布，由 {@link com.alibaba.nacos.common.notify.NotifyCenter} 分发给所有订阅者。</p>
 *
 * @author xiweng.yy
 */
public class ServerConfigChangeEvent extends Event {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = 289992068985663172L;
    
    /** 工厂方法：创建新的服务端配置变更事件实例 */
    public static ServerConfigChangeEvent newEvent() {
        return new ServerConfigChangeEvent();
    }
}
