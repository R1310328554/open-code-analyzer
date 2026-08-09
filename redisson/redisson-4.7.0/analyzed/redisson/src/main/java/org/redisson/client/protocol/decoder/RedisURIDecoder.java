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
import org.redisson.misc.RedisURI;

import java.util.List;

/**
 * Redis 节点地址（host + port）解码器。
 * <p>
 * 将 {@code [host, port]} 字符串对组装为 {@link RedisURI}，
 * scheme 由构造参数指定（如 {@code redis}、{@code rediss}）。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisURIDecoder implements MultiDecoder<RedisURI> {

    /** URI 协议前缀，例如 redis 或 rediss。 */
    private final String scheme;

    /** 指定连接协议 scheme。 */
    public RedisURIDecoder(String scheme) {
        this.scheme = scheme;
    }

    /** host 与 port 均按字符串解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return StringCodec.INSTANCE.getValueDecoder();
    }
    
    /** 解析 host/port 构造 RedisURI，空响应返回 null。 */
    @Override
    public RedisURI decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return null;
        }
        return new RedisURI(scheme, (String) parts.get(0), Integer.parseInt((String) parts.get(1)));
    }

}
