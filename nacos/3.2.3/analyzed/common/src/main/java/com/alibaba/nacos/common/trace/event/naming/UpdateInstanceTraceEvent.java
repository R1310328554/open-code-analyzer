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

import java.util.Map;

/**
 * 命名服务「更新实例」链路追踪事件：记录客户端对某服务下实例元数据或地址的变更操作，
 * 继承 {@link NamingTraceEvent}，携带客户端 IP、实例 IP/端口及更新后的 metadata。
 * Naming update instance trace event.
 *
 * @author stone-98
 * @date 2023/8/31
 */
public class UpdateInstanceTraceEvent extends NamingTraceEvent {
    
    private static final long serialVersionUID = -6995370254824508523L;
    
    /** 实例更新后的元数据键值对 */
    private final Map<String, String> metadata;
    
    /** 发起更新请求的客户端 IP */
    private final String clientIp;
    
    /** 被更新实例的 IP 地址 */
    private final String instanceIp;
    
    /** 被更新实例的端口号 */
    private final int instancePort;
    
    /** 返回实例元数据 */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /** 返回客户端 IP */
    public String getClientIp() {
        return clientIp;
    }
    
    /** 返回实例 IP */
    public String getInstanceIp() {
        return instanceIp;
    }
    
    /** 返回实例端口 */
    public int getInstancePort() {
        return instancePort;
    }
    
    /** 将实例 IP 与端口格式化为 {@code ip:port} 字符串 */
    public String toInetAddr() {
        return instanceIp + ":" + instancePort;
    }
    
    /**
     * 构造更新实例追踪事件。
     *
     * @param eventTime         事件发生时间戳
     * @param clientIp          客户端 IP
     * @param serviceNamespace  服务命名空间
     * @param serviceGroup      服务分组
     * @param serviceName       服务名
     * @param instanceIp        实例 IP
     * @param instancePort      实例端口
     * @param metadata          更新后的元数据
     */
    public UpdateInstanceTraceEvent(long eventTime, String clientIp, String serviceNamespace,
        String serviceGroup,
        String serviceName, String instanceIp, int instancePort, Map<String, String> metadata) {
        super("UPDATE_INSTANCE_TRACE_EVENT", eventTime, serviceNamespace, serviceGroup,
            serviceName);
        this.clientIp = clientIp;
        this.instanceIp = instanceIp;
        this.instancePort = instancePort;
        this.metadata = metadata;
    }
    
}
