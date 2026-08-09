/*
 * Copyright 2013 The Netty Project
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
package io.netty.buffer;


import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;

import java.nio.ByteBuffer;


/**
 * 包装只读直接 {@link ByteBuffer} 的只读 {@link ByteBuf}，通过 Unsafe 实现高性能读取。
 */
final class ReadOnlyUnsafeDirectByteBuf extends ReadOnlyByteBufferBuf {
    /** 直接缓冲区的本地内存基址。 */
    /** 直接缓冲区的本地内存基址。 */
    private final long memoryAddress;

    ReadOnlyUnsafeDirectByteBuf(ByteBufAllocator allocator, ByteBuffer byteBuffer) {
        super(allocator, byteBuffer);
        // 使用字段 buffer：父类会 slice 入参，position≠0 时地址可能变化
        memoryAddress = PlatformDependent.directBufferAddress(buffer);
    }

    @Override
    protected byte _getByte(int index) {
        return UnsafeByteBufUtil.getByte(addr(index));
    }

    @Override
    protected short _getShort(int index) {
        return UnsafeByteBufUtil.getShort(addr(index));
    }

    @Override
    protected int _getUnsignedMedium(int index) {
        return UnsafeByteBufUtil.getUnsignedMedium(addr(index));
    }

    @Override
    protected int _getInt(int index) {
        return UnsafeByteBufUtil.getInt(addr(index));
    }

    @Override
    protected long _getLong(int index) {
        return UnsafeByteBufUtil.getLong(addr(index));
    }

    @Override
    protected ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length, boolean internal) {
        checkIndex(index, length);
        ObjectUtil.checkNotNull(dst, "dst");
        if (dstIndex < 0 || dstIndex > dst.capacity() - length) {
            throw new IndexOutOfBoundsException("dstIndex: " + dstIndex);
        }

        if (dst.hasMemoryAddress()) {
            PlatformDependent.copyMemory(addr(index), dst.memoryAddress() + dstIndex, length);
        } else if (dst.hasArray()) {
            PlatformDependent.copyMemory(addr(index), dst.array(), dst.arrayOffset() + dstIndex, length);
        } else {
            dst.setBytes(dstIndex, this, index, length);
        }
        return this;
    }

    @Override
    protected ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length, boolean internal) {
        checkIndex(index, length);
        ObjectUtil.checkNotNull(dst, "dst");
        if (dstIndex < 0 || dstIndex > dst.length - length) {
            throw new IndexOutOfBoundsException(String.format(
                    "dstIndex: %d, length: %d (expected: range(0, %d))", dstIndex, length, dst.length));
        }

        if (length != 0) {
            PlatformDependent.copyMemory(addr(index), dst, dstIndex, length);
        }
        return this;
    }

    @Override
    public ByteBuf copy(int index, int length) {
        checkIndex(index, length);
        ByteBuf copy = alloc().directBuffer(length, maxCapacity());
        if (length != 0) {
            if (copy.hasMemoryAddress()) {
                PlatformDependent.copyMemory(addr(index), copy.memoryAddress(), length);
                copy.setIndex(0, length);
            } else {
                copy.writeBytes(this, index, length);
            }
        }
        return copy;
    }

    @Override
    public boolean hasMemoryAddress() {
        return true;
    }

    @Override
    public long memoryAddress() {
        return memoryAddress;
    }

    private long addr(int index) {
        return memoryAddress + index;
    }
}
