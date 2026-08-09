/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.store.ha;

import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.utils.ConcurrentHashMapUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 等待/唤醒工具：支持单线程 waitForRunning 与多线程 allWaitForRunning。
 */
public class WaitNotifyObject {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME);

    /** 多线程等待表：线程 ID -> 是否已被唤醒。 */
    protected final ConcurrentHashMap<Long/* thread id */, AtomicBoolean/* notified */> waitingThreadTable =
        new ConcurrentHashMap<>(16);

    /** 单线程模式下的唤醒标志。 */
    protected AtomicBoolean hasNotified = new AtomicBoolean(false);

    /** 唤醒单个等待线程。 */
    public void wakeup() {
        boolean needNotify = hasNotified.compareAndSet(false, true);
        if (needNotify) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    /** 带超时的 wait，被唤醒或超时后调用 onWaitEnd。 */
    protected void waitForRunning(long interval) {
        if (this.hasNotified.compareAndSet(true, false)) {
            this.onWaitEnd();
            return;
        }
        synchronized (this) {
            try {
                if (this.hasNotified.compareAndSet(true, false)) {
                    this.onWaitEnd();
                    return;
                }
                this.wait(interval);
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            } finally {
                this.hasNotified.set(false);
                this.onWaitEnd();
            }
        }
    }

    /** 等待结束钩子，子类可覆盖。 */
    protected void onWaitEnd() {
    }

    /** 唤醒 waitingThreadTable 中所有等待线程。 */
    public void wakeupAll() {
        boolean needNotify = false;
        for (Map.Entry<Long, AtomicBoolean> entry : this.waitingThreadTable.entrySet()) {
            if (entry.getValue().compareAndSet(false, true)) {
                needNotify = true;
            }
        }
        if (needNotify) {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    /** 当前线程注册到等待表并 wait。 */
    public void allWaitForRunning(long interval) {
        long currentThreadId = Thread.currentThread().getId();
        AtomicBoolean notified = ConcurrentHashMapUtils.computeIfAbsent(this.waitingThreadTable, currentThreadId, k -> new AtomicBoolean(false));
        if (notified.compareAndSet(true, false)) {
            this.onWaitEnd();
            return;
        }
        synchronized (this) {
            try {
                if (notified.compareAndSet(true, false)) {
                    this.onWaitEnd();
                    return;
                }
                this.wait(interval);
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            } finally {
                notified.set(false);
                this.onWaitEnd();
            }
        }
    }

    /** 从等待表移除当前线程。 */
    public void removeFromWaitingThreadTable() {
        long currentThreadId = Thread.currentThread().getId();
        synchronized (this) {
            this.waitingThreadTable.remove(currentThreadId);
        }
    }
}
