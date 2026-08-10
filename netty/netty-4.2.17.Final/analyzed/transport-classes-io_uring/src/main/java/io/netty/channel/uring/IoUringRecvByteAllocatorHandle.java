/*
 * Copyright 2024 The Netty Project
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
package io.netty.channel.uring;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.unix.PreferredDirectByteBufAllocator;
import io.netty.util.UncheckedBooleanSupplier;

/**
 * io_uring 接收字节分配器句柄：强制 direct buffer，并在有数据时持续读以批量处理 CQE。
 * <p>POLLRDHUP 时继续 drain 直至读尽。</p>
 */
final class IoUringRecvByteAllocatorHandle extends RecvByteBufAllocator.DelegatingHandle
        implements RecvByteBufAllocator.ExtendedHandle {
    private final PreferredDirectByteBufAllocator preferredDirectByteBufAllocator =
            new PreferredDirectByteBufAllocator();

    // io_uring 下只要上次读到数据就应继续读，否则无法高效批量处理 CQE
    private final UncheckedBooleanSupplier defaultSupplier = () -> lastBytesRead() > 0;

    IoUringRecvByteAllocatorHandle(RecvByteBufAllocator.ExtendedHandle handle) {
        super(handle);
    }

    private boolean firstRead;
    private boolean rdHupReceived;
    private boolean readComplete;

    @Override
    public void reset(ChannelConfig config) {
        super.reset(config);
        readComplete = false;
        firstRead = true;
    }

    void rdHupReceived() {
        this.rdHupReceived = true;
    }

    @Override
    public ByteBuf allocate(ByteBufAllocator alloc) {
        // JNI 读路径仅支持 direct ByteBuf
        preferredDirectByteBufAllocator.updateAllocator(alloc);
        return delegate().allocate(preferredDirectByteBufAllocator);
    }

    @Override
    public boolean continueReading() {
        // 使用默认 supplier：lastBytesRead>0 时继续读
        return continueReading(defaultSupplier);
    }

    @Override
    public boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier) {
        // 收到 POLLRDHUP 后须 drain 输入直至无数据
        return ((RecvByteBufAllocator.ExtendedHandle) delegate()).continueReading(maybeMoreDataSupplier)
                || rdHupReceived;
    }

    public boolean isFirstRead() {
        return firstRead;
    }

    @Override
    public void readComplete() {
        super.readComplete();
        readComplete = true;
    }

    boolean isReadComplete() {
        return readComplete;
    }

    @Override
    public void lastBytesRead(int bytes) {
        firstRead = false;
        super.lastBytesRead(bytes);
    }

    @Override
    public void incMessagesRead(int numMessages) {
        firstRead = false;
        super.incMessagesRead(numMessages);
    }
}
