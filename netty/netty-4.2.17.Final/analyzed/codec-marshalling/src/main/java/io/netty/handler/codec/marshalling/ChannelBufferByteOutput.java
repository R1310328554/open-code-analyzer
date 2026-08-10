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
package io.netty.handler.codec.marshalling;

import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.ByteOutput;

import java.io.IOException;

/**
 * {@link ByteOutput} implementation which writes the data to a {@link ByteBuf}
 * <p>JBoss {@link Marshaller} 序列化输出的桥接层：写入直接追加到 Netty 出站 {@link ByteBuf}，
 * 编码完成后可通过 {@link #getBuffer()} 取回完整字节序列。</p>
 */
class ChannelBufferByteOutput implements ByteOutput {

    private final ByteBuf buffer;

    /**
     * Create a new instance which use the given {@link ByteBuf}
     * <p>通常传入 {@link io.netty.handler.codec.MessageToByteEncoder} 的 {@code out} 参数。</p>
     */
    ChannelBufferByteOutput(ByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }

    @Override
    public void flush() throws IOException {
        // ByteBuf 无缓冲刷新语义
    }

    @Override
    public void write(int b) throws IOException {
        buffer.writeByte(b);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        buffer.writeBytes(bytes);
    }

    @Override
    public void write(byte[] bytes, int srcIndex, int length) throws IOException {
        buffer.writeBytes(bytes, srcIndex, length);
    }

    /**
     * Return the {@link ByteBuf} which contains the written content
     * <p>供 {@link MarshallingEncoder} 在 {@code marshaller.finish()} 后回填长度字段等后处理。</p>
     */
    ByteBuf getBuffer() {
        return buffer;
    }
}
