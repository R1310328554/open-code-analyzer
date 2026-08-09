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

import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

/**
 * 精简的对象编解码接口，仅暴露值级别的编码器与解码器。
 * <p>
 * 与完整 {@link org.redisson.client.codec.Codec} 不同，不包含 Map 键/值等细分编解码方法；
 * 通常由 {@link ObjectCodecWrapper} 适配为 Redisson 标准 Codec。
 *
 * @author Nikita Koksharov
 *
 */
public interface ObjectCodec {

    /**
     * 返回对象编码器。
     *
     * @return encoder 编码器实例
     */
    Encoder getEncoder();

    /**
     * 返回对象解码器。
     *
     * @return decoder 解码器实例
     */
    Decoder<Object> getDecoder();

}
