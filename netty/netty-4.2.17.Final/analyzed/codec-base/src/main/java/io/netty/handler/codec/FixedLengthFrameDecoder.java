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
package io.netty.handler.codec;

import static io.netty.util.internal.ObjectUtil.checkPositive;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * 按固定字节数切分入站 {@link ByteBuf} 的解码器。 For example, if you received the following four fragmented packets:
 * <pre>
 * +---+----+------+----+
 * | A | BC | DEFG | HI |
 * +---+----+------+----+
 * </pre>
 * A {@link FixedLengthFrameDecoder}{@code (3)} will decode them into the
 * following three packets with the fixed length:
 * <pre>
 * +-----+-----+-----+
 * | ABC | DEF | GHI |
 * +-----+-----+-----+
 * </pre>
 */
/**
 * 按固定字节数切分入站 {@link ByteBuf} 的帧解码器。
 * <p>
 * 例如收到 A、BC、DEFG、HI 四段，{@code FixedLengthFrameDecoder(3)} 输出 ABC、DEF、GHI。
 */
public class FixedLengthFrameDecoder extends ByteToMessageDecoder {

    /** 每帧固定字节数。 */
    /** 每帧固定字节数。 */
    private final int frameLength;

    /**
     * 创建新实例。
     *
      * @param frameLength 帧长度
     */
    public FixedLengthFrameDecoder(int frameLength) {
        checkPositive(frameLength, "frameLength");
        this.frameLength = frameLength;
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Object decoded = decode(ctx, in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    /**
     * 从 {@link ByteBuf} 提取一帧并返回。
     *
      * @param   ctx             本 {@link ByteToMessageDecoder} 所属的 {@link ChannelHandlerContext}
      * @param   in              待读取的 {@link ByteBuf}
     * @return frame 表示帧的 {@link ByteBuf}；数据不足时 {@code null}
     */
    protected Object decode(
            @SuppressWarnings("UnusedParameters") ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in.readableBytes() < frameLength) {
            return null;
        } else {
            return in.readRetainedSlice(frameLength);
        }
    }
}
