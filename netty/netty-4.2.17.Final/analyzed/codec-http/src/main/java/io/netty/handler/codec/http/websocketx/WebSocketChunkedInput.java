/*
 * Copyright 2016 The Netty Project
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
package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;
import io.netty.util.internal.ObjectUtil;

/**
 * 将 {@link ChunkedInput} 适配为 WebSocket 分片传输：每块数据包装为 {@link ContinuationWebSocketFrame}，
 * 最后一块的 {@code finalFragment} 为 true。
 */
public final class WebSocketChunkedInput implements ChunkedInput<WebSocketFrame> {
    /** 底层字节块输入源。 */
    private final ChunkedInput<ByteBuf> input;
    /** RSV 扩展位（RSV1/RSV2/RSV3）。 */
    private final int rsv;

    /**
     * 以指定 {@link ChunkedInput} 创建实例。
     * @param input {@link ChunkedInput} containing data to write
     */
    public WebSocketChunkedInput(ChunkedInput<ByteBuf> input) {
        this(input, 0);
    }

    /**
     * 创建实例并指定 RSV 扩展位。
     * @param input {@link ChunkedInput} containing data to write
     * @param rsv RSV1, RSV2, RSV3 used for extensions
     *
     * @throws  NullPointerException if {@code input} is null
     */
    public WebSocketChunkedInput(ChunkedInput<ByteBuf> input, int rsv) {
        this.input = ObjectUtil.checkNotNull(input, "input");
        this.rsv = rsv;
    }

    /**
     * @return 流已结束且无剩余数据时为 {@code true}。
     */
    @Override
    public boolean isEndOfInput() throws Exception {
        return input.isEndOfInput();
    }

    /**
     * 释放底层输入源资源。
     */
    @Override
    public void close() throws Exception {
        input.close();
    }

    /**
     * @deprecated Use {@link #readChunk(ByteBufAllocator)}.
     *
     * 从流中读取一块数据并包装为 {@link WebSocketFrame}；返回最后一块后，
     * 后续 {@link #isEndOfInput()} 应返回 {@code true}。
     *
     * @param ctx {@link ChannelHandlerContext} context of channelHandler
     * @return {@link WebSocketFrame} contain chunk of data
     */
    @Deprecated
    @Override
    public WebSocketFrame readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    /**
     * 从流中读取一块数据并包装为 {@link WebSocketFrame}。
     *
     * @param allocator {@link ByteBufAllocator}
     * @return {@link WebSocketFrame} contain chunk of data
     */
    @Override
    public WebSocketFrame readChunk(ByteBufAllocator allocator) throws Exception {
        ByteBuf buf = input.readChunk(allocator);
        if (buf == null) {
            return null;
        }
        return new ContinuationWebSocketFrame(input.isEndOfInput(), rsv, buf);
    }

    @Override
    public long length() {
        return input.length();
    }

    @Override
    public long progress() {
        return input.progress();
    }
}
