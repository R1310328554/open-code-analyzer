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

package io.netty.handler.codec.socksx.v4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AsciiString;
import io.netty.util.NetUtil;

/**
 * Encodes a {@link Socks4CommandRequest} into a {@link ByteBuf}.
 *
 * <p>将 {@link Socks4CommandRequest} 编码为 SOCKS4/4a 线格式。
 * 目标为合法 IPv4 时直接写入 4 字节地址；否则使用 SOCKS4a 扩展：
 * DSTIP 填 {@code 0.0.0.x} 占位符，USERID 后以 NUL 分隔再跟域名。</p>
 */
@Sharable
public final class Socks4ClientEncoder extends MessageToByteEncoder<Socks4CommandRequest> {

    /**
     * The singleton instance of {@link Socks4ClientEncoder}
     */
    public static final Socks4ClientEncoder INSTANCE = new Socks4ClientEncoder();

    /** SOCKS4a 域名模式标记：DSTIP 为 0.0.0.1 表示后续 USERID 之后携带域名。 */
    private static final byte[] IPv4_DOMAIN_MARKER = {0x00, 0x00, 0x00, 0x01};

    private Socks4ClientEncoder() {
        super(Socks4CommandRequest.class);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Socks4CommandRequest msg, ByteBuf out) throws Exception {
        out.writeByte(msg.version().byteValue());
        out.writeByte(msg.type().byteValue());
        ByteBufUtil.writeShortBE(out, msg.dstPort());
        if (NetUtil.isValidIpV4Address(msg.dstAddr())) {
            // 标准 SOCKS4：DSTIP 为真实 IPv4，USERID 以 NUL 结尾
            out.writeBytes(NetUtil.createByteArrayFromIpAddressString(msg.dstAddr()));
            ByteBufUtil.writeAscii(out, sanitize("userId", msg.userId()));
            out.writeByte(0);
        } else {
            // SOCKS4a：占位 DSTIP + USERID + NUL + 域名 + NUL
            out.writeBytes(IPv4_DOMAIN_MARKER);
            ByteBufUtil.writeAscii(out, sanitize("userId", msg.userId()));
            out.writeByte(0);
            ByteBufUtil.writeAscii(out, sanitize("dstAddr", msg.dstAddr()));
            out.writeByte(0);
        }
    }

    /**
     * 校验字符串字段不含 NUL——SOCKS4 以 0x00 作为字段分隔符，嵌入 NUL 会破坏报文边界。
     */
    private CharSequence sanitize(String fieldName, String strValue) {
        for (int i = 0, len = strValue.length(); i < len; i++) {
            char c = strValue.charAt(i);
            // SOCKS4 uses NUL-bytes as field delimiters.
            if (AsciiString.c2b(c) == 0) {
                throw new EncoderException("Illegal character in " + fieldName + " field.");
            }
        }
        return strValue;
    }
}
