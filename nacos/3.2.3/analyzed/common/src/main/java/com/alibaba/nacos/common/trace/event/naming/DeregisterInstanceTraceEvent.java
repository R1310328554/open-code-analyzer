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

import com.alibaba.nacos.common.trace.DeregisterInstanceReason;

/**
 * 注销实例追踪事件：记录实例从 Naming 注册表移除的完整上下文，
 * 包含客户端 IP、是否 RPC、实例地址及 {@link com.alibaba.nacos.common.trace.DeregisterInstanceReason} 原因。
 * Naming deregister instance trace event.
 *
 * @author yanda
 */
public class DeregisterInstanceTraceEvent extends NamingTraceEvent {
    
    private static final long serialVersionUID = 3850573686472190256L;
    
    /** 发起注销的客户端 IP */
    private final String clientIp;
    
    /** 是否通过 RPC 通道注销 */
    private final boolean rpc;
    
    /** 被注销实例 IP */
    private final String instanceIp;
    
    /** 被注销实例端口 */
    private final int instancePort;
    
    /** 实例注销原因枚举 */
    public final DeregisterInstanceReason reason;
    
    /** 获取客户端 IP */
    public String getClientIp() {
        return clientIp;
    }
    
    /** 是否为 RPC 注销 */
    public boolean isRpc() {
        return rpc;
    }
    
    /** 获取实例 IP */
    public String getInstanceIp() {
        return instanceIp;
    }
    
    /** 获取实例端口 */
    public int getInstancePort() {
        return instancePort;
    }
    
    /** 返回 {@code ip:port} 格式的实例网络地址 */
    public String toInetAddr() {
        return instanceIp + ":" + instancePort;
    }
    
    /** 获取注销原因 */
    public DeregisterInstanceReason getReason() {
        return reason;
    }
    
    public DeregisterInstanceTraceEvent(long eventTime, String clientIp, boolean rpc,
        DeregisterInstanceReason reason,
        String serviceNamespace, String serviceGroup, String serviceName, String instanceIp,
        int instancePort) {
        super("DEREGISTER_INSTANCE_TRACE_EVENT", eventTime, serviceNamespace, serviceGroup,
            serviceName);
        this.clientIp = clientIp;
        this.reason = reason;
        this.rpc = rpc;
        this.instanceIp = instanceIp;
        this.instancePort = instancePort;
    }
}
