/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.trace.publisher;

import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.EventPublisher;
import com.alibaba.nacos.common.notify.EventPublisherFactory;
import com.alibaba.nacos.common.trace.event.TraceEvent;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链路追踪事件发布器工厂（单例）：按 {@link com.alibaba.nacos.common.trace.event.TraceEvent}
 * 子类缓存 {@link TraceEventPublisher} 实例，避免重复创建消费线程；
 * 实现 {@link com.alibaba.nacos.common.notify.EventPublisherFactory}。
 * event publisher factory for trace event.
 *
 * @author yanda
 */

public class TraceEventPublisherFactory implements EventPublisherFactory {
    
    /** 工厂单例 */
    private static final TraceEventPublisherFactory INSTANCE = new TraceEventPublisherFactory();
    
    /** 事件基类 → 已创建的发布器 */
    private final Map<Class<? extends Event>, TraceEventPublisher> publisher;
    
    /** 已注册的追踪事件类型集合，用于匹配子类 */
    private final Set<Class<? extends Event>> publisherEvents;
    
    private TraceEventPublisherFactory() {
        publisher = new ConcurrentHashMap<>();
        publisherEvents = new ConcurrentHashSet<>();
    }
    
    /** 获取工厂单例 */
    public static TraceEventPublisherFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public EventPublisher apply(final Class<? extends Event> eventType,
        final Integer maxQueueSize) {
        Class<? extends Event> cachedEventType = TraceEvent.class;
        
        for (Class<? extends Event> publisherEvent : publisherEvents) {
            if (publisherEvent.isAssignableFrom(eventType)) {
                cachedEventType = publisherEvent;
                break;
            }
        }
        
        return publisher.computeIfAbsent(cachedEventType, eventClass -> {
            TraceEventPublisher result = new TraceEventPublisher();
            result.init(eventClass, maxQueueSize);
            return result;
        });
    }
    
    /** 汇总所有已创建发布器的 {@link TraceEventPublisher#getStatus()} 文本 */
    public String getAllPublisherStatues() {
        StringBuilder result = new StringBuilder("Trace event publisher statues:\n");
        for (TraceEventPublisher each : publisher.values()) {
            result.append('\t').append(each.getStatus()).append('\n');
        }
        return result.toString();
    }
    
    /** 注册可由此工厂管理的追踪事件类型 */
    public void addPublisherEvent(Class<? extends Event> event) {
        this.publisherEvents.add(event);
    }
}
