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
package com.alibaba.csp.sentinel.cluster.server.processor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.cluster.annotation.RequestType;
import com.alibaba.csp.sentinel.spi.SpiLoader;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * 集群请求处理器提供者，通过 SPI 加载并缓存 {@link RequestProcessor} 实例。
 * <p>处理器类型由 {@link RequestType} 注解的 value 决定。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class RequestProcessorProvider {

    private static final Map<Integer, RequestProcessor> PROCESSOR_MAP = new ConcurrentHashMap<>();

    static {
        loadAndInit();
    }

    private static void loadAndInit() {
        List<RequestProcessor> processors = SpiLoader.of(RequestProcessor.class).loadInstanceList();
        for (RequestProcessor processor : processors) {
            Integer type = parseRequestType(processor);
            if (type != null) {
                PROCESSOR_MAP.put(type, processor);
            }
        }
    }

    private static Integer parseRequestType(RequestProcessor processor) {
        RequestType requestType = processor.getClass().getAnnotation(RequestType.class);
        if (requestType != null) {
            return requestType.value();
        } else {
            return null;
        }
    }

    /** 按消息类型获取已注册的请求处理器。
     *
     * @param type 集群消息类型
     * @return 对应的处理器，未注册时返回 null
     */
    public static RequestProcessor getProcessor(int type) {
        return PROCESSOR_MAP.get(type);
    }

    static void addProcessorIfAbsent(int type, RequestProcessor processor) {
        PROCESSOR_MAP.putIfAbsent(type, processor);
    }

    static void addProcessor(int type, RequestProcessor processor) {
        AssertUtil.notNull(processor, "processor cannot be null");
        PROCESSOR_MAP.put(type, processor);
    }

    private RequestProcessorProvider() {}
}
