/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.redis;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.UnstableApi;

/**
 * A strategy interface for caching {@link RedisMessage}s.
 * <p>常见 RESP 值（如 {@code OK}、{@code PONG}、小整数）的缓存策略接口；
 * {@link RedisDecoder} 解码与 {@link RedisEncoder} 编码时复用池内实例与预编码字节，
 * 减少分配与 UTF-8 转换。未命中时返回 {@code null}，调用方自行构造新对象。</p>
 */
@UnstableApi

public interface RedisMessagePool {

    /**
     * Returns {@link SimpleStringRedisMessage} for given {@code content}. Returns {@code null} it does not exist.
     * <p>按字符串内容查找已缓存的简单字符串消息。</p>
     */
    SimpleStringRedisMessage getSimpleString(String content);

    /**
     * Returns {@link SimpleStringRedisMessage} for given {@code content}. Returns {@code null} it does not exist.
     * <p>按 {@link ByteBuf} 内容查找，避免额外 String 分配。</p>
     */
    SimpleStringRedisMessage getSimpleString(ByteBuf content);

    /**
     * Returns {@link ErrorRedisMessage} for given {@code content}. Returns {@code null} it does not exist.
     */
    ErrorRedisMessage getError(String content);

    /**
     * Returns {@link ErrorRedisMessage} for given {@code content}. Returns {@code null} it does not exist.
     */
    ErrorRedisMessage getError(ByteBuf content);

    /**
     * Returns {@link IntegerRedisMessage} for given {@code value}. Returns {@code null} it does not exist.
     * <p>按数值查找已缓存的整数 RESP 消息。</p>
     */
    IntegerRedisMessage getInteger(long value);

    /**
     * Returns {@link IntegerRedisMessage} for given {@code content}. Returns {@code null} it does not exist.
     */
    IntegerRedisMessage getInteger(ByteBuf content);

    /**
     * Returns {@code byte[]} for given {@code msg}. Returns {@code null} it does not exist.
     * <p>返回整数在 RESP 行中的 ASCII 字节表示，供编码器直接写入。</p>
     */
    byte[] getByteBufOfInteger(long value);
}
