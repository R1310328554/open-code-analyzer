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
 * 批量注册实例追踪事件：继承 {@link RegisterInstanceTraceEvent}，事件类型为
 * {@code BATCH_REGISTER_INSTANCE_TRACE_EVENT}，用于记录一次 RPC/HTTP 批量注册中的单条实例。
 * Naming register instance trace event.
 *
 * @author xiweng.yy
 */
public class BatchRegisterInstanceTraceEvent extends RegisterInstanceTraceEvent {
    
    /**
     * 构造批量注册实例追踪事件。
     *
     * @param eventTime 事件时间戳
     * @param clientIp 客户端 IP
     * @param rpc 是否经 RPC 通道注册
     * @param serviceNamespace 服务命名空间
     * @param serviceGroup 服务分组
     * @param serviceName 服务名
     * @param instanceIp 实例 IP
     * @param instancePort 实例端口
     */
    public BatchRegisterInstanceTraceEvent(long eventTime, String clientIp, boolean rpc,
        String serviceNamespace,
        String serviceGroup, String serviceName, String instanceIp, int instancePort) {
        super("BATCH_REGISTER_INSTANCE_TRACE_EVENT", eventTime, clientIp, rpc, serviceNamespace,
            serviceGroup,
            serviceName, instanceIp, instancePort);
    }
}
