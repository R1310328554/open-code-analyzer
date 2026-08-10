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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socksx.v4.Socks4ClientDecoder.State;
import io.netty.util.NetUtil;
import io.netty.util.internal.UnstableApi;

import java.util.List;

/**
 * Decodes a single {@link Socks4CommandResponse} from the inbound {@link ByteBuf}s.
 * On successful decode, this decoder will forward the received data to the next handler, so that
 * other handler can remove this decoder later.  On failed decode, this decoder will discard the
 * received data, so that other handler closes the connection later.
 *
 * <p>SOCKS4 客户端侧解码器：从入站字节流解析一条 {@link Socks4CommandResponse}。
 * 应答固定 8 字节：VN(0) + CD + DSTPORT(2) + DSTIP(4)。解码成功后进入 SUCCESS 状态，
 * 将后续残留字节透传给下游（便于隧道数据转发）；失败时产出带 {@link DecoderResult#failure}
 * 的占位响应并丢弃剩余输入。</p>
 */
public class Socks4ClientDecoder extends ReplayingDecoder<State> {

    /** 解码状态机：START 解析应答头，SUCCESS 透传隧道数据，FAILURE 丢弃无效输入。 */
    @UnstableApi
    public enum State {
        START,
        SUCCESS,
        FAILURE
    }

    public Socks4ClientDecoder() {
        super(State.START);
        // 仅解码一条 SOCKS 应答，之后由 SUCCESS 状态处理后续字节
        setSingleDecode(true);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            switch (state()) {
            case START: {
                // SOCKS4 应答版本字节恒为 0（与请求的 VN=4 不同）
                final int version = in.readUnsignedByte();
                if (version != 0) {
                    throw new DecoderException("unsupported reply version: " + version + " (expected: 0)");
                }

                final Socks4CommandStatus status = Socks4CommandStatus.valueOf(in.readByte());
                final int dstPort = ByteBufUtil.readUnsignedShortBE(in);
                final String dstAddr = NetUtil.intToIpAddress(ByteBufUtil.readIntBE(in));

                out.add(new DefaultSocks4CommandResponse(status, dstAddr, dstPort));
                checkpoint(State.SUCCESS);
            }
            case SUCCESS: {
                // 应答之后的字节属于已建立的 SOCKS 隧道负载，保留引用计数后上抛
                int readableBytes = actualReadableBytes();
                if (readableBytes > 0) {
                    out.add(in.readRetainedSlice(readableBytes));
                }
                break;
            }
            case FAILURE: {
                // 解码失败后丢弃缓冲区，避免脏数据影响后续 handler
                in.skipBytes(actualReadableBytes());
                break;
            }
            }
        } catch (Exception e) {
            fail(out, e);
        }
    }

    /** 构造失败响应并切换到 FAILURE，供上层根据 {@link DecoderResult} 关闭连接。 */
    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }

        Socks4CommandResponse m = new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED);
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);

        checkpoint(State.FAILURE);
    }
}
