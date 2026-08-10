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

package com.alibaba.nacos.common.notify;

import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The default share event publisher implementation for slow event.
 * <p>慢事件共享发布器：多种 {@link SlowEvent} 子类型共用单线程与队列，按事件 Class 维护 subMappings 以 O(1) 查找对应订阅者。</p>
 *
 * @author zongtanghu
 */
public class DefaultSharePublisher extends DefaultPublisher implements ShardedEventPublisher {
    
    /** 慢事件类型 → 订阅者集合的分组映射 */
    private final Map<Class<? extends SlowEvent>, Set<Subscriber>> subMappings =
        new ConcurrentHashMap<>();
    
    /** 保护 subMappings 增删的互斥锁 */
    private final Lock lock = new ReentrantLock();
    
    @Override
    public void addSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType) {
        // 按 SlowEvent 子类型分类注册；同时加入父类 subscribers 以唤醒消费线程
        // Actually, do a classification based on the slowEvent type.
        Class<? extends SlowEvent> subSlowEventType = (Class<? extends SlowEvent>) subscribeType;
        // For stop waiting subscriber, see {@link DefaultPublisher#openEventHandler}.
        subscribers.add(subscriber);
        
        lock.lock();
        try {
            Set<Subscriber> sets = subMappings.get(subSlowEventType);
            if (sets == null) {
                Set<Subscriber> newSet = new ConcurrentHashSet<>();
                newSet.add(subscriber);
                subMappings.put(subSlowEventType, newSet);
                return;
            }
            sets.add(subscriber);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void removeSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType) {
        // Actually, do a classification based on the slowEvent type.
        Class<? extends SlowEvent> subSlowEventType = (Class<? extends SlowEvent>) subscribeType;
        // For removing to parent class attributes synchronization.
        subscribers.remove(subscriber);
        
        lock.lock();
        try {
            Set<Subscriber> sets = subMappings.get(subSlowEventType);
            
            if (sets != null) {
                sets.remove(subscriber);
            }
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void receiveEvent(Event event) {
        
        final long currentEventSequence = event.sequence();
        // get subscriber set based on the slow EventType.
        final Class<? extends SlowEvent> slowEventType =
            (Class<? extends SlowEvent>) event.getClass();
        
        // 按具体 SlowEvent 类型 O(1) 取订阅者，无映射则直接返回
        // Get for Map, the algorithm is O(1).
        Set<Subscriber> subscribers = subMappings.get(slowEventType);
        if (null == subscribers) {
            LOGGER.debug("[NotifyCenter] No subscribers for slow event {}",
                slowEventType.getName());
            return;
        }
        
        // Notification single event subscriber
        for (Subscriber subscriber : subscribers) {
            // Whether to ignore expiration events
            if (subscriber.ignoreExpireEvent() && lastEventSequence > currentEventSequence) {
                LOGGER.debug(
                    "[NotifyCenter] the {} is unacceptable to this subscriber, because had expire",
                    event.getClass());
                continue;
            }
            
            // 逐个通知慢事件订阅者
            // Notify single subscriber for slow event.
            notifySubscriber(subscriber, event);
        }
    }
}
