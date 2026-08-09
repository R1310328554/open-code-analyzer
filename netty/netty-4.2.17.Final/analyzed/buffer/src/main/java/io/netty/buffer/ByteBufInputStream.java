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
package io.netty.buffer;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import io.netty.util.ReferenceCounted;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 从 {@link ByteBuf} 读取数据的 {@link InputStream}。
 * <p>
 * 读操作在底层缓冲区的 {@code readerIndex} 处进行，并随读取递增。
 * 注意：可读范围在构造时确定，之后修改 {@link ByteBuf#writerIndex()} 不会影响 {@link #available()}。
 * <p>
 * 同时实现 {@link DataInput}；字节序取决于底层缓冲区，未必为大端。
 *
 * @see ByteBufOutputStream
 */
public class ByteBufInputStream extends InputStream implements DataInput {
    private final ByteBuf buffer;
    private final int startIndex;
    private final int endIndex;
    private boolean closed;
    /**
     * 为兼容旧版（不转移所有权），可通过标志指定关闭时是否 {@link ByteBuf#release()}。
     * 后续版本将始终转移所有权；调用方必要时需自行 {@link ReferenceCounted#retain()}。
     */
    private final boolean releaseOnClose;

    /**
     * 从 {@code buffer} 当前 {@code readerIndex} 读到 {@code writerIndex} 创建输入流。
      * @param buffer 为此 {@link InputStream} 提供内容的缓冲区。
     */
    public ByteBufInputStream(ByteBuf buffer) {
        this(buffer, buffer.readableBytes());
    }

    /**
     * 从 {@code buffer} 的 {@code readerIndex} 起读取 {@code length} 字节创建输入流。
      * @param buffer 为此 {@link InputStream} 提供内容的缓冲区。
      * @param length 此 {@link InputStream} 使用的字节长度。
      * @throws IndexOutOfBoundsException 若 {@code readerIndex + length} 大于 {@code writerIndex}
     */
    public ByteBufInputStream(ByteBuf buffer, int length) {
        this(buffer, length, false);
    }

    /**
     * Creates a new stream which reads data from the specified {@code buffer}
     * starting at the current {@code readerIndex} and ending at the current
     * {@code writerIndex}.
      * @param buffer 为此 {@link InputStream} 提供内容的缓冲区。
      * @param releaseOnClose 为 {@code true} 时，{@link #close()} 会对 {@code buffer} 调用 {@link ByteBuf#release()}。
     */
    public ByteBufInputStream(ByteBuf buffer, boolean releaseOnClose) {
        this(buffer, buffer.readableBytes(), releaseOnClose);
    }

    /**
     * Creates a new stream which reads data from the specified {@code buffer}
     * starting at the current {@code readerIndex} and ending at
     * {@code readerIndex + length}.
      * @param buffer 为此 {@link InputStream} 提供内容的缓冲区。
      * @param length 此 {@link InputStream} 使用的字节长度。
      * @param releaseOnClose {@code true} means that when {@link #close()} is called then {@link ByteBuf#release()} will
     *                       be called on {@code buffer}.
     * @throws IndexOutOfBoundsException
     *         if {@code readerIndex + length} is greater than
     *            {@code writerIndex}
     */
    public ByteBufInputStream(ByteBuf buffer, int length, boolean releaseOnClose) {
        ObjectUtil.checkNotNull(buffer, "buffer");
        if (length < 0) {
            if (releaseOnClose) {
                buffer.release();
            }
            checkPositiveOrZero(length, "length");
        }
        if (length > buffer.readableBytes()) {
            if (releaseOnClose) {
                buffer.release();
            }
            throw new IndexOutOfBoundsException("Too many bytes to be read - Needs "
                    + length + ", maximum is " + buffer.readableBytes());
        }

        this.releaseOnClose = releaseOnClose;
        this.buffer = buffer;
        startIndex = buffer.readerIndex();
        endIndex = startIndex + length;
        buffer.markReaderIndex();
    }

    /**
      * 返回此流目前已读取的字节数。
     */
    public int readBytes() {
        return buffer.readerIndex() - startIndex;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            // Closeable 规定：流已关闭时调用 close 无效果
            if (releaseOnClose && !closed) {
                closed = true;
                buffer.release();
            }
        }
    }

    @Override
    public int available() throws IOException {
        return endIndex - buffer.readerIndex();
    }

    // 类非线程安全，抑制警告
    @Override
    public void mark(int readlimit) {
        buffer.markReaderIndex();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        int available = available();
        if (available == 0) {
            return -1;
        }
        return buffer.readByte() & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int available = available();
        if (available == 0) {
            return -1;
        }

        len = Math.min(available, len);
        buffer.readBytes(b, off, len);
        return len;
    }

    // 类非线程安全，抑制警告
    @Override
    public void reset() throws IOException {
        buffer.resetReaderIndex();
    }

    @Override
    public long skip(long n) throws IOException {
        if (n > Integer.MAX_VALUE) {
            return skipBytes(Integer.MAX_VALUE);
        } else {
            return skipBytes((int) n);
        }
    }

    @Override
    public boolean readBoolean() throws IOException {
        checkAvailable(1);
        return read() != 0;
    }

    @Override
    public byte readByte() throws IOException {
        checkAvailable(1);
        return buffer.readByte();
    }

    @Override
    public char readChar() throws IOException {
        return (char) readShort();
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        checkAvailable(len);
        buffer.readBytes(b, off, len);
    }

    @Override
    public int readInt() throws IOException {
        checkAvailable(4);
        return buffer.readInt();
    }

    private StringBuilder lineBuf;

    @Override
    public String readLine() throws IOException {
        int available = available();
        if (available == 0) {
            return null;
        }

        if (lineBuf != null) {
            lineBuf.setLength(0);
        }

        loop: do {
            int c = buffer.readUnsignedByte();
            --available;
            switch (c) {
                case '\n':
                    break loop;

                case '\r':
                    if (available > 0 && (char) buffer.getUnsignedByte(buffer.readerIndex()) == '\n') {
                        buffer.skipBytes(1);
                        --available;
                    }
                    break loop;

                default:
                    if (lineBuf == null) {
                        lineBuf = new StringBuilder();
                    }
                    lineBuf.append((char) c);
            }
        } while (available > 0);

        return lineBuf != null && lineBuf.length() > 0 ? lineBuf.toString() : StringUtil.EMPTY_STRING;
    }

    @Override
    public long readLong() throws IOException {
        checkAvailable(8);
        return buffer.readLong();
    }

    @Override
    public short readShort() throws IOException {
        checkAvailable(2);
        return buffer.readShort();
    }

    @Override
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return readByte() & 0xff;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xffff;
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int nBytes = Math.min(available(), n);
        buffer.skipBytes(nBytes);
        return nBytes;
    }

    private void checkAvailable(int fieldSize) throws IOException {
        if (fieldSize < 0) {
            throw new IndexOutOfBoundsException("fieldSize cannot be a negative number");
        }
        if (fieldSize > available()) {
            throw new EOFException("fieldSize is too long! Length is " + fieldSize
                    + ", but maximum is " + available());
        }
    }
}
