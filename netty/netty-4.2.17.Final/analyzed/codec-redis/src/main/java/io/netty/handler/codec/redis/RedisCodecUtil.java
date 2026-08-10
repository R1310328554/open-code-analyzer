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

import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;

/**
 * Utilities for codec-redis.
 * <p>编解码内部工具：长整型转 ASCII 字节、按平台字节序读写 16 位分隔符等，
 * 供 {@link RedisEncoder} 与 {@link RedisDecoder} 复用。</p>
 */
final class RedisCodecUtil {

    private RedisCodecUtil() {
    }

    /** 将 long 转为 US-ASCII 十进制字节，用于 RESP 长度/整数行。 */
    static byte[] longToAsciiBytes(long value) {
        return Long.toString(value).getBytes(CharsetUtil.US_ASCII);
    }

    /**
     * Returns a {@code short} value using endian order.
     * <p>将两个 ASCII 字符按本机字节序拼成 short，常用于快速比较 {@code \r\n}。</p>
     */
    static short makeShort(char first, char second) {
        return PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ?
                (short) ((second << 8) | first) : (short) ((first << 8) | second);
    }

    /**
     * Returns a {@code byte[]} of {@code short} value. This is opposite of {@code makeShort()}.
     * <p>{@link #makeShort(char, char)} 的逆操作，用于异常信息中打印错误分隔符字节。</p>
     */
    static byte[] shortToBytes(short value) {
        byte[] bytes = new byte[2];
        if (PlatformDependent.BIG_ENDIAN_NATIVE_ORDER) {
            bytes[1] = (byte) ((value >> 8) & 0xff);
            bytes[0] = (byte) (value & 0xff);
        } else {
            bytes[0] = (byte) ((value >> 8) & 0xff);
            bytes[1] = (byte) (value & 0xff);
        }
        return bytes;
    }
}
