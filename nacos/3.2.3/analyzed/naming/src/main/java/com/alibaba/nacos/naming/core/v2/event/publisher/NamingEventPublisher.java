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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.ShardedEventPublisher;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.common.utils.ThreadUtils;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alipay.sofa.jraft.util.concurrent.ConcurrentHashSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 命名模块事件发布器。
 *
 * <p>基于独立线程与有界队列异步分发 {@link Event}，实现 {@link ShardedEventPublisher} 接口；队列满时降级为同步处理以避免事件丢失。</p>
 *
 * @author xiweng.yy
 */
public class NamingEventPublisher extends Thread implements ShardedEventPublisher {
    
    /** 发布线程名前缀。 */
    private static final String THREAD_NAME = "naming.publisher-";
    
    /** 启动时等待首个订阅者注册的最大秒数。 */
    private static final int DEFAULT_WAIT_TIME = 60;
    
    /** 事件类型到订阅者集合的映射。 */
    private final Map<Class<? extends Event>, Set<Subscriber<? extends Event>>> subscribes =
        new ConcurrentHashMap<>();
    
    /** 是否已完成 init 并启动发布线程。 */
    private volatile boolean initialized = false;
    
    /** 是否已关闭，关闭后不再消费队列事件。 */
    private volatile boolean shutdown = false;
    
    /** 事件队列容量上限。 */
    private int queueMaxSize = -1;
    
    /** 待分发事件的有界阻塞队列。 */
    private BlockingQueue<Event> queue;
    
    /** 当前发布器对应的事件类型简称。 */
    private String publisherName;
    
    /** 初始化队列并启动守护线程消费事件。 */
    @Override
    public void init(Class<? extends Event> type, int bufferSize) {
        this.queueMaxSize = bufferSize;
        this.queue = new ArrayBlockingQueue<>(bufferSize);
        this.publisherName = type.getSimpleName();
        super.setName(THREAD_NAME + this.publisherName);
        super.setDaemon(true);
        super.start();
        initialized = true;
    }
    
    /** 返回当前队列中待处理事件数量。 */
    @Override
    public long currentEventSize() {
        return this.queue.size();
    }
    
    /** 按订阅者声明的类型注册订阅。 */
    @Override
    public void addSubscriber(Subscriber subscriber) {
        addSubscriber(subscriber, subscriber.subscribeType());
    }
    
    /** 将订阅者注册到指定事件类型。 */
    @Override
    public void addSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType) {
        subscribes.computeIfAbsent(subscribeType, inputType -> new ConcurrentHashSet<>())
            .add(subscriber);
    }
    
    /** 按订阅者默认类型移除订阅。 */
    @Override
    public void removeSubscriber(Subscriber subscriber) {
        removeSubscriber(subscriber, subscriber.subscribeType());
    }
    
    /** 从指定事件类型移除订阅者。 */
    @Override
    public void removeSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType) {
        subscribes.computeIfPresent(subscribeType, (inputType, subscribers) -> {
            subscribers.remove(subscriber);
            return subscribers.isEmpty() ? null : subscribers;
        });
    }
    
    /** 将事件入队；队列满时同步处理以保证不丢事件。 */
    @Override
    public boolean publish(Event event) {
        checkIsStart();
        boolean success = this.queue.offer(event);
        if (!success) {
            Loggers.EVT_LOG.warn(
                "Unable to plug in due to interruption, synchronize sending time, event : {}",
                event);
            handleEvent(event);
        }
        return true;
    }
    
    /** 通知单个订阅者，优先使用其自定义线程池执行回调。 */
    @Override
    public void notifySubscriber(Subscriber subscriber, Event event) {
        if (Loggers.EVT_LOG.isDebugEnabled()) {
            Loggers.EVT_LOG.debug("[NotifyCenter] the {} will received by {}", event, subscriber);
        }
        final Runnable job = () -> subscriber.onEvent(event);
        final Executor executor = subscriber.executor();
        if (executor != null) {
            executor.execute(job);
        } else {
            try {
                job.run();
            } catch (Throwable e) {
                Loggers.EVT_LOG.error("Event callback exception: ", e);
            }
        }
    }
    
    /** 关闭发布器并清空待处理队列。 */
    @Override
    public void shutdown() throws NacosException {
        this.shutdown = true;
        this.queue.clear();
    }
    
    /** 发布线程主循环：等待订阅者就绪后持续消费队列。 */
    @Override
    public void run() {
        try {
            waitSubscriberForInit();
            handleEvents();
        } catch (Exception e) {
            Loggers.EVT_LOG.error(
                "Naming Event Publisher {}, stop to handle event due to unexpected exception: ",
                this.publisherName, e);
        }
    }
    
    private void waitSubscriberForInit() {
        // 等待首个订阅者注册，避免启动阶段事件无人处理而丢失
        for (int waitTimes = DEFAULT_WAIT_TIME; waitTimes > 0; waitTimes--) {
            if (shutdown || !subscribes.isEmpty()) {
                break;
            }
            ThreadUtils.sleep(1000L);
        }
    }
    
    private void handleEvents() {
        while (!shutdown) {
            try {
                final Event event = queue.take();
                handleEvent(event);
            } catch (InterruptedException e) {
                Loggers.EVT_LOG.warn("Naming Event Publisher {} take event from queue failed:",
                    this.publisherName, e);
                // 恢复中断标志，便于上层感知线程中断
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void handleEvent(Event event) {
        Class<? extends Event> eventType = event.getClass();
        Set<Subscriber<? extends Event>> subscribers = subscribes.get(eventType);
        if (null == subscribers) {
            if (Loggers.EVT_LOG.isDebugEnabled()) {
                Loggers.EVT_LOG.debug("[NotifyCenter] No subscribers for slow event {}",
                    eventType.getName());
            }
            return;
        }
        for (Subscriber subscriber : subscribers) {
            notifySubscriber(subscriber, event);
        }
    }
    
    /** 校验发布器已初始化，否则抛出非法状态异常。 */
    void checkIsStart() {
        if (!initialized) {
            throw new IllegalStateException("Publisher does not start");
        }
    }
    
    /** 返回发布器运行状态摘要（关闭标志与队列占用）。 */
    public String getStatus() {
        return String.format("Publisher %-30s: shutdown=%5s, queue=%7d/%-7d", publisherName,
            shutdown,
            currentEventSize(), queueMaxSize);
    }
}
