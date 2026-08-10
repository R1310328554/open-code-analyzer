/*
 * Copyright 2015 The Netty Project
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
package io.netty.channel.epoll;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.RecvByteBufAllocator.DelegatingHandle;
import io.netty.channel.RecvByteBufAllocator.ExtendedHandle;
import io.netty.channel.unix.PreferredDirectByteBufAllocator;
import io.netty.util.UncheckedBooleanSupplier;

/** epoll 接收缓冲区分配 handle：强制直接内存，并处理 EPOLLRDHUP 续读逻辑 */
class EpollRecvByteAllocatorHandle extends DelegatingHandle implements ExtendedHandle {
    private final PreferredDirectByteBufAllocator preferredDirectByteBufAllocator =
            new PreferredDirectByteBufAllocator();
    private final UncheckedBooleanSupplier defaultMaybeMoreDataSupplier = new UncheckedBooleanSupplier() {
        @Override
        public boolean get() {
            return maybeMoreDataToRead();
        }
    };
    private boolean receivedRdHup;

    EpollRecvByteAllocatorHandle(ExtendedHandle handle) {
        super(handle);
    }

    /** 标记已收到 EPOLLRDHUP，需读至错误 */
    final void receivedRdHup() {
        receivedRdHup = true;
    }

    /** 是否已收到对端半关闭通知 */
    final boolean isReceivedRdHup() {
        return receivedRdHup;
    }

    /** 上次读满缓冲区则可能仍有数据 */
    boolean maybeMoreDataToRead() {
        return lastBytesRead() == attemptedBytesRead();
    }

    @Override
    public final ByteBuf allocate(ByteBufAllocator alloc) {
        // JNI 读路径仅支持直接缓冲区，故强制 direct 分配
        preferredDirectByteBufAllocator.updateAllocator(alloc);
        return delegate().allocate(preferredDirectByteBufAllocator);
    }

    @Override
    public final boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier) {
        return isReceivedRdHup() || ((ExtendedHandle) delegate()).continueReading(maybeMoreDataSupplier);
    }

    @Override
    public final boolean continueReading() {
        // 覆盖“是否还有数据可读”的默认判定逻辑
        return continueReading(defaultMaybeMoreDataSupplier);
    }
}
