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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.reactivestreams.Publisher;
import org.redisson.RedissonScoredSortedSet;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.api.RLexSortedSet;
import org.redisson.client.RedisClient;

/**
 * 字典序有序集合 {@link org.redisson.api.RLexSortedSetRx} 的 Rx 辅助实现。
 * <p>
 * {@link #addAll} 复用 {@link PublisherAdder}；迭代走 {@link SetRxIterator} 对 ZSET SCAN。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonLexSortedSetRx {

    /** 底层 RLexSortedSet（ScoredSortedSet 字典序视图）。 */
    private final RLexSortedSet instance;
    
    public RedissonLexSortedSetRx(RLexSortedSet instance) {
        this.instance = instance;
    }

    /** 将 Publisher 中字符串批量 addAsync 到集合。 */
    public Single<Boolean> addAll(Publisher<? extends String> c) {
        return new PublisherAdder<String>() {
            @Override
            public RFuture<Boolean> add(Object e) {
                return instance.addAsync((String) e);
            }
        }.addAll(c);
    }
    
    /** 内部：匿名 SetRxIterator 按 pattern/count SCAN 成员。 */
    private Flowable<String> scanIteratorReactive(String pattern, int count) {
        return new SetRxIterator<String>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((RedissonScoredSortedSet<String>) instance).scanIteratorAsync(client, nextIterPos, pattern, count);
            }
        }.create();
    }

    /** 默认每批 10 条的全量成员迭代 Flowable。 */
    public Flowable<String> iterator() {
        return scanIteratorReactive(null, 10);
    }

    public Flowable<String> iterator(String pattern) {
        return scanIteratorReactive(pattern, 10);
    }

    public Flowable<String> iterator(int count) {
        return scanIteratorReactive(null, count);
    }

    public Flowable<String> iterator(String pattern, int count) {
        return scanIteratorReactive(pattern, count);
    }

}
