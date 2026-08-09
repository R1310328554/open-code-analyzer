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

import java.util.AbstractMap;
import java.util.Map.Entry;

import org.redisson.RedissonMap;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.client.RedisClient;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.ReplayProcessor;

/**
 * 
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 * @param <M> entry type
 */
public class RedissonMapRxIterator<K, V, M> {

    private final RedissonMap<K, V> map;
    private final String pattern;
    private final int count;

    public RedissonMapRxIterator(RedissonMap<K, V> map, String pattern, int count) {
        this.map = map;
        this.pattern = pattern;
        this.count = count;
    }
    
    public Flowable<M> create() {
        ReplayProcessor<M> p = ReplayProcessor.create();
        return p.doOnRequest(new RxIteratorConsumer<M>(p) {
            @Override
            protected Object transformValue(Object value) {
                return getValue((Entry<Object, Object>) value);
            }

            @Override
            protected boolean tryAgain() {
                return RedissonMapRxIterator.this.tryAgain();
            }

            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return (RFuture<ScanResult<Object>>) (Object) map.scanIteratorAsync(map.getRawName(), client, nextIterPos, pattern, count);
            }
        });
    }

    protected boolean tryAgain() {
        return false;
    }

    M getValue(Entry<Object, Object> entry) {
        return (M) new AbstractMap.SimpleEntry<K, V>((K) entry.getKey(), (V) entry.getValue()) {

            @Override
            public V setValue(V value) {
                return map.put((K) entry.getKey(), value);
            }

        };
    }

}
