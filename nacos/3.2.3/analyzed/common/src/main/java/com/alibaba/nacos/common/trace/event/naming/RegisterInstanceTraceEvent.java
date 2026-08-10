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
 * 注册实例追踪事件：记录客户端向 Naming 注册单个服务实例的操作，
 * 包含客户端 IP、是否 RPC 通道及实例 {@code ip:port} 等字段。
 * Naming register instance trace event.
 *
 * @author yanda
 */
public class RegisterInstanceTraceEvent extends NamingTraceEvent {
    
    private static final long serialVersionUID = -8283438151444483864L;
    
    /** 发起注册的客户端 IP */
    private final String clientIp;
    
    /** 是否经 RPC 通道注册 */
    private final boolean rpc;
    
    /** 注册实例 IP */
    private final String instanceIp;
    
    /** 注册实例端口 */
    private final int instancePort;
    
    public String getClientIp() {
        return clientIp;
    }
    
    public boolean isRpc() {
        return rpc;
    }
    
    public String getInstanceIp() {
        return instanceIp;
    }
    
    public int getInstancePort() {
        return instancePort;
    }
    
    /** 返回 {@code ip:port} 格式的实例地址 */
    public String toInetAddr() {
        return instanceIp + ":" + instancePort;
    }
    
    /**
     * 以默认事件类型 {@code REGISTER_INSTANCE_TRACE_EVENT} 构造注册实例追踪事件。
     */
    public RegisterInstanceTraceEvent(long eventTime, String clientIp, boolean rpc,
        String serviceNamespace,
        String serviceGroup, String serviceName, String instanceIp, int instancePort) {
        this("REGISTER_INSTANCE_TRACE_EVENT", eventTime, clientIp, rpc, serviceNamespace,
            serviceGroup, serviceName,
            instanceIp, instancePort);
    }
    
    /**
     * 以自定义事件类型构造注册实例追踪事件（供 {@link BatchRegisterInstanceTraceEvent} 复用）。
     *
     * @param eventType 事件类型字符串
     * @param eventTime 时间戳
     * @param clientIp 客户端 IP
     * @param rpc 是否 RPC
     * @param serviceNamespace 命名空间
     * @param serviceGroup 分组
     * @param serviceName 服务名
     * @param instanceIp 实例 IP
     * @param instancePort 实例端口
     */
    public RegisterInstanceTraceEvent(String eventType, long eventTime, String clientIp,
        boolean rpc,
        String serviceNamespace, String serviceGroup, String serviceName, String instanceIp,
        int instancePort) {
        super(eventType, eventTime, serviceNamespace, serviceGroup, serviceName);
        this.clientIp = clientIp;
        this.rpc = rpc;
        this.instanceIp = instanceIp;
        this.instancePort = instancePort;
    }
}
