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
package io.netty.channel.local;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.util.internal.StringUtil;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 本地 channel 地址注册表，维护 {@link LocalAddress} 到 {@link Channel} 的全局映射。
 */
final class LocalChannelRegistry {

    /** 已绑定地址 -> channel 的并发映射。 */
    private static final ConcurrentMap<LocalAddress, Channel> boundChannels = new ConcurrentHashMap<>();

    /**
     * 将 channel 绑定到指定本地地址；{@link LocalAddress#ANY} 会自动分配临时地址。
     */
    static LocalAddress register(
            Channel channel, LocalAddress oldLocalAddress, SocketAddress localAddress) {
        if (oldLocalAddress != null) {
            throw new ChannelException("already bound");
        }
        if (!(localAddress instanceof LocalAddress)) {
            throw new ChannelException("unsupported address type: " + StringUtil.simpleClassName(localAddress));
        }

        LocalAddress addr = (LocalAddress) localAddress;
        if (LocalAddress.ANY.equals(addr)) {
            addr = new LocalAddress(channel);
        }

        Channel boundChannel = boundChannels.putIfAbsent(addr, channel);
        if (boundChannel != null) {
            throw new ChannelException("address already in use by: " + boundChannel);
        }
        return addr;
    }

    /** 按地址查找已绑定的 channel。 */
    static Channel get(SocketAddress localAddress) {
        return boundChannels.get(localAddress);
    }

    /** 解除地址绑定。 */
    static void unregister(LocalAddress localAddress) {
        boundChannels.remove(localAddress);
    }

    private LocalChannelRegistry() {
        // Unused
    }
}
