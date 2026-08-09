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
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import java.util.List;

/**
 * 带租约信息的 {@code EVAL} 脚本回复解码器。
 * <p>
 * 解析形如 {@code {status, value, token, leaseAcquired}} 的四元组，其中：
 * <ul>
 *     <li>{@code status}：{@link Long} 状态码</li>
 *     <li>{@code value}：通过 {@code codec.getMapValueDecoder()} 解码的业务值</li>
 *     <li>{@code token}：{@link String} 租约令牌</li>
 *     <li>{@code leaseAcquired}：{@link Long}，{@code 1} 表示脚本通过 {@code SET NX} 成功获取租约</li>
 * </ul>
 *
 * @author nhancdt2602
 */
public class MapValueLeaseDecoder implements MultiDecoder<List<Object>> {

    /** 按字段索引选择对应解码器：status/value/token/leaseAcquired。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size, List<Object> parts) {
        if (paramNum == 0) {
            return LongCodec.INSTANCE.getValueDecoder();
        }
        if (paramNum == 1) {
            return codec.getMapValueDecoder();
        }
        if (paramNum == 2) {
            return StringCodec.INSTANCE.getValueDecoder();
        }
        if (paramNum == 3) {
            return LongCodec.INSTANCE.getValueDecoder();
        }
        return MultiDecoder.super.getDecoder(codec, paramNum, state, size, parts);
    }

    /** 四字段均已解码，直接返回 parts 列表。 */
    @Override
    public List<Object> decode(List<Object> parts, State state) {
        return parts;
    }
}
