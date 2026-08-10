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
package io.netty.handler.stream;


import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

/**
 * 长度不确定的数据流，由 {@link ChunkedWriteHandler} 分块消费并写出。
 *
 * @param <B> 每个分块的数据类型（通常为 {@link io.netty.buffer.ByteBuf}）
 */
public interface ChunkedInput<B> {

    /**
     * 当流中无剩余数据且已到达末尾时返回 {@code true}。
     */
    boolean isEndOfInput() throws Exception;

    /**
     * 释放与输入关联的资源。
     */
    void close() throws Exception;

    /**
     * @deprecated Use {@link #readChunk(ByteBufAllocator)}.
     *
     * <p>从流中读取一个分块。当本方法返回最后一个分块后，
     * 后续 {@link #isEndOfInput()} 必须返回 {@code true}。
     *
     * @param ctx The context which provides a {@link ByteBufAllocator} if buffer allocation is necessary.
     * @return the fetched chunk.
     *         {@code null} if there is no data left in the stream.
     *         Please note that {@code null} does not necessarily mean that the
     *         stream has reached at its end.  In a slow stream, the next chunk
     *         might be unavailable just momentarily.
     */
    @Deprecated
    B readChunk(ChannelHandlerContext ctx) throws Exception;

    /**
     * 从流中读取一个分块。当本方法返回最后一个分块后，
     * 后续 {@link #isEndOfInput()} 必须返回 {@code true}。
     *
     * @param allocator {@link ByteBufAllocator} if buffer allocation is necessary.
     * @return the fetched chunk.
     *         {@code null} if there is no data left in the stream.
     *         Please note that {@code null} does not necessarily mean that the
     *         stream has reached at its end.  In a slow stream, the next chunk
     *         might be unavailable just momentarily.
     */
    B readChunk(ByteBufAllocator allocator) throws Exception;

    /**
     * 返回输入总长度。
     * @return  已知长度时为字节数；未知时为负值。
     */
    long length();

    /**
     * 返回当前传输进度（已传输字节数）。
     */
    long progress();

}
