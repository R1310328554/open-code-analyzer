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
package org.redisson.reactive;

import org.redisson.api.RFuture;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 将「每次 call 返回 {@link RFuture}」的工厂转为 Reactor {@link Flux}：
 * 按下游 request(n) 串行拉取 n 个元素，前一个完成后再发起下一个。
 *
 * @author Nikita Koksharov
 *
 */
public class ElementsStream {

    /** 递归/链式 take：完成一个 Future 后若 counter>0 继续下一个。 */
    private static <V> void take(Callable<RFuture<V>> factory, FluxSink<V> emitter, AtomicLong counter, AtomicReference<RFuture<V>> futureRef) {
        RFuture<V> future;
        try {
            future = factory.call();
        } catch (Exception e) {
            emitter.error(e);
            return;
        }
        futureRef.set(future);
        future.whenComplete((res, e) -> {
            if (e != null) {
                emitter.error(e);
                return;
            }
            
            emitter.next(res);
            // 已满足本次 request 数量则停止
            if (counter.decrementAndGet() == 0) {
                return;
            }
            
            take(factory, emitter, counter, futureRef);
        });
    }
    
    /** 创建背压 Flux：onRequest 驱动串行 RFuture 链。 */
    public static <V> Flux<V> takeElements(Callable<RFuture<V>> callable) {
        return Flux.create(emitter -> {
            AtomicReference<RFuture<V>> futureRef = new AtomicReference<RFuture<V>>();
            emitter.onRequest(n -> {
                AtomicLong counter = new AtomicLong(n);
                take(callable, emitter, counter, futureRef);
            });
            // 取消订阅时中断当前在途 Future
            emitter.onDispose(() -> {
                futureRef.get().cancel(true);
            });
        });
    }

    
}
