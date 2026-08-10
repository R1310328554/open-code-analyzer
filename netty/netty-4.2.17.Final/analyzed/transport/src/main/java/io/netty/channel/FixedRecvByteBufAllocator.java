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
package io.netty.channel;

import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * The {@link RecvByteBufAllocator} that always yields the same buffer
 * size prediction.  This predictor ignores the feed back from the I/O thread.
 * <p>始终返回固定缓冲区大小预测的 {@link RecvByteBufAllocator}，忽略 I/O 线程的读反馈。</p>
 */
public class FixedRecvByteBufAllocator extends DefaultMaxMessagesRecvByteBufAllocator {

    /** 每次读操作建议的缓冲区容量（字节） */
    private final int bufferSize;

    /** 固定容量预测的实现 */
    private final class HandleImpl extends MaxMessageHandle {
        private final int bufferSize;

        HandleImpl(int bufferSize) {
            this.bufferSize = bufferSize;
        }

        /** 始终返回构造时指定的固定容量。 */
        @Override
        public int guess() {
            return bufferSize;
        }
    }

    /**
     * Creates a new predictor that always returns the same prediction of
     * the specified buffer size.
     * <p>创建每次读均预测相同缓冲区大小的分配器。</p>
     */
    public FixedRecvByteBufAllocator(int bufferSize) {
        checkPositive(bufferSize, "bufferSize");
        this.bufferSize = bufferSize;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Handle newHandle() {
        return new HandleImpl(bufferSize);
    }

    /** 配置是否在可能仍有数据时继续读；支持链式调用。 */
    @Override
    public FixedRecvByteBufAllocator respectMaybeMoreData(boolean respectMaybeMoreData) {
        super.respectMaybeMoreData(respectMaybeMoreData);
        return this;
    }
}
