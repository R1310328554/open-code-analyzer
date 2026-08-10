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

package com.alibaba.nacos.common.trace.event;

import com.alibaba.nacos.common.notify.Event;

/**
 * 追踪事件基类：继承 {@link com.alibaba.nacos.common.notify.Event}，封装事件类型、
 * 发生时间、命名空间/分组/名称等通用维度，供各业务模块发布可观测性事件。
 * Trace event.
 *
 * @author yanda
 */
public class TraceEvent extends Event {
    
    /** 序列化版本号 */
    private static final long serialVersionUID = -3065900892505697062L;
    
    /** 事件类型标识，如 {@code REGISTER_INSTANCE_TRACE_EVENT} */
    private final String type;
    
    /** 事件发生时间戳（毫秒） */
    private final long eventTime;
    
    /** 命名空间 ID */
    private final String namespace;
    
    /** 分组名 */
    private final String group;
    
    /** 服务名或资源名 */
    private final String name;
    
    /** 获取事件类型字符串 */
    public String getType() {
        return type;
    }
    
    /** 获取事件发生时间戳 */
    public long getEventTime() {
        return eventTime;
    }
    
    /** 获取命名空间 */
    public String getNamespace() {
        return namespace;
    }
    
    /** 获取分组名 */
    public String getGroup() {
        return group;
    }
    
    /** 获取服务或资源名称 */
    public String getName() {
        return name;
    }
    
    /**
     * 构造通用追踪事件。
     *
     * @param eventType 事件类型
     * @param eventTime 发生时间戳
     * @param namespace 命名空间
     * @param group 分组
     * @param name 服务或资源名
     */
    public TraceEvent(String eventType, long eventTime, String namespace, String group,
        String name) {
        this.type = eventType;
        this.eventTime = eventTime;
        this.namespace = namespace;
        this.group = group;
        this.name = name;
    }
}
