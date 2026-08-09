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
 * 带过期策略的 Map 缓存键空间事件编解码器。
 * <p>
 * 解码 {@code RMapCache} 相关过期/淘汰事件消息，依次解析键、当前值，
 * 若缓冲区仍有数据则继续读取旧值（更新场景），返回最多 3 个元素的列表。
 *
 * @author Nikita Koksharov
 *
 */
public class MapCacheEventCodec extends BaseEventCodec {

    /** 解析 Map 缓存事件：键、值，以及可选的旧值。 */
    private final Decoder<Object> decoder = (buf, state) -> {
        List<Object> result = new ArrayList<Object>(3);

        Object key = MapCacheEventCodec.this.decode(buf, state, codec.getMapKeyDecoder());
        result.add(key);

        Object value = MapCacheEventCodec.this.decode(buf, state, codec.getMapValueDecoder());
        result.add(value);

        if (buf.isReadable()) {
            Object oldValue = MapCacheEventCodec.this.decode(buf, state, codec.getMapValueDecoder());
            result.add(oldValue);
        }

        return result;
    };

    /** @param codec 内层 Map 键值编解码器 @param osType 平台字节序类型 */
    public MapCacheEventCodec(Codec codec, OSType osType) {
        super(codec, osType);
    }
    
    /** 在指定类加载器下复制编解码器，重建内层 Codec 实例。 */
    public MapCacheEventCodec(ClassLoader classLoader, MapCacheEventCodec codec) {
        super(newCodec(classLoader, codec), codec.osType);
    }

    /** 通过反射构造带 ClassLoader 的内层编解码器副本。 */
    private static Codec newCodec(ClassLoader classLoader, MapCacheEventCodec codec) {
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
