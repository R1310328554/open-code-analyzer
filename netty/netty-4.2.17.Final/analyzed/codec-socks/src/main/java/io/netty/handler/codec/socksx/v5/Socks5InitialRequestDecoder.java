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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder.State;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * Decodes a single {@link Socks5InitialRequest} from the inbound {@link ByteBuf}s.
 * On successful decode, this decoder will forward the received data to the next handler, so that
 * other handler can remove or replace this decoder later.  On failed decode, this decoder will
 * discard the received data, so that other handler closes the connection later.
 *
 * <p>SOCKS5 服务端侧解码器：解析客户端方法协商请求（RFC 1928 第 3 节）。
 * 报文格式为 VER(5) + NMETHODS + METHODS[]。解码成功后进入 SUCCESS 状态透传后续字节；
 * 失败时产出带 {@link DecoderResult#failure} 的占位请求并丢弃剩余输入。</p>
 */
public class Socks5InitialRequestDecoder extends ReplayingDecoder<State> {

    /** 解码状态机：INIT 解析协商头，SUCCESS 透传隧道数据，FAILURE 丢弃无效输入。 */
    @UnstableApi
    public enum State {
        INIT,
        SUCCESS,
        FAILURE
    }

    public Socks5InitialRequestDecoder() {
        super(State.INIT);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            switch (state()) {
            case INIT: {
                // 版本号必须为 SOCKS5 (0x05)
                final byte version = in.readByte();
                if (version != SocksVersion.SOCKS5.byteValue()) {
                    throw new DecoderException(
                            "unsupported version: " + version + " (expected: " + SocksVersion.SOCKS5.byteValue() + ')');
                }

                // 客户端支持的认证方法个数及列表
                final int authMethodCnt = in.readUnsignedByte();

                final Socks5AuthMethod[] authMethods = new Socks5AuthMethod[authMethodCnt];
                for (int i = 0; i < authMethodCnt; i++) {
                    authMethods[i] = Socks5AuthMethod.valueOf(in.readByte());
                }

                out.add(new DefaultSocks5InitialRequest(authMethods));
                checkpoint(State.SUCCESS);
            }
            case SUCCESS: {
                // 协商报文之后的字节保留引用计数后上抛，便于后续 handler 处理
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

    /** 构造失败占位请求并切换到 FAILURE，供上层根据 {@link DecoderResult} 关闭连接。 */
    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }

        checkpoint(State.FAILURE);

        Socks5Message m = new DefaultSocks5InitialRequest(Socks5AuthMethod.NO_AUTH);
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
    }
}
