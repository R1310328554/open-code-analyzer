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

package com.alibaba.nacos.plugin.trace.ai;

import com.alibaba.nacos.common.trace.event.TraceEvent;
import com.alibaba.nacos.common.trace.event.ai.AiResourceTraceEvent;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.trace.spi.NacosTraceSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认 AI 资源追踪订阅者：将 {@link AiResourceTraceEvent} 以 JSON 写入专用 trace 日志。
 *
 * <p>订阅者名称为 {@value #NAME}，输出 logger 为 {@code com.alibaba.nacos.ai.resource.trace}，保持与既有文件日志格式兼容。</p>
 *
 * @author nacos
 */
public class AiResourceTraceLogSubscriber implements NacosTraceSubscriber {
    
    /** 追踪订阅者在 SPI 中的唯一名称。 */
    public static final String NAME = "ai-resource-trace-log";
    
    private static final Logger TRACE_LOG =
        LoggerFactory.getLogger("com.alibaba.nacos.ai.resource.trace");
    
    /** 返回订阅者名称 {@link #NAME}。 */
    @Override
    public String getName() {
        return NAME;
    }
    
    /** 收到 AI 资源追踪事件时序列化为 JSON 并写入 info 日志。 */
    @Override
    public void onEvent(TraceEvent event) {
        if (!(event instanceof AiResourceTraceEvent) || !TRACE_LOG.isInfoEnabled()) {
            return;
        }
        TRACE_LOG.info(JacksonUtils.toJson(buildLogEntry((AiResourceTraceEvent) event)));
    }
    
    /** 仅订阅 {@link AiResourceTraceEvent} 类型。 */
    @Override
    public List<Class<? extends TraceEvent>> subscribeTypes() {
        return Collections.singletonList(AiResourceTraceEvent.class);
    }
    
    /** 将追踪事件字段组装为结构化日志 Map（便于 JSON 输出）。 */
    static Map<String, Object> buildLogEntry(AiResourceTraceEvent event) {
        Map<String, Object> logEntry = new LinkedHashMap<>(10);
        logEntry.put("timestamp",
            DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(event.getEventTime())));
        logEntry.put("operator", StringUtils.defaultIfBlank(event.getOperator(), "-"));
        logEntry.put("resource_type", StringUtils.defaultIfBlank(event.getResourceType(), "-"));
        logEntry.put("resource_id", StringUtils.defaultIfBlank(event.getResourceId(), "-"));
        if (StringUtils.isNotBlank(event.getVersion())) {
            logEntry.put("version", event.getVersion());
        }
        logEntry.put("operation", StringUtils.defaultIfBlank(event.getOperation(), "-"));
        logEntry.put("status", StringUtils.defaultIfBlank(event.getStatus(), "-"));
        logEntry.put("ip", StringUtils.defaultIfBlank(event.getClientIp(), "-"));
        if (StringUtils.isNotBlank(event.getExt())) {
            logEntry.put("ext", event.getExt());
        }
        return logEntry;
    }
}
