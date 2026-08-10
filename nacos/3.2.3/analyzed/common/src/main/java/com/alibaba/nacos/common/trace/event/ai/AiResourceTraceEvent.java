/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.trace.event.ai;

import com.alibaba.nacos.common.trace.event.TraceEvent;

/**
 * AI 资源追踪事件：记录 AI 模块对资源（模型、数据集等）的增删改查操作，
 * 包含操作者、资源类型/ID、版本、状态及客户端 IP 等审计字段。
 * AI resource trace event.
 *
 * @author nacos
 */
public class AiResourceTraceEvent extends TraceEvent {
    
    /** AI 资源追踪事件的固定类型常量 */
    public static final String AI_RESOURCE_TRACE_EVENT = "AI_RESOURCE_TRACE_EVENT";
    
    private static final long serialVersionUID = -2114269686069278879L;
    
    /** 执行操作的用户或系统主体 */
    private final String operator;
    
    /** 资源类型，如模型、Prompt 模板等 */
    private final String resourceType;
    
    /** 资源唯一标识 */
    private final String resourceId;
    
    /** 资源版本号 */
    private final String version;
    
    /** 操作类型，如 CREATE、UPDATE、DELETE */
    private final String operation;
    
    /** 操作结果状态，如 SUCCESS、FAILED */
    private final String status;
    
    /** 发起请求的客户端 IP */
    private final String clientIp;
    
    /** 扩展信息 JSON 或附加说明 */
    private final String ext;
    
    public AiResourceTraceEvent(long eventTime, String operator, String resourceType,
        String resourceId, String version, String operation, String status, String clientIp,
        String ext) {
        super(AI_RESOURCE_TRACE_EVENT, eventTime, "", resourceType, resourceId);
        this.operator = operator;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.version = version;
        this.operation = operation;
        this.status = status;
        this.clientIp = clientIp;
        this.ext = ext;
    }
    
    /** 获取操作者标识 */
    public String getOperator() {
        return operator;
    }
    
    /** 获取资源类型 */
    public String getResourceType() {
        return resourceType;
    }
    
    /** 获取资源 ID */
    public String getResourceId() {
        return resourceId;
    }
    
    /** 获取资源版本 */
    public String getVersion() {
        return version;
    }
    
    /** 获取操作类型 */
    public String getOperation() {
        return operation;
    }
    
    /** 获取操作状态 */
    public String getStatus() {
        return status;
    }
    
    /** 获取客户端 IP */
    public String getClientIp() {
        return clientIp;
    }
    
    /** 获取扩展字段内容 */
    public String getExt() {
        return ext;
    }
    
    /** 标记为插件事件，由 NotifyCenter 插件通道分发 */
    @Override
    public boolean isPluginEvent() {
        return true;
    }
}
