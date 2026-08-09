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
package org.redisson.codec;

import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.Decoder;

import java.util.ArrayList;
import java.util.List;

/**
 * 带过期策略的 Set 缓存键空间事件编解码器。
 * <p>
 * 解码由 {@link org.redisson.eviction.SetCacheEvictionTask} 发布的过期条目事件；
 * 每条消息包含一个集合元素，使用 Set 的值编解码器解析，返回单元素列表。
 *
 * @author Nikita Koksharov
 *
 */
public class SetCacheEventCodec extends BaseEventCodec {

    /** 解析 Set 缓存过期事件中的单个元素值。 */
    private final Decoder<Object> decoder = (buf, state) -> {
        List<Object> result = new ArrayList<Object>(1);

        Object value = SetCacheEventCodec.this.decode(buf, state, codec.getValueDecoder());
        result.add(value);

        return result;
    };

    /** @param codec 内层值编解码器 @param osType 平台字节序类型 */
    public SetCacheEventCodec(Codec codec, OSType osType) {
        super(codec, osType);
    }

    /** 在指定类加载器下复制编解码器。 */
    public SetCacheEventCodec(ClassLoader classLoader, SetCacheEventCodec codec) {
        super(newCodec(classLoader, codec), codec.osType);
    }

    /** 通过反射构造带 ClassLoader 的内层编解码器副本。 */
    private static Codec newCodec(ClassLoader classLoader, SetCacheEventCodec codec) {
        try {
            return codec.codec.getClass().getConstructor(ClassLoader.class, codec.codec.getClass()).newInstance(classLoader, codec.codec);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

}
