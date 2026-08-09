/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.rx;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.redisson.api.RFuture;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.ReplayProcessor;

/**
 * 将阻塞式「逐条 take」异步 API 转为 RxJava3 {@link Flowable} 的工具类。
 * <p>
 * {@link #takeElements} 在每次 request 时递归调用 factory 得到 {@link RFuture}，
 * 每完成一条 onNext，直到 counter 归零后 onComplete。
 *
 * @author Nikita Koksharov
 *
 */
public class ElementsStream {

    /** 每次 downstream request n 时，连续 take 最多 n 个元素（阻塞队列/deque 等场景）。 */
    public static <V> Flowable<V> takeElements(Supplier<RFuture<V>> callable) {
        ReplayProcessor<V> p = ReplayProcessor.create();
        return p.doOnRequest(n -> {
            AtomicLong counter = new AtomicLong(n);
            AtomicReference<RFuture<V>> futureRef = new AtomicReference<RFuture<V>>();

            take(callable, p, counter, futureRef);

            p.doOnCancel(() -> futureRef.get().cancel(true));
        });
    }
    
    /** 递归 take：future 完成后 onNext 并递减 counter，counter>0 则继续取下一条。 */
    private static <V> void take(Supplier<RFuture<V>> factory, ReplayProcessor<V> p, AtomicLong counter, AtomicReference<RFuture<V>> futureRef) {
        RFuture<V> future = factory.get();
        futureRef.set(future);
        future.whenComplete((res, e) -> {
            if (e != null) {
                p.onError(e);
                return;
            }
            
            p.onNext(res);
            if (counter.decrementAndGet() == 0) {
                p.onComplete();
            }
            
            take(factory, p, counter, futureRef);
        });
    }
    
}
