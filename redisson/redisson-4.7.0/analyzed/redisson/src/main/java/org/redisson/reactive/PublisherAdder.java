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

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.redisson.api.RFuture;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 将上游 {@link Publisher} 元素逐个写入 Redis 集合/列表的抽象基类。
 * <p>
 * {@link #addAll} 以 request(1) 串行消费，每元素调用 {@link #add}；
 * 全部完成后 emit 是否至少新增过一个元素（{@code lastSize}）。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 上游元素类型
 */
public abstract class PublisherAdder<V> {

    /** 子类实现：异步添加单个元素，返回是否改变了集合。 */
    public abstract RFuture<Boolean> add(Object o);

    /** 串行订阅 {@code c}，逐条 add 并在全部完成后返回聚合布尔结果。 */
    public Publisher<Boolean> addAll(Publisher<? extends V> c) {
        return Mono.create(emitter -> emitter.onRequest(n -> {
            c.subscribe(new BaseSubscriber<V>() {

                /** 上游是否已 complete。 */
                volatile boolean completed;
                /** 尚未完成 add 回调的在途元素数。 */
                final AtomicLong values = new AtomicLong();
                Subscription s;
                /** 是否至少有一次 add 返回 true。 */
                volatile Boolean lastSize = false;

                /** 保存订阅并请求首个元素。 */
                @Override
                protected void hookOnSubscribe(Subscription s) {
                    this.s = s;
                    s.request(1);
                }

                /** 对每个元素发起 add，完成后继续 request(1)。 */
                @Override
                protected void hookOnNext(V o) {
                    values.getAndIncrement();
                    add(o).whenComplete((res, e) -> {
                        if (e != null) {
                            emitter.error(e);
                            return;
                        }

                        if (res) {
                            lastSize = true;
                        }
                        s.request(1);
                        if (values.decrementAndGet() == 0 && completed) {
                            emitter.success(lastSize);
                        }
                    });
                }

                /** 上游结束：若无在途 add 则立即 success。 */
                @Override
                protected void hookOnComplete() {
                    completed = true;
                    if (values.get() == 0) {
                        emitter.success(lastSize);
                    }
                }
            });
        }));
    }

}
