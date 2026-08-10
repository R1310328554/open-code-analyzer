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

package io.netty.handler.codec.socksx.v5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * Encodes a server-side {@link Socks5Message} into a {@link ByteBuf}.
 *
 * <p>SOCKS5 服务端编码器：将 {@link Socks5InitialResponse}、{@link Socks5PasswordAuthResponse}、
 * {@link Socks5CommandResponse}、{@link Socks5PrivateAuthResponse} 等应答消息序列化为字节流。
 * {@link Sharable} 可安全共享；命令应答中的绑定地址通过可插拔 {@link Socks5AddressEncoder} 编码。</p>
 */
@Sharable
public class Socks5ServerEncoder extends MessageToByteEncoder<Socks5Message> {

    /** 使用默认地址编码器的单例实例。 */
    public static final Socks5ServerEncoder DEFAULT = new Socks5ServerEncoder(Socks5AddressEncoder.DEFAULT);

    private final Socks5AddressEncoder addressEncoder;

    /**
     * Creates a new instance with the default {@link Socks5AddressEncoder}.
     */
    protected Socks5ServerEncoder() {
        this(Socks5AddressEncoder.DEFAULT);
    }

    /**
     * Creates a new instance with the specified {@link Socks5AddressEncoder}.
     */
    public Socks5ServerEncoder(Socks5AddressEncoder addressEncoder) {
        super(Socks5Message.class);
        this.addressEncoder = ObjectUtil.checkNotNull(addressEncoder, "addressEncoder");
    }

    /**
     * Returns the {@link Socks5AddressEncoder} of this encoder.
     */
    protected final Socks5AddressEncoder addressEncoder() {
        return addressEncoder;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Socks5Message msg, ByteBuf out) throws Exception {
        if (msg instanceof Socks5InitialResponse) {
            encodeAuthMethodResponse((Socks5InitialResponse) msg, out);
        } else if (msg instanceof Socks5PasswordAuthResponse) {
            encodePasswordAuthResponse((Socks5PasswordAuthResponse) msg, out);
        } else if (msg instanceof Socks5CommandResponse) {
            encodeCommandResponse((Socks5CommandResponse) msg, out);
        } else if (msg instanceof Socks5PrivateAuthResponse) {
            encodePrivateAuthResponse((Socks5PrivateAuthResponse) msg, out);
        } else {
            throw new EncoderException("unsupported message type: " + StringUtil.simpleClassName(msg));
        }
    }

    /** 编码方法协商应答：VER + METHOD。 */
    private static void encodeAuthMethodResponse(Socks5InitialResponse msg, ByteBuf out) {
        out.writeByte(msg.version().byteValue());
        out.writeByte(msg.authMethod().byteValue());
    }

    /** 编码用户名/密码子协商应答：VER(1) + STATUS。 */
    private static void encodePasswordAuthResponse(Socks5PasswordAuthResponse msg, ByteBuf out) {
        out.writeByte(0x01);
        out.writeByte(msg.status().byteValue());
    }

    /** 编码私有认证子协商应答：VER(1) + STATUS。 */
    private static void encodePrivateAuthResponse(Socks5PrivateAuthResponse msg, ByteBuf out) {
        out.writeByte(0x01);
        out.writeByte(msg.status().byteValue());
    }

    /** 编码命令应答：VER + STATUS + RSV(0) + ATYP + BND.ADDR + BND.PORT。 */
    private void encodeCommandResponse(Socks5CommandResponse msg, ByteBuf out) throws Exception {
        out.writeByte(msg.version().byteValue());
        out.writeByte(msg.status().byteValue());
        out.writeByte(0x00);

        final Socks5AddressType bndAddrType = msg.bndAddrType();
        out.writeByte(bndAddrType.byteValue());
        String addrValue = msg.bndAddr();
        if (addrValue != null && bndAddrType == Socks5AddressType.DOMAIN) {
            checkFieldLength(addrValue.length());
        }
        addressEncoder.encodeAddress(bndAddrType, addrValue, out);

        ByteBufUtil.writeShortBE(out, msg.bndPort());
    }

    /** 校验单字节长度前缀字段（域名长度等）是否在 0–255 范围内。 */
    private static void checkFieldLength(int length) {
        if (length > 255 || length < 0) {
            throw new EncoderException("Invalid field length value: " + length);
        }
    }
}
