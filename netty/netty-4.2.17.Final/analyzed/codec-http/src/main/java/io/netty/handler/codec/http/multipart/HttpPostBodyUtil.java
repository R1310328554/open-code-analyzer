/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.http.multipart;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpConstants;

/**
 * {@link HttpPostRequestDecoder}、{@link HttpPostRequestEncoder} 等共用的 multipart 解析/编码工具。
 * <p>
 * 提供换行查找、分隔符搜索、Content-Transfer-Encoding 枚举及缓冲预读优化。
 */
final class HttpPostBodyUtil {

    /** 默认分块读写大小（8096 字节）。 */
    public static final int chunkSize = 8096;

    /** 默认二进制 Content-Type（{@code application/octet-stream}）。 */

    public static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";

    /** 默认文本 Content-Type（{@code text/plain}）。 */

    public static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";

    /** multipart 允许的 Content-Transfer-Encoding 机制（7bit/8bit/binary）。 */

    public enum TransferEncodingMechanism {
        /** 7bit 编码（默认）。 */

        BIT7("7bit"),
        /** 8bit：短行非 ASCII，无需额外编码。 */

        BIT8("8bit"),
        /** binary：长二进制或非 ASCII 文本。 */

        BINARY("binary");

        private final String value;

        TransferEncodingMechanism(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private HttpPostBodyUtil() {
    }

    /** 在 {@link HttpPostRequestDecoder} 中通过底层字节数组预读，减少 CPU 开销。 */

    static class SeekAheadOptimize {
        byte[] bytes;
        int readerIndex;
        int pos;
        int origPos;
        int limit;
        ByteBuf buffer;

        /** @param buffer 须带 backing byte array 的 {@link ByteBuf} */

        SeekAheadOptimize(ByteBuf buffer) {
            if (!buffer.hasArray()) {
                throw new IllegalArgumentException("buffer hasn't backing byte array");
            }
            this.buffer = buffer;
            bytes = buffer.array();
            readerIndex = buffer.readerIndex();
            origPos = pos = buffer.arrayOffset() + readerIndex;
            limit = buffer.arrayOffset() + buffer.writerIndex();
        }

        /** 将读位置回退 {@code minus} 并同步 {@code readerIndex}。 */

        void setReadPosition(int minus) {
            pos -= minus;
            readerIndex = getReadPosition(pos);
            buffer.readerIndex(readerIndex);
        }

        /** 将底层数组下标转换为 {@code readerIndex} 等价偏移。 */

        int getReadPosition(int index) {
            return index - origPos + readerIndex;
        }
    }

    /** 从 {@code offset} 起查找首个非空白字符下标。 */

    static int findNonWhitespace(String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result ++) {
            if (!Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    /** 从末尾向前跳过空白，返回有效内容结束下标。 */

    static int findEndOfString(String sb) {
        int result;
        for (result = sb.length(); result > 0; result --) {
            if (!Character.isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }

    /** 自 {@code index} 起查找首个 LF/CRLF 换行，返回相对偏移或 {@code -1}。 */

    static int findLineBreak(ByteBuf buffer, int index) {
        int toRead = buffer.readableBytes() - (index - buffer.readerIndex());
        int posFirstChar = buffer.bytesBefore(index, toRead, HttpConstants.LF);
        if (posFirstChar == -1) {
            // No LF, so neither CRLF
            return -1;
        }
        if (posFirstChar > 0 && buffer.getByte(index + posFirstChar - 1) == HttpConstants.CR) {
            posFirstChar--;
        }
        return posFirstChar;
    }

    /** 自 {@code index} 起查找连续换行中的最后一处，返回相对偏移。 */

    static int findLastLineBreak(ByteBuf buffer, int index) {
        int candidate = findLineBreak(buffer, index);
        int findCRLF = 0;
        if (candidate >= 0) {
            if (buffer.getByte(index + candidate) == HttpConstants.CR) {
                findCRLF = 2;
            } else {
                findCRLF = 1;
            }
            candidate += findCRLF;
        }
        int next;
        while (candidate > 0 && (next = findLineBreak(buffer, index + candidate)) >= 0) {
            candidate += next;
            if (buffer.getByte(index + candidate) == HttpConstants.CR) {
                findCRLF = 2;
            } else {
                findCRLF = 1;
            }
            candidate += findCRLF;
        }
        return candidate - findCRLF;
    }

    /** 查找 multipart 边界分隔符；{@code precededByLineBreak} 要求前有 LF/CRLF。 */

    static int findDelimiter(ByteBuf buffer, int index, byte[] delimiter, boolean precededByLineBreak) {
        final int delimiterLength = delimiter.length;
        final int readerIndex = buffer.readerIndex();
        final int writerIndex = buffer.writerIndex();
        int toRead = writerIndex - index;
        int newOffset = index;
        boolean delimiterNotFound = true;
        while (delimiterNotFound && delimiterLength <= toRead) {
            // 先定位分隔符首字节
            int posDelimiter = buffer.bytesBefore(newOffset, toRead, delimiter[0]);
            if (posDelimiter < 0) {
                return -1;
            }
            newOffset += posDelimiter;
            toRead -= posDelimiter;
            // 逐字节匹配完整分隔符
            if (toRead >= delimiterLength) {
                delimiterNotFound = false;
                for (int i = 0; i < delimiterLength; i++) {
                    if (buffer.getByte(newOffset + i) != delimiter[i]) {
                        newOffset++;
                        toRead--;
                        delimiterNotFound = true;
                        break;
                    }
                }
            }
            if (!delimiterNotFound) {
                // 分隔符已找到，按需回溯换行前缀
                if (precededByLineBreak && newOffset > readerIndex) {
                    if (buffer.getByte(newOffset - 1) == HttpConstants.LF) {
                        newOffset--;
                        // Check if CR before: not mandatory to be there
                        if (newOffset > readerIndex && buffer.getByte(newOffset - 1) == HttpConstants.CR) {
                            newOffset--;
                        }
                    } else {
                        // Delimiter with Line Break could be further: iterate after first char of delimiter
                        newOffset++;
                        toRead--;
                        delimiterNotFound = true;
                        continue;
                    }
                }
                return newOffset - readerIndex;
            }
        }
        return -1;
    }
}
