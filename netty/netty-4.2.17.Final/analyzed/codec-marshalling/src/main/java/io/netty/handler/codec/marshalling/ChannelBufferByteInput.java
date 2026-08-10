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
import org.jboss.marshalling.ByteInput;

import java.io.IOException;

/**
 * {@link ByteInput} implementation which reads its data from a {@link ByteBuf}
 * <p>将 JBoss Marshalling 的 {@link ByteInput} 适配到 Netty {@link ByteBuf}，
 * 使 {@link Unmarshaller} 可直接从 Channel 缓冲区反序列化，无需额外拷贝到 {@code InputStream}。</p>
 */
class ChannelBufferByteInput implements ByteInput {

    /** 底层可读缓冲区；读指针随 {@link Unmarshaller} 消费而前移。 */
    private final ByteBuf buffer;

    ChannelBufferByteInput(ByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public void close() throws IOException {
        // ByteBuf 生命周期由 Netty 管理，此处无需释放
    }

    @Override
    public int available() throws IOException {
        return buffer.readableBytes();
    }

    @Override
    public int read() throws IOException {
        if (buffer.isReadable()) {
            // 与 InputStream.read() 一致：返回 0–255 或 -1 表示 EOF
            return buffer.readByte() & 0xff;
        }
        return -1;
    }

    @Override
    public int read(byte[] array) throws IOException {
        return read(array, 0, array.length);
    }

    @Override
    public int read(byte[] dst, int dstIndex, int length) throws IOException {
        int available = available();
        if (available == 0) {
            return -1;
        }

        length = Math.min(available, length);
        buffer.readBytes(dst, dstIndex, length);
        return length;
    }

    @Override
    public long skip(long bytes) throws IOException {
        int readable = buffer.readableBytes();
        if (readable < bytes) {
            bytes = readable;
        }
        buffer.readerIndex((int) (buffer.readerIndex() + bytes));
        return bytes;
    }

}
