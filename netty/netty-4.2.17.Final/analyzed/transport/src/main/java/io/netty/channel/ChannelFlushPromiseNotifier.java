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
package io.netty.channel;

import io.netty.util.internal.ObjectUtil;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * This implementation allows to register {@link ChannelFuture} instances which will get notified once some amount of
 * data was written and so a checkpoint was reached.
 * <p>按累计写入字节数管理 {@link ChannelPromise} 检查点：注册时指定待写字节阈值，
 * 当 {@link #writeCounter()} 达到对应检查点时自动成功通知，用于精确跟踪 flush 进度。</p>
 */
public final class ChannelFlushPromiseNotifier {

    /** 累计已写入（或已计数）的字节数。 */
    private long writeCounter;
    /** 按检查点排序的待通知 {@link ChannelPromise} 队列。 */
    private final Queue<FlushCheckpoint> flushCheckpoints = new ArrayDeque<FlushCheckpoint>();
    /** 为 {@code true} 时使用 {@code trySuccess/tryFailure} 而非 {@code setSuccess/setFailure}。 */
    private final boolean tryNotify;

    /**
     * Create a new instance
     * <p>创建 flush 承诺通知器。</p>
     *
     * @param tryNotify 为 {@code true} 时用 {@link ChannelPromise#trySuccess()} / {@link ChannelPromise#tryFailure(Throwable)} 通知；
     *                  否则使用 {@link ChannelPromise#setSuccess()} / {@link ChannelPromise#setFailure(Throwable)}
     */
    public ChannelFlushPromiseNotifier(boolean tryNotify) {
        this.tryNotify = tryNotify;
    }

    /**
     * Create a new instance which will use {@link ChannelPromise#setSuccess()} and
     * {@link ChannelPromise#setFailure(Throwable)} to notify the {@link ChannelPromise}s.
     * <p>默认使用 {@code setSuccess/setFailure} 完成通知。</p>
     */
    public ChannelFlushPromiseNotifier() {
        this(false);
    }

    /**
     * @deprecated use {@link #add(ChannelPromise, long)}
     */
    @Deprecated
    public ChannelFlushPromiseNotifier add(ChannelPromise promise, int pendingDataSize) {
        return add(promise, (long) pendingDataSize);
    }

    /**
     * Add a {@link ChannelPromise} to this {@link ChannelFlushPromiseNotifier} which will be notified after the given
     * {@code pendingDataSize} was reached.
     * <p>注册在累计写入达到 {@code writeCounter + pendingDataSize} 时成功完成的 {@link ChannelPromise}。</p>
     *
     * @param promise 待通知的承诺
     * @param pendingDataSize 自注册时起需再写入的字节数
     */
    public ChannelFlushPromiseNotifier add(ChannelPromise promise, long pendingDataSize) {
        ObjectUtil.checkNotNull(promise, "promise");
        checkPositiveOrZero(pendingDataSize, "pendingDataSize");
        long checkpoint = writeCounter + pendingDataSize;
        if (promise instanceof FlushCheckpoint) {
            FlushCheckpoint cp = (FlushCheckpoint) promise;
            cp.flushCheckpoint(checkpoint);
            flushCheckpoints.add(cp);
        } else {
            flushCheckpoints.add(new DefaultFlushCheckpoint(checkpoint, promise));
        }
        return this;
    }
    /**
     * Increase the current write counter by the given delta
     * <p>将内部写入计数器增加 {@code delta}，通常在有数据入队写出时调用。</p>
     *
     * @param delta 增量字节数（非负）
     */
    public ChannelFlushPromiseNotifier increaseWriteCounter(long delta) {
        checkPositiveOrZero(delta, "delta");
        writeCounter += delta;
        return this;
    }

    /**
     * Return the current write counter of this {@link ChannelFlushPromiseNotifier}
     * <p>返回当前累计写入计数，供与检查点比较。</p>
     */
    public long writeCounter() {
        return writeCounter;
    }

    /**
     * Notify all {@link ChannelFuture}s that were registered with {@link #add(ChannelPromise, int)} and
     * their pendingDatasize is smaller after the current writeCounter returned by {@link #writeCounter()}.
     * <p>通知所有已达检查点的 {@link ChannelPromise} 并成功完成，随后从队列移除。</p>
     */
    public ChannelFlushPromiseNotifier notifyPromises() {
        notifyPromises0(null);
        return this;
    }

    /**
     * @deprecated use {@link #notifyPromises()}
     */
    @Deprecated
    public ChannelFlushPromiseNotifier notifyFlushFutures() {
        return notifyPromises();
    }

    /**
     * Notify all {@link ChannelFuture}s that were registered with {@link #add(ChannelPromise, int)} and
     * their pendingDatasize isis smaller then the current writeCounter returned by {@link #writeCounter()}.
     * <p>先通知已达检查点的承诺，再将剩余未达检查点的承诺全部以 {@code cause} 失败；操作后队列为空。</p>
     *
     * @param cause 失败原因
     */
    public ChannelFlushPromiseNotifier notifyPromises(Throwable cause) {
        notifyPromises();
        for (;;) {
            FlushCheckpoint cp = flushCheckpoints.poll();
            if (cp == null) {
                break;
            }
            if (tryNotify) {
                cp.promise().tryFailure(cause);
            } else {
                cp.promise().setFailure(cause);
            }
        }
        return this;
    }

    /**
     * @deprecated use {@link #notifyPromises(Throwable)}
     */
    @Deprecated
    public ChannelFlushPromiseNotifier notifyFlushFutures(Throwable cause) {
        return notifyPromises(cause);
    }

    /**
     * Notify all {@link ChannelFuture}s that were registered with {@link #add(ChannelPromise, int)} and
     * their pendingDatasize is smaller then the current writeCounter returned by {@link #writeCounter()} using
     * the given cause1.
     * <p>已达检查点的承诺以 {@code cause1} 失败，剩余承诺以 {@code cause2} 失败。</p>
     *
     * @param cause1 用于失败已达检查点承诺的异常
     * @param cause2 用于失败剩余承诺的异常
     */
    public ChannelFlushPromiseNotifier notifyPromises(Throwable cause1, Throwable cause2) {
        notifyPromises0(cause1);
        for (;;) {
            FlushCheckpoint cp = flushCheckpoints.poll();
            if (cp == null) {
                break;
            }
            if (tryNotify) {
                cp.promise().tryFailure(cause2);
            } else {
                cp.promise().setFailure(cause2);
            }
        }
        return this;
    }

    /**
     * @deprecated use {@link #notifyPromises(Throwable, Throwable)}
     */
    @Deprecated
    public ChannelFlushPromiseNotifier notifyFlushFutures(Throwable cause1, Throwable cause2) {
        return notifyPromises(cause1, cause2);
    }

    private void notifyPromises0(Throwable cause) {
        if (flushCheckpoints.isEmpty()) {
            writeCounter = 0;
            return;
        }

        final long writeCounter = this.writeCounter;
        for (;;) {
            FlushCheckpoint cp = flushCheckpoints.peek();
            if (cp == null) {
                // Reset the counter if there's nothing in the notification list.
                this.writeCounter = 0;
                break;
            }

            if (cp.flushCheckpoint() > writeCounter) {
                if (writeCounter > 0 && flushCheckpoints.size() == 1) {
                    this.writeCounter = 0;
                    cp.flushCheckpoint(cp.flushCheckpoint() - writeCounter);
                }
                break;
            }

            flushCheckpoints.remove();
            ChannelPromise promise = cp.promise();
            if (cause == null) {
                if (tryNotify) {
                    promise.trySuccess();
                } else {
                    promise.setSuccess();
                }
            } else {
                if (tryNotify) {
                    promise.tryFailure(cause);
                } else {
                    promise.setFailure(cause);
                }
            }
        }

        // 计数过大时归零并调整剩余检查点，避免 long 溢出
        final long newWriteCounter = this.writeCounter;
        if (newWriteCounter >= 0x8000000000L) {
            // Reset the counter only when the counter grew pretty large
            // so that we can reduce the cost of updating all entries in the notification list.
            this.writeCounter = 0;
            for (FlushCheckpoint cp: flushCheckpoints) {
                cp.flushCheckpoint(cp.flushCheckpoint() - newWriteCounter);
            }
        }
    }

    /** flush 检查点：关联 {@link ChannelPromise} 与目标写入计数。 */
    interface FlushCheckpoint {
        long flushCheckpoint();
        void flushCheckpoint(long checkpoint);
        ChannelPromise promise();
    }

    /** {@link FlushCheckpoint} 的默认实现，包装普通 {@link ChannelPromise}。 */
    private static class DefaultFlushCheckpoint implements FlushCheckpoint {
        private long checkpoint;
        private final ChannelPromise future;

        DefaultFlushCheckpoint(long checkpoint, ChannelPromise future) {
            this.checkpoint = checkpoint;
            this.future = future;
        }

        @Override
        public long flushCheckpoint() {
            return checkpoint;
        }

        @Override
        public void flushCheckpoint(long checkpoint) {
            this.checkpoint = checkpoint;
        }

        @Override
        public ChannelPromise promise() {
            return future;
        }
    }
}
