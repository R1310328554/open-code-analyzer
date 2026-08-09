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
package org.apache.rocketmq.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 后台服务线程基类：封装启动/停止、可中断等待与唤醒机制。
 * 子类在 {@link #run()} 中循环调用 {@link #waitForRunning(long)} 实现定时任务。
 */
public abstract class ServiceThread implements Runnable {
    protected static final Logger log = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);

    /** shutdown 时 join 线程的默认超时（毫秒）。 */
    private static final long JOIN_TIME = 90 * 1000;

    /** 实际执行 {@link Runnable} 的工作线程。 */
    protected Thread thread;
    /** 等待/唤醒同步点，配合 {@link #wakeup()} 提前结束 sleep。 */
    protected final CountDownLatch2 waitPoint = new CountDownLatch2(1);
    /** 是否已被 {@link #wakeup()} 唤醒，避免重复 countDown。 */
    protected volatile AtomicBoolean hasNotified = new AtomicBoolean(false);
    /** 停止标志，子类循环中应检查此字段。 */
    protected volatile boolean stopped = false;
    /** 工作线程是否为守护线程。 */
    protected boolean isDaemon = false;

    // 允许 stop 后再次 start
    /** 是否已启动，保证 start/shutdown 幂等。 */
    private final AtomicBoolean started = new AtomicBoolean(false);

    public ServiceThread() {

    }

    /** 返回服务名（默认类简单名），用作线程名。 */
    public String getServiceName() {
        return this.getClass().getSimpleName();
    }

    /** 启动后台线程；重复调用会被忽略。 */
    public void start() {
        log.info("Try to start service thread:{} started:{} lastThread:{}", getServiceName(), started.get(), thread);
        if (!started.compareAndSet(false, true)) {
            return;
        }
        stopped = false;
        this.thread = new Thread(this, getServiceName());
        this.thread.setDaemon(isDaemon);
        this.thread.start();
        log.info("Start service thread:{} started:{} lastThread:{}", getServiceName(), started.get(), thread);
    }

    /** 优雅停止：不 interrupt，等待 join 超时。 */
    public void shutdown() {
        this.shutdown(false);
    }

    /** 停止服务；{@code interrupt=true} 时中断阻塞中的线程。 */
    public void shutdown(final boolean interrupt) {
        log.info("Try to shutdown service thread:{} started:{} lastThread:{}", getServiceName(), started.get(), thread);
        if (!started.compareAndSet(true, false)) {
            return;
        }
        this.stopped = true;
        log.info("shutdown thread[{}] interrupt={} ", getServiceName(), interrupt);

        // 若线程正在 waitForRunning，先唤醒以便尽快退出
        wakeup();

        try {
            if (interrupt) {
                this.thread.interrupt();
            }

            long beginTime = System.currentTimeMillis();
            if (!this.thread.isDaemon()) {
                this.thread.join(this.getJoinTime());
            }
            long elapsedTime = System.currentTimeMillis() - beginTime;
            log.info("join thread[{}], elapsed time: {}ms, join time:{}ms", getServiceName(), elapsedTime, this.getJoinTime());
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        }
    }

    /** shutdown 时 join 的超时毫秒数，子类可覆盖。 */
    public long getJoinTime() {
        return JOIN_TIME;
    }

    /** 仅置 stopped 标志，不 join 线程（软停止）。 */
    public void makeStop() {
        if (!started.get()) {
            return;
        }
        this.stopped = true;
        log.info("makestop thread[{}] ", this.getServiceName());
    }

    /** 唤醒正在 {@link #waitForRunning(long)} 的线程。 */
    public void wakeup() {
        if (hasNotified.compareAndSet(false, true)) {
            waitPoint.countDown(); // notify
        }
    }

    /** 等待 {@code interval} 毫秒或被 wakeup；结束时调用 {@link #onWaitEnd()}。 */
    protected void waitForRunning(long interval) {
        if (hasNotified.compareAndSet(true, false)) {
            this.onWaitEnd();
            return;
        }

        // 进入等待前先 reset latch
        waitPoint.reset();

        try {
            waitPoint.await(interval, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        } finally {
            hasNotified.set(false);
            this.onWaitEnd();
        }
    }

    /** 每次 waitForRunning 结束时的钩子，子类可覆写执行周期任务。 */
    protected void onWaitEnd() {
    }

    /** 是否已请求停止。 */
    public boolean isStopped() {
        return stopped;
    }

    /** 工作线程是否为守护线程。 */
    public boolean isDaemon() {
        return isDaemon;
    }

    /** 设置下次 start 时线程的 daemon 属性。 */
    public void setDaemon(boolean daemon) {
        isDaemon = daemon;
    }
}
