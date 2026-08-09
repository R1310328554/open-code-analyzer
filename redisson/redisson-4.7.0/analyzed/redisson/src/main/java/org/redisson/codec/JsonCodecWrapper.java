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
 * 将 {@link JsonCodec} 适配为 Redisson 标准 {@link org.redisson.client.codec.Codec} 的包装器。
 * <p>
 * 仅委托值的编解码；Map 键/值编解码沿用 {@link BaseCodec} 默认行为（通常与值相同）。
 *
 * @author Nikita Koksharov
 *
 */
public class JsonCodecWrapper extends BaseCodec {

    /** 内层 JSON 编解码器。 */
    private JsonCodec innerCodec;

    /** @param innerCodec 被包装的 JsonCodec 实例 */
    public JsonCodecWrapper(JsonCodec innerCodec) {
        this.innerCodec = innerCodec;
    }

    /** 按 ClassLoader 复制内层编解码器。 */
    public JsonCodecWrapper(ClassLoader classLoader, JsonCodecWrapper codec) throws ReflectiveOperationException {
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

    /** 仅比较内层 JsonCodec 是否相同。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonCodecWrapper that = (JsonCodecWrapper) o;
        return Objects.equals(innerCodec, that.innerCodec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(innerCodec);
    }
}
