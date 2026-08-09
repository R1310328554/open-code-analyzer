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
package org.redisson.spring.data.connection;

import java.util.List;
import java.util.Properties;

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.decoder.MultiDecoder;

/**
 * 将 Redis 扁平 key-value 列表响应解码为 {@link Properties}。
 * <p>按偶数下标为 key、奇数下标为 value 成对写入属性表。
 *
 * @author Nikita Koksharov
 *
 */
public class PropertiesListDecoder implements MultiDecoder<Properties> {

    /** 步长为 2 遍历 parts，将 key/value 填入 {@link Properties}。 */
    @Override
    public Properties decode(List<Object> parts, State state) {
        Properties result = new Properties();
        for (int j = 0; j < parts.size(); j+=2) {
            Object key = parts.get(j);
            Object value = parts.get(j+1);
            result.put(key, value);
        }
        return result;
    }

}
