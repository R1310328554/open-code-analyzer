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

/**
 * JSON 编解码器标记接口，继承 {@link ObjectCodec} 的编码/解码契约。
 * <p>
 * 具体实现如 {@link JsonJacksonCodec}、{@link JacksonCodec} 等提供 JSON 序列化能力；
 * 可通过 {@link JsonCodecWrapper} 包装为完整 {@link org.redisson.client.codec.Codec}。
 *
 * @author Nikita Koksharov
 *
 */
public interface JsonCodec extends ObjectCodec {

}
