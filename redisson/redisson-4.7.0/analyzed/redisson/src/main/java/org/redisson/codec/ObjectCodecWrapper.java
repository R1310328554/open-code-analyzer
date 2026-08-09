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

import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.util.Objects;

/**
 * 将 {@link ObjectCodec} 适配为 Redisson 标准 {@link org.redisson.client.codec.Codec} 的包装器。
 * <p>
 * 直接委托内层 {@link ObjectCodec} 的编码器与解码器，便于接入仅实现
 * 值级编解码的自定义实现。
 *
 * @author Nikita Koksharov
 *
 */
public class ObjectCodecWrapper extends BaseCodec {

    /** 被包装的内层 ObjectCodec。 */
    private final ObjectCodec innerCodec;

    /** @param innerCodec 内层对象编解码器 */
    public ObjectCodecWrapper(ObjectCodec innerCodec) {
        this.innerCodec = innerCodec;
    }

    /** 在指定类加载器下复制内层编解码器。 */
    public ObjectCodecWrapper(ClassLoader classLoader, ObjectCodecWrapper codec) throws ReflectiveOperationException {
        this(copy(classLoader, codec.innerCodec));
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return innerCodec.getDecoder();
    }

    @Override
    public Encoder getValueEncoder() {
        return innerCodec.getEncoder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectCodecWrapper that = (ObjectCodecWrapper) o;
        return Objects.equals(innerCodec, that.innerCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(innerCodec);
    }
}
