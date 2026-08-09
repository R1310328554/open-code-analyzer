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
 * RMapCache 键扫描回放解码器。
 * <p>
 * 将 Redis 三元素数组（游标、键列表、空闲键列表）解析为
 * {@link MapCacheKeyScanResult}。
 *
 * @author Nikita Koksharov
 *
 */
public class MapCacheKeyScanResultDecoder implements MultiDecoder<MapCacheKeyScanResult<Object>> {

    /** parts[0] 游标，[1] 键列表，[2] 空闲键列表。 */
    @Override
    public MapCacheKeyScanResult<Object> decode(List<Object> parts, State state) {
        String pos = (String) parts.get(0);
        List<Object> values = (List<Object>) parts.get(1);
        List<Object> idleKeys = (List<Object>) parts.get(2);
        return new MapCacheKeyScanResult<>(pos, values, idleKeys);
    }

    /** 字段值使用字符串解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return StringCodec.INSTANCE.getValueDecoder();
    }

}
