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
import org.redisson.RedissonObject;
import org.redisson.RedissonTimeSeries;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.api.RTimeSeries;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.RedisClient;
import reactor.core.publisher.Flux;

/**
 * {@link RTimeSeries} 的 Reactor 响应式辅助类：
 * 时间序列按时间戳排序存储，支持 SCAN 方式流式读取条目值。
 * <p>
 * 标签 {@code L} 用于多维时间序列分组（若底层实现支持）。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 时间序列值类型
 * @param <L> 标签类型
 */
public class RedissonTimeSeriesReactive<V, L> {

    /** 底层同步时间序列。 */
    private final RTimeSeries<V, L> instance;
    /** 响应式客户端（预留扩展）。 */
    private final RedissonReactiveClient redisson;

    /** @param instance 同步时间序列 @param redisson 响应式客户端 */
    public RedissonTimeSeriesReactive(RTimeSeries<V, L> instance, RedissonReactiveClient redisson) {
        this.instance = instance;
        this.redisson = redisson;
    }

    /** 通过 {@link SetReactiveIterator} 分页 SCAN 时间序列条目值。 */
    public Publisher<V> iterator() {
        return Flux.create(new SetReactiveIterator<V>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((RedissonTimeSeries) instance).scanIteratorAsync(((RedissonObject) instance).getRawName(), client, nextIterPos, 10);
            }
        });
    }

}
