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

package io.netty.buffer;

import io.netty.util.Recycler.EnhancedHandle;
import io.netty.util.internal.ObjectPool.Handle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;

/**
 * 池化 {@link ByteBuf} 抽象基类：内存来自 {@link PoolChunk}，释放时归还 arena 并回收到 {@link Recycler}。
 */
abstract class PooledByteBuf<T> extends AbstractReferenceCountedByteBuf {

    /** {@link Recycler} 句柄，释放后用于对象复用。 */
    /** {@link Recycler} 句柄，释放后用于对象复用。 */
    private final EnhancedHandle<PooledByteBuf<T>> recyclerHandle;

    /** 所属内存块。 */
    /** 所属内存块。 */
    protected PoolChunk<T> chunk;
    /** arena 内分配句柄；{@code -1} 表示已释放。 */
    /** arena 内分配句柄；{@code -1} 表示已释放。 */
    protected long handle;
    /** 底层内存（堆数组或 {@link ByteBuffer}）。 */
    /** 底层内存（堆数组或 {@link ByteBuffer}）。 */
    protected T memory;
    /** 在 chunk 内存中的起始偏移。 */
    /** 在 chunk 内存中的起始偏移。 */
    protected int offset;
    /** 当前逻辑容量（可小于 {@link #maxLength}）。 */
    /** 当前逻辑容量（可小于 {@link #maxLength}）。 */
    protected int length;
    /** 本次分配的最大可用长度（含扩容余量）。 */
    /** 本次分配的最大可用长度（含扩容余量）。 */
    int maxLength;
    /** 分配时使用的线程本地缓存。 */
    /** 分配时使用的线程本地缓存。 */
    PoolThreadCache cache;
    /** 复用的临时 NIO 视图，避免重复 {@link ByteBuffer#duplicate()}。 */
    /** 复用的临时 NIO 视图，避免重复 {@link ByteBuffer#duplicate()}。 */
    ByteBuffer tmpNioBuf;
    /** 创建此缓冲区的分配器。 */
    /** 创建此缓冲区的分配器。 */
    private ByteBufAllocator allocator;

    @SuppressWarnings("unchecked")
    protected PooledByteBuf(Handle<? extends PooledByteBuf<T>> recyclerHandle, int maxCapacity) {
        super(maxCapacity);
        this.recyclerHandle = (EnhancedHandle<PooledByteBuf<T>>) recyclerHandle;
    }

    /** 池化分配完成后绑定 chunk、句柄与线程缓存。 */
    /** 池化分配完成后绑定 chunk、句柄与线程缓存。 */
    void init(PoolChunk<T> chunk, ByteBuffer nioBuffer,
              long handle, int offset, int length, int maxLength, PoolThreadCache cache, boolean threadLocal) {
        init0(chunk, nioBuffer, handle, offset, length, maxLength, cache, true, threadLocal);
    }

    /** 非池化 chunk 上的大块分配初始化。 */
    /** 非池化 chunk 上的大块分配初始化。 */
    void initUnpooled(PoolChunk<T> chunk, int length) {
        init0(chunk, null, 0, 0, length, length, null, false,
                false /* 非池化缓冲区不会来自线程本地缓存 */);
    }

    private void init0(PoolChunk<T> chunk, ByteBuffer nioBuffer, long handle, int offset, int length, int maxLength,
                       PoolThreadCache cache, boolean pooled, boolean threadLocal) {
        assert handle >= 0;
        assert chunk != null;
        assert !PoolChunk.isSubpage(handle) ||
                chunk.arena.sizeClass.size2SizeIdx(maxLength) <= chunk.arena.sizeClass.smallMaxSizeIdx:
                "Allocated small sub-page handle for a buffer size that isn't \"small.\"";

        chunk.incrementPinnedMemory(maxLength);
        this.chunk = chunk;
        memory = chunk.memory;
        tmpNioBuf = nioBuffer;
        allocator = chunk.arena.parent;
        this.cache = cache;
        this.handle = handle;
        this.offset = offset;
        this.length = length;
        this.maxLength = maxLength;
        PooledByteBufAllocator.onAllocateBuffer(this, pooled, threadLocal);
    }

    /**
     * 从 {@link Recycler} 取出后、再次使用前必须调用，重置容量与索引。
     */
    final void reuse(int maxCapacity) {
        maxCapacity(maxCapacity);
        resetRefCnt();
        setIndex0(0, 0);
        discardMarks();
    }

    @Override
    public final int capacity() {
        return length;
    }

    @Override
    public int maxFastWritableBytes() {
        return Math.min(maxLength, maxCapacity()) - writerIndex;
    }

    @Override
    public final ByteBuf capacity(int newCapacity) {
        if (newCapacity == length) {
            ensureAccessible();
            return this;
        }
        checkNewCapacity(newCapacity);
        if (!chunk.unpooled) {
            // 若新容量无需重新分配，仅更新 length。
            if (newCapacity > length) {
                if (newCapacity <= maxLength) {
                    length = newCapacity;
                    return this;
                }
            } else if (newCapacity > maxLength >>> 1 &&
                    (maxLength > 512 || newCapacity > maxLength - 16)) {
                // 此处 newCapacity < length，可收缩逻辑容量
                length = newCapacity;
                trimIndicesToCapacity(newCapacity);
                return this;
            }
        }

        // 需要向 arena 重新分配。
        PooledByteBufAllocator.onReallocateBuffer(this, newCapacity);
        chunk.arena.reallocate(this, newCapacity);
        return this;
    }

    @Override
    public final ByteBufAllocator alloc() {
        return allocator;
    }

    @Override
    public final ByteOrder order() {
        return ByteOrder.BIG_ENDIAN;
    }

    @Override
    public final ByteBuf unwrap() {
        return null;
    }

    @Override
    public final ByteBuf retainedDuplicate() {
        return PooledDuplicatedByteBuf.newInstance(this, this, readerIndex(), writerIndex());
    }

    @Override
    public final ByteBuf retainedSlice() {
        final int index = readerIndex();
        return retainedSlice(index, writerIndex() - index);
    }

    @Override
    public final ByteBuf retainedSlice(int index, int length) {
        return PooledSlicedByteBuf.newInstance(this, this, index, length);
    }

    protected final ByteBuffer internalNioBuffer() {
        ByteBuffer tmpNioBuf = this.tmpNioBuf;
        if (tmpNioBuf == null) {
            this.tmpNioBuf = tmpNioBuf = newInternalNioBuffer(memory);
        } else {
            tmpNioBuf.clear();
        }
        return tmpNioBuf;
    }

    /** 由子类根据内存类型创建内部 NIO 缓冲区。 */
    /** 由子类根据内存类型创建内部 NIO 缓冲区。 */
    protected abstract ByteBuffer newInternalNioBuffer(T memory);

    @Override
    protected final void deallocate() {
        if (handle >= 0) {
            PooledByteBufAllocator.onDeallocateBuffer(this);
            final long handle = this.handle;
            this.handle = -1;
            memory = null;
            chunk.arena.free(chunk, tmpNioBuf, handle, maxLength, cache);
            tmpNioBuf = null;
            chunk = null;
            cache = null;
            this.recyclerHandle.unguardedRecycle(this);
        }
    }

    /** 将相对索引转换为 chunk 内绝对偏移。 */
    /** 将相对索引转换为 chunk 内绝对偏移。 */
    protected final int idx(int index) {
        return offset + index;
    }

    final ByteBuffer _internalNioBuffer(int index, int length, boolean duplicate) {
        index = idx(index);
        ByteBuffer buffer = duplicate ? newInternalNioBuffer(memory) : internalNioBuffer();
        buffer.limit(index + length).position(index);
        return buffer;
    }

    ByteBuffer duplicateInternalNioBuffer(int index, int length) {
        checkIndex(index, length);
        return _internalNioBuffer(index, length, true);
    }

    @Override
    public final ByteBuffer internalNioBuffer(int index, int length) {
        checkIndex(index, length);
        return _internalNioBuffer(index, length, false);
    }

    @Override
    public final int nioBufferCount() {
        return 1;
    }

    @Override
    public final ByteBuffer nioBuffer(int index, int length) {
        return duplicateInternalNioBuffer(index, length).slice();
    }

    @Override
    public final ByteBuffer[] nioBuffers(int index, int length) {
        return new ByteBuffer[] { nioBuffer(index, length) };
    }

    @Override
    public final boolean isContiguous() {
        return true;
    }

    @Override
    public final int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return out.write(duplicateInternalNioBuffer(index, length));
    }

    @Override
    public final int readBytes(GatheringByteChannel out, int length) throws IOException {
        checkReadableBytes(length);
        int readBytes = out.write(_internalNioBuffer(readerIndex, length, false));
        readerIndex += readBytes;
        return readBytes;
    }

    @Override
    public final int getBytes(int index, FileChannel out, long position, int length) throws IOException {
        return out.write(duplicateInternalNioBuffer(index, length), position);
    }

    @Override
    public final int readBytes(FileChannel out, long position, int length) throws IOException {
        checkReadableBytes(length);
        int readBytes = out.write(_internalNioBuffer(readerIndex, length, false), position);
        readerIndex += readBytes;
        return readBytes;
    }

    @Override
    public final int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        try {
            return in.read(internalNioBuffer(index, length));
        } catch (ClosedChannelException ignored) {
            return -1;
        }
    }

    @Override
    public final int setBytes(int index, FileChannel in, long position, int length) throws IOException {
        try {
            return in.read(internalNioBuffer(index, length), position);
        } catch (ClosedChannelException ignored) {
            return -1;
        }
    }
}
