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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.UncheckedBooleanSupplier;
import io.netty.util.internal.UnstableApi;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 为入站读操作分配合适容量的接收缓冲区。
 * <p>
 * 目标是在一次读操作中尽量读满数据，同时避免分配过大造成内存浪费；
 * 具体策略由实现类与 {@link Handle} 维护的历史统计决定。
 * </p>
 */
public interface RecvByteBufAllocator {
    /**
     * 创建新的操作句柄；句柄保存预测最优缓冲容量所需的内部状态。
     */
    Handle newHandle();

    /**
     * @deprecated 请使用 {@link ExtendedHandle}。
     */
    @Deprecated
    interface Handle {
        /**
         * 分配新的接收缓冲区，容量应能容纳预期入站数据且不过度浪费。
         */
        ByteBuf allocate(ByteBufAllocator alloc);

        /**
         * 与 {@link #allocate(ByteBufAllocator)} 类似，但不实际分配，仅返回建议容量。
         */
        int guess();

        /**
         * 重置累计计数，并为下一次读循环给出建议的消息/字节读取量。
         * <p>
         * {@link #continueReading()} 可能据此判断是否结束读循环；仅为提示，实现可忽略。
         * </p>
         *
         * @param config 可能影响行为的 Channel 配置
         */
        void reset(ChannelConfig config);

        /**
         * 增加当前读循环已读消息数。
         *
         * @param numMessages 增量
         */
        void incMessagesRead(int numMessages);

        /**
         * 记录上一次读操作实际读取的字节数。
         * <p>
         * 读错误时可为负值；负值应在下次 {@link #lastBytesRead()} 中返回，并可能作为
         * 外部终止条件，{@link #continueReading()} 不必强制处理。
         * </p>
         *
         * @param bytes 上次读操作的字节数
         */
        void lastBytesRead(int bytes);

        /**
         * 获取上一次读操作实际读取的字节数。
         *
         * @return 上次读操作的字节数
         */
        int lastBytesRead();

        /**
         * 设置本次读操作尝试读取的字节数（计划值或已完成值）。
         *
         * @param bytes 尝试读取的字节数
         */
        void attemptedBytesRead(int bytes);

        /**
         * 获取本次读操作尝试读取的字节数。
         *
         * @return 尝试读取的字节数
         */
        int attemptedBytesRead();

        /**
         * 判断当前读循环是否应继续读取。
         *
         * @return {@code true} 表示继续读；{@code false} 表示本次读循环结束
         */
        boolean continueReading();

        /** 本次读循环已全部完成时调用。 */
        void readComplete();
    }

    @SuppressWarnings("deprecation")
    interface ExtendedHandle extends Handle {
        /**
         * 与 {@link Handle#continueReading()} 类似，但由 {@code maybeMoreDataSupplier} 判断是否还有数据。
         *
         * @param maybeMoreDataSupplier 判断是否可能还有数据可读
         */
        boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier);
    }

    /**
     * 将所有调用委托给另一 {@link Handle} 的包装实现。
     */
    class DelegatingHandle implements Handle {
        /** 被委托的句柄 */
        private final Handle delegate;

        public DelegatingHandle(Handle delegate) {
            this.delegate = checkNotNull(delegate, "delegate");
        }

        /**
         * 返回所有方法委托的目标 {@link Handle}。
         *
         * @return 委托目标句柄
         */
        protected final Handle delegate() {
            return delegate;
        }

        @Override
        public ByteBuf allocate(ByteBufAllocator alloc) {
            return delegate.allocate(alloc);
        }

        @Override
        public int guess() {
            return delegate.guess();
        }

        @Override
        public void reset(ChannelConfig config) {
            delegate.reset(config);
        }

        @Override
        public void incMessagesRead(int numMessages) {
            delegate.incMessagesRead(numMessages);
        }

        @Override
        public void lastBytesRead(int bytes) {
            delegate.lastBytesRead(bytes);
        }

        @Override
        public int lastBytesRead() {
            return delegate.lastBytesRead();
        }

        @Override
        public boolean continueReading() {
            return delegate.continueReading();
        }

        @Override
        public int attemptedBytesRead() {
            return delegate.attemptedBytesRead();
        }

        @Override
        public void attemptedBytesRead(int bytes) {
            delegate.attemptedBytesRead(bytes);
        }

        @Override
        public void readComplete() {
            delegate.readComplete();
        }
    }
}
