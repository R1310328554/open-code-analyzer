/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.trace;

import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.SmartSubscriber;
import com.alibaba.nacos.common.trace.event.TraceEvent;
import com.alibaba.nacos.common.trace.publisher.TraceEventPublisherFactory;
import com.alibaba.nacos.plugin.trace.NacosTracePluginManager;
import com.alibaba.nacos.plugin.trace.spi.NacosTraceSubscriber;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 组合式链路追踪事件订阅者，将插件 {@link NacosTraceSubscriber} 聚合到统一 {@link SmartSubscriber}。
 *
 * <p>按事件类型分发到对应插件，支持插件自定义 {@link NacosTraceSubscriber#executor()} 异步执行。</p>
 *
 * @author xiweng.yy
 */
public class NacosCombinedTraceSubscriber extends SmartSubscriber {
    
    /** 事件类型到感兴趣插件订阅者的映射。 */
    private final Map<Class<? extends TraceEvent>, Set<NacosTraceSubscriber>> interestedEvents;
    
    /**
     * 构造并注册组合事件：扫描插件、过滤感兴趣类型并注册到 {@link NotifyCenter}。
     *
     * @param combinedEvent 组合追踪事件的基类
     */
        this.interestedEvents = new ConcurrentHashMap<>();
        TraceEventPublisherFactory.getInstance().addPublisherEvent(combinedEvent);
        for (NacosTraceSubscriber each : NacosTracePluginManager.getInstance()
            .getAllTraceSubscribers()) {
            filterInterestedEvents(each, combinedEvent);
        }
        NotifyCenter.registerSubscriber(this, TraceEventPublisherFactory.getInstance());
    }
    
    /** 将插件订阅的事件类型过滤并归入 {@link #interestedEvents}。 */
    private void filterInterestedEvents(NacosTraceSubscriber plugin,
        Class<? extends TraceEvent> combinedEvent) {
        for (Class<? extends TraceEvent> each : plugin.subscribeTypes()) {
            if (combinedEvent.isAssignableFrom(each)) {
                interestedEvents.compute(each, (eventClass, nacosTraceSubscribers) -> {
                    if (null == nacosTraceSubscribers) {
                        nacosTraceSubscribers = new HashSet<>();
                    }
                    nacosTraceSubscribers.add(plugin);
                    return nacosTraceSubscribers;
                });
            }
        }
    }
    
    /** {@inheritDoc} 返回本订阅者关注的全部追踪事件类型。 */
    @Override
    public List<Class<? extends Event>> subscribeTypes() {
        return new LinkedList<>(interestedEvents.keySet());
    }
    
    /** {@inheritDoc} 收到事件后分发给对应插件（同步或提交到插件线程池）。 */
    @Override
    public void onEvent(Event event) {
        Set<NacosTraceSubscriber> subscribers = interestedEvents.get(event.getClass());
        if (null == subscribers) {
            return;
        }
        TraceEvent traceEvent = (TraceEvent) event;
        for (NacosTraceSubscriber each : subscribers) {
            if (null != each.executor()) {
                each.executor().execute(() -> onEvent0(each, traceEvent));
            } else {
                onEvent0(each, traceEvent);
            }
        }
    }
    
    /** 调用单个插件的 {@link NacosTraceSubscriber#onEvent}，异常被吞掉以免阻塞其他插件。 */
    private void onEvent0(NacosTraceSubscriber subscriber, TraceEvent event) {
        try {
            subscriber.onEvent(event);
        } catch (Exception ignored) {
        }
    }
    
    /** 从 {@link NotifyCenter} 注销本订阅者。 */
    public void shutdown() {
        NotifyCenter.deregisterSubscriber(this);
    }
}
