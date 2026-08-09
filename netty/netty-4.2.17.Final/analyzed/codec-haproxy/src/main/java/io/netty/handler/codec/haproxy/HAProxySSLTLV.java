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

package io.netty.handler.codec.haproxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;

import java.util.Collections;
import java.util.List;

/**
 * 类型为 {@link HAProxyTLV.Type#PP2_TYPE_SSL} 的 TLV，封装 SSL 相关信息。
 * <p>
 * 含客户端位域、验证结果及嵌套子 TLV（ALPN、版本、CN 等）。
 */
public final class HAProxySSLTLV extends HAProxyTLV {

    private final int verify;
    private final List<HAProxyTLV> tlvs;
    private final byte clientBitField;

    /**
     * 创建 SSL TLV（空 rawContent）。
     *
     * @param verify the verification result as defined in the specification for the pp2_tlv_ssl struct (see
     * https://www.haproxy.org/download/1.8/doc/proxy-protocol.txt)
     * @param clientBitField the bitfield with client information
     * @param tlvs the encapsulated {@link HAProxyTLV}s
     */
    public HAProxySSLTLV(final int verify, final byte clientBitField, final List<HAProxyTLV> tlvs) {
        this(verify, clientBitField, tlvs, Unpooled.EMPTY_BUFFER);
    }

    /**
     * 创建 SSL TLV 并保留原始内容缓冲。
     *
     * @param verify the verification result as defined in the specification for the pp2_tlv_ssl struct (see
     * https://www.haproxy.org/download/1.8/doc/proxy-protocol.txt)
     * @param clientBitField the bitfield with client information
     * @param tlvs the encapsulated {@link HAProxyTLV}s
     * @param rawContent the raw TLV content
     */
    HAProxySSLTLV(final int verify, final byte clientBitField, final List<HAProxyTLV> tlvs, final ByteBuf rawContent) {
        super(Type.PP2_TYPE_SSL, (byte) 0x20, rawContent);

        this.verify = verify;
        this.tlvs = Collections.unmodifiableList(tlvs);
        this.clientBitField = clientBitField;
    }

    /** 客户端位域是否设置了 PP2_CLIENT_CERT_CONN。 */
    public boolean isPP2ClientCertConn() {
        return (clientBitField & 0x2) != 0;
    }

    /** 客户端位域是否设置了 PP2_CLIENT_SSL。 */
    public boolean isPP2ClientSSL() {
        return (clientBitField & 0x1) != 0;
    }

    /** 客户端位域是否设置了 PP2_CLIENT_CERT_SESS。 */
    public boolean isPP2ClientCertSess() {
        return (clientBitField & 0x4) != 0;
    }

    /** 返回客户端信息位域。 */
    public byte client() {
        return clientBitField;
    }

    /** 返回 SSL 验证结果（pp2_tlv_ssl.verify）。 */
    public int verify() {
        return verify;
    }

    /** 返回不可修改的嵌套 {@link HAProxyTLV} 列表。 */
    public List<HAProxyTLV> encapsulatedTLVs() {
        return tlvs;
    }

    @Override
    int contentNumBytes() {
        int tlvNumBytes = 0;
        for (int i = 0; i < tlvs.size(); i++) {
            tlvNumBytes += tlvs.get(i).totalNumBytes();
        }
        return 5 + tlvNumBytes; // client 1 字节 + verify 4 字节 + 子 TLV
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) +
               "(type: " + type() +
               ", typeByteValue: " + typeByteValue() +
               ", client: " + client() +
               ", verify: " + verify() +
               ", numEncapsulatedTlvs: " + tlvs.size() + ')';
    }
}
