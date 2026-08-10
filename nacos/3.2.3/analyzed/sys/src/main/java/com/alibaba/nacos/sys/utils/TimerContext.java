/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.sys.utils;

import com.alibaba.nacos.common.utils.LoggerUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 基于 ThreadLocal 的同步代码块耗时统计工具。
 *
 * <p>支持手动 start/end 配对，或对 {@link Runnable}、{@link Supplier}、 {@link Function}、{@link Consumer} 包装执行并输出分级日志。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class TimerContext {
    
    /** 线程本地计时起点表（任务名 → 开始毫秒时间戳）。 */
    private static final ThreadLocal<Map<String, Long>> TIME_RECORD =
        ThreadLocal.withInitial(() -> new HashMap<>(2));
    
    /**
     * 记录指定任务的计时起点。
     *
     * @param name 任务上下文名称
     */
    public static void start(final String name) {
        TIME_RECORD.get().put(name, System.currentTimeMillis());
    }
    
    /** 以 DEBUG 级别结束计时并输出耗时。 */
    public static void end(final String name, final Logger logger) {
        end(name, logger, LoggerUtils.DEBUG);
    }
    
    /**
     * 结束计时并按指定日志级别打印耗时。
     *
     * @param name 任务上下文名称
     * @param logger 日志记录器
     * @param level 日志级别常量（见 {@link com.alibaba.nacos.common.utils.LoggerUtils}）
     */
    public static void end(final String name, final Logger logger, final String level) {
        Map<String, Long> record = TIME_RECORD.get();
        long contextTime = System.currentTimeMillis() - record.remove(name);
        if (record.isEmpty()) {
            TIME_RECORD.remove();
        }
        switch (level) {
            case LoggerUtils.DEBUG:
                LoggerUtils.printIfDebugEnabled(logger, "{} cost time : {} ms", name, contextTime);
                break;
            case LoggerUtils.INFO:
                LoggerUtils.printIfInfoEnabled(logger, "{} cost time : {} ms", name, contextTime);
                break;
            case LoggerUtils.TRACE:
                LoggerUtils.printIfTraceEnabled(logger, "{} cost time : {} ms", name, contextTime);
                break;
            case LoggerUtils.ERROR:
                LoggerUtils.printIfErrorEnabled(logger, "{} cost time : {} ms", name, contextTime);
                break;
            case LoggerUtils.WARN:
                LoggerUtils.printIfWarnEnabled(logger, "{} cost time : {} ms", name, contextTime);
                break;
            default:
                LoggerUtils.printIfErrorEnabled(logger, "level not found , {} cost time : {} ms",
                    name, contextTime);
                break;
        }
    }
    
    /**
     * 包装 {@link Runnable} 执行并自动统计耗时。
     *
     * @param job 待执行任务
     * @param name 任务名称
     * @param logger 日志记录器
     */
    public static void run(final Runnable job, final String name, final Logger logger) {
        start(name);
        try {
            job.run();
        } finally {
            end(name, logger);
        }
    }
    
    /**
     * 包装 {@link Supplier} 执行并返回结果，同时统计耗时。
     *
     * @param job 待执行任务
     * @param name 任务名称
     * @param logger 日志记录器
     */
    public static <V> V run(final Supplier<V> job, final String name, final Logger logger) {
        start(name);
        try {
            return job.get();
        } finally {
            end(name, logger);
        }
    }
    
    /**
     * 包装 {@link Function} 执行并统计耗时。
     *
     * @param job 待执行函数
     * @param args 函数入参
     * @param name 任务名称
     * @param logger 日志记录器
     */
    public static <T, R> R run(final Function<T, R> job, T args, final String name,
        final Logger logger) {
        start(name);
        try {
            return job.apply(args);
        } finally {
            end(name, logger);
        }
    }
    
    /**
     * 包装 {@link Consumer} 执行并统计耗时。
     *
     * @param job 待执行消费者
     * @param args 消费入参
     * @param name 任务名称
     * @param logger 日志记录器
     */
    public static <T> void run(final Consumer<T> job, T args, final String name,
        final Logger logger) {
        start(name);
        try {
            job.accept(args);
        } finally {
            end(name, logger);
        }
    }
    
}
