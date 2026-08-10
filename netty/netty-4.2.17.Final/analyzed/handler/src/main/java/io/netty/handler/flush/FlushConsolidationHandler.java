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
package io.netty.handler.flush;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.internal.ObjectUtil;

import java.util.concurrent.Future;

/**
 * {@link ChannelDuplexHandler} which consolidates {@link Channel#flush()} / {@link ChannelHandlerContext#flush()}
 * operations (which also includes
 * {@link Channel#writeAndFlush(Object)} / {@link Channel#writeAndFlush(Object, ChannelPromise)} and
 * {@link ChannelOutboundInvoker#writeAndFlush(Object)} /
 * {@link ChannelOutboundInvoker#writeAndFlush(Object, ChannelPromise)}).
 * <p>
 * Flush operations are generally speaking expensive as these may trigger a syscall on the transport level. Thus it is
 * in most cases (where write latency can be traded with throughput) a good idea to try to minimize flush operations
 * as much as possible.
 * <p>
 * If a read loop is currently ongoing, {@link #flush(ChannelHandlerContext)} will not be passed on to the next
 * {@link ChannelOutboundHandler} in the {@link ChannelPipeline}, as it will pick up any pending flushes when
 * {@link #channelReadComplete(ChannelHandlerContext)} is triggered.
 * If no read loop is ongoing, the behavior depends on the {@code consolidateWhenNoReadInProgress} constructor argument:
 * <ul>
 *     <li>if {@code false}, flushes are passed on to the next handler directly;</li>
 *     <li>if {@code true}, the invocation of the next handler is submitted as a separate task on the event loop. Under
 *     high throughput, this gives the opportunity to process other flushes before the task gets executed, thus
 *     batching multiple flushes into one.</li>
 * </ul>
 * If {@code explicitFlushAfterFlushes} is reached the flush will be forwarded as well (whether while in a read loop, or
 * while batching outside of a read loop).
 * <p>
 * If the {@link Channel} becomes non-writable it will also try to execute any pending flush operations.
 * <p>
 * The {@link FlushConsolidationHandler} should be put as first {@link ChannelHandler} in the
 * {@link ChannelPipeline} to have the best effect.
 *
 * <p>flush 合并 handler：将多次 {@link Channel#flush()} 调用合并为更少的实际 flush，
 * 减少传输层系统调用，以略增写延迟换取更高吞吐。读循环进行中延迟 flush 至
 * {@link #channelReadComplete}；无读循环时可选择立即 flush 或调度到事件循环批量执行。
 * 建议放在 pipeline 最前面以获得最佳效果。</p>
 */
public class FlushConsolidationHandler extends ChannelDuplexHandler {
    /** 累计多少次 flush 后强制向下游执行一次显式 flush。 */
    private final int explicitFlushAfterFlushes;
    /** 无读循环进行时是否也合并 flush（通过事件循环任务批量执行）。 */
    private final boolean consolidateWhenNoReadInProgress;
    /** 无读循环时延迟 flush 的 Runnable 任务。 */
    private final Runnable flushTask;
    /** 当前待合并的 flush 计数。 */
    private int flushPendingCount;
    /** 是否处于读循环中（自上次 channelReadComplete 以来收到过 channelRead）。 */
    private boolean readInProgress;
    /** 本 handler 所在的上下文。 */
    private ChannelHandlerContext ctx;
    /** 已调度但尚未执行的 flush 任务 Future。 */
    private Future<?> nextScheduledFlush;

    /**
     * The default number of flushes after which a flush will be forwarded to downstream handlers (whether while in a
     * read loop, or while batching outside of a read loop).
     *
     * <p>默认阈值：累计 256 次 flush 后强制向下游 flush 一次。</p>
     */
    public static final int DEFAULT_EXPLICIT_FLUSH_AFTER_FLUSHES = 256;

    /**
     * Create new instance which explicit flush after {@value DEFAULT_EXPLICIT_FLUSH_AFTER_FLUSHES} pending flush
     * operations at the latest.
     *
     * <p>使用默认阈值 {@value DEFAULT_EXPLICIT_FLUSH_AFTER_FLUSHES}，无读循环时不合并 flush。</p>
     */
    public FlushConsolidationHandler() {
        this(DEFAULT_EXPLICIT_FLUSH_AFTER_FLUSHES, false);
    }

    /**
     * Create new instance which doesn't consolidate flushes when no read is in progress.
     *
     * @param explicitFlushAfterFlushes the number of flushes after which an explicit flush will be done.
     *
     * <p>指定显式 flush 阈值，无读循环时不合并 flush。</p>
     */
    public FlushConsolidationHandler(int explicitFlushAfterFlushes) {
        this(explicitFlushAfterFlushes, false);
    }

    /**
     * Create new instance.
     *
     * @param explicitFlushAfterFlushes the number of flushes after which an explicit flush will be done.
     * @param consolidateWhenNoReadInProgress whether to consolidate flushes even when no read loop is currently
     *                                        ongoing.
     *
     * <p>完整构造：指定显式 flush 阈值及无读循环时是否通过事件循环任务合并 flush。</p>
     */
    public FlushConsolidationHandler(int explicitFlushAfterFlushes, boolean consolidateWhenNoReadInProgress) {
        this.explicitFlushAfterFlushes =
                ObjectUtil.checkPositive(explicitFlushAfterFlushes, "explicitFlushAfterFlushes");
        this.consolidateWhenNoReadInProgress = consolidateWhenNoReadInProgress;
        this.flushTask = consolidateWhenNoReadInProgress ?
                new Runnable() {
                    @Override
                    public void run() {
                        if (flushPendingCount > 0 && !readInProgress) {
                            flushPendingCount = 0;
                            nextScheduledFlush = null;
                            ctx.flush();
                        } // else we'll flush when the read completes
                    }
                }
                : null;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (readInProgress) {
            // If there is still a read in progress we are sure we will see a channelReadComplete(...) call. Thus
            // we only need to flush if we reach the explicitFlushAfterFlushes limit.
            // 读循环中：延迟 flush，仅在达到阈值时立即 flush
            if (++flushPendingCount == explicitFlushAfterFlushes) {
                flushNow(ctx);
            }
        } else if (consolidateWhenNoReadInProgress) {
            // Flush immediately if we reach the threshold, otherwise schedule
            // 无读循环且启用合并：达阈值立即 flush，否则调度到事件循环
            if (++flushPendingCount == explicitFlushAfterFlushes) {
                flushNow(ctx);
            } else {
                scheduleFlush(ctx);
            }
        } else {
            // Always flush directly
            // 无读循环且不合并：每次 flush 直接向下游传递
            flushNow(ctx);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // This may be the last event in the read loop, so flush now!
        // 读循环结束，执行所有待合并的 flush
        resetReadAndFlushIfNeeded(ctx);
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        readInProgress = true;
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // To ensure we not miss to flush anything, do it now.
        // 异常路径：确保待合并 flush 不丢失
        resetReadAndFlushIfNeeded(ctx);
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        // Try to flush one last time if flushes are pending before disconnect the channel.
        resetReadAndFlushIfNeeded(ctx);
        ctx.disconnect(promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        // Try to flush one last time if flushes are pending before close the channel.
        resetReadAndFlushIfNeeded(ctx);
        ctx.close(promise);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().isWritable()) {
            // The writability of the channel changed to false, so flush all consolidated flushes now to free up memory.
            // 通道不可写时立即 flush，释放 outbound buffer 中的数据
            flushIfNeeded(ctx);
        }
        ctx.fireChannelWritabilityChanged();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        flushIfNeeded(ctx);
    }

    /** 结束读循环并在有待合并 flush 时执行 flush。 */
    private void resetReadAndFlushIfNeeded(ChannelHandlerContext ctx) {
        readInProgress = false;
        flushIfNeeded(ctx);
    }

    /** 若有待合并 flush 则立即执行。 */
    private void flushIfNeeded(ChannelHandlerContext ctx) {
        if (flushPendingCount > 0) {
            flushNow(ctx);
        }
    }

    /** 取消已调度的 flush 任务并立即向下游 flush。 */
    private void flushNow(ChannelHandlerContext ctx) {
        cancelScheduledFlush();
        flushPendingCount = 0;
        ctx.flush();
    }

    /** 将 flush 调度为事件循环上的独立任务，以便合并同一 tick 内的多次 flush。 */
    private void scheduleFlush(final ChannelHandlerContext ctx) {
        if (nextScheduledFlush == null) {
            // Run as soon as possible, but still yield to give a chance for additional writes to enqueue.
            // 尽快执行但仍让出 CPU，使更多 write 有机会入队后再 flush
            nextScheduledFlush = ctx.channel().eventLoop().submit(flushTask);
        }
    }

    /** 取消尚未执行的调度 flush 任务。 */
    private void cancelScheduledFlush() {
        if (nextScheduledFlush != null) {
            nextScheduledFlush.cancel(false);
            nextScheduledFlush = null;
        }
    }
}
