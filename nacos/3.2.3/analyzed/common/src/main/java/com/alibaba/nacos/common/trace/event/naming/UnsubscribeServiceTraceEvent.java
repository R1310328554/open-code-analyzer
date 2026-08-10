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
 * 取消订阅服务追踪事件：记录客户端取消对某服务的订阅，
 * 事件类型为 {@code UNSUBSCRIBE_SERVICE_TRACE_EVENT}。
 * Naming unsubscribe service trace event.
 *
 * @author yanda
 */
public class UnsubscribeServiceTraceEvent extends NamingTraceEvent {
    
    private static final long serialVersionUID = -7461808613817897106L;
    
    /** 发起取消订阅的客户端 IP */
    private final String clientIp;
    
    /** 获取客户端 IP */
    public String getClientIp() {
        return clientIp;
    }
    
    /**
     * 构造取消订阅追踪事件。
     *
     * @param eventTime 事件时间戳
     * @param clientIp 客户端 IP
     * @param serviceNamespace 命名空间
     * @param serviceGroup 分组
     * @param serviceName 服务名
     */
    public UnsubscribeServiceTraceEvent(long eventTime, String clientIp, String serviceNamespace,
        String serviceGroup,
        String serviceName) {
        super("UNSUBSCRIBE_SERVICE_TRACE_EVENT", eventTime, serviceNamespace, serviceGroup,
            serviceName);
        this.clientIp = clientIp;
    }
}
