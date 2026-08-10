/*
 * Copyright 2021 The Netty Project
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

/**
 * 专用于 {@link ServerChannel} 的 {@link MaxMessagesRecvByteBufAllocator} 实现。
 * <p>
 * 默认每次读循环仅处理一条消息（{@code maxMessagesPerRead = 1}），
 * 且初始接收缓冲区容量猜测值为 128 字节，适合 accept 等轻量服务端读场景。
 * </p>
 */
public final class ServerChannelRecvByteBufAllocator extends DefaultMaxMessagesRecvByteBufAllocator {
    /** 构造服务端 Channel 专用分配器：单次读一条消息。 */
    public ServerChannelRecvByteBufAllocator() {
        super(1, true);
    }

    @Override
    public Handle newHandle() {
        return new MaxMessageHandle() {
            /** 服务端读操作通常较短，默认猜测 128 字节容量。 */
            @Override
            public int guess() {
                return 128;
            }
        };
    }
}
