/*
 * Copyright 2025 The Netty Project
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

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * HTTP/3 SETTINGS 帧中标准设置项的 IANA 注册标识符枚举。
 * <p>控制流上交换的键值对协商 QPACK 动态表容量、字段块上限、CONNECT 扩展等连接级能力；
 * {@link #fromId(long)} 用于解码时将原始 id 映射回类型安全枚举。
 */
public enum Http3SettingIdentifier {

    /**
     * QPACK maximum table capacity setting identifier (<b>0x1</b>).
     * <p>
     * Defined in <a href="https://datatracker.ietf.org/doc/html/rfc9204#section-5">
     * RFC 9204, Section 5 (SETTINGS_QPACK_MAX_TABLE_CAPACITY)</a> and registered in
     * the <a href="https://www.iana.org/assignments/http3-parameters/http3-parameters.xhtml#settings">
     * HTTP/3 SETTINGS registry (IANA)</a>.
     * <br>
     * Controls the maximum size of the dynamic table used by QPACK.
     * <p>对端 QPACK 动态表允许的最大字节数；编码器不得超出此容量插入新条目。
     */
    HTTP3_SETTINGS_QPACK_MAX_TABLE_CAPACITY(0x1),

    /**
     * Maximum field section size setting identifier (<b>0x6</b>).
     * <p>
     * Defined in <a href="https://datatracker.ietf.org/doc/html/rfc9114#section-7.2.4.1">
     * RFC 9114, Section 7.2.4.1 (SETTINGS_MAX_FIELD_SECTION_SIZE)</a> , also referenced
     * in the <a href="https://datatracker.ietf.org/doc/html/rfc9114#section-7.2.4.1">
     * HTTP/3 SETTINGS registry (RFC 9114, Section 7.2.4.1)</a> and registered in
     * the <a href="https://www.iana.org/assignments/http3-parameters/http3-parameters.xhtml#settings">
     * HTTP/3 SETTINGS registry (IANA)</a>.
     * <br>
     * Specifies the upper bound on the total size of HTTP field sections accepted by a peer.
     * <p>单条 HEADERS（含 QPACK 编码开销）对端可接受的最大字节数。
     */
    HTTP3_SETTINGS_MAX_FIELD_SECTION_SIZE(0x6),

    /**
     * QPACK blocked streams setting identifier (<b>0x7</b>).
     * <p>
     * Defined in <a href="https://datatracker.ietf.org/doc/html/rfc9204#section-5">
     * RFC 9204, Section 5 (SETTINGS_QPACK_BLOCKED_STREAMS)</a> and registered in
     * the <a href="https://www.iana.org/assignments/http3-parameters/http3-parameters.xhtml#settings">
     * HTTP/3 SETTINGS registry (IANA)</a>.
     * <br>
     * Indicates the maximum number of streams that can be blocked waiting for QPACK instructions.
     * <p>等待 QPACK 解码器指令而阻塞的最大请求流数量。
     */
    HTTP3_SETTINGS_QPACK_BLOCKED_STREAMS(0x7),

    /**
     * ENABLE_CONNECT_PROTOCOL setting identifier (<b>0x8</b>).
     * <p>
     * Defined and registered in <a href="https://datatracker.ietf.org/doc/html/rfc9220#section-5">
     * RFC 9220, Section 5 (IANA Considerations)</a> and registered in
     * the <a href="https://www.iana.org/assignments/http3-parameters/http3-parameters.xhtml#settings">
     * HTTP/3 SETTINGS registry (IANA)</a>.
     * <br>
     * Enables use of the CONNECT protocol in HTTP/3 when set to 1; disabled when 0.
     * <p>为 1 时允许在 HTTP/3 上使用 CONNECT 方法建立隧道（如 WebTransport 前置）。
     */
    HTTP3_SETTINGS_ENABLE_CONNECT_PROTOCOL(0x8),

    /**
     * ENABLE_H3_DATAGRAM setting identifier (<b>0x8</b>).
     * <p>
     * Defined and registered in <a href="https://datatracker.ietf.org/doc/html/rfc9297#name-http-3-setting">
     * RFC 9220, Section 5 (IANA Considerations)</a> and registered in
     * the <a href="https://www.iana.org/assignments/http3-parameters/http3-parameters.xhtml#settings">
     * HTTP/3 SETTINGS registry (IANA)</a>.
     * <br>
     * Enables use of the CONNECT protocol in HTTP/3 when set to 1; disabled when 0.
     * <p>为 1 时启用 HTTP/3 Datagram 扩展，可在 QUIC DATAGRAM 帧上承载应用数据。
     */
    HTTP3_SETTINGS_H3_DATAGRAM(0x33);

    private final long id;

    /** id → 枚举常量，供 {@link #fromId(long)} O(1) 查找。 */
    private static final Map<Long, Http3SettingIdentifier> LOOKUP = Collections.unmodifiableMap(
        Arrays.stream(values()).collect(Collectors.toMap(Http3SettingIdentifier::id, Function.identity()))
    );

    Http3SettingIdentifier(long id) {
        this.id = id;
    }

    /**
     * Returns the Identifier of {@link Http3SettingIdentifier}
     * for example:
     * SETTINGS_QPACK_MAX_TABLE_CAPACITY = 0x1 = 1 in the settings frame
     * <br>
     * @return long(represented as hexadecimal above) value of the Identifier
     */
    public long id() {
        return id;
    }

    /**
     * Returns {@link Http3SettingIdentifier}
     * @param id
     * @return {@link Http3SettingIdentifier} enum which represents @param id, null otherwise
     */
    @Nullable
    public static Http3SettingIdentifier fromId(long id) {
        return LOOKUP.get(id);
    }
}
