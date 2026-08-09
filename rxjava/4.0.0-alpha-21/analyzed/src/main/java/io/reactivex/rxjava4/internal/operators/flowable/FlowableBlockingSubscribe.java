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

package io.reactivex.rxjava4.internal.operators.flowable;

import java.util.Objects;
import java.util.concurrent.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.functions.*;
import io.reactivex.rxjava4.internal.subscribers.*;
import io.reactivex.rxjava4.internal.util.*;

/**
 * 以阻塞方式消费 {@link Publisher} 的工具方法，支持回调或 {@link Subscriber}。
 */
public final class FlowableBlockingSubscribe {

    /** 工具类，禁止实例化。 */
    private FlowableBlockingSubscribe() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * 订阅 source 并在当前线程上调用 Subscriber 方法。
     * <p>取消与背压通过 BlockingSubscriber 组合传递。
     * @param source 源 Publisher
     * @param subscriber 在当前线程接收事件的 Subscriber
     * @param <T> 元素类型
     */
    public static <T> void subscribe(Publisher<? extends T> source, Subscriber<? super T> subscriber) {
        final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();

        BlockingSubscriber<T> bs = new BlockingSubscriber<>(queue);

        source.subscribe(bs);

        try {
            for (;;) {
                if (bs.isCancelled()) {
                    break;
                }
                Object v = queue.poll();
                if (v == null) {
                    if (bs.isCancelled()) {
                        break;
                    }
                    BlockingHelper.verifyNonBlocking();
                    v = queue.take();
                }
                if (bs.isCancelled()) {
                    break;
                }
                if (v == BlockingSubscriber.TERMINATED
                        || NotificationLite.acceptFull(v, subscriber)) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            bs.cancel();
            subscriber.onError(e);
        }
    }

    /**
     * 阻塞等待 source 到达终止事件，忽略所有元素并在有异常时重新抛出。
     * @param source 要等待的源 Publisher
     * @param <T> 元素类型
     */
    public static <T> void subscribe(Publisher<? extends T> source) {
        BlockingIgnoringReceiver callback = new BlockingIgnoringReceiver();
        LambdaSubscriber<T> ls = new LambdaSubscriber<>(Functions.emptyConsumer(),
        callback, callback, Functions.REQUEST_MAX);

        source.subscribe(ls);

        BlockingHelper.awaitForComplete(callback, ls);
        Throwable e = callback.error;
        if (e != null) {
            throw ExceptionHelper.wrapOrThrow(e);
        }
    }

    /**
     * 订阅 source 并在当前线程上调用给定回调。
     * @param o 源 Publisher
     * @param onNext 每个元素的回调
     * @param onError 错误事件回调
     * @param onComplete 完成事件回调
     * @param <T> 元素类型
     */
    public static <T> void subscribe(Publisher<? extends T> o, final Consumer<? super T> onNext,
            final Consumer<? super Throwable> onError, final Action onComplete) {
        Objects.requireNonNull(onNext, "onNext is null");
        Objects.requireNonNull(onError, "onError is null");
        Objects.requireNonNull(onComplete, "onComplete is null");
        subscribe(o, new LambdaSubscriber<T>(onNext, onError, onComplete, Functions.REQUEST_MAX));
    }

    /**
     * 订阅 source 并在当前线程上调用给定回调，使用有界预取缓冲。
     * @param o 源 Publisher
     * @param onNext 每个元素的回调
     * @param onError 错误事件回调
     * @param onComplete 完成事件回调
     * @param bufferSize 从源 Publisher 预取的元素数量
     * @param <T> 元素类型
     */
    public static <T> void subscribe(Publisher<? extends T> o, final Consumer<? super T> onNext,
        final Consumer<? super Throwable> onError, final Action onComplete, int bufferSize) {
        Objects.requireNonNull(onNext, "onNext is null");
        Objects.requireNonNull(onError, "onError is null");
        Objects.requireNonNull(onComplete, "onComplete is null");
        ObjectHelper.verifyPositive(bufferSize, "number > 0 required");
        subscribe(o, new BoundedSubscriber<T>(onNext, onError, onComplete, Functions.boundedConsumer(bufferSize),
                bufferSize));
    }
}
