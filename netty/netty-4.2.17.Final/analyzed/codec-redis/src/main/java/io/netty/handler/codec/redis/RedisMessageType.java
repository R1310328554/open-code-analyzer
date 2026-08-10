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
 * Type of <a href="https://redis.io/topics/protocol">RESP (REdis Serialization Protocol)</a>.
 * <p>RESP 报文类型枚举：首字节前缀（或无前缀的内联命令）、是否内联（无独立长度行）、
 * 以及从 {@link ByteBuf} 读取/写入类型前缀的辅助方法。</p>
 */
@UnstableApi
public enum RedisMessageType {

    /** 无前缀字节的纯文本命令行（需显式开启 {@link RedisDecoder} 内联命令解码）。 */
    INLINE_COMMAND(null, true),
    /** {@code +} 简单字符串，单行至 CRLF。 */
    SIMPLE_STRING((byte) '+', true),
    /** {@code -} 错误字符串。 */
    ERROR((byte) '-', true),
    /** {@code :} 整数，十进制 ASCII。 */
    INTEGER((byte) ':', true),
    /** {@code $} Bulk String，先长度行再正文与 CRLF。 */
    BULK_STRING((byte) '$', false),
    /** {@code *} 数组头，元素个数行后接 N 个子 RESP 值。 */
    ARRAY_HEADER((byte) '*', false);

    private final Byte value;
    /** 内联类型无长度字段，正文与类型在同一逻辑行。 */
    private final boolean inline;

    RedisMessageType(Byte value, boolean inline) {
        this.value = value;
        this.inline = inline;
    }

    /**
     * Returns length of this type.
     * <p>类型前缀写入长度；{@link #INLINE_COMMAND} 为 0。</p>
     */
    public int length() {
        return value != null ? RedisConstants.TYPE_LENGTH : 0;
    }

    /**
     * Returns {@code true} if this type is inline type, or returns {@code false}. If this is {@code true},
     * this type doesn't have length field.
     * <p>内联类型解码时进入 {@code DECODE_INLINE} 状态而非读长度行。</p>
     */
    public boolean isInline() {
        return inline;
    }

    /**
     * Determine {@link RedisMessageType} based on the type prefix {@code byte} read from given the buffer.
     * <p>读首字节映射类型；若为内联命令且未启用解码则回退 readerIndex 并抛异常。</p>
     */
    public static RedisMessageType readFrom(ByteBuf in, boolean decodeInlineCommands) {
        final int initialIndex = in.readerIndex();
        final RedisMessageType type = valueOf(in.readByte());
        if (type == INLINE_COMMAND) {
            if (!decodeInlineCommands) {
                throw new RedisCodecException("Decoding of inline commands is disabled");
            }
            // reset index to make content readable again
            in.readerIndex(initialIndex);
        }
        return type;
    }

    /**
     * Write the message type's prefix to the given buffer.
     */
    public void writeTo(ByteBuf out) {
        if (value == null) {
            return;
        }
        out.writeByte(value.byteValue());
    }

    private static RedisMessageType valueOf(byte value) {
        switch (value) {
        case '+':
            return SIMPLE_STRING;
        case '-':
            return ERROR;
        case ':':
            return INTEGER;
        case '$':
            return BULK_STRING;
        case '*':
            return ARRAY_HEADER;
        default:
            return INLINE_COMMAND;
        }
    }
}
