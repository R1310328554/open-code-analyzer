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

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.core.Observer;

/**
 * 半序列化工具：保证 onNext 单线程调用，
 * 而 onError/onComplete 可能来自任意线程。
 */
public final class HalfSerializer {
    /** 工具类，禁止实例化。 */
    private HalfSerializer() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * 若可则发射给定值；发射期间若已 onComplete/onError 则终止，否则丢弃该值。
     * @param <T> 值类型
     * @param subscriber 目标 Subscriber
     * @param value 要发射的值
     * @param wip 序列化进行中计数/标志
     * @param errors Throwable 容器
     * @return 操作成功为 true；序列已完成为 false
     */
    public static <T> boolean onNext(Subscriber<? super T> subscriber, T value,
            AtomicInteger wip, AtomicThrowable errors) {
        if (wip.get() == 0 && wip.compareAndSet(0, 1)) {
            subscriber.onNext(value);
            if (wip.decrementAndGet() == 0) {
                return true;
            }
            errors.tryTerminateConsumer(subscriber);
        }
        return false;
    }

    /**
     * 若可则发射异常，否则加入错误容器由并发 onNext 稍后发射。
     * 无法投递的异常交给 RxJavaPlugins.onError。
     * @param subscriber 目标 Subscriber
     * @param ex 要发射的 Throwable
     * @param wip 序列化进行中计数/标志
     * @param errors Throwable 容器
     */
    public static void onError(Subscriber<?> subscriber, Throwable ex,
            AtomicInteger wip, AtomicThrowable errors) {
        if (errors.tryAddThrowableOrReport(ex)) {
            if (wip.getAndIncrement() == 0) {
                errors.tryTerminateConsumer(subscriber);
            }
        }
    }

    /**
     * 发射 onComplete 或 onError，或由并发 onNext 负责终止。
     * @param subscriber 目标 Subscriber
     * @param wip 序列化进行中计数/标志
     * @param errors Throwable 容器
     */
    public static void onComplete(Subscriber<?> subscriber, AtomicInteger wip, AtomicThrowable errors) {
        if (wip.getAndIncrement() == 0) {
            errors.tryTerminateConsumer(subscriber);
        }
    }

    /**
     * Observer 版 onNext：逻辑同 Subscriber 版本。
     * @param <T> 值类型
     * @param observer 目标 Observer
     * @param value 要发射的值
     * @param wip 序列化进行中计数/标志
     * @param errors Throwable 容器
     */
    public static <T> void onNext(Observer<? super T> observer, T value,
            AtomicInteger wip, AtomicThrowable errors) {
        if (wip.get() == 0 && wip.compareAndSet(0, 1)) {
            observer.onNext(value);
            if (wip.decrementAndGet() != 0) {
                errors.tryTerminateConsumer(observer);
            }
        }
    }

    /**
     * Observer 版 onError：逻辑同 Subscriber 版本。
     * @param observer 目标 Observer
     * @param ex 要发射的 Throwable
     * @param wip 序列化进行中计数/标志
     * @param errors Throwable 容器
     */
    public static void onError(Observer<?> observer, Throwable ex,
            AtomicInteger wip, AtomicThrowable errors) {
        if (errors.tryAddThrowableOrReport(ex)) {
            if (wip.getAndIncrement() == 0) {
                errors.tryTerminateConsumer(observer);
            }
        }
    }

    /**
     * Observer 版 onComplete：逻辑同 Subscriber 版本。
     * @param observer 目标 Observer
     * @param wip 序列化进行中计数/标志
     * @param errors Throwable 容器
     */
    public static void onComplete(Observer<?> observer, AtomicInteger wip, AtomicThrowable errors) {
        if (wip.getAndIncrement() == 0) {
            errors.tryTerminateConsumer(observer);
        }
    }

}
