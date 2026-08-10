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

import com.alibaba.nacos.common.trace.HealthCheckType;

/**
 * 实例健康状态变更追踪事件：记录实例由健康/不健康切换的原因与探测类型，
 * 供运维审计与 SLA 分析；{@link #getHealthCheckTypeFromReason(String)} 从原因字符串推断检查类型。
 * Naming instance health state change trace event.
 *
 * @author yanda
 */
public class HealthStateChangeTraceEvent extends NamingTraceEvent {
    
    private static final long serialVersionUID = 6966396191118694597L;
    
    /** 实例 IP 地址 */
    private final String instanceIp;
    
    /** 实例端口 */
    private final int instancePort;
    
    /** 变更后的健康状态：{@code true} 为健康 */
    private final boolean isHealthy;
    
    /** 触发本次变更的健康检查类型 */
    private final HealthCheckType healthCheckType;
    
    /** 健康状态变更的详细原因描述 */
    private final String healthStateChangeReason;
    
    public String getInstanceIp() {
        return instanceIp;
    }
    
    public int getInstancePort() {
        return instancePort;
    }
    
    public String toInetAddr() {
        return instanceIp + ":" + instancePort;
    }
    
    /** 返回变更后是否健康 */
    public boolean isHealthy() {
        return isHealthy;
    }
    
    /** 获取健康检查类型 */
    public HealthCheckType getHealthCheckType() {
        return healthCheckType;
    }
    
    /** 获取状态变更原因字符串 */
    public String getHealthStateChangeReason() {
        return healthStateChangeReason;
    }
    
    public HealthStateChangeTraceEvent(long eventTime, String serviceNamespace, String serviceGroup,
        String serviceName,
        String instanceIp, int instancePort, boolean isHealthy, String healthStateChangeReason) {
        super("HEALTH_STATE_CHANGE_TRACE_EVENT", eventTime, serviceNamespace, serviceGroup,
            serviceName);
        this.instanceIp = instanceIp;
        this.instancePort = instancePort;
        this.isHealthy = isHealthy;
        this.healthCheckType = getHealthCheckTypeFromReason(healthStateChangeReason);
        this.healthStateChangeReason = healthStateChangeReason;
    }
    
    /**
     * 根据原因字符串前缀匹配对应的 {@link HealthCheckType}；未匹配时默认为客户端心跳。
     *
     * @param reason 健康状态变更原因
     * @return 推断出的健康检查类型
     */
    public HealthCheckType getHealthCheckTypeFromReason(String reason) {
        if (reason.startsWith(HealthCheckType.HTTP_HEALTH_CHECK.getPrefix())) {
            return HealthCheckType.HTTP_HEALTH_CHECK;
        } else if (reason.startsWith(HealthCheckType.TCP_SUPER_SENSE.getPrefix())) {
            return HealthCheckType.TCP_SUPER_SENSE;
        } else if (reason.startsWith(HealthCheckType.MYSQL_HEALTH_CHECK.getPrefix())) {
            return HealthCheckType.MYSQL_HEALTH_CHECK;
        }
        return HealthCheckType.CLIENT_BEAT;
    }
}
