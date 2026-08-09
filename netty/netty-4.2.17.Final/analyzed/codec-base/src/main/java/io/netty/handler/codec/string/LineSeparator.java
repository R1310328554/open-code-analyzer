/*
 * Copyright 2016 The Netty Project
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
package io.netty.handler.codec.string;

import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * 表示不同平台/约定下的行分隔符。
 */
public final class LineSeparator {

    /**
     * 当前操作系统的默认行分隔符。
     */
    public static final LineSeparator DEFAULT = new LineSeparator(StringUtil.NEWLINE);

    /**
     * Unix 行分隔符（LF，{@code \n}）。
     */
    public static final LineSeparator UNIX = new LineSeparator("\n");

    /**
     * Windows 行分隔符（CRLF，{@code \r\n}）。
     */
    public static final LineSeparator WINDOWS = new LineSeparator("\r\n");

    /** 行分隔符字符串值。 */
    private final String value;

    /**
     * 使用指定 {@code lineSeparator} 字符串创建 {@link LineSeparator}。
     */
    public LineSeparator(String lineSeparator) {
        this.value = ObjectUtil.checkNotNull(lineSeparator, "lineSeparator");
    }

    /**
     * 返回行分隔符的字符串形式。
     */
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LineSeparator)) {
            return false;
        }
        LineSeparator that = (LineSeparator) o;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    /**
     * 以 UTF-8 编码返回行分隔符的 <a href="https://en.wikipedia.org/wiki/Hex_dump">十六进制转储</a>。
     */
    @Override
    public String toString() {
        return ByteBufUtil.hexDump(value.getBytes(CharsetUtil.UTF_8));
    }
}
