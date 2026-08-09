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

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.LongConsumer;
import io.reactivex.rxjava3.processors.ReplayProcessor;
import org.reactivestreams.Publisher;
import org.redisson.BaseRedissonList;
import org.redisson.api.RFuture;

/**
 * 分布式 {@link java.util.List} 的 RxJava 3 适配层。
 * <p>
 * 通过 {@link ReplayProcessor} 与 Reactive Streams 背压协议，将按索引顺序读取
 * Redis 列表元素的异步操作暴露为 {@link Publisher}；批量追加则返回 {@link Single}。
 *
 * @author Nikita Koksharov
 *
 * @param <V> the type of elements held in this collection
 */
public class RedissonListRx<V> {

    /** 底层同步/异步列表实现，所有 Redis 命令经此委托。 */
    private final BaseRedissonList<V> instance;

    public RedissonListRx(BaseRedissonList<V> instance) {
        this.instance = instance;
    }

    /** 从列表末尾向前遍历，等价于 {@code iterator(-1, false)}。 */
    public Publisher<V> descendingIterator() {
        return iterator(-1, false);
    }

    /** 从索引 0 起正向遍历整个列表。 */
    public Publisher<V> iterator() {
        return iterator(0, true);
    }

    /** 从指定索引起向列表头部（索引递减）遍历。 */
    public Publisher<V> descendingIterator(int startIndex) {
        return iterator(startIndex, false);
    }

    /** 从指定索引起向列表尾部（索引递增）遍历。 */
    public Publisher<V> iterator(int startIndex) {
        return iterator(startIndex, true);
    }

    /**
     * 构建按索引拉取的响应式迭代器。
     * <p>
     * 订阅者通过 {@code request(n)} 声明需求；{@link LongConsumer} 在每次请求时
     * 递归调用 {@code getAsync}，遇 null 表示越界并 {@code onComplete}。
     * 注意：索引游标在回调线程中更新，并发多次 request 可能导致乱序。
     */
    private Publisher<V> iterator(int startIndex, boolean forward) {
        ReplayProcessor<V> p = ReplayProcessor.create();
        return p.doOnRequest(new LongConsumer() {

            private int currentIndex = startIndex;
            
            @Override
            public void accept(long n) throws Exception {
                instance.getAsync(currentIndex).whenComplete((value, e) -> {
                    if (e != null) {
                        p.onError(e);
                        return;
                    }
                    
                    if (value != null) {
                        p.onNext(value);
                        if (forward) {
                            currentIndex++;
                        } else {
                            currentIndex--;
                        }
                    }
                    
                    if (value == null) {
                        p.onComplete();
                        return;
                    }
                    if (n-1 == 0) {
                        return;
                    }
                    try {
                        accept(n-1);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
            }
        });
    }
    
    /** 消费 {@link Publisher} 中全部元素并依次 {@code addAsync}，返回是否整体成功。 */
    public Single<Boolean> addAll(Publisher<? extends V> c) {
        return new PublisherAdder<V>() {

            @Override
            public RFuture<Boolean> add(Object o) {
                return instance.addAsync((V) o);
            }

        }.addAll(c);
    }

}
