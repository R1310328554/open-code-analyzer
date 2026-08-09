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

import com.github.luben.zstd.ZstdInputStreamNoFinalizer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.UnstableApi;

import java.io.IOException;
import java.io.InputStream;

/**
 * 使用 Zstandard 算法解压压缩块 {@link ByteBuf}。
 * 参见 <a href="https://facebook.github.io/zstd">Zstandard</a>。
 */
@UnstableApi
public final class ZstdDecompressor implements Decompressor {
    /**
     * 解压器接受的 {@code Window_Log} 默认上限。
     * {@code 27} 对应 128 MiB 解压窗口。
     */
    public static final int DEFAULT_MAX_WINDOW_LOG = 27;
    private static final int MIN_WINDOW_LOG = 10;
    private static final int MAX_WINDOW_LOG = 31;
    private static final int DEFAULT_MAX_FORWARD_BYTES = CompressionUtil.DEFAULT_MAX_FORWARD_BYTES;

    private final ByteBufAllocator allocator;

    private final MutableByteBufInputStream mutableInput = new MutableByteBufInputStream();
    private final ZstdInputStreamNoFinalizer output;

    ZstdDecompressor(Builder builder, ByteBufAllocator allocator) {
        // 不用 static，以便在类加载失败时仍能加载本类
        try {
            Zstd.ensureAvailability();
        } catch (Throwable throwable) {
            throw new ExceptionInInitializerError(throwable);
        }
        this.allocator = allocator;
        ZstdInputStreamNoFinalizer output = null;
        try {
            output = new ZstdInputStreamNoFinalizer(mutableInput);
            output.setContinuous(true);
            output.setLongMax(builder.maxWindowLog);
            this.output = output;
        } catch (IOException e) {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException closeException) {
                    e.addSuppressed(closeException);
                }
            }
            throw new DecompressionException(e);
        }
    }

    @Override
    public Status status() throws DecompressionException {
        try {
            if (output.available() == 0) {
                if (!output.getContinuous()) {
                    return Status.COMPLETE;
                }
                return Status.NEED_INPUT;
            }
            return Status.NEED_OUTPUT;
        } catch (IOException e) {
            throw new DecompressionException(e);
        }
    }

    @Override
    public void addInput(ByteBuf buf) throws DecompressionException {
        if (!buf.isReadable()) {
            buf.release();
            return;
        }
        if (mutableInput.current != null) {
            mutableInput.current.release();
        }
        mutableInput.current = buf;
    }

    @Override
    public void endOfInput() throws DecompressionException {
        try {
            output.setContinuous(false);
            if (output.read() != -1) {
                throw new DecompressionException("Unexpected output after end of input");
            }
        } catch (IOException e) {
            throw new DecompressionException(e);
        }
    }

    @Override
    public ByteBuf takeOutput() throws DecompressionException {
        ByteBuf buf = allocator.buffer(DEFAULT_MAX_FORWARD_BYTES, DEFAULT_MAX_FORWARD_BYTES);
        try {
            buf.writeBytes(output, DEFAULT_MAX_FORWARD_BYTES);
        } catch (IOException e) {
            buf.release();
            throw new DecompressionException(e);
        }
        if (buf.isReadable()) {
            return buf;
        }
        buf.release();
        return Unpooled.EMPTY_BUFFER;
    }

    @Override
    public void close() {
        if (mutableInput.current != null) {
            mutableInput.current.release();
            mutableInput.current = null;
        }
        try {
            output.close();
        } catch (IOException ignored) {
            // 忽略关闭异常
        }
    }

    @UnstableApi
    public static Builder builder() {
        return new Builder();
    }

    @UnstableApi
    public static final class Builder extends AbstractDecompressorBuilder {
        private int maxWindowLog = DEFAULT_MAX_WINDOW_LOG;

        Builder() {
        }

        /**
         * 设置接受的 {@code Window_Log} 上限。
         * <p>
         * 窗口对数限制 ZSTD 帧解压滑动窗口的内存占用；
         * 声明更大窗口的帧将被拒绝，以限制每条流可分配的内存。
         *
         * @param maxWindowLog 入站帧 {@code Window_Log} 字段的上限，须在 {@code [10, 31]}
         * @return 本构建器
         */
        @UnstableApi
        public Builder maxWindowLog(int maxWindowLog) {
            this.maxWindowLog = ObjectUtil.checkInRange(
                    maxWindowLog, MIN_WINDOW_LOG, MAX_WINDOW_LOG, "maxWindowLog");
            return this;
        }

        @Override
        public Decompressor build(ByteBufAllocator allocator) throws DecompressionException {
            return new DefensiveDecompressor(new ZstdDecompressor(this, allocator));
        }
    }

    private static final class MutableByteBufInputStream extends InputStream {
        ByteBuf current;

        @Override
        public int read() {
            if (available() == 0) {
                return -1;
            }
            return current.readByte() & 0xff;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            int available = available();
            if (available == 0) {
                return -1;
            }

            len = Math.min(available, len);
            current.readBytes(b, off, len);
            return len;
        }

        @Override
        public int available() {
            return current == null ? 0 : current.readableBytes();
        }
    }
}
