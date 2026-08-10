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

package com.alibaba.nacos.naming.core.v2.event.publisher;

import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.EventPublisher;
import com.alibaba.nacos.common.notify.EventPublisherFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 命名事件发布器工厂（单例）。
 *
 * <p>部分命名事件需严格保序，因此按事件类型复用同一 {@link NamingEventPublisher} （同线程、同队列）进行同步分发。</p>
 *
 * @author xiweng.yy
 */
public class NamingEventPublisherFactory implements EventPublisherFactory {
    
    /** 全局单例实例。 */
    private static final NamingEventPublisherFactory INSTANCE = new NamingEventPublisherFactory();
    
    /** 事件类型到发布器实例的缓存。 */
    private final Map<Class<? extends Event>, NamingEventPublisher> publisher;
    
    private NamingEventPublisherFactory() {
        publisher = new ConcurrentHashMap<>();
    }
    
    /** 获取工厂单例。 */
    public static NamingEventPublisherFactory getInstance() {
        return INSTANCE;
    }
    
    @Override
    public EventPublisher apply(final Class<? extends Event> eventType,
        final Integer maxQueueSize) {
        // 内部类事件（如 ClientEvent$ClientChangeEvent）按外部类缓存发布器
        Class<? extends Event> cachedEventType =
            eventType.isMemberClass() ? (Class<? extends Event>) eventType.getEnclosingClass()
                : eventType;
        return publisher.computeIfAbsent(cachedEventType, eventClass -> {
            NamingEventPublisher result = new NamingEventPublisher();
            result.init(eventClass, maxQueueSize);
            return result;
        });
    }
    
    /** 汇总所有已创建发布器的运行状态。 */
    public String getAllPublisherStatues() {
        StringBuilder result = new StringBuilder("Naming event publisher statues:\n");
        for (NamingEventPublisher each : publisher.values()) {
            result.append('\t').append(each.getStatus()).append('\n');
        }
        return result.toString();
    }
}
