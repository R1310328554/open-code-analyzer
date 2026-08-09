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
package org.apache.rocketmq.remoting.common;


import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 后台服务线程基类：封装启动、优雅关闭与 join 等待逻辑。
 */
public abstract class ServiceThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_REMOTING_NAME);

    /** shutdown 时等待线程结束的最大毫秒数。 */
    private static final long JOIN_TIME = 90 * 1000;
    protected final Thread thread;
    protected volatile boolean hasNotified = false;
    protected volatile boolean stopped = false;

    /** 以 {@link #getServiceName()} 命名并创建内部线程。 */
    public ServiceThread() {
        this.thread = new Thread(this, this.getServiceName());
    }

    /** 返回线程名称，供日志与监控标识。 */
    public abstract String getServiceName();

    /** 启动后台线程。 */
    public void start() {
        this.thread.start();
    }

    /** 非中断方式请求停止并 join 线程。 */
    public void shutdown() {
        this.shutdown(false);
    }

    /**
     * 请求停止服务线程。
     *
     * @param interrupt 为 true 时对线程调用 {@link Thread#interrupt()}
     */
    public void shutdown(final boolean interrupt) {
        this.stopped = true;
        log.info("shutdown thread " + this.getServiceName() + " interrupt " + interrupt);
        synchronized (this) {
            if (!this.hasNotified) {
                this.hasNotified = true;
                this.notify();
            }
        }

        try {
            if (interrupt) {
                this.thread.interrupt();
            }

            long beginTime = System.currentTimeMillis();
            this.thread.join(this.getJointime());
            long elapsedTime = System.currentTimeMillis() - beginTime;
            log.info("join thread " + this.getServiceName() + " elapsed time(ms) " + elapsedTime + " "
                + this.getJointime());
        } catch (InterruptedException e) {
            log.error("Interrupted", e);
        }
    }

    /** 返回 shutdown 时 join 的超时毫秒数。 */
    public long getJointime() {
        return JOIN_TIME;
    }

    /** 是否已收到停止信号。 */
    public boolean isStopped() {
        return stopped;
    }
}
