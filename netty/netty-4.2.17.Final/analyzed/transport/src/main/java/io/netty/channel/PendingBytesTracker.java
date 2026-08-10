/*
 * Copyright 2017 The Netty Project
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

import io.netty.util.internal.ObjectUtil;

/**
 * 跟踪 Channel 待发送（pending）出站字节数的抽象基类。
 * <p>
 * 同时实现 {@link MessageSizeEstimator.Handle}，复用消息大小估算逻辑；
 * 根据 Pipeline 类型委托给 {@link DefaultChannelPipeline}、
 * {@link ChannelOutboundBuffer} 或空实现。
 * </p>
 */
abstract class PendingBytesTracker implements MessageSizeEstimator.Handle {
    /** 底层消息大小估算句柄 */
    private final MessageSizeEstimator.Handle estimatorHandle;

    private PendingBytesTracker(MessageSizeEstimator.Handle estimatorHandle) {
        this.estimatorHandle = ObjectUtil.checkNotNull(estimatorHandle, "estimatorHandle");
    }

    @Override
    public final int size(Object msg) {
        return estimatorHandle.size(msg);
    }

    /** 增加待发送出站字节计数。 */
    public abstract void incrementPendingOutboundBytes(long bytes);
    /** 减少待发送出站字节计数。 */
    public abstract void decrementPendingOutboundBytes(long bytes);

    /**
     * 根据 Channel 的 Pipeline 与出站缓冲区状态创建合适的 tracker。
     * <p>
     * 若 Channel 在构造 tracker 时已关闭，{@link Channel#unsafe()#outboundBuffer()} 可能为 null，
     * 此时使用 {@link NoopPendingBytesTracker}。参见
     * <a href="https://github.com/netty/netty/issues/3967">netty#3967</a>。
     * </p>
     */
    static PendingBytesTracker newTracker(Channel channel) {
        if (channel.pipeline() instanceof DefaultChannelPipeline) {
            return new DefaultChannelPipelinePendingBytesTracker((DefaultChannelPipeline) channel.pipeline());
        } else {
            ChannelOutboundBuffer buffer = channel.unsafe().outboundBuffer();
            MessageSizeEstimator.Handle handle = channel.config().getMessageSizeEstimator().newHandle();
            // channel.unsafe().outboundBuffer() 在 Channel 已关闭时可能为 null
            // 参见 https://github.com/netty/netty/issues/3967
            return buffer == null ?
                    new NoopPendingBytesTracker(handle) : new ChannelOutboundBufferPendingBytesTracker(buffer, handle);
        }
    }

    /** 委托 {@link DefaultChannelPipeline} 维护 pending 字节计数。 */
    private static final class DefaultChannelPipelinePendingBytesTracker extends PendingBytesTracker {
        private final DefaultChannelPipeline pipeline;

        DefaultChannelPipelinePendingBytesTracker(DefaultChannelPipeline pipeline) {
            super(pipeline.estimatorHandle());
            this.pipeline = pipeline;
        }

        @Override
        public void incrementPendingOutboundBytes(long bytes) {
            pipeline.incrementPendingOutboundBytes(bytes);
        }

        @Override
        public void decrementPendingOutboundBytes(long bytes) {
            pipeline.decrementPendingOutboundBytes(bytes);
        }
    }

    /** 委托 {@link ChannelOutboundBuffer} 维护 pending 字节计数。 */
    private static final class ChannelOutboundBufferPendingBytesTracker extends PendingBytesTracker {
        private final ChannelOutboundBuffer buffer;

        ChannelOutboundBufferPendingBytesTracker(
                ChannelOutboundBuffer buffer, MessageSizeEstimator.Handle estimatorHandle) {
            super(estimatorHandle);
            this.buffer = buffer;
        }

        @Override
        public void incrementPendingOutboundBytes(long bytes) {
            buffer.incrementPendingOutboundBytes(bytes);
        }

        @Override
        public void decrementPendingOutboundBytes(long bytes) {
            buffer.decrementPendingOutboundBytes(bytes);
        }
    }

    /** Channel 已关闭或无需统计时的空实现。 */
    private static final class NoopPendingBytesTracker extends PendingBytesTracker {

        NoopPendingBytesTracker(MessageSizeEstimator.Handle estimatorHandle) {
            super(estimatorHandle);
        }

        @Override
        public void incrementPendingOutboundBytes(long bytes) {
            // 无操作
        }

        @Override
        public void decrementPendingOutboundBytes(long bytes) {
            // 无操作
        }
    }
}
