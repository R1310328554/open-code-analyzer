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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOutboundBuffer.MessageProcessor;
import io.netty.util.internal.CleanableDirectBuffer;
import io.netty.util.internal.PlatformDependent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static io.netty.channel.unix.Limits.IOV_MAX;
import static io.netty.channel.unix.Limits.SSIZE_MAX;
import static io.netty.util.internal.ObjectUtil.checkPositive;
import static java.lang.Math.min;

/**
 * Represent an array of struct array and so can be passed directly over via JNI without the need to do any more
 * array copies.
 * <p>堆外 {@code struct iovec} 数组：将多个 {@link ByteBuf} 片段写入连续 direct 内存， 供 {@code writev(2)} JNI 零拷贝调用；实现 {@link MessageProcessor} 供  {@link io.netty.channel.ChannelOutboundBuffer} 遍历。</p>
 *
 * The buffers are written out directly into direct memory to match the struct iov. See also {@code man writev}.
 *
 * <pre>
 * struct iovec {
 *   void  *iov_base;
 *   size_t iov_len;
 * };
 * </pre>
 *
 * See also
 * <a href="https://rkennke.wordpress.com/2007/07/30/efficient-jni-programming-iv-wrapping-native-data-objects/"
 * >Efficient JNI programming IV: Wrapping native data objects</a>.
 */
public final class IovArray implements MessageProcessor {

    /** 指针宽度：64 位为 8，32 位为 4（决定 iovec 布局） */
    private static final int ADDRESS_SIZE = Buffer.addressSize();

    /**
     * The size of an {@code iovec} struct in bytes. This is calculated as we have 2 entries each of the size of the
     * address.
     * <p>单个 iovec 字节数 = 2 × 地址宽度（base + len）。</p>
     */
    public static final int IOV_SIZE = 2 * ADDRESS_SIZE;

    /**
     * The needed memory to hold up to {@code IOV_MAX} iov entries, where {@code IOV_MAX} signified
     * the maximum number of {@code iovec} structs that can be passed to {@code writev(...)}.
     * <p>按 {@link Limits#IOV_MAX} 预分配的最大堆外容量。</p>
     */
    private static final int MAX_CAPACITY = IOV_MAX * IOV_SIZE;

    private final long memoryAddress;
    private final ByteBuf memory;
    private final CleanableDirectBuffer cleanable;
    private int count;
    private long size;
    private long maxBytes = SSIZE_MAX;
    private int maxCount;

    /**
     * @deprecated Use {@link #IovArray(int)} instead.
      * <p>Netty Unix 原生传输 API；详见上方英文说明。</p>
     */
    @Deprecated
    public IovArray() {
        this(IOV_MAX);
    }

    /**
     * Allocate an IovArray with enough room for the given number of <strong>entries</strong> (not bytes).
     * @param numEntries The desired number of entries in the IovArray.
     * <p>分配可容纳 {@code numEntries} 个 iovec 的 direct 缓冲。</p>
     */
    @SuppressWarnings("deprecation")
    public IovArray(int numEntries) {
        int sizeBytes = Math.multiplyExact(checkPositive(numEntries, "numEntries"), IOV_SIZE);
        cleanable = Buffer.allocateDirectBufferWithNativeOrder(sizeBytes);
        ByteBuf bbuf = Unpooled.wrappedBuffer(cleanable.buffer()).setIndex(0, 0);
        memory = PlatformDependent.hasUnsafe() ? bbuf : bbuf.order(
                PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        if (memory.hasMemoryAddress()) {
            memoryAddress = memory.memoryAddress();
        } else {
            // 无法直接取 memoryAddress 时经 JNI 解析 NIO 缓冲地址
            // 使用 internalNioBuffer 减少分配；须加上 position 以应对共享 ByteBuffer
            ByteBuffer byteBuffer = memory.internalNioBuffer(0, memory.capacity());
            memoryAddress = Buffer.memoryAddress(byteBuffer) + byteBuffer.position();
        }
        maxCount = IOV_MAX;
    }

    /**
     * @param memory The underlying memory.
     * @deprecated Use {@link #IovArray(int)} instead.
      * <p>Netty Unix 原生传输 API；详见上方英文说明。</p>
     */
    @Deprecated
    public IovArray(ByteBuf memory) {
        assert memory.writerIndex() == 0;
        assert memory.readerIndex() == 0;
        this.memory = PlatformDependent.hasUnsafe() ? memory : memory.order(
                PlatformDependent.BIG_ENDIAN_NATIVE_ORDER ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        if (memory.hasMemoryAddress()) {
            memoryAddress = memory.memoryAddress();
        } else {
            // Fallback to using JNI as we were not be able to access the address otherwise.

            // Use internalNioBuffer to reduce object creation.
            // It is important to add the position as the returned ByteBuffer might be shared by multiple ByteBuf
            // instances and so has an address that starts before the start of the ByteBuf itself.
            ByteBuffer byteBuffer = memory.internalNioBuffer(0, memory.capacity());
            memoryAddress = Buffer.memoryAddress(byteBuffer) + byteBuffer.position();
        }
        cleanable = null;
        maxCount = IOV_MAX;
    }

    public void clear() {
        count = 0;
        size = 0;
        maxCount = IOV_MAX;
    }

    /**
     * @deprecated Use {@link #add(ByteBuf, int, int)}
      * <p>Netty Unix 原生传输 API；详见上方英文说明。</p>
     */
    @Deprecated
    public boolean add(ByteBuf buf) {
        return add(buf, buf.readerIndex(), buf.readableBytes());
    }

    public boolean add(ByteBuf buf, int offset, int len) {
        if (count == maxCount) {
            // 已达 maxCount 上限
            return false;
        }
        if (buf.nioBufferCount() == 1) {
            if (len == 0) {
                return true;
            }
            if (buf.hasMemoryAddress()) {
                return add(memoryAddress, buf.memoryAddress() + offset, len);
            } else {
                ByteBuffer nioBuffer = buf.internalNioBuffer(offset, len);
                return add(memoryAddress, Buffer.memoryAddress(nioBuffer) + nioBuffer.position(), len);
            }
        } else {
            ByteBuffer[] buffers = buf.nioBuffers(offset, len);
            for (ByteBuffer nioBuffer : buffers) {
                final int remaining = nioBuffer.remaining();
                if (remaining != 0 &&
                        (!add(memoryAddress, Buffer.memoryAddress(nioBuffer) + nioBuffer.position(), remaining)
                                || count == IOV_MAX)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Return {@code true} if there is no more space left in the {@link IovArray}.
     *
     * @return full or not.
     * <p>容量或 maxBytes 已满时返回 {@code true}。</p>
     */
    public boolean isFull() {
        return memory.capacity() < (count + 1) * IOV_SIZE || size >= maxBytes;
    }

    private boolean add(long memoryAddress, long addr, int len) {
        assert addr != 0;

        // 已有条目时 enforce maxBytes；至少保留一条以保证 writev 能推进
        if ((maxBytes - len < size && count > 0) ||
                // Check if we have enough space left
                memory.capacity() < (count + 1) * IOV_SIZE) {
            // 累计字节超过 SSIZE_MAX 时停止添加，否则 writev 返回 EINVAL
            //
            // See also:
            // - https://linux.die.net//man/2/writev
            return false;
        }
        final int baseOffset = idx(count);
        final int lengthOffset = baseOffset + ADDRESS_SIZE;

        size += len;
        ++count;

        if (ADDRESS_SIZE == 8) {
            // 64bit
            if (PlatformDependent.hasUnsafe()) {
                PlatformDependent.putLong(baseOffset + memoryAddress, addr);
                PlatformDependent.putLong(lengthOffset + memoryAddress, len);
            } else {
                memory.setLong(baseOffset, addr);
                memory.setLong(lengthOffset, len);
            }
        } else {
            assert ADDRESS_SIZE == 4;
            if (PlatformDependent.hasUnsafe()) {
                PlatformDependent.putInt(baseOffset + memoryAddress, (int) addr);
                PlatformDependent.putInt(lengthOffset + memoryAddress, len);
            } else {
                memory.setInt(baseOffset, (int) addr);
                memory.setInt(lengthOffset, len);
            }
        }
        return true;
    }

    /**
     * Returns the number if iov entries.
     * <p>当前已填充的 iovec 条数。</p>
     */
    public int count() {
        return count;
    }

    /**
     * Returns the size in bytes
     * <p>返回累计字节数。</p>
     */
    public long size() {
        return size;
    }

    /**
     * Set the maximum amount of bytes that can be added to this {@link IovArray} via {@link #add(ByteBuf, int, int)}
     * <p>
     * This will not impact the existing state of the {@link IovArray}, and only applies to subsequent calls to
     * {@link #add(ByteBuf)}.
     * <p>
     * In order to ensure some progress is made at least one {@link ByteBuf} will be accepted even if it's size exceeds
     * this value.
     * @param maxBytes the maximum amount of bytes that can be added to this {@link IovArray}.
      * <p>Netty Unix 原生传输 API；详见上方英文说明。</p>
     */
    public void maxBytes(long maxBytes) {
        this.maxBytes = min(SSIZE_MAX, checkPositive(maxBytes, "maxBytes"));
    }

    /**
     * Set the maximum amount of buffers that can be added to this {@link IovArray} via {@link #add(ByteBuf, int, int)}
     * <p>
     * This will not impact the existing state of the {@link IovArray}, and only applies to subsequent calls to
     * {@link #add(ByteBuf)}.
     * <p>
     * @param maxCount the maximum amount of bytes that can be added to this {@link IovArray}.
      * <p>Netty Unix 原生传输 API；详见上方英文说明。</p>
     */
    public void maxCount(int maxCount) {
        this.maxCount = min(IOV_MAX, checkPositive(maxCount, "maxCount"));
    }

    /**
     * Get the maximum amount of bytes that can be added to this {@link IovArray}.
     * @return the maximum amount of bytes that can be added to this {@link IovArray}.
      * <p>Netty Unix 原生传输 API；详见上方英文说明。</p>
     */
    public long maxBytes() {
        return maxBytes;
    }

    /**
     * Get the maximum amount of buffers that can be added to this {@link IovArray}.
     * @return the maximum amount of buffers that can be added to this {@link IovArray}.
      * <p>Netty Unix 原生传输 API；详见上方英文说明。</p>
     */
    public int maxCount() {
        return maxCount;
    }

    /**
     * Returns the {@code memoryAddress} for the given {@code offset}.
      * <p>Netty Unix 原生传输 API；详见上方英文说明。</p>
     */
    public long memoryAddress(int offset) {
        return memoryAddress + idx(offset);
    }

    /**
     * Release the {@link IovArray}. Once release further using of it may crash the JVM!
     * <p>释放堆外内存；释放后不可再使用。</p>
     */
    public void release() {
        memory.release();
        if (cleanable != null) {
            // 外部传入 ByteBuf 时 cleanable 为 null，仅 release memory
            cleanable.clean();
        }
    }

    @Override
    public boolean processMessage(Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buffer = (ByteBuf) msg;
            return add(buffer, buffer.readerIndex(), buffer.readableBytes());
        }
        return false;
    }

    private static int idx(int index) {
        return IOV_SIZE * index;
    }
}
