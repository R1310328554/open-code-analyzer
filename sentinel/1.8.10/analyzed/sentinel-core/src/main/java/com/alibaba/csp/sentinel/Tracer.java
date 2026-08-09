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
package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.context.NullContext;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

/**
 * 用于记录除 BlockException 以外的其他异常。
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class Tracer {

    protected static Class<? extends Throwable>[] traceClasses;
    protected static Class<? extends Throwable>[] ignoreClasses;

    protected static Predicate<Throwable> exceptionPredicate;

    protected Tracer() {}

    /**
     * 将给定 {@link Throwable} 追踪到当前上下文中的资源 Entry。
     *
     * @param e 要记录的异常
     */
    public static void trace(Throwable e) {
        traceContext(e, ContextUtil.getContext());
    }

    /**
     * 将给定 {@link Throwable} 追踪到当前上下文中的当前 Entry。
     *
     * @param e     要记录的异常
     * @param count 要增加的异常计数
     */
    @Deprecated
    public static void trace(Throwable e, int count) {
        traceContext(e, count, ContextUtil.getContext());
    }

    /**
     * 将给定 {@link Throwable} 追踪到指定入口上下文的当前 Entry。
     *
     * @param e       要记录的异常
     * @param context 目标入口上下文
     * @since 1.8.0
     */
    public static void traceContext(Throwable e, Context context) {
        if (!shouldTrace(e)) {
            return;
        }

        if (context == null || context instanceof NullContext) {
            return;
        }
        traceEntryInternal(e, context.getCurEntry());
    }

    /**
     * 将给定 {@link Throwable} 追踪到指定上下文中当前 Entry 并增加异常计数。
     *
     * @param e     要记录的异常
     * @param count 要增加的异常计数
     * @since 1.4.2
     */
    @Deprecated
    public static void traceContext(Throwable e, int count, Context context) {
        if (!shouldTrace(e)) {
            return;
        }

        if (context == null || context instanceof NullContext) {
            return;
        }
        traceEntryInternal(e, context.getCurEntry());
    }

    /**
     * 将给定 {@link Throwable} 追踪到指定资源 Entry。
     *
     * @param e 要记录的异常
     * @since 1.4.2
     */
    public static void traceEntry(Throwable e, Entry entry) {
        if (!shouldTrace(e)) {
            return;
        }
        traceEntryInternal(e, entry);
    }

    private static void traceEntryInternal(/*@NeedToTrace*/ Throwable e, Entry entry) {
        if (entry == null) {
            return;
        }

        entry.setError(e);
    }

    /**
     * 设置要追踪的异常。若未设置，除 {@link BlockException} 外的所有 Exception 都会被追踪。
     * <p>
     * 注意若同时设置 {@link #setExceptionsToIgnore(Class[])} 与本方法，
     * ExceptionsToIgnore 优先级更高。
     * </p>
     *
     * @param traceClasses 要追踪的异常类列表
     * @since 1.6.1
     */
    @SafeVarargs
    public static void setExceptionsToTrace(Class<? extends Throwable>... traceClasses) {
        checkNotNull(traceClasses);
        Tracer.traceClasses = traceClasses;
    }

    /**
     * 获取要追踪的异常类。
     *
     * @return 要追踪的异常类数组
     * @since 1.6.1
     */
    public static Class<? extends Throwable>[] getExceptionsToTrace() {
        return traceClasses;
    }

    /**
     * 设置要忽略的异常。若未设置，除 {@link BlockException} 外的所有 Exception 都会被追踪。
     * <p>
     * 注意若同时设置 {@link #setExceptionsToTrace(Class[])} 与本方法，
     * ExceptionsToIgnore 优先级更高。
     * </p>
     *
     * @param ignoreClasses 要忽略的异常类列表
     * @since 1.6.1
     */
    @SafeVarargs
    public static void setExceptionsToIgnore(Class<? extends Throwable>... ignoreClasses) {
        checkNotNull(ignoreClasses);
        Tracer.ignoreClasses = ignoreClasses;
    }

    /**
     * 获取要忽略的异常类。
     *
     * @return 要忽略的异常类数组
     * @since 1.6.1
     */
    public static Class<? extends Throwable>[] getExceptionsToIgnore() {
        return ignoreClasses;
    }

    /**
     * 获取异常谓词。
     * @return 异常谓词
     */
    public static Predicate<? extends Throwable> getExceptionPredicate() {
        return exceptionPredicate;
    }

    /**
     * 设置异常谓词，指示异常应被追踪（返回 true）还是忽略（返回 false），
     * {@link BlockException} 除外。
     * @param exceptionPredicate 异常谓词
     */
    public static void setExceptionPredicate(Predicate<Throwable> exceptionPredicate) {
        AssertUtil.notNull(exceptionPredicate, "exception predicate must not be null");
        Tracer.exceptionPredicate = exceptionPredicate;
    }

    private static void checkNotNull(Class<? extends Throwable>[] classes) {
        AssertUtil.notNull(classes, "trace or ignore classes must not be null");
        for (Class<? extends Throwable> clazz : classes) {
            AssertUtil.notNull(clazz, "trace or ignore classes must not be null");
        }
    }

    /**
     * 检查 Throwable 是否应被追踪。
     *
     * @param t 要检查的 Throwable
     * @return 若应追踪则返回 true，否则返回 false
     */
    protected static boolean shouldTrace(Throwable t) {
        if (t == null || t instanceof BlockException) {
            return false;
        }
        if (exceptionPredicate != null) {
            return exceptionPredicate.test(t);
        }

        if (ignoreClasses != null) {
            for (Class<? extends Throwable> clazz : ignoreClasses) {
                if (clazz != null && clazz.isAssignableFrom(t.getClass())) {
                    return false;
                }
            }
        }
        if (traceClasses != null) {
            for (Class<? extends Throwable> clazz : traceClasses) {
                if (clazz != null && clazz.isAssignableFrom(t.getClass())) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}