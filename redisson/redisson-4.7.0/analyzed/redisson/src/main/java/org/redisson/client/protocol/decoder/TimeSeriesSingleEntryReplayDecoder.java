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
package org.redisson.client.protocol.decoder;

import org.redisson.api.TimeSeriesEntry;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import java.util.List;

/**
 * TimeSeries 单条目解码器。
 * <p>
 * 直接解析一组四元响应为单个 {@link TimeSeriesEntry}，
 * 布局与 {@link TimeSeriesEntryReplayDecoder} 单组一致：
 * {@code [flag, timestamp, value, label?]}。
 *
 * @author Nikita Koksharov
 *
 */
public class TimeSeriesSingleEntryReplayDecoder implements MultiDecoder<TimeSeriesEntry<Object, Object>> {

    /** 时间戳与 flag 字段（索引 0、1）使用 {@link LongCodec}。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (paramNum == 0 || paramNum == 1) {
            return LongCodec.INSTANCE.getValueDecoder();
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);
    }
    
    /** {@code flag==3} 时取第 4 个元素为标签，否则标签为 {@code null}。 */
    @Override
    public TimeSeriesEntry<Object, Object> decode(List<Object> parts, State state) {
        Long n = (Long) parts.get(0);
        Object label = null;
        if (n == 3) {
            label = parts.get(3);
        }
        return new TimeSeriesEntry<>((Long) parts.get(1), parts.get(2), label);
    }

}
