/*
 * Copyright 2018 The Netty Project
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

import io.netty.util.internal.CleanableDirectBuffer;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.UnstableApi;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Unix 原生传输堆外缓冲工具：分配、地址与字节序。
 * <p>为 epoll/kqueue/io_uring 等 JNI 层提供 direct buffer 与指针大小查询。</p>
 */
@UnstableApi
public final class Buffer {

    private Buffer() { }

    /**
     * Free the direct {@link ByteBuffer}.
     * @deprecated Use {@link #allocateDirectBufferWithNativeOrder(int)} instead.
     * <p>释放 direct ByteBuffer（已废弃，请用 CleanableDirectBuffer）。</p>
     */
    @Deprecated
    public static void free(ByteBuffer buffer) {
        PlatformDependent.freeDirectBuffer(buffer);
    }

    /**
     * Returns a new {@link ByteBuffer} which has the same {@link ByteOrder} as the native order of the machine.
     * @deprecated Use {@link #allocateDirectBufferWithNativeOrder(int)} instead.
     * <p>分配与机器 native 字节序一致的 direct 缓冲（已废弃）。</p>
     */
    @Deprecated
    public static ByteBuffer allocateDirectWithNativeOrder(int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(
                PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Returns a new {@link CleanableDirectBuffer} which has the same {@link ByteOrder} as the native order of the
     * machine.
     * <p>分配 native 字节序的 {@link CleanableDirectBuffer}，供 JNI 与自动清理。</p>
     */
    public static CleanableDirectBuffer allocateDirectBufferWithNativeOrder(int capacity) {
        CleanableDirectBuffer cleanableDirectBuffer = PlatformDependent.allocateDirect(capacity);
        cleanableDirectBuffer.buffer().order(
                PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        return cleanableDirectBuffer;
    }

    /**
     * Returns the memory address of the given direct {@link ByteBuffer}.
     * <p>返回 direct ByteBuffer 的堆外起始地址。</p>
     */
    public static long memoryAddress(ByteBuffer buffer) {
        assert buffer.isDirect();
        if (PlatformDependent.hasDirectByteBufferAddress(buffer)) {
            return PlatformDependent.directBufferAddress(buffer);
        }
        return memoryAddress0(buffer);
    }

    /**
     * Returns the size of a pointer.
     * <p>返回指针大小（4 或 8 字节）；无 Unsafe 时走 JNI。</p>
     */
    public static int addressSize() {
        if (PlatformDependent.hasUnsafe()) {
            return PlatformDependent.addressSize();
        }
        return addressSize0();
    }

    // 无 Unsafe 时 addressSize/memoryAddress 走 JNI
    private static native int addressSize0();
    private static native long memoryAddress0(ByteBuffer buffer);

    public static ByteBuffer wrapMemoryAddressWithNativeOrder(long memoryAddress, int capacity) {
        return wrapMemoryAddress(memoryAddress, capacity).order(ByteOrder.nativeOrder());
    }

    public static native ByteBuffer wrapMemoryAddress(long memoryAddress, int capacity);
}
