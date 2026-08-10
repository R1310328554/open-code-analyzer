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
 * 服务推送追踪事件：记录 Naming 向订阅客户端推送实例列表的耗时与 SLA 指标，
 * 包含网络耗时、总耗时、SLA 阈值及推送实例数量，用于性能诊断。
 * Naming push service trace event.
 *
 * @author yanda
 */
public class PushServiceTraceEvent extends NamingTraceEvent {
    
    private static final long serialVersionUID = 787915741281241877L;
    
    /** 接收推送的客户端 IP */
    private final String clientIp;
    
    /** 本次推送包含的实例数量 */
    private final int instanceSize;
    
    /** 网络传输耗时（毫秒） */
    private final long pushCostTimeForNetWork;
    
    /** 推送全流程总耗时（毫秒） */
    private final long pushCostTimeForAll;
    
    /** SLA 约定的时间阈值（毫秒） */
    private final long serviceLevelAgreementTime;
    
    public String getClientIp() {
        return clientIp;
    }
    
    /** 获取推送实例数 */
    public int getInstanceSize() {
        return instanceSize;
    }
    
    /** 获取网络层推送耗时 */
    public long getPushCostTimeForNetWork() {
        return pushCostTimeForNetWork;
    }
    
    /** 获取推送总耗时 */
    public long getPushCostTimeForAll() {
        return pushCostTimeForAll;
    }
    
    /** 获取 SLA 时间阈值 */
    public long getServiceLevelAgreementTime() {
        return serviceLevelAgreementTime;
    }
    
    public PushServiceTraceEvent(long eventTime, long pushCostTimeForNetWork,
        long pushCostTimeForAll,
        long serviceLevelAgreementTime, String clientIp, String serviceNamespace,
        String serviceGroup,
        String serviceName, int instanceSize) {
        super("PUSH_SERVICE_TRACE_EVENT", eventTime, serviceNamespace, serviceGroup, serviceName);
        this.clientIp = clientIp;
        this.instanceSize = instanceSize;
        this.pushCostTimeForAll = pushCostTimeForAll;
        this.pushCostTimeForNetWork = pushCostTimeForNetWork;
        this.serviceLevelAgreementTime = serviceLevelAgreementTime;
    }
}
