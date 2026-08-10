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
package io.netty.handler.codec.socksx.v4;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.NetUtil;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * The default {@link Socks4CommandResponse}.
 *
 * <p>SOCKS4/4a 命令响应的默认不可变实现。
 * 成功时 {@code dstAddr}/{@code dstPort} 携带绑定地址（BIND 场景）或占位零地址；
 * 失败响应通常仅含 {@link Socks4CommandStatus}，地址字段为 null/0。</p>
 */
public class DefaultSocks4CommandResponse extends AbstractSocks4Message implements Socks4CommandResponse {

    /** 响应状态码（CD 字段，如 0x5a 表示 granted）。 */
    private final Socks4CommandStatus status;
    /** 绑定 IPv4 地址；null 表示响应未携带 DSTIP。 */
    private final String dstAddr;
    /** 绑定端口；与 {@code dstAddr == null} 时通常为 0。 */
    private final int dstPort;

    /**
     * Creates a new instance.
     *
     * @param status the status of the response
     */
    public DefaultSocks4CommandResponse(Socks4CommandStatus status) {
        this(status, null, 0);
    }

    /**
     * Creates a new instance.
     *
     * @param status the status of the response
     * @param dstAddr the {@code DSTIP} field of the response
     * @param dstPort the {@code DSTPORT} field of the response
     */
    public DefaultSocks4CommandResponse(Socks4CommandStatus status, String dstAddr, int dstPort) {
        // 非 null 地址必须是合法 IPv4 字面量（SOCKS4 不支持域名响应）
        if (dstAddr != null) {
            if (!NetUtil.isValidIpV4Address(dstAddr)) {
                throw new IllegalArgumentException(
                        "dstAddr: " + dstAddr + " (expected: a valid IPv4 address)");
            }
        }
        // 端口允许 0（表示未指定），上限 65535
        if (dstPort < 0 || dstPort > 65535) {
            throw new IllegalArgumentException("dstPort: " + dstPort + " (expected: 0~65535)");
        }

        this.status = ObjectUtil.checkNotNull(status, "cmdStatus");
        this.dstAddr = dstAddr;
        this.dstPort = dstPort;
    }

    @Override
    public Socks4CommandStatus status() {
        return status;
    }

    @Override
    public String dstAddr() {
        return dstAddr;
    }

    @Override
    public int dstPort() {
        return dstPort;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(96);
        buf.append(StringUtil.simpleClassName(this));

        DecoderResult decoderResult = decoderResult();
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", dstAddr: ");
        } else {
            buf.append("(dstAddr: ");
        }
        buf.append(dstAddr());
        buf.append(", dstPort: ");
        buf.append(dstPort());
        buf.append(')');

        return buf.toString();
    }
}
