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
package io.netty.channel.uring;

import io.netty.channel.ChannelOption;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.util.internal.ObjectUtil;

import java.util.Map;

/**
 * io_uring 流式通道配置基类：管理 {@link IoUringChannelOption#IO_URING_BUFFER_GROUP_ID}。
 * <p>指定接收时使用的 io_uring buffer ring 组 id。</p>
 */
abstract class IoUringStreamChannelConfig extends IoUringChannelConfig {

    /** io_uring 接收 buffer ring 的 buffer group id；-1 表示未使用 ring */
    private volatile short bufferGroupId = -1;

    IoUringStreamChannelConfig(AbstractIoUringChannel channel) {
        super(channel);
    }

    IoUringStreamChannelConfig(AbstractIoUringChannel channel, RecvByteBufAllocator allocator) {
        super(channel, allocator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == IoUringChannelOption.IO_URING_BUFFER_GROUP_ID) {
            return (T) Short.valueOf(getBufferGroupId());
        }
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        if (option == IoUringChannelOption.IO_URING_BUFFER_GROUP_ID) {
            setBufferGroupId((Short) value);
            return true;
        }
        return super.setOption(option, value);
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions(),
                IoUringChannelOption.IO_URING_BUFFER_GROUP_ID
        );
    }

    /** 返回当前配置的 buffer group id */
    short getBufferGroupId() {
        return bufferGroupId;
    }

    /** 设置接收使用的 buffer ring 组 id（须在 {@link IoUringIoHandlerConfig} 中已注册） */
    IoUringStreamChannelConfig setBufferGroupId(short bufferGroupId) {
        this.bufferGroupId = (short) ObjectUtil.checkPositiveOrZero(bufferGroupId, "bufferGroupId");
        return this;
    }
}
