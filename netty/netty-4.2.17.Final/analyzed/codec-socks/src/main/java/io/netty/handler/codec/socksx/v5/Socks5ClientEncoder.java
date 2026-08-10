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

import java.util.List;
import java.util.RandomAccess;

/**
 * Encodes a client-side {@link Socks5Message} into a {@link ByteBuf}.
 *
 * <p>客户端 SOCKS5 出站编码器：方法协商、用户名/密码、私有认证及命令请求。
 * 单字节长度字段上限 255（RFC 1928/1929）。{@link Sharable} 可在多 Channel 复用。</p>
 */
@Sharable
public class Socks5ClientEncoder extends MessageToByteEncoder<Socks5Message> {

    public static final Socks5ClientEncoder DEFAULT = new Socks5ClientEncoder();

    private final Socks5AddressEncoder addressEncoder;

    /**
     * Creates a new instance with the default {@link Socks5AddressEncoder}.
     */
    protected Socks5ClientEncoder() {
        this(Socks5AddressEncoder.DEFAULT);
    }

    /**
     * Creates a new instance with the specified {@link Socks5AddressEncoder}.
     */
    public Socks5ClientEncoder(Socks5AddressEncoder addressEncoder) {
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
        if (msg instanceof Socks5InitialRequest) {
            encodeAuthMethodRequest((Socks5InitialRequest) msg, out);
        } else if (msg instanceof Socks5PasswordAuthRequest) {
            encodePasswordAuthRequest((Socks5PasswordAuthRequest) msg, out);
        } else if (msg instanceof Socks5PrivateAuthRequest) {
            encodePrivateAuthRequest((Socks5PrivateAuthRequest) msg, out);
        } else if (msg instanceof Socks5CommandRequest) {
            encodeCommandRequest((Socks5CommandRequest) msg, out);
        } else {
            throw new EncoderException("unsupported message type: " + StringUtil.simpleClassName(msg));
        }
    }

    /** VER + NMETHOD + METHODS 列表。 */
    private static void encodeAuthMethodRequest(Socks5InitialRequest msg, ByteBuf out) {
        out.writeByte(msg.version().byteValue());

        final List<Socks5AuthMethod> authMethods = msg.authMethods();
        final int numAuthMethods = authMethods.size();
        writeFieldLength(out, numAuthMethods);

        if (authMethods instanceof RandomAccess) {
            for (int i = 0; i < numAuthMethods; i ++) {
                out.writeByte(authMethods.get(i).byteValue());
            }
        } else {
            for (Socks5AuthMethod a: authMethods) {
                out.writeByte(a.byteValue());
            }
        }
    }

    /** RFC 1929：VER(1) + ULEN + UNAME + PLEN + PASSWD。 */
    private static void encodePasswordAuthRequest(Socks5PasswordAuthRequest msg, ByteBuf out) {
        out.writeByte(0x01);

        final String username = msg.username();
        writeFieldLength(out, username.length());
        ByteBufUtil.writeAscii(out, username);

        final String password = msg.password();
        writeFieldLength(out, password.length());
        ByteBufUtil.writeAscii(out, password);
    }

    /** 私有认证：VER(1) + LEN + TOKEN。 */
    private static void encodePrivateAuthRequest(Socks5PrivateAuthRequest msg, ByteBuf out) {
        byte[] bytes = msg.privateToken();
        out.writeByte(0x01);
        writeFieldLength(out, bytes.length);
        out.writeBytes(bytes);
    }

    /** VER + CMD + RSV(0) + ATYP + DST.ADDR + DST.PORT。 */
    private void encodeCommandRequest(Socks5CommandRequest msg, ByteBuf out) throws Exception {
        out.writeByte(msg.version().byteValue());
        out.writeByte(msg.type().byteValue());
        out.writeByte(0x00);

        final Socks5AddressType dstAddrType = msg.dstAddrType();
        out.writeByte(dstAddrType.byteValue());
        String addrValue = msg.dstAddr();
        if (addrValue != null && dstAddrType == Socks5AddressType.DOMAIN) {
            checkFieldLength(addrValue.length());
        }
        addressEncoder.encodeAddress(dstAddrType, addrValue, out);
        ByteBufUtil.writeShortBE(out, msg.dstPort());
    }

    private static void writeFieldLength(ByteBuf out, int length) {
        checkFieldLength(length);
        out.writeByte(length);
    }

    /** SOCKS5 单字节长度字段有效范围 0–255。 */
    private static void checkFieldLength(int length) {
        if (length > 255 || length < 0) {
            throw new EncoderException("Invalid field length value: " + length);
        }
    }
}
