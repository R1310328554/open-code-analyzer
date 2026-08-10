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

package com.alibaba.nacos.common.trace.event.naming;

import com.alibaba.nacos.common.trace.event.TraceEvent;

/**
 * Naming 模块追踪事件基类：继承 {@link com.alibaba.nacos.common.trace.event.TraceEvent}，
 * 统一将 namespace/group/name 映射为服务的命名空间、分组与服务名；标记为插件事件。
 * Naming trace event.
 *
 * @author yanda
 */
public class NamingTraceEvent extends TraceEvent {
    
    private static final long serialVersionUID = 2923077640400851816L;
    
    /**
     * 构造 Naming 追踪事件。
     *
     * @param eventType 事件类型常量
     * @param eventTime 发生时间戳
     * @param serviceNamespace 服务命名空间
     * @param serviceGroup 服务分组
     * @param name 服务名
     */
    public NamingTraceEvent(String eventType, long eventTime, String serviceNamespace,
        String serviceGroup,
        String name) {
        super(eventType, eventTime, serviceNamespace, serviceGroup, name);
    }
    
    /** 标记为插件事件，经 NotifyCenter 插件通道异步分发 */
    @Override
    public boolean isPluginEvent() {
        return true;
    }
}
