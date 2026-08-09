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
package org.redisson.client.protocol;

import java.io.IOException;

import org.redisson.client.handler.State;

import io.netty.buffer.ByteBuf;

/**
 * 将 Netty {@link ByteBuf} 中的 RESP 片段解码为 Java 对象。
 * <p>
 * 解码过程可通过 {@link State} 传递嵌套层级等上下文。
 *
 * @author Nikita Koksharov
 *
 * @param <R> result type
 */
public interface Decoder<R> {

    /** 从缓冲区读取并解码，必要时更新 {@code state}。 */
    R decode(ByteBuf buf, State state) throws IOException;

}
