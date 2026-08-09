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
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.plugins.RxJavaPlugins;

/**
 * {@link Throwable} 的原子容器，支持合并异常并通过 ExceptionHelper 进入终止态。
 * <p>
 * 注意：AtomicReference 的公开方法可能泄漏内部状态！
 */
public final class AtomicThrowable extends AtomicReference<Throwable> {

    @Serial
    private static final long serialVersionUID = 3949248817947090603L;

    /**
     * 原子地向容器添加 Throwable（若已有异常则合并）。
     * @param t 要添加的异常
     * @return 成功为 true；容器已终止为 false
     */
    public boolean tryAddThrowable(Throwable t) {
        return ExceptionHelper.addThrowable(this, t);
    }

    /**
     * 原子添加 Throwable，或在容器已终止时将错误上报全局处理器且不修改容器。
     * @param t 要添加的异常
     * @return 成功为 true；容器已终止为 false
     */
    public boolean tryAddThrowableOrReport(Throwable t) {
        if (tryAddThrowable(t)) {
            return true;
        }
        RxJavaPlugins.onError(t);
        return false;
    }

    /**
     * 原子终止容器并返回其中最后一个非终止 Throwable。
     * @return 最后的 Throwable
     */
    public Throwable terminate() {
        return ExceptionHelper.terminate(this);
    }

    public boolean isTerminated() {
        return get() == ExceptionHelper.TERMINATED;
    }

    /**
     * 尝试终止本原子 Throwable（写入 TERMINATED 标记），
     * 若此前存有非 null 且非标记异常则调用 {@link RxJavaPlugins#onError(Throwable)}。
     * @since 3.0.0
     */
    public void tryTerminateAndReport() {
        Throwable ex = terminate();
        if (ex != null && ex != ExceptionHelper.TERMINATED) {
            RxJavaPlugins.onError(ex);
        }
    }

    /**
     * 尝试终止本原子 Throwable 并通知 consumer：
     * 无错误时 onComplete，有非标记异常时 onError；已终止则不通知。
     * @param consumer 要通知的 consumer
     */
    public void tryTerminateConsumer(Subscriber<?> consumer) {
        Throwable ex = terminate();
        if (ex == null) {
            consumer.onComplete();
        } else if (ex != ExceptionHelper.TERMINATED) {
            consumer.onError(ex);
        }
    }

    /**
     * Tries to terminate this atomic throwable (by swapping in the TERMINATED indicator)
     * and notifies the consumer if there was no error (onComplete) or there was a
     * non-null, non-indicator exception contained before (onError).
     * If there was a terminated indicator, the consumer is not signaled.
     * @param consumer the consumer to notify
     */
    /** 终止并通知 {@link Observer}（逻辑同 Subscriber 版本）。 */
    public void tryTerminateConsumer(Observer<?> consumer) {
        Throwable ex = terminate();
        if (ex == null) {
            consumer.onComplete();
        } else if (ex != ExceptionHelper.TERMINATED) {
            consumer.onError(ex);
        }
    }

    /**
     * Tries to terminate this atomic throwable (by swapping in the TERMINATED indicator)
     * and notifies the consumer if there was no error (onComplete) or there was a
     * non-null, non-indicator exception contained before (onError).
     * If there was a terminated indicator, the consumer is not signaled.
     * @param consumer the consumer to notify
     */
    /** 终止并通知 {@link MaybeObserver}。 */
    public void tryTerminateConsumer(MaybeObserver<?> consumer) {
        Throwable ex = terminate();
        if (ex == null) {
            consumer.onComplete();
        } else if (ex != ExceptionHelper.TERMINATED) {
            consumer.onError(ex);
        }
    }

    /**
     * 尝试终止并通知 consumer：仅在有非标记异常时 onError；已终止则不通知。
     * @param consumer 要通知的 consumer
     */
    public void tryTerminateConsumer(SingleObserver<?> consumer) {
        Throwable ex = terminate();
        if (ex != null && ex != ExceptionHelper.TERMINATED) {
            consumer.onError(ex);
        }
    }

    /**
     * Tries to terminate this atomic throwable (by swapping in the TERMINATED indicator)
     * and notifies the consumer if there was no error (onComplete) or there was a
     * non-null, non-indicator exception contained before (onError).
     * If there was a terminated indicator, the consumer is not signaled.
     * @param consumer the consumer to notify
     */
    /** 终止并通知 {@link CompletableObserver}。 */
    public void tryTerminateConsumer(CompletableObserver consumer) {
        Throwable ex = terminate();
        if (ex == null) {
            consumer.onComplete();
        } else if (ex != ExceptionHelper.TERMINATED) {
            consumer.onError(ex);
        }
    }

    /**
     * Tries to terminate this atomic throwable (by swapping in the TERMINATED indicator)
     * and notifies the consumer if there was no error (onComplete) or there was a
     * non-null, non-indicator exception contained before (onError).
     * If there was a terminated indicator, the consumer is not signaled.
     * @param consumer the consumer to notify
     */
    /** 终止并通知 {@link Emitter}。 */
    public void tryTerminateConsumer(Emitter<?> consumer) {
        Throwable ex = terminate();
        if (ex == null) {
            consumer.onComplete();
        } else if (ex != ExceptionHelper.TERMINATED) {
            consumer.onError(ex);
        }
    }
}
