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

import java.util.List;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 布隆过滤器 {@code BF.INFO} 单字段回复解码器。
 * <p>
 * 当命令仅查询某一统计项时，回复为单元素数组，取首项 {@code Long}。
 *
 * @author Su Ko
 *
 */
public class BloomFilterInfoSingleDecoder implements MultiDecoder<Long> {

    /** 单值回复使用 {@link LongCodec}。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return LongCodec.INSTANCE.getValueDecoder();
    }

    /** 返回数组第一个元素作为 Long 统计值。 */
    @Override
    public Long decode(List<Object> parts, State state) {
        return (Long) parts.get(0);
    }

}
