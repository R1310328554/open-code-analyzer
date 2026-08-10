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

import io.netty.util.internal.SystemPropertyUtil;

/**
 * Constant values for Redis encoder/decoder.
 * <p>RESP 编解码共用常量：类型前缀长度、CRLF、null 标记（{@code $-1} / {@code *-1}）、
 * 消息与内联长度上限，以及数组最大元素数的系统属性名。</p>
 */
final class RedisConstants {

    private RedisConstants() {
    }

    /** RESP 类型前缀占 1 字节（如 {@code +}、{@code $}）。 */
    static final int TYPE_LENGTH = 1;

    /** 行结束符 {@code \r\n} 长度。 */
    static final int EOL_LENGTH = 2;

    /** null bulk/array 行中 {@code -1} 占 2 字节。 */
    static final int NULL_LENGTH = 2;

    /** RESP 中表示 null bulk string / null array 的长度字段值。 */
    static final int NULL_VALUE = -1;

    static final int REDIS_MESSAGE_MAX_LENGTH = 512 * 1024 * 1024; // 512MB

    // 64KB is max inline length of current Redis server implementation.
    /** 内联类型（Simple String / Error / Integer）单行最大长度，与 Redis 服务端一致。 */
    static final int REDIS_INLINE_MESSAGE_MAX_LENGTH = 64 * 1024;

    static final int POSITIVE_LONG_MAX_LENGTH = 19; // length of Long.MAX_VALUE

    static final int LONG_MAX_LENGTH = POSITIVE_LONG_MAX_LENGTH + 1; // +1 is sign

    /** 预计算的 {@code -1} 两字节，用于写入 null 长度行。 */
    static final short NULL_SHORT = RedisCodecUtil.makeShort('-', '1');

    /** 预计算的 {@code \r\n}，用于快速校验与写入行尾。 */
    static final short EOL_SHORT = RedisCodecUtil.makeShort('\r', '\n');

    /** 系统属性：单次数组聚合允许的最大元素个数，默认 1_000_000。 */
    static final String PROP_REDIS_MAX_ARRAY_LENGTH = "io.netty.handler.codec.redis.maxArrayLength";
    static final int REDIS_MAX_ARRAY_LENGTH = SystemPropertyUtil.getInt(PROP_REDIS_MAX_ARRAY_LENGTH, 1_000_000);
}
