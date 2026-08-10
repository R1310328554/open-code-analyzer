/*
 * Copyright 2025 The Netty Project
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
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.internal.EmptyArrays;

import java.util.List;

/**
 * Decodes a single {@link Socks5PrivateAuthRequest} from the inbound {@link ByteBuf}s.
 * On successful decode, this decoder will forward the received data to the next handler, so that
 * other handler can remove or replace this decoder later.
 *
 * <p>SOCKS5 私有认证子协商请求解码器。报文格式：VER(1) + TOKEN_LEN + TOKEN[]。
 * 使用 {@link ByteToMessageDecoder} 而非 ReplayingDecoder，在数据不足时等待更多字节；
 * 解码完成后通过 {@code decoded} 标志透传后续隧道数据。</p>
 */
public final class Socks5PrivateAuthRequestDecoder extends ByteToMessageDecoder {

    /** 是否已完成首帧解码；之后调用仅透传剩余字节。 */
    private boolean decoded;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            if (decoded) {
                int readableBytes = in.readableBytes();
                if (readableBytes > 0) {
                    out.add(in.readRetainedSlice(readableBytes));
                }
                return;
            }

            // 至少需要 VER + TOKEN_LEN 两字节
            if (in.readableBytes() < 2) {
                return;
            }

            final int startOffset = in.readerIndex();
            final byte version = in.getByte(startOffset);
            if (version != 1) {
                throw new DecoderException("unsupported subnegotiation version: " + version + " (expected: 1)");
            }

            final int tokenLength = in.getUnsignedByte(startOffset + 1);

            // 等待完整令牌数据到达
            if (in.readableBytes() < 2 + tokenLength) {
                return;
            }

            // 跳过版本与长度前缀
            in.skipBytes(2);

            // 读取令牌字节
            byte[] token = new byte[tokenLength];
            in.readBytes(token);

            out.add(new DefaultSocks5PrivateAuthRequest(token));

            decoded = true;
        } catch (Exception e) {
            fail(out, e);
        }
    }

    /** 构造失败占位请求并标记已解码，避免重复解析脏数据。 */
    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }

        decoded = true;

        Socks5Message m = new
            DefaultSocks5PrivateAuthRequest(EmptyArrays.EMPTY_BYTES);
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
    }
}
