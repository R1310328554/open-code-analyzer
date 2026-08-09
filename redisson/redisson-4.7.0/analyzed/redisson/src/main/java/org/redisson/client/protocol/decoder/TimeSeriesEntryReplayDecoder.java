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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TimeSeries 多条目批量回放解码器。
 * <p>
 * 每 4 个元素为一组：{@code [value, label?, flag, timestamp]}，
 * 其中 {@code flag==3} 表示带标签条目。可选 {@code reverse} 在返回前
 * 反转列表顺序（如 {@code REVRANGE} 结果）。
 *
 * @author Nikita Koksharov
 *
 */
public class TimeSeriesEntryReplayDecoder implements MultiDecoder<List<TimeSeriesEntry<Object, Object>>> {

    /** 为 {@code true} 时在 decode 末尾 {@link Collections#reverse} 结果列表。 */
    private boolean reverse;

    /** 默认保持 Redis 返回的时间顺序。 */
    public TimeSeriesEntryReplayDecoder() {
        this(false);
    }

    /** @param reverse 是否在组装完成后反转条目顺序 */
    public TimeSeriesEntryReplayDecoder(boolean reverse) {
        this.reverse = reverse;
    }

    /** 每组第 3、4 个字段（索引 2、3）使用 {@link LongCodec} 解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (paramNum % 4 == 2 || paramNum % 4 == 3) {
            return LongCodec.INSTANCE.getValueDecoder();
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size);
    }
    
    /** 按四元组步进解析，构造 {@link TimeSeriesEntry} 列表。 */
    @Override
    public List<TimeSeriesEntry<Object, Object>> decode(List<Object> parts, State state) {
        List<TimeSeriesEntry<Object, Object>> result = new ArrayList<>();
        for (int i = 0; i < parts.size(); i += 4) {
            Long n = (Long) parts.get(i + 2);
            Object label = null;
            if (n == 3) {
               label = parts.get(i + 1);
            }
            result.add(new TimeSeriesEntry<>((Long) parts.get(i + 3), parts.get(i), label));
        }
        if (reverse) {
            Collections.reverse(result);
        }
        return result;
    }

}
