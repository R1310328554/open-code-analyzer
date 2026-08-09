/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.handler.codec;


import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;

/**
 * 将 ASCII 协议头（名/值对）编码写入 {@link ByteBuf}，支持可配置分隔符与换行符。
 */
public final class AsciiHeadersEncoder {

    /** 头名与头值之间的分隔符样式。 */
    public enum SeparatorType {
        /** 仅冒号 {@code ':'} */
        COLON,
        /** 冒号加空格 {@code ': '} */
        COLON_SPACE,
    }

    /** 相邻头字段之间的换行样式。 */
    public enum NewlineType {
        /** 仅 LF {@code '\n'} */
        LF,
        /** CRLF {@code '\r\n'} */
        CRLF
    }

    private final ByteBuf buf;
    private final SeparatorType separatorType;
    private final NewlineType newlineType;

    /** 默认 {@link SeparatorType#COLON_SPACE} 与 {@link NewlineType#CRLF}。 */
    public AsciiHeadersEncoder(ByteBuf buf) {
        this(buf, SeparatorType.COLON_SPACE, NewlineType.CRLF);
    }

    /** @param buf 目标缓冲区；@param separatorType 名值分隔符；@param newlineType 行尾换行 */ 
    public AsciiHeadersEncoder(ByteBuf buf, SeparatorType separatorType, NewlineType newlineType) {
        this.buf = ObjectUtil.checkNotNull(buf, "buf");
        this.separatorType = ObjectUtil.checkNotNull(separatorType, "separatorType");
        this.newlineType = ObjectUtil.checkNotNull(newlineType, "newlineType");
    }

    /** 将单条头字段 {@code name:value} 追加写入 buf。 */
    public void encode(Entry<CharSequence, CharSequence> entry) {
        final CharSequence name = entry.getKey();
        final CharSequence value = entry.getValue();
        final ByteBuf buf = this.buf;
        final int nameLen = name.length();
        final int valueLen = value.length();
        final int entryLen = nameLen + valueLen + 4;
        int offset = buf.writerIndex();
        buf.ensureWritable(entryLen);
        writeAscii(buf, offset, name);
        offset += nameLen;

        switch (separatorType) {
            case COLON:
                buf.setByte(offset ++, ':');
                break;
            case COLON_SPACE:
                buf.setByte(offset ++, ':');
                buf.setByte(offset ++, ' ');
                break;
            default:
                throw new Error("Unexpected separator type: " + separatorType);
        }

        writeAscii(buf, offset, value);
        offset += valueLen;

        switch (newlineType) {
            case LF:
                buf.setByte(offset ++, '\n');
                break;
            case CRLF:
                buf.setByte(offset ++, '\r');
                buf.setByte(offset ++, '\n');
                break;
            default:
                throw new Error("Unexpected newline type: " + newlineType);
        }

        buf.writerIndex(offset);
    }

    private static void writeAscii(ByteBuf buf, int offset, CharSequence value) {
        if (value instanceof AsciiString) {
            ByteBufUtil.copy((AsciiString) value, 0, buf, offset, value.length());
        } else {
            buf.setCharSequence(offset, value, CharsetUtil.US_ASCII);
        }
    }
}
