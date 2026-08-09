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
package io.netty.handler.codec.compression;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 带输入累积的 {@link Decompressor} 抽象基类，行为类似 {@link io.netty.handler.codec.ByteToMessageDecoder}：
 * 分片输入先合并再按状态机逐步解压。
 */
abstract class InputBufferingDecompressor implements Decompressor {
    protected final ByteBufAllocator allocator;
    private ByteBuf cumulation;

    /** 使用给定分配器创建解压器。 */
    InputBufferingDecompressor(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    @Override
    public final void addInput(ByteBuf buf) throws DecompressionException {
        if (!buf.isReadable()) {
            buf.release();
            return;
        }
        if (this.cumulation != null) {
            buf = ByteToMessageDecoder.MERGE_CUMULATOR.cumulate(allocator, this.cumulation, buf);
            this.cumulation = null;
        }
        try {
            processInput(buf);
        } catch (Throwable t) {
            buf.release();
            throw t;
        }
        if (buf.isReadable()) {
            this.cumulation = buf;
        } else {
            buf.release();
        }
    }

    @Override
    public final ByteBuf takeOutput() throws DecompressionException {
        ByteBuf buf = cumulation == null ? Unpooled.EMPTY_BUFFER : cumulation;
        ByteBuf output = processOutput(buf);
        try {
            if (status() == Status.NEED_INPUT && buf.isReadable()) {
                processInput(buf);
            }
        } catch (Throwable t) {
            output.release();
            throw t;
        }
        if (this.cumulation != null && !this.cumulation.isReadable()) {
            this.cumulation.release();
            this.cumulation = null;
        }
        return output;
    }

    /**
     * 解析部分输入；未读完的数据由基类缓冲，所有权不转入本方法。
     *
     * @param buf The input buffer
     */
    abstract void processInput(ByteBuf buf) throws DecompressionException;

    /**
     * 生成解压输出；可从 {@code buf} 继续消费数据，即使输入不足也必须返回缓冲（可为空）。
     *
     * @param buf The input buffer
     */
    abstract ByteBuf processOutput(ByteBuf buf) throws DecompressionException;

    /** @return 当前累积缓冲中的可读字节数。 */
    final int available() {
        return cumulation == null ? 0 : cumulation.readableBytes();
    }

    @Override
    public void close() {
        if (this.cumulation != null) {
            this.cumulation.release();
            this.cumulation = null;
        }
    }
}
