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

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import java.util.List;

/**
 * 带分数有序集合 {@code ZSCAN} 扫描结果回放解码器。
 * <p>
 * Redis 返回 {@code [游标, [成员, 分数, 成员, 分数, ...]]} 结构；
 * 本解码器剔除分数字段，仅保留成员列表与游标。
 *
 * @author Nikita Koksharov
 *
 */
public class ScoredSortedSetScanReplayDecoder implements MultiDecoder<ListScanResult<Object>> {

    /** 成员与游标字段统一用 {@link StringCodec} 解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return StringCodec.INSTANCE.getValueDecoder();
    }
    
    /**
     * 组装扫描结果：提取游标，并从成员/分数交替列表中移除分数。
     * <p>
     * 从索引 1 起每隔一个元素删除分数，使 values 仅含成员。
     */
    @Override
    public ListScanResult<Object> decode(List<Object> parts, State state) {
        List<Object> values = (List<Object>) parts.get(1);
        for (int i = 1; i < values.size(); i++) {
            values.remove(i);
        }
        return new ListScanResult<>((String) parts.get(0), values);
    }

}
