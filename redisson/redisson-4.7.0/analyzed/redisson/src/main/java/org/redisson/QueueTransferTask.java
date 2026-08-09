/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson;

import io.netty.util.Timeout;
import org.redisson.api.RFuture;
import org.redisson.api.RTopic;
import org.redisson.api.listener.BaseStatusListener;
import org.redisson.connection.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 抽象队列转移任务：通过 {@link RTopic} 协调多节点上的 push/schedule。
 * <p>订阅调度主题后，按远端下发的 startTime 延迟执行 {@link #pushTaskAsync()}。
 *
 * @author Nikita Koksharov
 */
public abstract class QueueTransferTask {
    
    private static final Logger log = LoggerFactory.getLogger(QueueTransferTask.class);

    /** 记录 Netty 延迟任务及其计划开始时间。 */
    public static class TimeoutTask {
        
        private final long startTime;
        private final Timeout task;
        
        public TimeoutTask(long startTime, Timeout task) {
            super();
            this.startTime = startTime;
            this.task = task;
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public Timeout getTask() {
            return task;
        }
        
    }
    
    private volatile int usage = 1;
    private final AtomicReference<TimeoutTask> lastTimeout = new AtomicReference<TimeoutTask>();
    private final ServiceManager serviceManager;

    /** @param serviceManager 提供定时器与关闭检测 */
    public QueueTransferTask(ServiceManager serviceManager) {
        super();
        this.serviceManager = serviceManager;
    }

    /** 增加引用计数（{@link QueueTransferService} 复用时调用）。 */
    public void incUsage() {
        usage++;
    }
    
    /** 递减引用计数并返回当前值。 */
    public int decUsage() {
        usage--;
        return usage;
    }
    
    private int messageListenerId;
    private int statusListenerId;
    
    /** 订阅调度 Topic：就绪时 push，收到 startTime 时 schedule。 */
    public void start() {
        RTopic schedulerTopic = getTopic();
        statusListenerId = schedulerTopic.addListener(new BaseStatusListener() {
            @Override
            public void onSubscribe(String channel) {
                pushTask();
            }
        });
        
        messageListenerId = schedulerTopic.addListener(Long.class, (channel, startTime) -> scheduleTask(startTime));
    }
    
    /** 移除监听器并取消未执行的延迟任务。 */
    public void stop() {
        RTopic schedulerTopic = getTopic();
        schedulerTopic.removeListener(messageListenerId, statusListenerId);

        TimeoutTask oldTimeout = lastTimeout.get();
        if (oldTimeout != null) {
            oldTimeout.getTask().cancel();
        }
    }

    /** 按 startTime 与当前时间差安排 push；过近则立即执行。 */
    private void scheduleTask(final Long startTime) {
        if (usage == 0) {
            return;
        }

        if (startTime == null) {
            return;
        }

        TimeoutTask oldTimeout = lastTimeout.get();
        if (oldTimeout != null) {
            oldTimeout.getTask().cancel();
        }
        
        long delay = startTime - System.currentTimeMillis();
        if (delay > 10) {
            Timeout timeout = serviceManager.newTimeout(timeout1 -> {
                pushTask();

                TimeoutTask currentTimeout = lastTimeout.get();
                if (currentTimeout != null
                        && currentTimeout.getTask() == timeout1) {
                    lastTimeout.compareAndSet(currentTimeout, null);
                }
            }, delay, TimeUnit.MILLISECONDS);
            
            lastTimeout.compareAndSet(oldTimeout, new TimeoutTask(startTime, timeout));
        } else {
            pushTask();
        }
    }
    
    /** 返回本队列使用的调度 {@link RTopic}。 */
    protected abstract RTopic getTopic();
    
    /** 执行一次队列转移并返回下次计划的 startTime（可为 null）。 */
    protected abstract RFuture<Long> pushTaskAsync();
    
    /** 异步 push；失败时 5 秒后重试。 */
    private void pushTask() {
        if (usage == 0) {
            return;
        }

        RFuture<Long> startTimeFuture = pushTaskAsync();
        startTimeFuture.whenComplete((res, e) -> {
            if (e != null) {
                if (serviceManager.isShuttingDown(e)) {
                    return;
                }
                log.error(e.getMessage(), e);
                scheduleTask(System.currentTimeMillis() + 5 * 1000L);
                return;
            }
            
            if (res != null) {
                scheduleTask(res);
            }
        });
    }

}
