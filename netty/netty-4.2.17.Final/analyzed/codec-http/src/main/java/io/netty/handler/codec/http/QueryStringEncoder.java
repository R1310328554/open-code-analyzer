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
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * 由路径与键值参数对构建 URL 编码 URI。
 * <p>
 * 一次性编码器，每个 URI 应新建实例；空格编码为 {@code %20}（非 {@code +}）。
 *
 * <pre>
 * {@link QueryStringEncoder} encoder = new {@link QueryStringEncoder}("/hello");
 * encoder.addParam("recipient", "world");
 * assert encoder.toString().equals("/hello?recipient=world");
 * </pre>
 *
 * @see QueryStringDecoder
 */
public class QueryStringEncoder {

    private final Charset charset;
    private final StringBuilder uriBuilder;
    private boolean hasParams;
    private static final byte WRITE_UTF_UNKNOWN = (byte) '?';
    private static final char[] CHAR_MAP = "0123456789ABCDEF".toCharArray();

    /** 以指定路径创建编码器，默认 UTF-8 编码。 */
    public QueryStringEncoder(String uri) {
        this(uri, HttpConstants.DEFAULT_CHARSET);
    }

    /** 以指定路径与字符集创建编码器。 */
    public QueryStringEncoder(String uri, Charset charset) {
        ObjectUtil.checkNotNull(charset, "charset");
        uriBuilder = new StringBuilder(uri);
        this.charset = CharsetUtil.UTF_8.equals(charset) ? null : charset;
    }

    /** 追加 name=value 参数；首次追加前自动插入 {@code ?}，后续用 {@code &} 连接。 */
    public void addParam(String name, String value) {
        ObjectUtil.checkNotNull(name, "name");
        if (hasParams) {
            uriBuilder.append('&');
        } else {
            uriBuilder.append('?');
            hasParams = true;
        }

        encodeComponent(name);
        if (value != null) {
            uriBuilder.append('=');
            encodeComponent(value);
        }
    }

    private void encodeComponent(CharSequence s) {
        if (charset == null) {
            encodeUtf8Component(s);
        } else {
            encodeNonUtf8Component(s);
        }
    }

    /** 返回 {@link URI} 形式的编码结果。 */
    public URI toUri() throws URISyntaxException {
        return new URI(toString());
    }

    /** 返回 URL 编码后的 URI 字符串。 */
    @Override
    public String toString() {
        return uriBuilder.toString();
    }

    /**
     * 按 RFC 3986 §2 编码；空格用 {@code %20} 而非 JDK {@link URLEncoder} 的 {@code +}。
     * 复用 {@link #uriBuilder} 减少 GC 分配。
     *
     * @param s The String to encode
     */
    private void encodeNonUtf8Component(CharSequence s) {
        // 延迟分配缓冲，仅在遇到需编码字符时分配
        char[] buf = null;

        for (int i = 0, len = s.length(); i < len;) {
            char c = s.charAt(i);
            if (dontNeedEncoding(c)) {
                uriBuilder.append(c);
                i++;
            } else {
                int index = 0;
                if (buf == null) {
                    buf = new char[s.length() - i];
                }

                do {
                    buf[index] = c;
                    index++;
                    i++;
                } while (i < s.length() && !dontNeedEncoding(c = s.charAt(i)));

                byte[] bytes = new String(buf, 0, index).getBytes(charset);

                for (byte b : bytes) {
                    appendEncoded(b);
                }
            }
        }
    }

    /**
     * @see ByteBufUtil#writeUtf8(io.netty.buffer.ByteBuf, CharSequence, int, int)
     */
    private void encodeUtf8Component(CharSequence s) {
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            if (!dontNeedEncoding(c)) {
                encodeUtf8Component(s, i, len);
                return;
            }
        }
        uriBuilder.append(s);
    }

    private void encodeUtf8Component(CharSequence s, int encodingStart, int len) {
        if (encodingStart > 0) {
            // 先追加无需编码的前缀
            uriBuilder.append(s, 0, encodingStart);
        }
        encodeUtf8ComponentSlow(s, encodingStart, len);
    }

    private void encodeUtf8ComponentSlow(CharSequence s, int start, int len) {
        for (int i = start; i < len; i++) {
            char c = s.charAt(i);
            if (c < 0x80) {
                if (dontNeedEncoding(c)) {
                    uriBuilder.append(c);
                } else {
                    appendEncoded(c);
                }
            } else if (c < 0x800) {
                appendEncoded(0xc0 | (c >> 6));
                appendEncoded(0x80 | (c & 0x3f));
            } else if (StringUtil.isSurrogate(c)) {
                if (!Character.isHighSurrogate(c)) {
                    appendEncoded(WRITE_UTF_UNKNOWN);
                    continue;
                }
                // 代理对占 2 个 char
                if (++i == s.length()) {
                    appendEncoded(WRITE_UTF_UNKNOWN);
                    break;
                }
                // 拆出方法以便内联 UTF-8 主路径
                writeUtf8Surrogate(c, s.charAt(i));
            } else {
                appendEncoded(0xe0 | (c >> 12));
                appendEncoded(0x80 | ((c >> 6) & 0x3f));
                appendEncoded(0x80 | (c & 0x3f));
            }
        }
    }

    private void writeUtf8Surrogate(char c, char c2) {
        if (!Character.isLowSurrogate(c2)) {
            appendEncoded(WRITE_UTF_UNKNOWN);
            appendEncoded(Character.isHighSurrogate(c2) ? WRITE_UTF_UNKNOWN : c2);
            return;
        }
        int codePoint = Character.toCodePoint(c, c2);
        // 4 字节 UTF-8 编码（Unicode 7.0 §3）
        appendEncoded(0xf0 | (codePoint >> 18));
        appendEncoded(0x80 | ((codePoint >> 12) & 0x3f));
        appendEncoded(0x80 | ((codePoint >> 6) & 0x3f));
        appendEncoded(0x80 | (codePoint & 0x3f));
    }

    private void appendEncoded(int b) {
        uriBuilder.append('%').append(forDigit(b >> 4)).append(forDigit(b));
    }

    /**
     * 将半字节转为大写十六进制字符。
     *
     * @param digit the number to convert to a character.
     * @return the {@code char} representation of the specified digit
     * in hexadecimal.
     */
    private static char forDigit(int digit) {
        return CHAR_MAP[digit & 0xF];
    }

    /**
     * 判断字符是否为 RFC 3986 unreserved，无需百分号编码。
     * <p>
     * unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~" / "*"
     *
     * @param ch the char to be judged whether it need to be encode
     * @return true or false
     */
    private static boolean dontNeedEncoding(char ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9'
                || ch == '-' || ch == '_' || ch == '.' || ch == '*' || ch == '~';
    }
}
