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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.ObjectUtil;

import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * 从 {@link InputStream} 按块读取数据的 {@link ChunkedInput}。
 * <p>
 * 供流的 {@link InputStream#available()} 应尽可能准确，
 * 否则会产生过多小块或频繁阻塞。
 */
public class ChunkedStream implements ChunkedInput<ByteBuf> {

    /** 默认分块大小（8 KiB）。 */
    static final int DEFAULT_CHUNK_SIZE = 8192;

    /** 带回退能力的输入流。 */
    private final PushbackInputStream in;
    /** 每块最大字节数。 */
    private final int chunkSize;
    /** 已传输字节数。 */
    private long offset;
    /** 是否已关闭。 */
    private boolean closed;

    /**
     * 从输入流创建实例（默认块大小）。
     */
    public ChunkedStream(InputStream in) {
        this(in, DEFAULT_CHUNK_SIZE);
    }

    /**
     * 从输入流创建实例。
     *
     * @param chunkSize the number of bytes to fetch on each
     *                  {@link #readChunk(ChannelHandlerContext)} call
     */
    public ChunkedStream(InputStream in, int chunkSize) {
        ObjectUtil.checkNotNull(in, "in");
        ObjectUtil.checkPositive(chunkSize, "chunkSize");

        if (in instanceof PushbackInputStream) {
            this.in = (PushbackInputStream) in;
        } else {
            this.in = new PushbackInputStream(in);
        }
        this.chunkSize = chunkSize;
    }

    /**
     * 返回已传输字节数。
     */
    public long transferredBytes() {
        return offset;
    }

    @Override
    public boolean isEndOfInput() throws Exception {
        if (closed) {
            return true;
        }
        if (in.available() > 0) {
            return false;
        }

        int b = in.read();
        if (b < 0) {
            return true;
        } else {
            in.unread(b);
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        closed = true;
        in.close();
    }

    @Deprecated
    @Override
    public ByteBuf readChunk(ChannelHandlerContext ctx) throws Exception {
        return readChunk(ctx.alloc());
    }

    @Override
    public ByteBuf readChunk(ByteBufAllocator allocator) throws Exception {
        if (isEndOfInput()) {
            return null;
        }

        final int availableBytes = in.available();
        final int chunkSize;
        if (availableBytes <= 0) {
            chunkSize = this.chunkSize;
        } else {
            chunkSize = Math.min(this.chunkSize, in.available());
        }

        boolean release = true;
        ByteBuf buffer = allocator.buffer(chunkSize);
        try {
            // 从 InputStream 写入 ByteBuf
            int written = buffer.writeBytes(in, chunkSize);
            if (written < 0) {
                return null;
            }
            offset += written;
            release = false;
            return buffer;
        } finally {
            if (release) {
                buffer.release();
            }
        }
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public long progress() {
        return offset;
    }
}
