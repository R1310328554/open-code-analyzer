/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * 能够向对端写出 HTTP/2 {@code DATA} 帧的能力抽象。
 * <p>实现类通常由 {@link Http2ConnectionEncoder} 承担，负责按流控窗口拆分/合并载荷并写入 pipeline。
 */
public interface Http2DataWriter {
    /**
     * 向远端写入 {@code DATA} 帧；大载荷可能被编码器拆成多个帧依次写出。
     *
     * @param ctx the context to use for writing.
     * @param streamId the stream for which to send the frame.
     * @param data the payload of the frame. This will be released by this method.
     * @param padding additional bytes that should be added to obscure the true content size. Must be between 0 and
     *                256 (inclusive). A 1 byte padding is encoded as just the pad length field with value 0.
     *                A 256 byte padding is encoded as the pad length field with value 255 and 255 padding bytes
     *                appended to the end of the frame.
     * @param endStream indicates if this is the last frame to be sent for the stream.
     * @param promise the promise for the write.
     * @return the future for the write.
     */
    ChannelFuture writeData(ChannelHandlerContext ctx, int streamId,
            ByteBuf data, int padding, boolean endStream, ChannelPromise promise);
}
