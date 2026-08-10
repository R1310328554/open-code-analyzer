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

package com.alibaba.nacos.common.utils;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程工厂构建器（Builder 模式）：可配置线程名模板、优先级、
 * 守护线程、未捕获异常处理器及自定义底层工厂。
 * build thread factory.
 * @author zzq
 * @date 2021/8/3
 */
public class ThreadFactoryBuilder {
    
    /** 是否为守护线程，默认 false */

    private Boolean daemon = false;
    
    /** 线程优先级，null 表示不修改 */

    private Integer priority = null;
    
    /** 线程名格式串（支持 {@code String.format} 占位） */

    private String nameFormat = null;
    
    /** 未捕获异常处理器 */

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;
    
    /** 自定义底层 ThreadFactory，null 时使用 Executors 默认工厂 */

    private ThreadFactory customizeFactory = null;
    
    /** 设置线程名模板并返回 this */

    public ThreadFactoryBuilder nameFormat(String nameFormat) {
        checkNullParameter(nameFormat, "nameFormat cannot be null.");
        this.nameFormat = nameFormat;
        return this;
    }
    
    /** 设置线程优先级（须在 MIN~MAX 范围内）并返回 this */

    public ThreadFactoryBuilder priority(int priority) {
        if (priority > Thread.MAX_PRIORITY || priority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException(
                String.format("The value of priority should be between %s and %s",
                    Thread.MIN_PRIORITY + 1, Thread.MAX_PRIORITY + 1));
        }
        this.priority = priority;
        return this;
    }
    
    /** 设置未捕获异常处理器并返回 this */

    public ThreadFactoryBuilder uncaughtExceptionHandler(
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        checkNullParameter(uncaughtExceptionHandler, "uncaughtExceptionHandler cannot be null.");
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }
    
    /** 设置是否为守护线程并返回 this */

    public ThreadFactoryBuilder daemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }
    
    /** 设置自定义 ThreadFactory 并返回 this */

    public ThreadFactoryBuilder customizeFactory(ThreadFactory factory) {
        checkNullParameter(factory, "factory cannot be null.");
        this.customizeFactory = factory;
        return this;
    }
    
    /** 构建最终 {@link ThreadFactory}，按配置包装新线程 */

    public ThreadFactory build() {
        ThreadFactory factory =
            customizeFactory == null ? Executors.defaultThreadFactory() : customizeFactory;
        final AtomicLong count = (nameFormat != null) ? new AtomicLong(0) : null;
        return r -> {
            Thread thread = factory.newThread(r);
            if (nameFormat != null) {
                thread.setName(format(nameFormat, count.getAndIncrement()));
            }
            if (priority != null) {
                thread.setPriority(priority);
            }
            if (uncaughtExceptionHandler != null) {
                thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }
            thread.setDaemon(daemon);
            return thread;
        };
    }
    
    private String format(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }
    
    private void checkNullParameter(Object obj, String msg) {
        if (obj == null) {
            throw new IllegalArgumentException(msg);
        }
    }
}
