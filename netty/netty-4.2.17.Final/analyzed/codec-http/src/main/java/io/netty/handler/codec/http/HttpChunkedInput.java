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
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;

/**
 * 用于 HTTP 分块传输的 {@link ChunkedInput} 适配器。
 * <p>
 * 将底层 {@link ChunkedInput} 逐块读取并包装为 {@link HttpContent}；
 * 输入结束后写出 {@link LastHttpContent} 作为终止 chunk。
 * <p>
 * 使用前须在响应头设置 {@code Transfer-Encoding: chunked}。
 * <p>
 * <pre>
 * public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
 *     HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
 *     response.headers().set(TRANSFER_ENCODING, CHUNKED);
 *     ctx.write(response);
 *
 *     HttpChunkedInput httpChunkWriter = new HttpChunkedInput(
 *         new ChunkedFile(&quot;/tmp/myfile.txt&quot;));
 *     ChannelFuture sendFileFuture = ctx.write(httpChunkWriter);
 * }
 * </pre>
 */
public class HttpChunkedInput implements ChunkedInput<HttpContent> {

    private final ChunkedInput<ByteBuf> input;
    private final LastHttpContent lastHttpContent;
    /** 是否已发送终止 chunk（LastHttpContent） */
    private boolean sentLastChunk;

    /**
     * 使用指定 {@link ChunkedInput} 创建实例，终止块为 {@link LastHttpContent#EMPTY_LAST_CONTENT}。
     * @param input {@link ChunkedInput} containing data to write
     */
    public HttpChunkedInput(ChunkedInput<ByteBuf> input) {
        this.input = input;
        lastHttpContent = LastHttpContent.EMPTY_LAST_CONTENT;
    }

    /**
     * 创建实例并指定自定义终止块（可携带 trailer 头）。
     * @param input {@link ChunkedInput} containing data to write
     * @param lastHttpContent {@link LastHttpContent} that will be written as the terminating chunk. Use this for
     *            training headers.
     */
    public HttpChunkedInput(ChunkedInput<ByteBuf> input, LastHttpContent lastHttpContent) {
        this.input = input;
        this.lastHttpContent = lastHttpContent;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        if (input.isEndOfInput()) {
            // 底层输入已结束，但须等 LastHttpContent 发出后才算真正结束
            return sentLastChunk;
        } else {
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        input.close();
    }

    @Deprecated
    @Override
    public HttpContent readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    @Override
    public HttpContent readChunk(ByteBufAllocator allocator) throws Exception {
        if (input.isEndOfInput()) {
            if (sentLastChunk) {
                return null;
            } else {
                // 底层已无数据，发送终止 chunk
                sentLastChunk = true;
                return lastHttpContent;
            }
        } else {
            ByteBuf buf = input.readChunk(allocator);
            if (buf == null) {
                return null;
            }
            return new DefaultHttpContent(buf);
        }
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
