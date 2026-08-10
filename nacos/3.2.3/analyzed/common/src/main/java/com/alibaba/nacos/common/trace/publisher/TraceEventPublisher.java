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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.ShardedEventPublisher;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.alibaba.nacos.common.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 链路追踪事件发布器：独立守护线程从有界队列取 {@link com.alibaba.nacos.common.notify.Event}，
 * 按事件类型分发给已注册 {@link com.alibaba.nacos.common.notify.listener.Subscriber}；
 * 实现 {@link com.alibaba.nacos.common.notify.ShardedEventPublisher}，队列满时仅 WARN 不阻塞调用方。
 * Event publisher for trace event.
 *
 * @author yanda
 */
public class TraceEventPublisher extends Thread implements ShardedEventPublisher {
    
    /** 发布线程名前缀 */
    private static final String THREAD_NAME = "trace.publisher-";
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger("com.alibaba.nacos.common.trace.publisher");
    
    /** 启动时等待首个订阅者注册的最长秒数 */
    private static final int DEFAULT_WAIT_TIME = 60;
    
    /** 事件类型 → 订阅者集合（线程安全） */
    private final Map<Class<? extends Event>, Set<Subscriber<? extends Event>>> subscribes =
        new ConcurrentHashMap<>();
    
    /** 是否已完成 init 并启动消费线程 */
    private volatile boolean initialized = false;
    
    /** 关闭标志，true 时退出事件循环 */
    private volatile boolean shutdown = false;
    
    private int queueMaxSize = -1;
    
    private BlockingQueue<Event> queue;
    
    private String publisherName;
    
    /** 初始化有界队列并启动守护消费线程 */
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
    
    @Override
    public long currentEventSize() {
        return this.queue.size();
    }
    
    @Override
    public void addSubscriber(Subscriber subscriber) {
        addSubscriber(subscriber, subscriber.subscribeType());
    }
    
    @Override
    public void addSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType) {
        subscribes.computeIfAbsent(subscribeType, inputType -> new ConcurrentHashSet<>())
            .add(subscriber);
    }
    
    @Override
    public void removeSubscriber(Subscriber subscriber) {
        removeSubscriber(subscriber, subscriber.subscribeType());
    }
    
    @Override
    public void removeSubscriber(Subscriber subscriber, Class<? extends Event> subscribeType) {
        subscribes.computeIfPresent(subscribeType, (inputType, subscribers) -> {
            subscribers.remove(subscriber);
            return subscribers.isEmpty() ? null : subscribers;
        });
    }
    
    /** 非阻塞入队；队列满时记录 WARN，仍返回 true */
    @Override
    public boolean publish(Event event) {
        checkIsStart();
        boolean success = this.queue.offer(event);
        if (!success) {
            LOGGER.warn("Trace Event Publish failed, event : {}, publish queue size : {}", event,
                currentEventSize());
        }
        return true;
    }
    
    /** 同步或委托订阅者 executor 异步执行 onEvent */
    @Override
    public void notifySubscriber(Subscriber subscriber, Event event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[NotifyCenter] the {} will received by {}", event, subscriber);
        }
        final Runnable job = () -> subscriber.onEvent(event);
        final Executor executor = subscriber.executor();
        if (executor != null) {
            executor.execute(job);
        } else {
            try {
                job.run();
            } catch (Throwable e) {
                LOGGER.error("Event callback exception: ", e);
            }
        }
    }
    
    /** 置 shutdown 标志并清空队列 */
    @Override
    public void shutdown() throws NacosException {
        this.shutdown = true;
        this.queue.clear();
    }
    
    @Override
    public void run() {
        try {
            waitSubscriberForInit();
            handleEvents();
        } catch (Exception e) {
            LOGGER.error(
                "Trace Event Publisher {}, stop to handle event due to unexpected exception: ",
                this.publisherName, e);
        }
    }
    
    private void waitSubscriberForInit() {
        // 为避免首个订阅者注册前丢事件，启动后最多等待 DEFAULT_WAIT_TIME 秒
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
                LOGGER.warn("Trace Event Publisher {} take event from queue failed:",
                    this.publisherName, e);
                // 恢复中断标志，便于上层感知
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void handleEvent(Event event) {
        Class<? extends Event> eventType = event.getClass();
        Set<Subscriber<? extends Event>> subscribers = subscribes.get(eventType);
        if (null == subscribers) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[NotifyCenter] No subscribers for slow event {}",
                    eventType.getName());
            }
            return;
        }
        for (Subscriber subscriber : subscribers) {
            notifySubscriber(subscriber, event);
        }
    }
    
    /** 未 init 时抛出 IllegalStateException */
    void checkIsStart() {
        if (!initialized) {
            throw new IllegalStateException("Publisher does not start");
        }
    }
    
    /** 返回发布器运行状态摘要（shutdown、队列占用等） */
    public String getStatus() {
        return String.format("Publisher %-30s: shutdown=%5s, queue=%7d/%-7d", publisherName,
            shutdown,
            currentEventSize(), queueMaxSize);
    }
}
