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

package org.apache.rocketmq.srvutil;

import org.apache.rocketmq.logging.org.slf4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * filtersrv 与 namesrv 模块的标准 JVM 关闭钩子线程。
 * 通过 {@link Callable} 回调可在任意位置定制关闭逻辑。
 */
public class ShutdownHookThread extends Thread {
    /** 是否已执行过关闭逻辑（防重复）。 */
    private volatile boolean hasShutdown = false;
    /** 钩子被调用次数计数。 */
    private AtomicInteger shutdownTimes = new AtomicInteger(0);
    /** 日志实例。 */
    private final Logger log;
    /** 关闭时执行的回调。 */
    private final Callable callback;

    /**
     * 构造标准关闭钩子线程。
     *
     * @param log 钩子线程使用的日志
     * @param callback 关闭回调函数
     */
    public ShutdownHookThread(Logger log, Callable callback) {
        super("ShutdownHook");
        this.log = log;
        this.callback = callback;
    }

    /**
     * JVM 关闭时执行：
     * 1. 累计调用次数；
     * 2. 执行 {@link ShutdownHookThread#callback} 并记录耗时。
     */
    @Override
    public void run() {
        synchronized (this) {
            log.info("shutdown hook was invoked, " + this.shutdownTimes.incrementAndGet() + " times.");
            if (!this.hasShutdown) {
                this.hasShutdown = true;
                long beginTime = System.currentTimeMillis();
                try {
                    this.callback.call();
                } catch (Exception e) {
                    log.error("shutdown hook callback invoked failure.", e);
                }
                long consumingTimeTotal = System.currentTimeMillis() - beginTime;
                log.info("shutdown hook done, consuming time total(ms): " + consumingTimeTotal);
            }
        }
    }
}
