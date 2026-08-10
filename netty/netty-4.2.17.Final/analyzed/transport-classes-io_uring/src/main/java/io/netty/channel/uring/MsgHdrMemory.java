/*
 * Copyright 2024 The Netty Project
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

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.unix.Buffer;
import io.netty.util.internal.CleanableDirectBuffer;
import io.netty.util.internal.PlatformDependent;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 单条 msghdr 及其关联 sockaddr/iovec/cmsg 的堆外内存布局。
 * <p>支持独立分配或从 {@link MsgHdrMemoryArray} 切片复用。</p>
 * <p>供 datagram sendmsg/recvmsg 与 fd 传递使用。</p>
 */
final class MsgHdrMemory {
    public static final int MSG_HDR_SIZE =
            Native.SIZEOF_MSGHDR + Native.SIZEOF_SOCKADDR_STORAGE + Native.SIZEOF_IOVEC + Native.CMSG_SPACE;
    private static final byte[] EMPTY_SOCKADDR_STORAGE = new byte[Native.SIZEOF_SOCKADDR_STORAGE];
    // sendFd 不允许零长度 iovec，故使用 1 字节占位 buffer
    private static final int GLOBAL_IOV_LEN = 1;
    private static final ByteBuffer GLOBAL_IOV_BASE =  Buffer.allocateDirectWithNativeOrder(GLOBAL_IOV_LEN);
    private static final long GLOBAL_IOV_BASE_ADDRESS = Buffer.memoryAddress(GLOBAL_IOV_BASE);
    private final CleanableDirectBuffer msgHdrMemoryCleanable;
    private final CleanableDirectBuffer socketAddrMemoryCleanable;
    private final CleanableDirectBuffer iovMemoryCleanable;
    private final CleanableDirectBuffer cmsgDataMemoryCleanable;
    private final ByteBuffer msgHdrMemory;
    private final ByteBuffer socketAddrMemory;
    private final ByteBuffer iovMemory;
    private final ByteBuffer cmsgDataMemory;

    private final long msgHdrMemoryAddress;
    private final short idx;
    private final int cmsgDataOffset;

    MsgHdrMemory(short idx, ByteBuffer msgHdrMemoryArray) {
        this.idx = idx;
        this.msgHdrMemoryCleanable = null;
        this.socketAddrMemoryCleanable = null;
        this.iovMemoryCleanable = null;
        this.cmsgDataMemoryCleanable = null;
        int offset = idx * MSG_HDR_SIZE;
        // slice/duplicate 默认为 BIG_ENDIAN；显式设为 nativeOrder 以正确写入 C 结构体
        this.msgHdrMemory = PlatformDependent.offsetSlice(
                msgHdrMemoryArray, offset, Native.SIZEOF_MSGHDR
        ).order(ByteOrder.nativeOrder());
        offset += Native.SIZEOF_MSGHDR;
        this.socketAddrMemory = PlatformDependent.offsetSlice(
                msgHdrMemoryArray, offset, Native.SIZEOF_SOCKADDR_STORAGE
        ).order(ByteOrder.nativeOrder());
        offset += Native.SIZEOF_SOCKADDR_STORAGE;
        this.iovMemory = PlatformDependent.offsetSlice(
                msgHdrMemoryArray, offset, Native.SIZEOF_IOVEC
        ).order(ByteOrder.nativeOrder());
        offset += Native.SIZEOF_IOVEC;
        this.cmsgDataMemory = PlatformDependent.offsetSlice(
                msgHdrMemoryArray, offset, Native.CMSG_SPACE
        ).order(ByteOrder.nativeOrder());

        msgHdrMemoryAddress = Buffer.memoryAddress(msgHdrMemory);

        long cmsgDataMemoryAddr = Buffer.memoryAddress(cmsgDataMemory);
        long cmsgDataAddr = Native.cmsghdrData(cmsgDataMemoryAddr);
        cmsgDataOffset = (int) (cmsgDataAddr - cmsgDataMemoryAddr);
    }

    MsgHdrMemory() {
        this.idx = 0;
        // JDK 分配的直接内存已清零，此处无需再 memset
        msgHdrMemoryCleanable = Buffer.allocateDirectBufferWithNativeOrder(Native.SIZEOF_MSGHDR);
        socketAddrMemoryCleanable = null;
        iovMemoryCleanable = Buffer.allocateDirectBufferWithNativeOrder(Native.SIZEOF_IOVEC);
        cmsgDataMemoryCleanable = Buffer.allocateDirectBufferWithNativeOrder(Native.CMSG_SPACE_FOR_FD);

        msgHdrMemory = msgHdrMemoryCleanable.buffer();
        socketAddrMemory = null;
        iovMemory = iovMemoryCleanable.buffer();
        cmsgDataMemory = cmsgDataMemoryCleanable.buffer();

        msgHdrMemoryAddress = Buffer.memoryAddress(msgHdrMemory);
        // iovec 基址与长度不可为 0，否则 recvmsg 得到的 fd 恒为 0
        Iov.set(iovMemory, GLOBAL_IOV_BASE_ADDRESS, GLOBAL_IOV_LEN);

        long cmsgDataMemoryAddr = Buffer.memoryAddress(cmsgDataMemory);
        long cmsgDataAddr = Native.cmsghdrData(cmsgDataMemoryAddr);
        cmsgDataOffset = (int) (cmsgDataAddr - cmsgDataMemoryAddr);
    }

    /** 配置 datagram 发送：目标地址、iovec 与可选 UDP GSO segmentSize */
    void set(LinuxSocket socket, InetSocketAddress address, long bufferAddress , int length, short segmentSize) {
        int addressLength = setSocketAddress(socket, address);
        Iov.set(iovMemory, bufferAddress, length);
        MsgHdr.set(msgHdrMemory, socketAddrMemory, addressLength, iovMemory, 1, cmsgDataMemory,
                cmsgDataOffset, segmentSize);
    }

    void set(long iovArray, int length) {
        MsgHdr.set(msgHdrMemory, iovArray, length);
    }

    void setWithIovArrayAddress(LinuxSocket socket, InetSocketAddress address,
                                long iovArrayAddress, int iovArrayLength, short segmentSize) {
        int addressLength = setSocketAddress(socket, address);
        MsgHdr.set(msgHdrMemory, socketAddrMemory, addressLength, iovArrayAddress, iovArrayLength,
                cmsgDataMemory, cmsgDataOffset, segmentSize);
    }

    private int setSocketAddress(LinuxSocket socket, InetSocketAddress address) {
        int addressLength;
        if (address == null) {
            addressLength = socket.isIpv6() ? Native.SIZEOF_SOCKADDR_IN6 : Native.SIZEOF_SOCKADDR_IN;
            socketAddrMemory.mark();
            try {
                socketAddrMemory.put(EMPTY_SOCKADDR_STORAGE);
            } finally {
                socketAddrMemory.reset();
            }
        } else {
            addressLength = SockaddrIn.set(socket.isIpv6(), socketAddrMemory, address);
        }
        return addressLength;
    }

    /** 配置通过 SCM_RIGHTS 发送指定 fd */
    void setScmRightsFd(int fd) {
        MsgHdr.prepSendFd(msgHdrMemory, fd, cmsgDataMemory, cmsgDataOffset, iovMemory, 1);
    }

    int getScmRightsFd() {
        return MsgHdr.getCmsgData(msgHdrMemory, cmsgDataMemory, cmsgDataOffset);
    }

    /** 准备 recvmsg 以接收 SCM_RIGHTS 传递的 fd */
    void prepRecvReadFd() {
        MsgHdr.prepReadFd(msgHdrMemory, cmsgDataMemory, cmsgDataOffset, iovMemory, 1);
    }

    boolean hasPort(IoUringDatagramChannel channel) {
        if (channel.socket.isIpv6()) {
            return SockaddrIn.hasPortIpv6(socketAddrMemory);
        }
        return SockaddrIn.hasPortIpv4(socketAddrMemory);
    }

    DatagramPacket get(IoUringDatagramChannel channel, IoUringIoHandler handler, ByteBuf buffer, int bytesRead) {
        InetSocketAddress sender;
        if (channel.socket.isIpv6()) {
            byte[] ipv6Bytes = handler.inet6AddressArray();
            byte[] ipv4bytes = handler.inet4AddressArray();

            sender = SockaddrIn.getIPv6(socketAddrMemory, ipv6Bytes, ipv4bytes);
        } else {
            byte[] bytes = handler.inet4AddressArray();
            sender = SockaddrIn.getIPv4(socketAddrMemory, bytes);
        }
        long bufferAddress = Iov.getBufferAddress(iovMemory);
        int bufferLength = Iov.getBufferLength(iovMemory);
        // 根据 buffer 基址与 iovec 中的地址反推 readerIndex
        long memoryAddress = IoUring.memoryAddress(buffer);
        int readerIndex = (int) (bufferAddress - memoryAddress);

        ByteBuf slice = buffer.slice(readerIndex, bufferLength)
                .writerIndex(bytesRead);
        return new DatagramPacket(slice.retain(), channel.localAddress(), sender);
    }

    short idx() {
        return idx;
    }

    long address() {
        return msgHdrMemoryAddress;
    }

    /** 释放独立分配模式下各 CleanableDirectBuffer */
    void release() {
        if (msgHdrMemoryCleanable != null) {
            msgHdrMemoryCleanable.clean();
        }
        if (socketAddrMemoryCleanable != null) {
            socketAddrMemoryCleanable.clean();
        }
        if (iovMemoryCleanable != null) {
            iovMemoryCleanable.clean();
        }
        if (cmsgDataMemoryCleanable != null) {
            cmsgDataMemoryCleanable.clean();
        }
    }
}
