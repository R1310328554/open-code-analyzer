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
 * 列表 SCAN 回放解码器。
 * <p>
 * 将 Redis 两元素数组（游标 + 值列表）解析为 {@link ListScanResult}。
 *
 * @author Nikita Koksharov
 *
 */
public class ListScanResultReplayDecoder implements MultiDecoder<ListScanResult<Object>> {

    /** 字段值使用字符串解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return StringCodec.INSTANCE.getValueDecoder();
    }
    
    /** parts[0] 为游标，parts[1] 为扫描到的对象列表。 */
    @Override
    public ListScanResult<Object> decode(List<Object> parts, State state) {
        return new ListScanResult<>((String) parts.get(0), (List<Object>) parts.get(1));
    }

}
