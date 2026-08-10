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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder.State;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * Decodes a single {@link Socks5CommandRequest} from the inbound {@link ByteBuf}s.
 * On successful decode, this decoder will forward the received data to the next handler, so that
 * other handler can remove or replace this decoder later.  On failed decode, this decoder will
 * discard the received data, so that other handler closes the connection later.
 *
 * <p>从入站缓冲解码单条 SOCKS5 命令请求。成功后进入 SUCCESS 状态并将后续字节透传；
 * 失败时产出带 {@link DecoderResult#failure(Throwable)} 的占位消息并丢弃剩余数据。</p>
 */
public class Socks5CommandRequestDecoder extends ReplayingDecoder<State> {

    @UnstableApi
    public enum State {
        /** 等待完整命令请求帧。 */
        INIT,
        /** 首帧已解码，透传隧道数据。 */
        SUCCESS,
        /** 解码失败，丢弃入站字节。 */
        FAILURE
    }

    private final Socks5AddressDecoder addressDecoder;

    public Socks5CommandRequestDecoder() {
        this(Socks5AddressDecoder.DEFAULT);
    }

    public Socks5CommandRequestDecoder(Socks5AddressDecoder addressDecoder) {
        super(State.INIT);
        this.addressDecoder = ObjectUtil.checkNotNull(addressDecoder, "addressDecoder");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            switch (state()) {
            case INIT: {
                final byte version = in.readByte();
                if (version != SocksVersion.SOCKS5.byteValue()) {
                    throw new DecoderException(
                            "unsupported version: " + version + " (expected: " + SocksVersion.SOCKS5.byteValue() + ')');
                }

                final Socks5CommandType type = Socks5CommandType.valueOf(in.readByte());
                in.skipBytes(1); // RSV
                final Socks5AddressType dstAddrType = Socks5AddressType.valueOf(in.readByte());
                final String dstAddr = addressDecoder.decodeAddress(dstAddrType, in);
                final int dstPort = ByteBufUtil.readUnsignedShortBE(in);

                out.add(new DefaultSocks5CommandRequest(type, dstAddrType, dstAddr, dstPort));
                checkpoint(State.SUCCESS);
            }
            case SUCCESS: {
                // 代理隧道建立后，后续应用数据原样上送
                int readableBytes = actualReadableBytes();
                if (readableBytes > 0) {
                    out.add(in.readRetainedSlice(readableBytes));
                }
                break;
            }
            case FAILURE: {
                in.skipBytes(actualReadableBytes());
                break;
            }
            }
        } catch (Exception e) {
            fail(out, e);
        }
    }

    /** 构造失败占位请求并转入 FAILURE，避免反复抛异常。 */
    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }

        checkpoint(State.FAILURE);

        Socks5Message m = new DefaultSocks5CommandRequest(
                Socks5CommandType.CONNECT, Socks5AddressType.IPv4, "0.0.0.0", 1);
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
    }
}
