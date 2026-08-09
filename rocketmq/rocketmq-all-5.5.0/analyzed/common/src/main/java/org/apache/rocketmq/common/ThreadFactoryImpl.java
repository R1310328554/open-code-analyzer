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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * RocketMQ 通用线程工厂：按前缀递增编号命名线程，可选 daemon 与 Broker 容器标识前缀。
 * 为未捕获异常注册统一日志处理器。
 */
public class ThreadFactoryImpl implements ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);

    /** 线程序号，用于生成唯一线程名后缀。 */
    private final AtomicLong threadIndex = new AtomicLong(0);
    /** 线程名前缀。 */
    private final String threadNamePrefix;
    /** 新建线程是否为守护线程。 */
    private final boolean daemon;

    /** 非 daemon 线程工厂。 */
    public ThreadFactoryImpl(final String threadNamePrefix) {
        this(threadNamePrefix, false);
    }

    /** 指定 daemon 属性的线程工厂。 */
    public ThreadFactoryImpl(final String threadNamePrefix, boolean daemon) {
        this.threadNamePrefix = threadNamePrefix;
        this.daemon = daemon;
    }

    /** Broker 容器模式下自动附加 Broker 标识前缀。 */
    public ThreadFactoryImpl(final String threadNamePrefix, BrokerIdentity brokerIdentity) {
        this(threadNamePrefix, false, brokerIdentity);
    }

    /** 完整构造：前缀、daemon 与 Broker 标识。 */
    public ThreadFactoryImpl(final String threadNamePrefix, boolean daemon, BrokerIdentity brokerIdentity) {
        this.daemon = daemon;
        if (brokerIdentity != null && brokerIdentity.isInBrokerContainer()) {
            this.threadNamePrefix = brokerIdentity.getIdentifier() + threadNamePrefix;
        } else {
            this.threadNamePrefix = threadNamePrefix;
        }
    }

    @Override
    /** 创建命名线程并设置 uncaughtExceptionHandler。 */
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, threadNamePrefix + this.threadIndex.incrementAndGet());
        thread.setDaemon(daemon);

        // 记录所有未捕获异常，便于排查后台线程 BUG
        thread.setUncaughtExceptionHandler((t, e) ->
            LOGGER.error("[BUG] Thread has an uncaught exception, threadId={}, threadName={}",
                t.getId(), t.getName(), e));

        return thread;
    }
}
