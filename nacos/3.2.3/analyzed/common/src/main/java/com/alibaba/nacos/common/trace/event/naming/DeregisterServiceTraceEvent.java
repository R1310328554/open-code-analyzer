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

/**
 * 注销服务追踪事件：当 Naming 模块删除整个服务元数据时发布，
 * 事件类型为 {@code DEREGISTER_SERVICE_TRACE_EVENT}，携带命名空间/分组/服务名。
 * Naming deregister service trace event.
 *
 * @author yanda
 */
public class DeregisterServiceTraceEvent extends NamingTraceEvent {
    
    private static final long serialVersionUID = 7358195336881398548L;
    
    /**
     * 构造服务注销追踪事件。
     *
     * @param eventTime 事件时间戳
     * @param serviceNamespace 命名空间
     * @param serviceGroup 分组
     * @param serviceName 服务名
     */
    public DeregisterServiceTraceEvent(long eventTime, String serviceNamespace, String serviceGroup,
        String serviceName) {
        super("DEREGISTER_SERVICE_TRACE_EVENT", eventTime, serviceNamespace, serviceGroup,
            serviceName);
    }
}
