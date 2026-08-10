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

import io.netty.channel.DefaultFileRegion;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.SocketProtocolFamily;
import io.netty.channel.unix.IovArray;
import io.netty.channel.unix.PeerCredentials;
import io.netty.channel.unix.Socket;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import static io.netty.channel.kqueue.AcceptFilter.PLATFORM_UNSUPPORTED;
import static io.netty.channel.kqueue.Native.CONNECT_TCP_FASTOPEN;
import static io.netty.channel.unix.Errors.ERRNO_EINPROGRESS_NEGATIVE;
import static io.netty.channel.unix.Errors.ioResult;
import static io.netty.channel.unix.NativeInetAddress.ipv4MappedIpv6Address;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * A socket which provides access BSD native methods.
 * <p>封装 BSD/macOS 原生 socket：accept 过滤器、TCP_NOPUSH、SO_SNDLOWAT、 TCP FastOpen、sendfile 与 {@code connectx(2)} 等。</p>
 */
final class BsdSocket extends Socket {

    // SO_SNDLOWAT 上限：基于实测，Apple 与 FreeBSD 取较小值
    /** macOS 上 SO_SNDLOWAT 观测上限 */
    private static final int APPLE_SND_LOW_AT_MAX = 1 << 17;
    private static final int FREEBSD_SND_LOW_AT_MAX = 1 << 15;
    /** 跨 BSD 平台 SO_SNDLOWAT 安全上限 */
    static final int BSD_SND_LOW_AT_MAX = Math.min(APPLE_SND_LOW_AT_MAX, FREEBSD_SND_LOW_AT_MAX);
    /**
     * The `endpoints` structure passed to `connectx(2)` has an optional "source interface" field,
     * which is the index of the network interface to use.
     * According to `if_nametoindex(3)`, the value 0 is used when no interface is specified.
      * <p>Netty KQueue 传输 API；详见上方英文说明。</p>
     */
    /** connectx 未指定源网卡时使用 0（见 if_nametoindex(3)） */
    private static final int UNSPECIFIED_SOURCE_INTERFACE = 0;

    BsdSocket(int fd) {
        super(fd);
    }

    /** 在监听套接字上设置 SO_ACCEPTFILTER */
    void setAcceptFilter(AcceptFilter acceptFilter) throws IOException {
        setAcceptFilter(intValue(), acceptFilter.filterName(), acceptFilter.filterArgs());
    }

    /** 设置 TCP_NOPUSH（类似 Linux TCP_CORK，延迟小包发送） */
    void setTcpNoPush(boolean tcpNoPush) throws IOException {
        setTcpNoPush(intValue(), tcpNoPush ? 1 : 0);
    }

    void setSndLowAt(int lowAt) throws IOException {
        setSndLowAt(intValue(), lowAt);
    }

    /** 启用或禁用 TCP FastOpen 套接字选项 */
    public void setTcpFastOpen(boolean enableTcpFastOpen) throws IOException {
        setTcpFastOpen(intValue(), enableTcpFastOpen ? 1 : 0);
    }

    boolean isTcpNoPush() throws IOException {
        return getTcpNoPush(intValue()) != 0;
    }

    int getSndLowAt() throws IOException {
        return getSndLowAt(intValue());
    }

    /** 读取当前 SO_ACCEPTFILTER；不支持时返回 {@link AcceptFilter#PLATFORM_UNSUPPORTED} */
    AcceptFilter getAcceptFilter() throws IOException {
        String[] result = getAcceptFilter(intValue());
        return result == null ? PLATFORM_UNSUPPORTED : new AcceptFilter(result[0], result[1]);
    }

    public boolean isTcpFastOpen() throws IOException {
        return isTcpFastOpen(intValue()) != 0;
    }

    /** 读取对端 Unix 凭证（uid/gid/pid，SO_PEERCRED） */
    PeerCredentials getPeerCredentials() throws IOException {
        return getPeerCredentials(intValue());
    }

    long sendFile(DefaultFileRegion src, long baseOffset, long offset, long length) throws IOException {
        // 惰性构造的 FileRegion 需先 open，JNI 直接访问 FileChannel
        src.open();

        long res = sendFile(intValue(), src, baseOffset, offset, length);
        if (res >= 0) {
            return res;
        }
        return ioResult("sendfile", (int) res);
    }

    /**
     * Establish a connection to the given destination address, and send the given data to it.
     *
     * <strong>Note:</strong> This method relies on the {@code connectx(2)} system call, which is MacOS specific.
     * <p>macOS 专用 {@code connectx(2)}：建立连接并可附带首包数据；支持 TCP FastOpen 标志。</p>
     *
     * @param source      the source address we are connecting from.
     * @param destination the destination address we are connecting to.
     * @param data        the data to copy to the kernel-side socket buffer.
     * @param tcpFastOpen if {@code true}, set the flags needed to enable TCP FastOpen connecting.
     * @return The number of bytes copied to the kernel-side socket buffer, or the number of bytes sent to the
     * destination. This number is <em>negative</em> if connecting is left in an in-progress state,
     * or <em>positive</em> if the connection was immediately established.
     * @throws IOException if an IO error occurs, if the {@code data} is too big to send in one go,
     * or if the system call is not supported on your platform.
     */
    int connectx(InetSocketAddress source, InetSocketAddress destination, IovArray data, boolean tcpFastOpen)
            throws IOException {
        checkNotNull(destination, "Destination InetSocketAddress cannot be null.");
        int flags = tcpFastOpen ? CONNECT_TCP_FASTOPEN : 0;

        boolean sourceIPv6;
        byte[] sourceAddress;
        int sourceScopeId;
        int sourcePort;
        if (source == null) {
            sourceIPv6 = false;
            sourceAddress = null;
            sourceScopeId = 0;
            sourcePort = 0;
        } else {
            InetAddress sourceInetAddress = source.getAddress();
            sourceIPv6 = useIpv6(this, sourceInetAddress);
            if (sourceInetAddress instanceof Inet6Address) {
                sourceAddress = sourceInetAddress.getAddress();
                sourceScopeId = ((Inet6Address) sourceInetAddress).getScopeId();
            } else {
                // IPv4 地址转为 IPv4-mapped IPv6 以匹配 connectx 端点格式
                sourceScopeId = 0;
                sourceAddress = ipv4MappedIpv6Address(sourceInetAddress.getAddress());
            }
            sourcePort = source.getPort();
        }

        InetAddress destinationInetAddress = destination.getAddress();
        boolean destinationIPv6 = useIpv6(this, destinationInetAddress);
        byte[] destinationAddress;
        int destinationScopeId;
        if (destinationInetAddress instanceof Inet6Address) {
            destinationAddress = destinationInetAddress.getAddress();
            destinationScopeId = ((Inet6Address) destinationInetAddress).getScopeId();
        } else {
            // convert to ipv4 mapped ipv6 address;
            destinationScopeId = 0;
            destinationAddress = ipv4MappedIpv6Address(destinationInetAddress.getAddress());
        }
        int destinationPort = destination.getPort();

        long iovAddress;
        int iovCount;
        int iovDataLength;
        if (data == null || data.count() == 0) {
            iovAddress = 0;
            iovCount = 0;
            iovDataLength = 0;
        } else {
            iovAddress = data.memoryAddress(0);
            iovCount = data.count();
            long size = data.size();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("IovArray.size() too big: " + size + " bytes.");
            }
            iovDataLength = (int) size;
        }

        int result = connectx(intValue(),
                UNSPECIFIED_SOURCE_INTERFACE, sourceIPv6, sourceAddress, sourceScopeId, sourcePort,
                destinationIPv6, destinationAddress, destinationScopeId, destinationPort,
                flags, iovAddress, iovCount, iovDataLength);
        if (result == ERRNO_EINPROGRESS_NEGATIVE) {
            // 非阻塞 EINPROGRESS 为正常路径；可写事件表示连接完成
            // 告知 channel 内核已拷贝的数据量，以便更新 outbound 缓冲位置
            return -iovDataLength;
        }
        if (result < 0) {
            return ioResult("connectx", result);
        }
        return result;
    }

    /** 创建 TCP 流式套接字（默认协议族） */
    public static BsdSocket newSocketStream() {
        return new BsdSocket(newSocketStream0());
    }

    /**
     * @deprecated use {@link #newSocketStream(SocketProtocolFamily)}
      * <p>Netty KQueue 传输 API；详见上方英文说明。</p>
     */
    public static BsdSocket newSocketStream(InternetProtocolFamily protocol) {
        return new BsdSocket(newSocketStream0(protocol));
    }

    public static BsdSocket newSocketStream(SocketProtocolFamily protocol) {
        return new BsdSocket(newSocketStream0(protocol));
    }

    public static BsdSocket newSocketDgram() {
        return new BsdSocket(newSocketDgram0());
    }

    /**
     * @deprecated use {@link #newSocketDgram(SocketProtocolFamily)}
      * <p>Netty KQueue 传输 API；详见上方英文说明。</p>
     */
    public static BsdSocket newSocketDgram(InternetProtocolFamily protocol) {
        return new BsdSocket(newSocketDgram0(protocol));
    }

    public static BsdSocket newSocketDgram(SocketProtocolFamily protocol) {
        return new BsdSocket(newSocketDgram0(protocol));
    }

    /** 创建 Unix 域流式套接字 */
    public static BsdSocket newSocketDomain() {
        return new BsdSocket(newSocketDomain0());
    }

    /** 创建 Unix 域数据报套接字 */
    public static BsdSocket newSocketDomainDgram() {
        return new BsdSocket(newSocketDomainDgram0());
    }

    private static native long sendFile(int socketFd, DefaultFileRegion src, long baseOffset,
                                        long offset, long length) throws IOException;

    /**
     * @return If successful, zero or positive number of bytes transfered, otherwise negative errno.
     * <p>成功返回传输字节数，失败返回负 errno。</p>
     */
    private static native int connectx(
            int socketFd,
            // sa_endpoints_t *endpoints:
            int sourceInterface,
            boolean sourceIPv6, byte[] sourceAddress, int sourceScopeId, int sourcePort,
            boolean destinationIPv6, byte[] destinationAddress, int destinationScopeId, int destinationPort,
            // sae_associd_t associd is reserved
            int flags,
            long iovAddress, int iovCount, int iovDataLength
            // sae_connid_t *connid is reserved
    );

    private static native String[] getAcceptFilter(int fd) throws IOException;

    private static native int getTcpNoPush(int fd) throws IOException;

    private static native int getSndLowAt(int fd) throws IOException;

    private static native int isTcpFastOpen(int fd) throws IOException;

    private static native PeerCredentials getPeerCredentials(int fd) throws IOException;

    private static native void setAcceptFilter(int fd, String filterName, String filterArgs) throws IOException;

    private static native void setTcpNoPush(int fd, int tcpNoPush) throws IOException;

    private static native void setSndLowAt(int fd, int lowAt) throws IOException;

    private static native void setTcpFastOpen(int fd, int enableFastOpen) throws IOException;
}
