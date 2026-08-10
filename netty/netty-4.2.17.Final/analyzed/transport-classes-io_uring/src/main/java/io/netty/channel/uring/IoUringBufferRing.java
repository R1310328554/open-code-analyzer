/*
 * Copyright 2025 The Netty Project
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
import io.netty.channel.unix.Buffer;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * io_uring 提供的 buffer ring 管理：预分配 direct {@link ByteBuf} 并注册到内核。
 * <p>支持批量/逐 buffer 分配、增量消费与懒扩展。</p>
 * <p>耗尽时通过 {@link IoUringBufferRingExhaustedEvent} 通知上层。</p>
 */
final class IoUringBufferRing {
    private static final VarHandle SHORT_HANDLE =
            MethodHandles.byteBufferViewVarHandle(short[].class, ByteOrder.nativeOrder());
    private final ByteBuffer ioUringBufRing;
    private final int tailFieldPosition;
    private final short entries;
    private final short mask;
    private final short bufferGroupId;
    private final int ringFd;
    private final ByteBuf[] buffers;
    private final IoUringBufferRingAllocator allocator;
    private final boolean batchAllocation;
    private final IoUringBufferRingExhaustedEvent exhaustedEvent;
    private final RingConsumer ringConsumer;
    private final boolean incremental;
    private final int batchSize;
    private boolean corrupted;
    private boolean closed;
    private int usableBuffers;
    private int allocatedBuffers;
    private boolean needExpand;
    private short lastGeneratedBid;

    IoUringBufferRing(int ringFd, ByteBuffer ioUringBufRing,
                      short entries, int batchSize, short bufferGroupId, boolean incremental,
                      IoUringBufferRingAllocator allocator, boolean batchAllocation) {
        assert entries % 2 == 0;
        assert batchSize % 2 == 0;
        this.batchSize = batchSize;
        this.ioUringBufRing = ioUringBufRing;
        this.tailFieldPosition = Native.IO_URING_BUFFER_RING_TAIL;
        this.entries = entries;
        this.mask = (short) (entries - 1);
        this.bufferGroupId = bufferGroupId;
        this.ringFd = ringFd;
        this.buffers = new ByteBuf[entries];
        this.incremental = incremental;
        this.allocator = allocator;
        this.batchAllocation = batchAllocation;
        this.ringConsumer  = new RingConsumer();
        this.exhaustedEvent = new IoUringBufferRingExhaustedEvent(bufferGroupId);
    }

    boolean isUsable() {
        return !closed && !corrupted;
    }

    void initialize() {
        // batchSize 已校验不超过 ring 长度
        fill((short) 0, batchSize);
        allocatedBuffers = batchSize;
    }

    private final class RingConsumer implements Consumer<ByteBuf> {
        private int expectedBuffers;
        private short num;
        private short bid;
        private short oldTail;

        short fill(short startBid, int numBuffers) {
            // 批量分配前先读取 tail
            oldTail = (short) SHORT_HANDLE.get(ioUringBufRing, tailFieldPosition);

            // 当前从 bid 0 开始；num 与 bid 实现上相同但语义分离
            this.num = 0;
            this.bid = startBid;
            this.expectedBuffers = numBuffers;
            try {
                if (batchAllocation) {
                    allocator.allocateBatch(this, numBuffers);
                } else {
                    for (int i = 0; i < numBuffers; i++) {
                        add(oldTail, bid++, num++, allocator.allocate());
                    }
                }
            } catch (Throwable t) {
                corrupted = true;
                for (int i = 0; i < buffers.length; i++) {
                    ByteBuf buffer = buffers[i];
                    if (buffer != null) {
                        buffer.release();
                        buffers[i] = null;
                    }
                }
                throw t;
            }
            // 按新增 buffer 数量推进 tail
            SHORT_HANDLE.setRelease(ioUringBufRing, tailFieldPosition, (short) (oldTail + num));

            return (short) (bid - 1);
        }

        void fill(short bid) {
            short tail = (short) SHORT_HANDLE.get(ioUringBufRing, tailFieldPosition);
            add(tail, bid, 0, allocator.allocate());
            // tail 推进 1
            SHORT_HANDLE.setRelease(ioUringBufRing, tailFieldPosition, (short) (tail + 1));
        }

        @Override
        public void accept(ByteBuf byteBuf) {
            if (corrupted || closed) {
                byteBuf.release();
                throw new IllegalStateException("Already closed");
            }
            if (expectedBuffers == num) {
                byteBuf.release();
                throw new IllegalStateException("Produced too many buffers");
            }
            add(oldTail, bid++, num++, byteBuf);
        }

        private void add(int tail, short bid, int offset, ByteBuf byteBuf) {
            short ringIndex = (short) ((tail + offset) & mask);
            assert buffers[bid] == null;

            long memoryAddress = IoUring.memoryAddress(byteBuf) + byteBuf.writerIndex();
            int writable = byteBuf.writableBytes();

            // 参见 liburing io_uring_buf 结构布局
            int position = Native.SIZEOF_IOURING_BUF * ringIndex;
            ioUringBufRing.putLong(position + Native.IOURING_BUFFER_OFFSETOF_ADDR, memoryAddress);
            ioUringBufRing.putInt(position + Native.IOURING_BUFFER_OFFSETOF_LEN, writable);
            ioUringBufRing.putShort(position + Native.IOURING_BUFFER_OFFSETOF_BID, bid);

            buffers[bid] = byteBuf;
        }
    }

    /**
     * Try to expand by adding more buffers to the ring if there is any space left, this will be done lazy.
     * <p>懒扩展：若 ring 尚有空间则标记需要增加 buffer。</p>
     *
     * @return {@code true} if we can expand the number of buffers in the ring, {@code false} otherwise.
     */
    boolean expand() {
        needExpand = true;
        return allocatedBuffers < buffers.length;
    }

    private void fill(short startBid, int buffers) {
        if (corrupted || closed) {
            return;
        }
        assert buffers % 2 == 0;
        lastGeneratedBid = ringConsumer.fill(startBid, buffers);
        usableBuffers += buffers;
    }

    private void fill(short bid) {
        if (corrupted || closed) {
            return;
        }
        ringConsumer.fill(bid);
        usableBuffers++;
    }

    /**
     * @return the {@link IoUringBufferRingExhaustedEvent} that should be used to signal that there were no buffers
     * left for this buffer ring.
      * <p>Netty io_uring 传输 API；详见上方英文说明。</p>
     */
    IoUringBufferRingExhaustedEvent getExhaustedEvent() {
        return exhaustedEvent;
    }

    /**
     * Return the amount of bytes that we attempted to read for the given id.
     * <p>返回指定 bid 的尝试读取字节数。</p>
     * This method must be called before {@link #useBuffer(short, int, boolean)}.
     *
     * @param bid   the id of the buffer.
     * @return      the attempted bytes.
     */
    int attemptedBytesRead(short bid) {
        return buffers[bid].writableBytes();
    }

    private int calculateNextBufferBatch() {
        return Math.min(batchSize, entries - allocatedBuffers);
    }

    /**
     * Use the buffer for the given buffer id. The returned {@link ByteBuf} must be released once not used anymore.
     * <p>使用指定 bid 的 buffer；返回的 {@link ByteBuf} 用毕须 release。</p>
     *
     * @param bid           the id of the buffer
     * @param read          the number of bytes that could be read. This value might be larger then what a single
     *                      {@link ByteBuf} can hold. Because of this, the caller should call
     *                      @link #useBuffer(short, int, boolean)} in a loop (obtaining the next bid to use by calling
     *                      {@link #nextBid(short)}) until all buffers could be obtained.
     * @return              the buffer.
     */
    ByteBuf useBuffer(short bid, int read, boolean more) {
        assert read > 0;
        ByteBuf byteBuf = buffers[bid];

        allocator.lastBytesRead(byteBuf.writableBytes(), read);
        // 始终 slice 返回，避免用户修改原 buffer 状态
        ByteBuf buffer = byteBuf.retainedSlice(byteBuf.writerIndex(), read);
        byteBuf.writerIndex(byteBuf.writerIndex() + read);

        if (incremental && more && byteBuf.isWritable()) {
            // 增量模式且仍有数据：仅 slice 已读部分，buffer 稍后复用
            return buffer;
        }

        // buffer 已用尽，清空槽位并 release
        buffers[bid] = null;
        byteBuf.release();
        if (--usableBuffers == 0) {
            int numBuffers = allocatedBuffers;
            if (needExpand) {
                // 收到扩展信号，尝试增长 ring
                needExpand = false;
                numBuffers += calculateNextBufferBatch();
            }
            fill((short) 0, numBuffers);
            allocatedBuffers = numBuffers;
            assert allocatedBuffers % 2 == 0;
        } else if (!batchAllocation) {
            // 非批量分配时，用完后立即补回该 bid
            fill(bid);

            if (needExpand && lastGeneratedBid == bid) {
                // 扩展信号且刚补回 last bid，可安全增长并保证 RECVSEND_BUNDLE 顺序
                needExpand = false;
                int numBuffers = calculateNextBufferBatch();
                fill((short) (bid + 1), numBuffers);
                allocatedBuffers += numBuffers;
                assert allocatedBuffers % 2 == 0;
            }
        }
        return buffer;
    }

    short nextBid(short bid) {
        return (short) ((bid + 1) & allocatedBuffers - 1);
    }

    /**
     * The group id that is assigned to this buffer ring.
     * <p>此 buffer ring 的 group id。</p>
     *
     * @return group id.
     */
    short bufferGroupId() {
        return bufferGroupId;
    }

    /**
     * Close this {@link IoUringBufferRing}, using it after this method is called will lead to undefined behaviour.
     * <p>关闭 buffer ring；之后使用行为未定义。</p>
     */
    void close() {
        if (closed) {
            return;
        }
        closed = true;
        Native.ioUringUnRegisterBufRing(ringFd, Buffer.memoryAddress(ioUringBufRing), entries, bufferGroupId);
        for (ByteBuf byteBuf : buffers) {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }
        Arrays.fill(buffers, null);
    }
}
