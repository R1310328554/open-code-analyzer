/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.internal.util;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.exceptions.CompositeException;

/**
 * Throwable 容器的终止态原子操作工具。
 */
public final class ExceptionHelper {

    /** 工具类，禁止实例化。 */
    private ExceptionHelper() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * 若 error 为 Error 则直接抛出；否则对受检异常包装为 CompletionException 后返回。
     * @param error 要包装或抛出的错误
     * @return 包装后的错误
     */
    @NonNull
    public static RuntimeException wrapOrThrow(@NonNull Throwable error) {
        if (error instanceof Error err) {
            throw err;
        }
        if (error instanceof RuntimeException rte) {
            return rte;
        }
        return new CompletionException("You forgot to unwrap me!", error);
    }
    /**
     * 解包 {@link CompletionException} 并重新抛出其中的 {@link Error} 或 {@link RuntimeException}；
     * 若 cause 为受检异常则原样返回。
     * @param error 要解包的错误
     * @return cause 为受检异常时返回原 error
     * @since 4.0.0
     */
    @NonNull
    public static RuntimeException unwrapOrThrow(@NonNull CompletionException error) {
        var cause = error.getCause();
        if (cause instanceof Error err) {
            throw err;
        }
        if (cause instanceof RuntimeException rte) {
            return rte;
        }
        return error;
    }

    /**
     * 表示异常容器终止态的单例 Throwable，请勿泄漏。
     */
    public static final Throwable TERMINATED = new Termination();

    public static boolean addThrowable(AtomicReference<Throwable> field, Throwable exception) {
        for (;;) {
            Throwable current = field.get();

            if (current == TERMINATED) {
                return false;
            }

            Throwable update;
            if (current == null) {
                update = exception;
            } else {
                update = new CompositeException(current, exception);
            }

            if (field.compareAndSet(current, update)) {
                return true;
            }
        }
    }

    public static Throwable terminate(AtomicReference<Throwable> field) {
        Throwable current = field.get();
        if (current != TERMINATED) {
            current = field.getAndSet(TERMINATED);
        }
        return current;
    }

    /**
     * 将树状 CompositeException 链深度优先展平为 Throwable 列表。
     * @param t 起始异常
     * @return 展平后的异常列表
     */
    public static List<Throwable> flatten(Throwable t) {
        List<Throwable> list = new ArrayList<>();
        ArrayDeque<Throwable> deque = new ArrayDeque<>();
        deque.offer(t);

        while (!deque.isEmpty()) {
            Throwable e = deque.removeFirst();
            if (e instanceof CompositeException ce) {
                List<Throwable> exceptions = ce.getExceptions();
                for (int i = exceptions.size() - 1; i >= 0; i--) {
                    deque.offerFirst(exceptions.get(i));
                }
            } else {
                list.add(e);
            }
        }

        return list;
    }

    /**
     * Java 6 无法在 catch 中抛出 final Throwable 的变通方法。
     * @param <E> 泛型异常类型
     * @param e 要返回或抛出的 Throwable
     * @return 若 e 为 Exception 子类则返回 e
     * @throws E 否则抛出
     */
    @SuppressWarnings("unchecked")
    public static <E extends Throwable> Exception throwIfThrowable(Throwable e) throws E {
        if (e instanceof Exception) {
            return (Exception)e;
        }
        throw (E)e;
    }

    /** 构建超时终止消息。 */
    public static String timeoutMessage(long timeout, TimeUnit unit) {
        return "The source did not signal an event for "
                + timeout
                + " "
                + unit.toString().toLowerCase()
                + " and has been terminated.";
    }

    /** 终止态占位异常，不填充堆栈。 */
    static final class Termination extends Throwable {

        @Serial
        private static final long serialVersionUID = -4649703670690200604L;

        Termination() {
            super("No further exceptions");
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    /**
     * 拼接含 null 警告的消息字符串。
     * @param prefix 消息前缀
     * @return 拼接后的字符串
     * @since 3.0.0
     */
    public static String nullWarning(String prefix) {
        return prefix + " Null values are generally not allowed in 3.x operators and sources.";
    }

    /**
     * 通过 {@link #nullWarning(String)} 创建 NullPointerException。
     * @param prefix 消息前缀
     * @return NullPointerException
     * @since 3.0.0
     */
    public static NullPointerException createNullPointerException(String prefix) {
        return new NullPointerException(nullWarning(prefix));
    }

    /**
     * 类似 Objects.requireNonNull，但错误消息由 {@link #nullWarning(String)} 拼接。
     * @param <T> 值类型
     * @param value 待检查的值
     * @param prefix 错误消息前缀
     * @return value
     * @throws NullPointerException value 为 null 时
     * @since 3.0.0
     */
    public static <T> T nullCheck(T value, String prefix) {
        if (value == null) {
            throw createNullPointerException(prefix);
        }
        return value;
    }

    /**
     * 解包两个可能为 {@link CompletionException} 的异常；若均非 null 则将 b 压入 a 的 suppressed 并返回 a。
     * @param main 第一个异常
     * @param secondary 第二个异常
     * @return 解包合并后的异常，或 null
     */
    @Nullable
    public static Throwable unwrapAndCombine(@Nullable Throwable main, @Nullable Throwable secondary) {
        if (main instanceof CompletionException) {
            main = main.getCause();
        }
        if (secondary instanceof CompletionException) {
            secondary = secondary.getCause();
        }
        if (main != null && secondary != null && main != secondary) {
            main.addSuppressed(secondary);
        }
        if (main != null) {
            return main;
        }
        return secondary;
    }

    /**
     * 解包给定 {@link CompletionException}。
     * @param t 可能需解包的 Throwable
     * @return 解包后的 Throwable
     */
    public static Throwable unwrap(@Nullable Throwable t) {
        if (t instanceof CompletionException) {
            t = t.getCause();
        }
        return t;
    }
}
