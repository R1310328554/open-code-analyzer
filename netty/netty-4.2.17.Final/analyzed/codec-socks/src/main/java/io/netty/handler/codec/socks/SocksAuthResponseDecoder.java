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
package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socks.SocksAuthResponseDecoder.State;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * Decodes {@link ByteBuf}s into {@link SocksAuthResponse}.
 * Before returning SocksResponse decoder removes itself from pipeline.
 * <p>解析服务端 AUTH 子协商应答（2 字节：版本 0x01 + {@link SocksAuthStatus}），
 * 版本不符时产出 {@link SocksCommonUtils#UNKNOWN_SOCKS_RESPONSE}；完成后自 pipeline 移除。</p>
 */
public class SocksAuthResponseDecoder extends ReplayingDecoder<State> {

    public SocksAuthResponseDecoder() {
        super(State.CHECK_PROTOCOL_VERSION);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out)
            throws Exception {
        switch (state()) {
            case CHECK_PROTOCOL_VERSION: {
                if (byteBuf.readByte() != SocksSubnegotiationVersion.AUTH_PASSWORD.byteValue()) {
                    out.add(SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE);
                    break;
                }
                checkpoint(State.READ_AUTH_RESPONSE);
            }
            case READ_AUTH_RESPONSE: {
                SocksAuthStatus authStatus = SocksAuthStatus.valueOf(byteBuf.readByte());
                out.add(new SocksAuthResponse(authStatus));
                break;
            }
            default: {
                throw new Error("Unexpected response decoder state: " + state());
            }
        }
        channelHandlerContext.pipeline().remove(this);
    }

    @UnstableApi
    public enum State {
        /** 校验子协商版本必须为 {@link SocksSubnegotiationVersion#AUTH_PASSWORD}（0x01）。 */
        CHECK_PROTOCOL_VERSION,
        /** 读取 1 字节认证结果并构造 {@link SocksAuthResponse}。 */
        READ_AUTH_RESPONSE
    }
}
