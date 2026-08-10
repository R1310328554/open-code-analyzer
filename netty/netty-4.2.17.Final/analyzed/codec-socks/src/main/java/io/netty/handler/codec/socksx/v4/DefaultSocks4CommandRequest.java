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
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.net.IDN;

/**
 * The default {@link Socks4CommandRequest}.
 *
 * <p>SOCKS4/4a CONNECT/BIND 命令请求的默认不可变实现。
 * 线格式字段：VN(4) | CD | DSTPORT | DSTIP | USERID | NULL；
 * {@code dstAddr} 可为 IPv4 字面量或域名（SOCKS4a 扩展，DSTIP 为 0.0.0.x）。</p>
 */
public class DefaultSocks4CommandRequest extends AbstractSocks4Message implements Socks4CommandRequest {

    /** 命令码：CONNECT(1) 或 BIND(2)。 */
    private final Socks4CommandType type;
    /** 目标地址；构造时经 {@link IDN#toASCII(String)} 规范化国际化域名。 */
    private final String dstAddr;
    /** 目标端口，有效范围 1~65535。 */
    private final int dstPort;
    /** USERID 字段，以 NUL 结尾；可为空串。 */
    private final String userId;

    /**
     * Creates a new instance.
     *
     * @param type the type of the request
     * @param dstAddr the {@code DSTIP} field of the request
     * @param dstPort the {@code DSTPORT} field of the request
     */
    public DefaultSocks4CommandRequest(Socks4CommandType type, String dstAddr, int dstPort) {
        this(type, dstAddr, dstPort, "");
    }

    /**
     * Creates a new instance.
     *
     * @param type the type of the request
     * @param dstAddr the {@code DSTIP} field of the request
     * @param dstPort the {@code DSTPORT} field of the request
     * @param userId the {@code USERID} field of the request
     */
    public DefaultSocks4CommandRequest(Socks4CommandType type, String dstAddr, int dstPort, String userId) {
        // SOCKS4 端口为 16 位无符号整数，0 保留
        if (dstPort <= 0 || dstPort >= 65536) {
            throw new IllegalArgumentException("dstPort: " + dstPort + " (expected: 1~65535)");
        }
        this.type = ObjectUtil.checkNotNull(type, "type");
        this.dstAddr = IDN.toASCII(
                ObjectUtil.checkNotNull(dstAddr, "dstAddr"));
        this.userId = ObjectUtil.checkNotNull(userId, "userId");
        this.dstPort = dstPort;
    }

    @Override
    public Socks4CommandType type() {
        return type;
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
    public String userId() {
        return userId;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append(StringUtil.simpleClassName(this));

        DecoderResult decoderResult = decoderResult();
        // 解码失败时在 toString 中附带 DecoderResult，便于日志排查
        if (!decoderResult.isSuccess()) {
            buf.append("(decoderResult: ");
            buf.append(decoderResult);
            buf.append(", type: ");
        } else {
            buf.append("(type: ");
        }
        buf.append(type());
        buf.append(", dstAddr: ");
        buf.append(dstAddr());
        buf.append(", dstPort: ");
        buf.append(dstPort());
        buf.append(", userId: ");
        buf.append(userId());
        buf.append(')');

        return buf.toString();
    }
}
