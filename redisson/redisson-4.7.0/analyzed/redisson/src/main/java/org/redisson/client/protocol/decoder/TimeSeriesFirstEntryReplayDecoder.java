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
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import java.util.List;

/**
 * TimeSeries 首条目解码器。
 * <p>
 * 复用 {@link TimeSeriesEntryReplayDecoder} 解析完整列表，
 * 仅返回第一个 {@link TimeSeriesEntry}；空响应时返回 {@code null}。
 * 适用于 {@code TS.GET} 等单条查询命令。
 *
 * @author Nikita Koksharov
 *
 */
public class TimeSeriesFirstEntryReplayDecoder implements MultiDecoder<Object> {

    /** 委托给标准 TimeSeries 列表解码器的元素级解码策略。 */
    private final TimeSeriesEntryReplayDecoder decoder = new TimeSeriesEntryReplayDecoder();

    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return decoder.getDecoder(codec, paramNum, state, size);
    }
    
    /** 解码列表后取首元素，无数据则 {@code null}。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        List<TimeSeriesEntry<Object, Object>> list = decoder.decode(parts, state);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

}
