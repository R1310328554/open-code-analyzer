/*
 * Copyright 2015 The Netty Project
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

/*
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.netty.handler.codec.http2;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2Headers.PseudoHeaderName;
import io.netty.util.AsciiString;
import io.netty.util.internal.PlatformDependent;

import java.util.Arrays;
import java.util.List;

import static io.netty.handler.codec.http2.HpackUtil.equalsVariableTime;

/**
 * RFC 7541 附录 A 定义的 HPACK 静态表：61 条预置头域，编码时可直接用索引引用而无需字面量传输。
 * <p>启动时构建两个哈希表——按头名查最小索引、按「名+非空值」查完整索引，供 {@link HpackEncoder}/{@link HpackDecoder} 快速命中。
 */
final class HpackStaticTable {

    /** 静态表中未找到匹配项时的返回值。 */
    static final int NOT_FOUND = -1;

    // Appendix A: Static Table — RFC 7541 附录 A 完整 61 项静态表
    // https://tools.ietf.org/html/rfc7541#appendix-A
    private static final List<HpackHeaderField> STATIC_TABLE = Arrays.asList(
    /*  1 */ newEmptyPseudoHeaderField(PseudoHeaderName.AUTHORITY),
    /*  2 */ newPseudoHeaderMethodField(HttpMethod.GET),
    /*  3 */ newPseudoHeaderMethodField(HttpMethod.POST),
    /*  4 */ newPseudoHeaderField(PseudoHeaderName.PATH, "/"),
    /*  5 */ newPseudoHeaderField(PseudoHeaderName.PATH, "/index.html"),
    /*  6 */ newPseudoHeaderField(PseudoHeaderName.SCHEME, "http"),
    /*  7 */ newPseudoHeaderField(PseudoHeaderName.SCHEME, "https"),
    /*  8 */ newPseudoHeaderField(PseudoHeaderName.STATUS, HttpResponseStatus.OK.codeAsText()),
    /*  9 */ newPseudoHeaderField(PseudoHeaderName.STATUS, HttpResponseStatus.NO_CONTENT.codeAsText()),
    /* 10 */ newPseudoHeaderField(PseudoHeaderName.STATUS, HttpResponseStatus.PARTIAL_CONTENT.codeAsText()),
    /* 11 */ newPseudoHeaderField(PseudoHeaderName.STATUS, HttpResponseStatus.NOT_MODIFIED.codeAsText()),
    /* 12 */ newPseudoHeaderField(PseudoHeaderName.STATUS, HttpResponseStatus.BAD_REQUEST.codeAsText()),
    /* 13 */ newPseudoHeaderField(PseudoHeaderName.STATUS, HttpResponseStatus.NOT_FOUND.codeAsText()),
    /* 14 */ newPseudoHeaderField(PseudoHeaderName.STATUS, HttpResponseStatus.INTERNAL_SERVER_ERROR.codeAsText()),
    /* 15 */ newEmptyHeaderField(HttpHeaderNames.ACCEPT_CHARSET),
    /* 16 */ newHeaderField(HttpHeaderNames.ACCEPT_ENCODING, "gzip, deflate"),
    /* 17 */ newEmptyHeaderField(HttpHeaderNames.ACCEPT_LANGUAGE),
    /* 18 */ newEmptyHeaderField(HttpHeaderNames.ACCEPT_RANGES),
    /* 19 */ newEmptyHeaderField(HttpHeaderNames.ACCEPT),
    /* 20 */ newEmptyHeaderField(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN),
    /* 21 */ newEmptyHeaderField(HttpHeaderNames.AGE),
    /* 22 */ newEmptyHeaderField(HttpHeaderNames.ALLOW),
    /* 23 */ newEmptyHeaderField(HttpHeaderNames.AUTHORIZATION),
    /* 24 */ newEmptyHeaderField(HttpHeaderNames.CACHE_CONTROL),
    /* 25 */ newEmptyHeaderField(HttpHeaderNames.CONTENT_DISPOSITION),
    /* 26 */ newEmptyHeaderField(HttpHeaderNames.CONTENT_ENCODING),
    /* 27 */ newEmptyHeaderField(HttpHeaderNames.CONTENT_LANGUAGE),
    /* 28 */ newEmptyHeaderField(HttpHeaderNames.CONTENT_LENGTH),
    /* 29 */ newEmptyHeaderField(HttpHeaderNames.CONTENT_LOCATION),
    /* 30 */ newEmptyHeaderField(HttpHeaderNames.CONTENT_RANGE),
    /* 31 */ newEmptyHeaderField(HttpHeaderNames.CONTENT_TYPE),
    /* 32 */ newEmptyHeaderField(HttpHeaderNames.COOKIE),
    /* 33 */ newEmptyHeaderField(HttpHeaderNames.DATE),
    /* 34 */ newEmptyHeaderField(HttpHeaderNames.ETAG),
    /* 35 */ newEmptyHeaderField(HttpHeaderNames.EXPECT),
    /* 36 */ newEmptyHeaderField(HttpHeaderNames.EXPIRES),
    /* 37 */ newEmptyHeaderField(HttpHeaderNames.FROM),
    /* 38 */ newEmptyHeaderField(HttpHeaderNames.HOST),
    /* 39 */ newEmptyHeaderField(HttpHeaderNames.IF_MATCH),
    /* 40 */ newEmptyHeaderField(HttpHeaderNames.IF_MODIFIED_SINCE),
    /* 41 */ newEmptyHeaderField(HttpHeaderNames.IF_NONE_MATCH),
    /* 42 */ newEmptyHeaderField(HttpHeaderNames.IF_RANGE),
    /* 43 */ newEmptyHeaderField(HttpHeaderNames.IF_UNMODIFIED_SINCE),
    /* 44 */ newEmptyHeaderField(HttpHeaderNames.LAST_MODIFIED),
    /* 45 */ newEmptyHeaderField("link"),
    /* 46 */ newEmptyHeaderField(HttpHeaderNames.LOCATION),
    /* 47 */ newEmptyHeaderField(HttpHeaderNames.MAX_FORWARDS),
    /* 48 */ newEmptyHeaderField(HttpHeaderNames.PROXY_AUTHENTICATE),
    /* 49 */ newEmptyHeaderField(HttpHeaderNames.PROXY_AUTHORIZATION),
    /* 50 */ newEmptyHeaderField(HttpHeaderNames.RANGE),
    /* 51 */ newEmptyHeaderField(HttpHeaderNames.REFERER),
    /* 52 */ newEmptyHeaderField("refresh"),
    /* 53 */ newEmptyHeaderField(HttpHeaderNames.RETRY_AFTER),
    /* 54 */ newEmptyHeaderField(HttpHeaderNames.SERVER),
    /* 55 */ newEmptyHeaderField(HttpHeaderNames.SET_COOKIE),
    /* 56 */ newEmptyHeaderField("strict-transport-security"),
    /* 57 */ newEmptyHeaderField(HttpHeaderNames.TRANSFER_ENCODING),
    /* 58 */ newEmptyHeaderField(HttpHeaderNames.USER_AGENT),
    /* 59 */ newEmptyHeaderField(HttpHeaderNames.VARY),
    /* 60 */ newEmptyHeaderField(HttpHeaderNames.VIA),
    /* 61 */ newEmptyHeaderField(HttpHeaderNames.WWW_AUTHENTICATE)
    );

    private static HpackHeaderField newEmptyHeaderField(AsciiString name) {
        return new HpackHeaderField(name, AsciiString.EMPTY_STRING);
    }

    private static HpackHeaderField newEmptyHeaderField(String name) {
        return new HpackHeaderField(AsciiString.cached(name), AsciiString.EMPTY_STRING);
    }

    private static HpackHeaderField newHeaderField(AsciiString name, String value) {
        return new HpackHeaderField(name, AsciiString.cached(value));
    }

    private static HpackHeaderField newPseudoHeaderMethodField(HttpMethod method) {
        return new HpackHeaderField(PseudoHeaderName.METHOD.value(), method.asciiName());
    }

    private static HpackHeaderField newPseudoHeaderField(PseudoHeaderName name, AsciiString value) {
        return new HpackHeaderField(name.value(), value);
    }

    private static HpackHeaderField newPseudoHeaderField(PseudoHeaderName name, String value) {
        return new HpackHeaderField(name.value(), AsciiString.cached(value));
    }

    private static HpackHeaderField newEmptyPseudoHeaderField(PseudoHeaderName name) {
        return new HpackHeaderField(name.value(), AsciiString.EMPTY_STRING);
    }

    // 表大小与位移经过精心选取，使每个哈希桶仅容纳一个头名，查找 O(1) 且无链式冲突
    private static final int HEADER_NAMES_TABLE_SIZE = 1 << 9;

    private static final int HEADER_NAMES_TABLE_SHIFT = PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? 22 : 18;

    /** 头名 → 最小静态索引 的查找表（含是否空值标志）。 */
    private static final HeaderNameIndex[] HEADER_NAMES = new HeaderNameIndex[HEADER_NAMES_TABLE_SIZE];
    static {
        // 逆序遍历静态表，使同名头域保留最小索引（HPACK 索引越小编码越短）
        for (int index = STATIC_TABLE.size(); index > 0; index--) {
            HpackHeaderField entry = getEntry(index);
            int bucket = headerNameBucket(entry.name);
            HeaderNameIndex tableEntry = HEADER_NAMES[bucket];
            if (tableEntry != null && !equalsVariableTime(tableEntry.name, entry.name)) {
                // Can happen if AsciiString.hashCode changes
                throw new IllegalStateException("Hash bucket collision between " +
                  tableEntry.name + " and " + entry.name);
            }
            HEADER_NAMES[bucket] = new HeaderNameIndex(entry.name, index, entry.value.length() == 0);
        }
    }

    // 非空值头域表：桶数 64，同样保证每桶唯一，用于「名+值」完全匹配查找
    private static final int HEADERS_WITH_NON_EMPTY_VALUES_TABLE_SIZE = 1 << 6;

    private static final int HEADERS_WITH_NON_EMPTY_VALUES_TABLE_SHIFT =
      PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? 0 : 6;

    /** 名+非空值 → 静态索引，支持 Indexed Header Field 整项命中。 */
    private static final HeaderIndex[] HEADERS_WITH_NON_EMPTY_VALUES =
      new HeaderIndex[HEADERS_WITH_NON_EMPTY_VALUES_TABLE_SIZE];
    static {
        for (int index = STATIC_TABLE.size(); index > 0; index--) {
            HpackHeaderField entry = getEntry(index);
            if (entry.value.length() > 0) {
                int bucket = headerBucket(entry.value);
                HeaderIndex tableEntry = HEADERS_WITH_NON_EMPTY_VALUES[bucket];
                if (tableEntry != null) {
                    // Can happen if AsciiString.hashCode changes
                    throw new IllegalStateException("Hash bucket collision between " +
                      tableEntry.value + " and " + entry.value);
                }
                HEADERS_WITH_NON_EMPTY_VALUES[bucket] = new HeaderIndex(entry.name, entry.value, index);
            }
        }
    }

    /** 静态表条目总数（61）。 */
    static final int length = STATIC_TABLE.size();

    /**
     * 按 HPACK 索引（1-based）返回静态表项。
     */
    static HpackHeaderField getEntry(int index) {
        return STATIC_TABLE.get(index - 1);
    }

    /**
     * 返回给定头名在静态表中的最小索引；不存在则 {@link #NOT_FOUND}。
     */
    static int getIndex(CharSequence name) {
        HeaderNameIndex entry = getEntry(name);
        return entry == null ? NOT_FOUND : entry.index;
    }

    /**
     * 按头名+值查找完整静态表项索引；空值时仅匹配「名+空值」条目，否则走非空值哈希表。
     */
    static int getIndexInsensitive(CharSequence name, CharSequence value) {
        if (value.length() == 0) {
            HeaderNameIndex entry = getEntry(name);
            return entry == null || !entry.emptyValue ? NOT_FOUND : entry.index;
        }
        int bucket = headerBucket(value);
        HeaderIndex header = HEADERS_WITH_NON_EMPTY_VALUES[bucket];
        if (header == null) {
            return NOT_FOUND;
        }
        if (equalsVariableTime(header.name, name) && equalsVariableTime(header.value, value)) {
            return header.index;
        }
        return NOT_FOUND;
    }

    private static HeaderNameIndex getEntry(CharSequence name) {
        int bucket = headerNameBucket(name);
        HeaderNameIndex entry = HEADER_NAMES[bucket];
        if (entry == null) {
            return null;
        }
        return equalsVariableTime(entry.name, name) ? entry : null;
    }

    private static int headerNameBucket(CharSequence name) {
        return bucket(name, HEADER_NAMES_TABLE_SHIFT, HEADER_NAMES_TABLE_SIZE - 1);
    }

    private static int headerBucket(CharSequence value) {
        return bucket(value, HEADERS_WITH_NON_EMPTY_VALUES_TABLE_SHIFT, HEADERS_WITH_NON_EMPTY_VALUES_TABLE_SIZE - 1);
    }

    private static int bucket(CharSequence s, int shift, int mask) {
        return (AsciiString.hashCode(s) >> shift) & mask;
    }

    /** 头名索引条目：记录最小索引及该条目是否以空值为值。 */
    private static final class HeaderNameIndex {
        final CharSequence name;
        final int index;
        final boolean emptyValue;

        HeaderNameIndex(CharSequence name, int index, boolean emptyValue) {
            this.name = name;
            this.index = index;
            this.emptyValue = emptyValue;
        }
    }

    /** 完整头域（名+非空值）索引条目。 */
    private static final class HeaderIndex {
        final CharSequence name;
        final CharSequence value;
        final int index;

        HeaderIndex(CharSequence name, CharSequence value, int index) {
            this.name = name;
            this.value = value;
            this.index = index;
        }
    }

    // 工具类，禁止实例化
    private HpackStaticTable() {
    }
}
