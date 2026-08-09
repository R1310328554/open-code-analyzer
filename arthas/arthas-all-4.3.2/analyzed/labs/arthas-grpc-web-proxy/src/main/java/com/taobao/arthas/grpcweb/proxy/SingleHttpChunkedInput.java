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
package com.taobao.arthas.grpcweb.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedInput;

/**
 * 单块 HTTP chunked 输入适配器：将 {@link ChunkedInput}{@code <ByteBuf>} 包装为 {@link HttpContent} 流。
 *
 * <p>与标准用法相比，不在此处自动追加 {@link LastHttpContent#EMPTY_LAST_CONTENT}，
 * 由 {@link SendGrpcWebResponse#writeEndChunk()} 单独发送结束块，便于 gRPC-Web 协议分段控制。</p>
 *
 * @see LastHttpContent
 * @see LastHttpContent#EMPTY_LAST_CONTENT
 */
public class SingleHttpChunkedInput implements ChunkedInput<HttpContent> {

    /** 底层字节分块源 */
    private final ChunkedInput<ByteBuf> input;

    /**
     * 使用指定分块输入创建实例。
     * @param input 待写入的 {@link ChunkedInput} 数据源
     */
    public SingleHttpChunkedInput(ChunkedInput<ByteBuf> input) {
        this.input = input;
//        lastHttpContent = LastHttpContent.EMPTY_LAST_CONTENT;
    }

    /**
     * 使用指定分块输入创建实例；{@code lastHttpContent} 参数保留以兼容 Netty API，当前未使用。
     * @param input 待写入的 {@link ChunkedInput} 数据源
     * @param lastHttpContent 本实现中由外部单独发送终止 chunk，此参数未挂载
     */
    public SingleHttpChunkedInput(ChunkedInput<ByteBuf> input, LastHttpContent lastHttpContent) {
        this.input = input;
//        this.lastHttpContent = lastHttpContent;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        if (input.isEndOfInput()) {
            // 底层输入读完即视为结束（终止 chunk 由调用方另行发送）
            return true;
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
            return null;
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
