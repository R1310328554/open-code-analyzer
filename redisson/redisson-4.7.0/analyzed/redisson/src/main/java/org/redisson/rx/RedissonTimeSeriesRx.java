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

import org.reactivestreams.Publisher;
import org.redisson.RedissonObject;
import org.redisson.RedissonTimeSeries;
import org.redisson.ScanResult;
import org.redisson.api.RFuture;
import org.redisson.api.RTimeSeries;
import org.redisson.api.RedissonRxClient;
import org.redisson.client.RedisClient;

/**
 * 时间序列（按时间戳索引的值集合）的 Rx 适配。
 * <p>
 * 通过内部 {@link SetRxIterator} 对 TimeSeries 执行 SCAN 式遍历，
 * 将各时间桶中的值作为 {@link Publisher} 顺序下发（背压由 Rx 层处理）。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 * @param <L> label type
 */
public class RedissonTimeSeriesRx<V, L> {

    /** 底层 RTimeSeries。 */
    private final RTimeSeries<V, L> instance;
    /** 保留 Rx 客户端引用（与同类 Rx 包装一致，便于扩展）。 */
    private final RedissonRxClient redisson;

    public RedissonTimeSeriesRx(RTimeSeries<V, L> instance, RedissonRxClient redisson) {
        this.instance = instance;
        this.redisson = redisson;
    }

    /** 默认每批 10 条扫描时间序列中的值。 */
    public Publisher<V> iterator() {
        return new SetRxIterator<V>() {
            @Override
            protected RFuture<ScanResult<Object>> scanIterator(RedisClient client, String nextIterPos) {
                return ((RedissonTimeSeries) instance).scanIteratorAsync(((RedissonObject) instance).getRawName(), client, nextIterPos, 10);
            }
        }.create();
    }

}
