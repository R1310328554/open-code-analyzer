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
import org.redisson.RedissonScoredSortedSet;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.api.RLexSortedSet;
import org.redisson.client.RedisClient;
import reactor.core.publisher.Flux;


/**
 * 字典序有序集合的 Reactor 门面：{@link PublisherAdder} 批量 add，
 * {@link SetReactiveIterator} 驱动 ZSCAN 迭代成员。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonLexSortedSetReactive {

    /** 底层字典序 ZSet。 */
    private final RLexSortedSet instance;
    
    /** 包装已有 {@link RLexSortedSet}。 */
    public RedissonLexSortedSetReactive(RLexSortedSet instance) {
        this.instance = instance;
    }

    /** 串行消费上游字符串流并逐条 {@code addAsync}。 */
    public Publisher<Boolean> addAll(Publisher<? extends String> c) {
        return new PublisherAdder<String>() {
            @Override
            public RFuture<Boolean> add(Object e) {
                return instance.addAsync((String) e);
            }
        }.addAll(c);
    }

    /** 创建 ZSCAN 背压迭代 Flux。 */
    private Publisher<String> scanIteratorReactive(final String pattern, final int count) {
        return Flux.create(new SetReactiveIterator<String>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((RedissonScoredSortedSet<String>) instance).scanIteratorAsync(client, nextIterPos, pattern, count);
            }
        });
    }

    /** 全量成员迭代，默认 chunk=10。 */
    public Publisher<String> iterator() {
        return scanIteratorReactive(null, 10);
    }

    /** 按模式迭代成员。 */
    public Publisher<String> iterator(String pattern) {
        return scanIteratorReactive(pattern, 10);
    }

    /** 指定 chunk 大小的全量迭代。 */
    public Publisher<String> iterator(int count) {
        return scanIteratorReactive(null, count);
    }

    /** 同时指定匹配模式与 chunk 大小。 */
    public Publisher<String> iterator(String pattern, int count) {
        return scanIteratorReactive(pattern, count);
    }

}
