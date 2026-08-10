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

import io.netty.channel.ChannelOption;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.unix.UnixChannelOption;

/**
 * KQueue/BSD 特有 {@link ChannelOption} 定义。
 * <p>含 SO_SNDLOWAT、TCP_NOPUSH、SO_ACCEPTFILTER 等 macOS/FreeBSD 选项。</p>
 */
public final class KQueueChannelOption<T> extends UnixChannelOption<T> {
    /** 发送低水位（SO_SNDLOWAT）：可写事件触发阈值 */
    public static final ChannelOption<Integer> SO_SNDLOWAT = valueOf(KQueueChannelOption.class, "SO_SNDLOWAT");
    /** TCP_NOPUSH：延迟小包发送直至缓冲区满或显式取消 */
    public static final ChannelOption<Boolean> TCP_NOPUSH = valueOf(KQueueChannelOption.class, "TCP_NOPUSH");
    /** 监听套接字 accept 内核过滤器（如 httpready） */
    public static final ChannelOption<AcceptFilter> SO_ACCEPTFILTER =
            valueOf(KQueueChannelOption.class, "SO_ACCEPTFILTER");
    /**
     * If this is {@code true} then the {@link RecvByteBufAllocator.Handle#guess()} will be overridden to always attempt
     * to read as many bytes as kqueue says are available.
     * <p>已废弃：曾控制接收分配器是否采用 kqueue 可读字节猜测。</p>
     */
    @Deprecated
    public static final ChannelOption<Boolean> RCV_ALLOC_TRANSPORT_PROVIDES_GUESS =
            valueOf(KQueueChannelOption.class, "RCV_ALLOC_TRANSPORT_PROVIDES_GUESS");

    @SuppressWarnings({ "unused", "deprecation" })
    private KQueueChannelOption() {
    }
}
