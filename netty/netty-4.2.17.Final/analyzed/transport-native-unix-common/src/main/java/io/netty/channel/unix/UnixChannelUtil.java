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
package io.netty.channel.unix;

import io.netty.buffer.ByteBuf;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static io.netty.channel.unix.Limits.IOV_MAX;

/**
 * Unix 原生通道工具方法：写路径缓冲拷贝判定与远程地址合并。
 * <p>供 epoll/kqueue 等在 JNI 写之前判断是否需要聚合/复制 {@link ByteBuf}。</p>
 */
public final class UnixChannelUtil {

    private UnixChannelUtil() {
    }

    /**
     * Checks if the specified buffer has memory address or is composed of n(n <= IOV_MAX) NIO direct buffers.
     * (We check this because otherwise we need to make it a new direct buffer.)
     * <p>若无法直接 writev（无 memoryAddress、非 direct 或 nioBufferCount &gt; IOV_MAX）， 写路径需拷贝为 direct 缓冲。</p>
     */
    public static boolean isBufferCopyNeededForWrite(ByteBuf byteBuf) {
        return isBufferCopyNeededForWrite(byteBuf, IOV_MAX);
    }

    /** 内部重载：可指定 iov 上限 */
    static boolean isBufferCopyNeededForWrite(ByteBuf byteBuf, int iovMax) {
        return !byteBuf.hasMemoryAddress() && (!byteBuf.isDirect() || byteBuf.nioBufferCount() > iovMax);
    }

    /** 合并用户配置的 remote 与内核 getpeername 结果，保留 hostname 语义 */
    public static InetSocketAddress computeRemoteAddr(InetSocketAddress remoteAddr, InetSocketAddress osRemoteAddr) {
        if (osRemoteAddr != null) {
            try {
                // Java 7+ 才用 getHostString 合并 hostname，避免 EventLoop 上触发 DNS 反查
                return new InetSocketAddress(InetAddress.getByAddress(remoteAddr.getHostString(),
                        osRemoteAddr.getAddress().getAddress()),
                        osRemoteAddr.getPort());
            } catch (UnknownHostException ignore) {
                // 不应发生；回退使用 OS 返回的地址
            }
            return osRemoteAddr;
        }
        return remoteAddr;
    }
}
