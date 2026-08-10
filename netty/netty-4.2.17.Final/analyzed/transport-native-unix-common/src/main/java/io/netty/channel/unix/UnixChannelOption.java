/*
 * Copyright 2014 The Netty Project
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
package io.netty.channel.unix;

import io.netty.channel.ChannelOption;

/**
 * Unix 原生传输专用 {@link ChannelOption} 定义。
 * <p>扩展标准 Channel 选项，暴露 Linux/BSD 特有的套接字行为。</p>
 */
public class UnixChannelOption<T> extends ChannelOption<T> {
    /** {@code SO_REUSEPORT}：允许多套接字绑定同一端口（需内核支持） */
    public static final ChannelOption<Boolean> SO_REUSEPORT = valueOf(UnixChannelOption.class, "SO_REUSEPORT");
    /** Unix 域通道读取模式：字节流或 FD 传递 */
    public static final ChannelOption<DomainSocketReadMode> DOMAIN_SOCKET_READ_MODE =
            ChannelOption.valueOf(UnixChannelOption.class, "DOMAIN_SOCKET_READ_MODE");

    @SuppressWarnings({ "unused", "deprecation" })
    protected UnixChannelOption() {
        super(null);
    }

    UnixChannelOption(String name) {
        super(name);
    }
}
