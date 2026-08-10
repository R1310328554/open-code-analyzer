/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.http3;

import io.netty.handler.codec.UnsupportedValueConverter;
import io.netty.util.AsciiString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QPACK 静态表：RFC 9204 附录 A 预定义的常见 HTTP 头名/值对，编码时可直接用索引引用以节省字节。
 * <p>与 HPACK 不同，QPACK 静态表索引从 0 开始；同名头可能有多条记录，查找时需区分「精确匹配」与「仅匹配名」。</p>
 */
final class QpackStaticTable {

    /** 查找未命中时的返回值。 */
    static final int NOT_FOUND = -1;

    /**
     * Special mask used to disambiguate exact pair index from
     * name only index and avoid executing lookup twice. Supposed
     * to be used internally. The value should be large enough
     * not to override bits from static table index (current size
     * of the table is 99 elements).
     * <p>当值无法在静态表精确匹配时，将最低索引与 {@code MASK_NAME_REF} 按位或，
     * 表示「可引用该名、但值需以字面量发送」，避免二次查表。</p>
     */
    static final int MASK_NAME_REF = 1 << 10;

    /**
     * <a href="https://www.rfc-editor.org/rfc/rfc9204.html#name-static-table-2>Appendix A: Static Table</a>
     * <p>启动时一次性构建，条目顺序与 RFC 一致；{@link AsciiString#cached} 避免重复分配。</p>
     */
    private static final List<QpackHeaderField> STATIC_TABLE = Arrays.asList(
        newEmptyHeaderField(":authority"),
        newHeaderField(":path", "/"),
        newHeaderField("age", "0"),
        newEmptyHeaderField("content-disposition"),
        newHeaderField("content-length", "0"),
        newEmptyHeaderField("cookie"),
        newEmptyHeaderField("date"),
        newEmptyHeaderField("etag"),
        newEmptyHeaderField("if-modified-since"),
        newEmptyHeaderField("if-none-match"),
        newEmptyHeaderField("last-modified"),
        newEmptyHeaderField("link"),
        newEmptyHeaderField("location"),
        newEmptyHeaderField("referer"),
        newEmptyHeaderField("set-cookie"),
        newHeaderField(":method", "CONNECT"),
        newHeaderField(":method", "DELETE"),
        newHeaderField(":method", "GET"),
        newHeaderField(":method", "HEAD"),
        newHeaderField(":method", "OPTIONS"),
        newHeaderField(":method", "POST"),
        newHeaderField(":method", "PUT"),
        newHeaderField(":scheme", "http"),
        newHeaderField(":scheme", "https"),
        newHeaderField(":status", "103"),
        newHeaderField(":status", "200"),
        newHeaderField(":status", "304"),
        newHeaderField(":status", "404"),
        newHeaderField(":status", "503"),
        newHeaderField("accept", "*/*"),
        newHeaderField("accept", "application/dns-message"),
        newHeaderField("accept-encoding", "gzip, deflate, br"),
        newHeaderField("accept-ranges", "bytes"),
        newHeaderField("access-control-allow-headers", "cache-control"),
        newHeaderField("access-control-allow-headers", "content-type"),
        newHeaderField("access-control-allow-origin", "*"),
        newHeaderField("cache-control", "max-age=0"),
        newHeaderField("cache-control", "max-age=2592000"),
        newHeaderField("cache-control", "max-age=604800"),
        newHeaderField("cache-control", "no-cache"),
        newHeaderField("cache-control", "no-store"),
        newHeaderField("cache-control", "public, max-age=31536000"),
        newHeaderField("content-encoding", "br"),
        newHeaderField("content-encoding", "gzip"),
        newHeaderField("content-type", "application/dns-message"),
        newHeaderField("content-type", "application/javascript"),
        newHeaderField("content-type", "application/json"),
        newHeaderField("content-type", "application/x-www-form-urlencoded"),
        newHeaderField("content-type", "image/gif"),
        newHeaderField("content-type", "image/jpeg"),
        newHeaderField("content-type", "image/png"),
        newHeaderField("content-type", "text/css"),
        newHeaderField("content-type", "text/html;charset=utf-8"),
        newHeaderField("content-type", "text/plain"),
        newHeaderField("content-type", "text/plain;charset=utf-8"),
        newHeaderField("range", "bytes=0-"),
        newHeaderField("strict-transport-security", "max-age=31536000"),
        newHeaderField("strict-transport-security", "max-age=31536000;includesubdomains"),
        newHeaderField("strict-transport-security", "max-age=31536000;includesubdomains;preload"),
        newHeaderField("vary", "accept-encoding"),
        newHeaderField("vary", "origin"),
        newHeaderField("x-content-type-options", "nosniff"),
        newHeaderField("x-xss-protection", "1; mode=block"),
        newHeaderField(":status", "100"),
        newHeaderField(":status", "204"),
        newHeaderField(":status", "206"),
        newHeaderField(":status", "302"),
        newHeaderField(":status", "400"),
        newHeaderField(":status", "403"),
        newHeaderField(":status", "421"),
        newHeaderField(":status", "425"),
        newHeaderField(":status", "500"),
        newEmptyHeaderField("accept-language"),
        newHeaderField("access-control-allow-credentials", "FALSE"),
        newHeaderField("access-control-allow-credentials", "TRUE"),
        newHeaderField("access-control-allow-headers", "*"),
        newHeaderField("access-control-allow-methods", "get"),
        newHeaderField("access-control-allow-methods", "get, post, options"),
        newHeaderField("access-control-allow-methods", "options"),
        newHeaderField("access-control-expose-headers", "content-length"),
        newHeaderField("access-control-request-headers", "content-type"),
        newHeaderField("access-control-request-method", "get"),
        newHeaderField("access-control-request-method", "post"),
        newHeaderField("alt-svc", "clear"),
        newEmptyHeaderField("authorization"),
        newHeaderField("content-security-policy", "script-src 'none';object-src 'none';base-uri 'none'"),
        newHeaderField("early-data", "1"),
        newEmptyHeaderField("expect-ct"),
        newEmptyHeaderField("forwarded"),
        newEmptyHeaderField("if-range"),
        newEmptyHeaderField("origin"),
        newHeaderField("purpose", "prefetch"),
        newEmptyHeaderField("server"),
        newHeaderField("timing-allow-origin", "*"),
        newHeaderField("upgrade-insecure-requests", "1"),
        newEmptyHeaderField("user-agent"),
        newEmptyHeaderField("x-forwarded-for"),
        newHeaderField("x-frame-options", "deny"),
        newHeaderField("x-frame-options", "sameorigin"));

    /**
     * The number of header fields in the static table.
     * <p>静态表条目总数，供编解码器校验索引范围。</p>
     */
    static final int length = STATIC_TABLE.size();

    /** 头名 → 该名在静态表中所有索引的列表（同名多条时按表顺序排列）。 */
    private static final CharSequenceMap<List<Integer>> STATIC_INDEX_BY_NAME = createMap(length);

    /** 仅含头名、值为空的静态表项（如 {@code cookie} 只有名无预设值）。 */
    private static QpackHeaderField newEmptyHeaderField(String name) {
        return new QpackHeaderField(AsciiString.cached(name), AsciiString.EMPTY_STRING);
    }

    private static QpackHeaderField newHeaderField(String name, String value) {
        return new QpackHeaderField(AsciiString.cached(name), AsciiString.cached(value));
    }

    /**
     * Return the header field at the given index value.
     * Note that QPACK uses 0-based indexing when HPACK is using 1-based.
     * <p>按 0 基索引取静态表项，解码器解析索引引用时调用。</p>
     */
    static QpackHeaderField getField(int index) {
        return STATIC_TABLE.get(index);
    }

    /**
     * Returns the lowest index value for the given header field name in the static
     * table. Returns -1 if the header field name is not in the static table.
     * <p>返回该头名在静态表中的最小索引，用于「仅索引头名」编码。</p>
     */
    static int getIndex(CharSequence name) {
        List<Integer> index = STATIC_INDEX_BY_NAME.get(name);
        if (index == null) {
            return NOT_FOUND;
        }

        return index.get(0);
    }

    /**
     * Returns:
     *    a) the index value for the given header field in the static table (when found);
     *    b) the index value for a given name with a single bit masked (no exact match);
     *    c) -1 if name was not found in the static table.
     * <p>编码器首选入口：先尝试名+值精确匹配；否则返回带 {@link #MASK_NAME_REF} 的名索引。</p>
     */
    static int findFieldIndex(CharSequence name, CharSequence value) {
        final List<Integer> nameIndex = STATIC_INDEX_BY_NAME.get(name);

        // 头名不在静态表中，无法引用。
        if (nameIndex == null) {
            return NOT_FOUND;
        }

        // 遍历同名所有条目，用变长时比较（静态表查找无需防时序侧信道）。
        for (int index: nameIndex) {
            QpackHeaderField field = STATIC_TABLE.get(index);
            if (QpackUtil.equalsVariableTime(value, field.value)) {
                return index;
            }
        }

        // 值不匹配时仍可引用头名，掩码标记「非完整静态表项」。
        return nameIndex.get(0) | MASK_NAME_REF;
    }

    /**
     * Creates a map CharSequenceMap header name to index value to allow quick lookup.
     * <p>构建头名到索引列表的反向索引，启动时 O(n) 完成，后续查找 O(1) 取列表。</p>
     */
    @SuppressWarnings("unchecked")
    private static CharSequenceMap<List<Integer>> createMap(int length) {
        CharSequenceMap<List<Integer>> mapping =
            new CharSequenceMap<List<Integer>>(true, UnsupportedValueConverter.<List<Integer>>instance(), length);
        for (int index = 0; index < length; index++) {
            final QpackHeaderField field = getField(index);
            final List<Integer> cursor = mapping.get(field.name);
            if (cursor == null) {
                final List<Integer> holder = new ArrayList<>(16);
                holder.add(index);
                mapping.set(field.name, holder);
            } else {
                cursor.add(index);
            }
        }
        return mapping;
    }

    private QpackStaticTable() {
    }
}
