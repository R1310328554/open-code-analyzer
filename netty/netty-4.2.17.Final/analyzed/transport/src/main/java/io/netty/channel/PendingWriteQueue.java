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
package io.netty.channel;

import io.netty.buffer.AbstractReferenceCountedByteBuf;
import io.netty.util.Recycler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.PromiseCombiner;
import io.netty.util.internal.ObjectPool;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * 延迟执行的待写操作队列，并同步更新关联 {@link Channel} 的
 * {@linkplain Channel#isWritable() 可写性}，使 pending 写操作也参与可写判断。
 * <p>
 * 须在所属 EventLoop 线程上调用；内部使用链表与对象池降低分配开销。
 * </p>
 */
public final class PendingWriteQueue {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PendingWriteQueue.class);
    // 64 位 JVM 下单个 PendingWrite 节点的近似对象开销（对象头 + 引用 + long 字段）
    //  - 16 bytes object header
    //  - 4 reference fields
    //  - 1 long fields
    private static final int PENDING_WRITE_OVERHEAD =
            SystemPropertyUtil.getInt("io.netty.transport.pendingWriteSizeOverhead", 64);

    /** 用于触发 write 的 invoker（Context 或 Channel） */
    private final ChannelOutboundInvoker invoker;
    /** 所属 EventLoop */
    private final EventExecutor executor;
    /** 待发送字节 tracker，用于更新 Channel 可写状态 */
    private final PendingBytesTracker tracker;

    // 链表头尾指针；空队列时 head、tail 均为 null
    private PendingWrite head;
    private PendingWrite tail;
    /** 队列中待写操作数量 */
    private int size;
    /** 队列中待写字节总数（估算） */
    private long bytes;

    /**
     * 基于 {@link ChannelHandlerContext} 创建队列。
     */
    public PendingWriteQueue(ChannelHandlerContext ctx) {
        tracker = PendingBytesTracker.newTracker(ctx.channel());
        this.invoker = ctx;
        this.executor = ctx.executor();
    }

    /**
     * 基于 {@link Channel} 创建队列。
     */
    public PendingWriteQueue(Channel channel) {
        tracker = PendingBytesTracker.newTracker(channel);
        this.invoker = channel;
        this.executor = channel.eventLoop();
    }

    /**
     * 若队列中无待写操作则返回 {@code true}。
     */
    public boolean isEmpty() {
        assert executor.inEventLoop();
        return head == null;
    }

    /**
     * 返回待写操作数量。
     */
    public int size() {
        assert executor.inEventLoop();
        return size;
    }

    /**
     * 返回因 pending 消息而待发送的字节总数估计值，仅供参考。
     */
    public long bytes() {
        assert executor.inEventLoop();
        return bytes;
    }

    /** 估算单条待写消息占用的字节（含节点开销）。 */
    private int size(Object msg) {
        // removeAndFailAll() 可能触发新的 write；为保序仍入队，稍后由 removeAndFailAll 统一失败
        int messageSize = tracker.size(msg);
        if (messageSize < 0) {
            // 大小未知时使用 0
            messageSize = 0;
        }
        return messageSize + PENDING_WRITE_OVERHEAD;
    }

    /**
     * 将 {@code msg} 与 {@link ChannelPromise} 加入队列。
     */
    public void add(Object msg, ChannelPromise promise) {
        assert executor.inEventLoop();
        ObjectUtil.checkNotNull(msg, "msg");
        ObjectUtil.checkNotNull(promise, "promise");
        // removeAndFailAll() 可能触发新的 write；为保序仍入队，稍后由 removeAndFailAll 统一失败
        int messageSize = size(msg);

        PendingWrite write = PendingWrite.newInstance(msg, messageSize, promise);
        PendingWrite currentTail = tail;
        if (currentTail == null) {
            tail = head = write;
        } else {
            currentTail.next = write;
            tail = write;
        }
        size ++;
        bytes += messageSize;
        tracker.incrementPendingOutboundBytes(write.size);
        // touch 消息以便排查 buffer 泄漏

        // 针对 AbstractReferenceCountedByteBuf 直接调用，减少接口分派
        if (msg instanceof AbstractReferenceCountedByteBuf) {
            ((AbstractReferenceCountedByteBuf) msg).touch();
        } else {
            ReferenceCountUtil.touch(msg);
        }
    }

    /**
     * 移除全部 pending 写操作，并通过
     * {@link ChannelHandlerContext#write(Object, ChannelPromise)} 执行。
     *
     * @return  若有写入则返回聚合 {@link ChannelFuture}；队列为空时返回 {@code null}
     */
    public ChannelFuture removeAndWriteAll() {
        assert executor.inEventLoop();

        if (isEmpty()) {
            return null;
        }

        ChannelPromise p = invoker.newPromise();
        PromiseCombiner combiner = new PromiseCombiner(executor);
        try {
            // 部分 promise 完成可能触发新 write 并“复活”队列，需循环直到真正清空
            for (PendingWrite write = head; write != null; write = head) {
                head = tail = null;
                size = 0;
                bytes = 0;

                while (write != null) {
                    PendingWrite next = write.next;
                    Object msg = write.msg;
                    ChannelPromise promise = write.promise;
                    recycle(write, false);
                    if (!(promise instanceof VoidChannelPromise)) {
                        combiner.add(promise);
                    }
                    invoker.write(msg, promise);
                    write = next;
                }
            }
            combiner.finish(p);
        } catch (Throwable cause) {
            p.setFailure(cause);
        }
        assertEmpty();
        return p;
    }

    /**
     * 移除全部 pending 写操作并以 {@code cause} 失败；消息经
     * {@link ReferenceCountUtil#safeRelease(Object)} 释放。
     */
    public void removeAndFailAll(Throwable cause) {
        assert executor.inEventLoop();
        ObjectUtil.checkNotNull(cause, "cause");
        // 失败的 promise 可能触发新 write，需循环清空
        for (PendingWrite write = head; write != null; write = head) {
            head = tail = null;
            size = 0;
            bytes = 0;
            while (write != null) {
                PendingWrite next = write.next;
                ReferenceCountUtil.safeRelease(write.msg);
                ChannelPromise promise = write.promise;
                recycle(write, false);
                safeFail(promise, cause);
                write = next;
            }
        }
        assertEmpty();
    }

    /**
     * 移除队首 pending 写操作并以 {@code cause} 失败；消息经
     * {@link ReferenceCountUtil#safeRelease(Object)} 释放。
     */
    public void removeAndFail(Throwable cause) {
        assert executor.inEventLoop();
        ObjectUtil.checkNotNull(cause, "cause");

        PendingWrite write = head;
        if (write == null) {
            return;
        }
        ReferenceCountUtil.safeRelease(write.msg);
        ChannelPromise promise = write.promise;
        safeFail(promise, cause);
        recycle(write, true);
    }

    private void assertEmpty() {
        assert tail == null && head == null && size == 0;
    }

    /**
     * 移除队首 pending 写操作并通过
     * {@link ChannelHandlerContext#write(Object, ChannelPromise)} 执行。
     *
     * @return  若有写入则返回 {@link ChannelFuture}；队列为空时返回 {@code null}
     */
    public ChannelFuture removeAndWrite() {
        assert executor.inEventLoop();
        PendingWrite write = head;
        if (write == null) {
            return null;
        }
        Object msg = write.msg;
        ChannelPromise promise = write.promise;
        recycle(write, true);
        return invoker.write(msg, promise);
    }

    /**
     * 移除队首 pending 写操作并释放消息（{@link ReferenceCountUtil#safeRelease(Object)}）。
     *
     * @return  队首写操作的 {@link ChannelPromise}；队列为空时返回 {@code null}
     *
     */
    public ChannelPromise remove() {
        assert executor.inEventLoop();
        PendingWrite write = head;
        if (write == null) {
            return null;
        }
        ChannelPromise promise = write.promise;
        ReferenceCountUtil.safeRelease(write.msg);
        recycle(write, true);
        return promise;
    }

    /**
     * 返回队首消息，队列为空时返回 {@code null}。
     */
    public Object current() {
        assert executor.inEventLoop();
        PendingWrite write = head;
        if (write == null) {
            return null;
        }
        return write.msg;
    }

    /** 回收节点并可选更新队列头/计数；同步递减 pending 字节。 */
    private void recycle(PendingWrite write, boolean update) {
        final PendingWrite next = write.next;
        final long writeSize = write.size;

        if (update) {
            if (next == null) {
                // 最后一个节点：直接重置 head/tail，防止重入
                head = tail = null;
                size = 0;
                bytes = 0;
            } else {
                head = next;
                size --;
                bytes -= writeSize;
                assert size > 0 && bytes >= 0;
            }
        }

        write.recycle();
        tracker.decrementPendingOutboundBytes(writeSize);
    }

    /** 安全地将 promise 标记为失败，忽略已完成的情况。 */
    private static void safeFail(ChannelPromise promise, Throwable cause) {
        if (!(promise instanceof VoidChannelPromise) && !promise.tryFailure(cause)) {
            logger.warn("Failed to mark a promise as failure because it's done already: {}", promise, cause);
        }
    }

    /**
     * 链表节点：保存单条 pending 写的元数据，由 {@link Recycler} 复用。
     */
    static final class PendingWrite {
        private static final Recycler<PendingWrite> RECYCLER =
                new Recycler<PendingWrite>() {
                    @Override
                    protected PendingWrite newObject(Handle<PendingWrite> handle) {
                        return new PendingWrite(handle);
                    }
                };

        private final ObjectPool.Handle<PendingWrite> handle;
        /** 下一节点 */
        private PendingWrite next;
        /** 本条写操作估算字节数（含开销） */
        private long size;
        private ChannelPromise promise;
        private Object msg;

        private PendingWrite(ObjectPool.Handle<PendingWrite> handle) {
            this.handle = handle;
        }

        static PendingWrite newInstance(Object msg, int size, ChannelPromise promise) {
            PendingWrite write = RECYCLER.get();
            write.size = size;
            write.msg = msg;
            write.promise = promise;
            return write;
        }

        private void recycle() {
            size = 0;
            next = null;
            msg = null;
            promise = null;
            handle.recycle(this);
        }
    }
}
