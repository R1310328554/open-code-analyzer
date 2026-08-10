/*
 * Copyright 2017 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.util.internal.UnstableApi;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.ArrayDeque;

import static io.netty.util.ReferenceCountUtil.safeRelease;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;
import static io.netty.util.internal.PlatformDependent.throwException;

@UnstableApi
public abstract class AbstractCoalescingBufferQueue {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractCoalescingBufferQueue.class);
    /** 交替存储 ByteBuf 与其后的 ChannelFutureListener/Promise 通知器 */
    private final ArrayDeque<Object> bufAndListenerPairs;
    /** 跟踪待写字节数以反映 {@link Channel#isWritable()}；channel 为 null 时不更新 */
    private final PendingBytesTracker tracker;
    /** 队列中所有 ByteBuf 的可读字节总数 */
    private int readableBytes;

    /**
     * 创建实例
     *
     * @param channel 关联的 {@link Channel}，用于更新可写性；无则传 {@code null} which will have the {@link Channel#isWritable()} reflect the amount of queued
     *                buffers or {@code null} if there is no writability state updated.
     * @param initSize 底层队列初始容量 of the underlying queue.
     */
    protected AbstractCoalescingBufferQueue(Channel channel, int initSize) {
        bufAndListenerPairs = new ArrayDeque<Object>(initSize);
        tracker = channel == null ? null : PendingBytesTracker.newTracker(channel);
    }

    /**
     * 将缓冲区插入队首并关联 Promise and associate a promise with it that should be completed when
     * all the buffer's bytes have been consumed from the queue and written.
     * @param buf to add to the head of the queue
     * @param promise to complete when all the bytes have been consumed and written, can be void.
     */
    public final void addFirst(ByteBuf buf, ChannelPromise promise) {
        addFirst(buf, toChannelFutureListener(promise));
    }

    private void addFirst(ByteBuf buf, ChannelFutureListener listener) {
        // touch 便于排查缓冲区泄漏
        buf.touch();

        if (listener != null) {
            bufAndListenerPairs.addFirst(listener);
        }
        bufAndListenerPairs.addFirst(buf);
        incrementReadableBytes(buf.readableBytes());
    }

    /**
     * 将缓冲区追加到队尾
     */
    public final void add(ByteBuf buf) {
        add(buf, (ChannelFutureListener) null);
    }

    /**
     * 将缓冲区追加到队尾并关联 Promise with it that should be completed when
     * all the buffer's bytes have been consumed from the queue and written.
     * @param buf to add to the tail of the queue
     * @param promise to complete when all the bytes have been consumed and written, can be void.
     */
    public final void add(ByteBuf buf, ChannelPromise promise) {
        // 先入队 buffer 再入队 promise，出队时先消费完 buffer 再完成 promise 'consume' the entire buffer during removal
        // before we complete it's promise.
        add(buf, toChannelFutureListener(promise));
    }

    /**
     * 将缓冲区追加到队尾并关联 Listener with it that should be completed when
     * all the buffers  bytes have been consumed from the queue and written.
     * @param buf to add to the tail of the queue
     * @param listener to notify when all the bytes have been consumed and written, can be {@code null}.
     */
    public final void add(ByteBuf buf, ChannelFutureListener listener) {
        // touch 便于排查缓冲区泄漏
        buf.touch();

        // 先入队 buffer 再入队 promise，出队时先消费完 buffer 再完成 promise 'consume' the entire buffer during removal
        // before we complete it's promise.
        bufAndListenerPairs.add(buf);
        if (listener != null) {
            bufAndListenerPairs.add(listener);
        }
        incrementReadableBytes(buf.readableBytes());
    }

    /**
     * 移除并返回队首 {@link ByteBuf}
     * @param aggregatePromise used to aggregate the promises and listeners for the returned buffer.
     * @return the first {@link ByteBuf} from the queue.
     */
    public final ByteBuf removeFirst(ChannelPromise aggregatePromise) {
        Object entry = bufAndListenerPairs.poll();
        if (entry == null) {
            return null;
        }
        assert entry instanceof ByteBuf;
        ByteBuf result = (ByteBuf) entry;

        decrementReadableBytes(result.readableBytes());

        entry = bufAndListenerPairs.peek();
        if (entry instanceof ChannelFutureListener) {
            aggregatePromise.addListener((ChannelFutureListener) entry);
            bufAndListenerPairs.poll();
        }
        reconcileReadableBytes();
        return result;
    }

    /**
     * 按指定字节数从队列移除并聚合 {@link ByteBuf}；完全消费的 buffer 在 aggregatePromise 完成时通知 Any added buffer who's bytes are
     * fully consumed during removal will have it's promise completed when the passed aggregate {@link ChannelPromise}
     * completes.
     *
     * @param alloc The allocator used if a new {@link ByteBuf} is generated during the aggregation process.
     * @param bytes the maximum number of readable bytes in the returned {@link ByteBuf}, if {@code bytes} is greater
     *              than {@link #readableBytes} then a buffer of length {@link #readableBytes} is returned.
     * @param aggregatePromise used to aggregate the promises and listeners for the constituent buffers.
     * @return a {@link ByteBuf} composed of the enqueued buffers.
     */
    public final ByteBuf remove(ByteBufAllocator alloc, int bytes, ChannelPromise aggregatePromise) {
        checkPositiveOrZero(bytes, "bytes");
        checkNotNull(aggregatePromise, "aggregatePromise");

        // 空队列判定用 isEmpty，因可能存在与空 buffer 绑定的 promise as we may have a promise associated with an empty buffer.
        if (bufAndListenerPairs.isEmpty()) {
            reconcileReadableBytes();
            return removeEmptyValue();
        }
        bytes = Math.min(bytes, readableBytes);

        ByteBuf toReturn = null;
        ByteBuf entryBuffer = null;
        int originalBytes = bytes;
        Object entry = null;
        try {
            for (;;) {
                entry = bufAndListenerPairs.poll();
                if (entry == null) {
                    break;
                }
                // ByteBuf 快速路径
                if (entry instanceof ByteBuf) {
                    entryBuffer = (ByteBuf) entry;
                    int bufferBytes = entryBuffer.readableBytes();

                    if (bufferBytes > bytes) {
                        // 当前 buffer 未完全消费，插回队首
                        bufAndListenerPairs.addFirst(entryBuffer);
                        if (bytes > 0) {
                            // Take a slice of what we can consume and retain it.
                            entryBuffer = entryBuffer.readRetainedSlice(bytes);
                            // we end here, so if this is the only buffer to return, skip composing
                            toReturn = toReturn == null ? entryBuffer
                                    : compose(alloc, toReturn, entryBuffer);
                            bytes = 0;
                        }
                        break;
                    }

                    bytes -= bufferBytes;
                    if (toReturn == null) {
                        // if there are no more bytes to read, there's no reason to compose
                        toReturn = bytes == 0
                                ? entryBuffer
                                : composeFirst(alloc, entryBuffer, bufferBytes + bytes);
                    } else {
                        toReturn = compose(alloc, toReturn, entryBuffer);
                    }
                    entryBuffer = null;
                } else if (entry instanceof DelegatingChannelPromiseNotifier) {
                    aggregatePromise.addListener((DelegatingChannelPromiseNotifier) entry);
                } else if (entry instanceof ChannelFutureListener) {
                    aggregatePromise.addListener((ChannelFutureListener) entry);
                }
            }
        } catch (Throwable cause) {
            // 异常路径仍递减 readableBytes 以保持状态一致 We decrement directly here and not in a finally-block
            // to ensure that the state is consistent even if it would be accessed via a listener that is
            // attached to the promise that we fail below.
            decrementReadableBytes(originalBytes - bytes);

            // Poll the next element if it's a listener that belongs to the ByteBuf.
            entry = bufAndListenerPairs.peek();
            if (entry instanceof ChannelFutureListener) {
                aggregatePromise.addListener((ChannelFutureListener) entry);
                bufAndListenerPairs.poll();
            }

            safeRelease(entryBuffer);
            safeRelease(toReturn);
            aggregatePromise.setFailure(cause);
            throwException(cause);
        }
        decrementReadableBytes(originalBytes - bytes);
        reconcileReadableBytes();
        return toReturn;
    }

    /**
     * 队列中可读字节总数
     */
    public final int readableBytes() {
        return readableBytes;
    }

    /**
     * 队列是否为空（无待写 buffer）
     */
    public final boolean isEmpty() {
        return bufAndListenerPairs.isEmpty();
    }

    /**
     *  释放队列中全部 buffer 并以失败完成所有 listener/promise
     */
    public final void releaseAndFailAll(ChannelOutboundInvoker invoker, Throwable cause) {
        releaseAndCompleteAll(invoker.newFailedFuture(cause));
    }

    /**
     * 将本队列全部待处理条目复制到目标队列
     * @param dest to copy pending buffers to.
     */
    public final void copyTo(AbstractCoalescingBufferQueue dest) {
        dest.bufAndListenerPairs.addAll(bufAndListenerPairs);
        dest.incrementReadableBytes(readableBytes);
    }

    /**
     * 将队列剩余元素全部写出到 {@link ChannelHandlerContext}
     * @param ctx The context to write all elements to.
     */
    public final void writeAndRemoveAll(ChannelHandlerContext ctx) {
        Throwable pending = null;
        ByteBuf previousBuf = null;
        for (;;) {
            Object entry = bufAndListenerPairs.poll();
            try {
                if (entry == null) {
                    if (previousBuf != null) {
                        decrementReadableBytes(previousBuf.readableBytes());
                        ctx.write(previousBuf, ctx.voidPromise());
                    }
                    break;
                }

                if (entry instanceof ByteBuf) {
                    if (previousBuf != null) {
                        decrementReadableBytes(previousBuf.readableBytes());
                        ctx.write(previousBuf, ctx.voidPromise());
                    }
                    previousBuf = (ByteBuf) entry;
                } else if (entry instanceof ChannelPromise) {
                    decrementReadableBytes(previousBuf.readableBytes());
                    ctx.write(previousBuf, (ChannelPromise) entry);
                    previousBuf = null;
                } else {
                    decrementReadableBytes(previousBuf.readableBytes());
                    ctx.write(previousBuf).addListener((ChannelFutureListener) entry);
                    previousBuf = null;
                }
            } catch (Throwable t) {
                if (pending == null) {
                    pending = t;
                } else {
                    logger.info("Throwable being suppressed because Throwable {} is already pending", pending, t);
                }
            }
        }
        reconcileReadableBytes();
        if (pending != null) {
            throw new IllegalStateException(pending);
        }
    }

    @Override
    public String toString() {
        return "bytes: " + readableBytes + " buffers: " + (size() >> 1);
    }

    /**
     * 子类实现：合并 {@code cumulation} 与 {@code next}
     */
    protected abstract ByteBuf compose(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next);

    /**
     * 将两段 buffer 合并为 {@link CompositeByteBuf}
     */
    protected final ByteBuf composeIntoComposite(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next) {
        // Create a composite buffer to accumulate this pair and potentially all the buffers
        // in the queue. Using +2 as we have already dequeued current and next.
        CompositeByteBuf composite = alloc.compositeBuffer(size() + 2);
        try {
            composite.addComponent(true, cumulation);
            composite.addComponent(true, next);
        } catch (Throwable cause) {
            composite.release();
            safeRelease(next);
            throwException(cause);
        }
        return composite;
    }

    /**
     * 复制合并为新的 ioBuffer 并释放原 buffer
     * @param alloc The allocator to use to allocate the new buffer.
     * @param cumulation The current cumulation.
     * @param next The next buffer.
     * @return The result of {@code cumulation + next}.
     */
    protected final ByteBuf copyAndCompose(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf next) {
        ByteBuf newCumulation = alloc.ioBuffer(cumulation.readableBytes() + next.readableBytes());
        try {
            newCumulation.writeBytes(cumulation).writeBytes(next);
        } catch (Throwable cause) {
            newCumulation.release();
            safeRelease(next);
            throwException(cause);
        }
        cumulation.release();
        next.release();
        return newCumulation;
    }

    /**
     * 计算后续 {@link #compose} 使用的首个累积 buffer to
     * {@link #compose(ByteBufAllocator, ByteBuf, ByteBuf)}.
     * @param bufferSize the optimal size of the buffer needed for cumulation
     * @return the first buffer
     */
    protected ByteBuf composeFirst(ByteBufAllocator allocator, ByteBuf first, int bufferSize) {
        return composeFirst(allocator, first);
    }

    /**
     * 计算后续 {@link #compose} 使用的首个累积 buffer to
     * {@link #compose(ByteBufAllocator, ByteBuf, ByteBuf)}.
     * This method is deprecated and will be removed in the future. Implementing classes should
     * override {@link #composeFirst(ByteBufAllocator, ByteBuf, int)} instead.
     * @deprecated Use {AbstractCoalescingBufferQueue#composeFirst(ByteBufAllocator, ByteBuf, int)}
     */
    @Deprecated
    protected ByteBuf composeFirst(ByteBufAllocator allocator, ByteBuf first) {
        return first;
    }

    /**
     * 队列为空时 {@link #remove} 的返回值，由子类定义
     * @return the {@link ByteBuf} which represents an empty queue.
     */
    protected abstract ByteBuf removeEmptyValue();

    /**
     * 通过 {@link #add} 入队的元素个数（含 listener 对）
     * @return the number of elements in this queue.
     */
    protected final int size() {
        return bufAndListenerPairs.size();
    }

    private void releaseAndCompleteAll(ChannelFuture future) {
        Throwable pending = null;
        for (;;) {
            Object entry = bufAndListenerPairs.poll();
            if (entry == null) {
                break;
            }
            try {
                if (entry instanceof ByteBuf) {
                    ByteBuf buffer = (ByteBuf) entry;
                    decrementReadableBytes(buffer.readableBytes());
                    safeRelease(buffer);
                } else {
                    ((ChannelFutureListener) entry).operationComplete(future);
                }
            } catch (Throwable t) {
                if (pending == null) {
                    pending = t;
                } else {
                    logger.info("Throwable being suppressed because Throwable {} is already pending", pending, t);
                }
            }
        }
        reconcileReadableBytes();
        if (pending != null) {
            throw new IllegalStateException(pending);
        }
    }

    private void incrementReadableBytes(int increment) {
        int nextReadableBytes = readableBytes + increment;
        if (nextReadableBytes < readableBytes) {
            throw new IllegalStateException("buffer queue length overflow: " + readableBytes + " + " + increment);
        }
        readableBytes = nextReadableBytes;
        if (tracker != null) {
            tracker.incrementPendingOutboundBytes(increment);
        }
    }

    private void decrementReadableBytes(int decrement) {
        readableBytes -= decrement;
        assert readableBytes >= 0;
        if (tracker != null) {
            tracker.decrementPendingOutboundBytes(decrement);
        }
    }

    /**
     * 队列为空但 readableBytes 非零时重置计数（表示 buffer 被提前释放/消费的 bug，见 netty#16946） They can only diverge if a queued buffer was released
     * or consumed while still referenced by the queue (similar to a reference-counting bug) after it was added,
     * which would otherwise make remove(...) return empty buffers forever. Logged at error level because it
     * always indicates a bug that needs to be found.
     * See https://github.com/netty/netty/issues/16946
     */
    private void reconcileReadableBytes() {
        if (readableBytes != 0 && bufAndListenerPairs.isEmpty()) {
            logger.error("readableBytes is {} but the queue is empty: a queued buffer was released or consumed " +
                    "while still referenced by the queue. This indicates a bug in the code that produced the " +
                    "buffer. Resetting readableBytes to 0.", readableBytes);
            decrementReadableBytes(readableBytes);
        }
    }

    private static ChannelFutureListener toChannelFutureListener(ChannelPromise promise) {
        return promise.isVoid() ? null : new DelegatingChannelPromiseNotifier(promise);
    }
}
