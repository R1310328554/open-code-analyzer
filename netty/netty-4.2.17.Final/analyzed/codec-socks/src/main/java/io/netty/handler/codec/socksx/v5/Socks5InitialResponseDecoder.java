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
import io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder.State;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * Decodes a single {@link Socks5InitialResponse} from the inbound {@link ByteBuf}s.
 * On successful decode, this decoder will forward the received data to the next handler, so that
 * other handler can remove or replace this decoder later.  On failed decode, this decoder will
 * discard the received data, so that other handler closes the connection later.
 *
 * <p>SOCKS5 客户端侧解码器：解析服务端方法协商应答（RFC 1928 第 3 节）。
 * 报文固定 2 字节：VER(5) + METHOD。解码成功后透传后续字节；失败时产出
 * {@link Socks5AuthMethod#UNACCEPTED} 占位响应。</p>
 */
public class Socks5InitialResponseDecoder extends ReplayingDecoder<State> {

    /** 解码状态机：INIT 解析协商应答，SUCCESS 透传隧道数据，FAILURE 丢弃无效输入。 */
    @UnstableApi
    public enum State {
        INIT,
        SUCCESS,
        FAILURE
    }

    public Socks5InitialResponseDecoder() {
        super(State.INIT);
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

                // 服务端选定的认证方法（0xFF 表示无可接受的方法）
                final Socks5AuthMethod authMethod = Socks5AuthMethod.valueOf(in.readByte());
                out.add(new DefaultSocks5InitialResponse(authMethod));
                checkpoint(State.SUCCESS);
            }
            case SUCCESS: {
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

    /** 构造失败占位应答并切换到 FAILURE，供上层根据 {@link DecoderResult} 关闭连接。 */
    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }

        checkpoint(State.FAILURE);

        Socks5Message m = new DefaultSocks5InitialResponse(Socks5AuthMethod.UNACCEPTED);
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
    }
}
