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
package io.netty.channel.kqueue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.RecvByteBufAllocator.DelegatingHandle;
import io.netty.channel.RecvByteBufAllocator.ExtendedHandle;
import io.netty.channel.unix.PreferredDirectByteBufAllocator;
import io.netty.util.UncheckedBooleanSupplier;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * KQueue 接收字节缓冲分配器扩展句柄。
 * <p>强制直缓冲、配合 EV_CLEAR 与 autoRead 控制是否继续读； 支持 EOF 与 pending 字节数语义。</p>
 */
final class KQueueRecvByteAllocatorHandle extends DelegatingHandle implements ExtendedHandle {
    private final PreferredDirectByteBufAllocator preferredDirectByteBufAllocator =
            new PreferredDirectByteBufAllocator();

    private final UncheckedBooleanSupplier defaultMaybeMoreDataSupplier = new UncheckedBooleanSupplier() {
        @Override
        public boolean get() {
            return maybeMoreDataToRead();
        }
    };
    /** 是否已读到流结束（EV_EOF） */
    private boolean readEOF;
    /** kqueue data 字段报告的可读字节数（pending） */
    private long numberBytesPending;

    KQueueRecvByteAllocatorHandle(ExtendedHandle handle) {
        super(handle);
    }

    @Override
    public ByteBuf allocate(ByteBufAllocator alloc) {
        // JNI 读路径仅支持 direct ByteBuf，故包装 PreferredDirectByteBufAllocator
        preferredDirectByteBufAllocator.updateAllocator(alloc);
        return delegate().allocate(preferredDirectByteBufAllocator);
    }

    @Override
    public boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier) {
        return readEOF || ((ExtendedHandle) delegate()).continueReading(maybeMoreDataSupplier);
    }

    @Override
    public boolean continueReading() {
        // 覆盖 maybeMoreData 判定以适配 kqueue EV_CLEAR 语义
        return continueReading(defaultMaybeMoreDataSupplier);
    }

    void readEOF() {
        readEOF = true;
    }

    boolean isReadEOF() {
        return readEOF;
    }

    void numberBytesPending(long numberBytesPending) {
        this.numberBytesPending = numberBytesPending;
    }

    private boolean maybeMoreDataToRead() {
        /*
         * kqueue with EV_CLEAR flag set requires that we read until we consume "data" bytes
         * (see <a href="https://www.freebsd.org/cgi/man.cgi?kqueue">kqueue man</a>). However in order to
         * respect auto read we supporting reading to stop if auto read is off. If auto read is on we force reading to
         * continue to avoid a {@link StackOverflowError} between channelReadComplete and reading from the
         * channel. It is expected that the {@link #KQueueSocketChannel} implementations will track if all data was not
         * read, and will force a EVFILT_READ ready event.
         * <p>EV_CLEAR 要求读尽 data 字节；autoRead 关闭时可提前停止； EOF 由 {@link #isReadEOF()} 外部处理。</p>
         *
         * It is assumed EOF is handled externally by checking {@link #isReadEOF()}.
         */
        return lastBytesRead() == attemptedBytesRead();
    }
}
