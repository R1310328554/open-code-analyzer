/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.traffic;

import static io.netty.util.internal.ObjectUtil.checkPositive;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.FileRegion;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 流量整形抽象基类：限制读/写带宽并通过 {@link TrafficCounter} 周期性回调 {@link #doAccounting}。
 * <p>子类 {@link GlobalTrafficShapingHandler} 做全局限速，
 * {@link ChannelTrafficShapingHandler} 做 per-channel 限速。</p>
 *
 * <p>可通过 {@link #configure}、{@link #trafficCounter()} 动态调整限额与统计间隔（对已调度流量为 best-effort）。</p>
public abstract class AbstractTrafficShapingHandler extends ChannelDuplexHandler {
    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(AbstractTrafficShapingHandler.class);
    /**
     * 两次带宽统计之间的默认间隔：1 秒
     */
    public static final long DEFAULT_CHECK_INTERVAL = 1000;

   /**
    * 流量整形期间允许的最大等待时间（此期间无 I/O）；应小于协议 TIMEOUT，默认 15s
    */
    public static final long DEFAULT_MAX_TIME = 15000;

    /**
     * 写缓冲队列的默认最大字节数
     */
    static final long DEFAULT_MAX_SIZE = 4 * 1024 * 1024L;

    /**
     * 最小等待时间（毫秒），低于此值不暂停读写
     */
    static final long MINIMAL_WAIT = 10;

    /** 关联的流量计数器 */
    protected TrafficCounter trafficCounter;

    /** 写带宽上限（字节/秒）；0 表示不限 */
    private volatile long writeLimit;

    /** 读带宽上限（字节/秒）；0 表示不限 */
    private volatile long readLimit;

    /** 单次整形最大等待时间（毫秒） */
    protected volatile long maxTime = DEFAULT_MAX_TIME; // default 15 s

    /** 两次性能快照之间的间隔（毫秒） */
    protected volatile long checkInterval = DEFAULT_CHECK_INTERVAL; // default 1 s

    /** 读暂停标记：AutoRead 被关闭时置 true */
    static final AttributeKey<Boolean> READ_SUSPENDED = AttributeKey
            .valueOf(AbstractTrafficShapingHandler.class.getName() + ".READ_SUSPENDED");
    /** 延迟恢复 AutoRead 的定时任务 */
    static final AttributeKey<Runnable> REOPEN_TASK = AttributeKey.valueOf(AbstractTrafficShapingHandler.class
            .getName() + ".REOPEN_TASK");

    /**
     * 写队列延迟或大小超阈值后，暂停上游 write 前的最大等待（毫秒），默认 4s
     */
    volatile long maxWriteDelay = 4 * DEFAULT_CHECK_INTERVAL; // default 4 s
    /**
     * 写队列字节数超阈值后暂停上游 write，默认 4MB
     */
    volatile long maxWriteSize = DEFAULT_MAX_SIZE; // default 4MB

    /**
     * {@link ChannelOutboundBuffer#setUserDefinedWritability(int, boolean)} 使用的索引
     * （Channel=1，Global=2，GlobalChannel=3）
     */
    final int userDefinedWritabilityIndex;

    /** Channel 级 TSH 默认 writability 索引 */
    static final int CHANNEL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX = 1;

    /** Global TSH 默认 writability 索引 */
    static final int GLOBAL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX = 2;

    /** GlobalChannel TSH 默认 writability 索引 */
    static final int GLOBALCHANNEL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX = 3;

    /**
     * @param newTrafficCounter
     *            the TrafficCounter to set
     */
    void setTrafficCounter(TrafficCounter newTrafficCounter) {
        trafficCounter = newTrafficCounter;
    }

    /**
     * @return the index to be used by the TrafficShapingHandler to manage the user defined writability.
     *              For Channel TSH it is defined as {@value #CHANNEL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX},
     *              for Global TSH it is defined as {@value #GLOBAL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX},
     *              for GlobalChannel TSH it is defined as
     *              {@value #GLOBALCHANNEL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX}.
     */
    protected int userDefinedWritabilityIndex() {
        return CHANNEL_DEFAULT_USER_DEFINED_WRITABILITY_INDEX;
    }

    /**
     * @param writeLimit
     *          0 or a limit in bytes/s
     * @param readLimit
     *          0 or a limit in bytes/s
     * @param checkInterval
     *            The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed.
     * @param maxTime
     *            The maximum delay to wait in case of traffic excess.
     *            Must be positive.
     */
    protected AbstractTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval, long maxTime) {
        this.maxTime = checkPositive(maxTime, "maxTime");

        userDefinedWritabilityIndex = userDefinedWritabilityIndex();
        this.writeLimit = writeLimit;
        this.readLimit = readLimit;
        this.checkInterval = checkInterval;
    }

    /**
     * Constructor using default max time as delay allowed value of {@value #DEFAULT_MAX_TIME} ms.
     * @param writeLimit
     *            0 or a limit in bytes/s
     * @param readLimit
     *            0 or a limit in bytes/s
     * @param checkInterval
     *            The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed.
     */
    protected AbstractTrafficShapingHandler(long writeLimit, long readLimit, long checkInterval) {
        this(writeLimit, readLimit, checkInterval, DEFAULT_MAX_TIME);
    }

    /**
     * Constructor using default Check Interval value of {@value #DEFAULT_CHECK_INTERVAL} ms and
     * default max time as delay allowed value of {@value #DEFAULT_MAX_TIME} ms.
     *
     * @param writeLimit
     *          0 or a limit in bytes/s
     * @param readLimit
     *          0 or a limit in bytes/s
     */
    protected AbstractTrafficShapingHandler(long writeLimit, long readLimit) {
        this(writeLimit, readLimit, DEFAULT_CHECK_INTERVAL, DEFAULT_MAX_TIME);
    }

    /**
     * Constructor using NO LIMIT, default Check Interval value of {@value #DEFAULT_CHECK_INTERVAL} ms and
     * default max time as delay allowed value of {@value #DEFAULT_MAX_TIME} ms.
     */
    protected AbstractTrafficShapingHandler() {
        this(0, 0, DEFAULT_CHECK_INTERVAL, DEFAULT_MAX_TIME);
    }

    /**
     * Constructor using NO LIMIT and
     * default max time as delay allowed value of {@value #DEFAULT_MAX_TIME} ms.
     *
     * @param checkInterval
     *            The delay between two computations of performances for
     *            channels or 0 if no stats are to be computed.
     */
    protected AbstractTrafficShapingHandler(long checkInterval) {
        this(0, 0, checkInterval, DEFAULT_MAX_TIME);
    }

    /**
     * Change the underlying limitations and check interval.
     * <p>Note the change will be taken as best effort, meaning
     * that all already scheduled traffics will not be
     * changed, but only applied to new traffics.</p>
     * <p>So the expected usage of this method is to be used not too often,
     * accordingly to the traffic shaping configuration.</p>
     *
     * @param newWriteLimit The new write limit (in bytes)
     * @param newReadLimit The new read limit (in bytes)
     * @param newCheckInterval The new check interval (in milliseconds)
     */
    public void configure(long newWriteLimit, long newReadLimit,
            long newCheckInterval) {
        configure(newWriteLimit, newReadLimit);
        configure(newCheckInterval);
    }

    /**
     * Change the underlying limitations.
     * <p>Note the change will be taken as best effort, meaning
     * that all already scheduled traffics will not be
     * changed, but only applied to new traffics.</p>
     * <p>So the expected usage of this method is to be used not too often,
     * accordingly to the traffic shaping configuration.</p>
     *
     * @param newWriteLimit The new write limit (in bytes)
     * @param newReadLimit The new read limit (in bytes)
     */
    public void configure(long newWriteLimit, long newReadLimit) {
        writeLimit = newWriteLimit;
        readLimit = newReadLimit;
        if (trafficCounter != null) {
            trafficCounter.resetAccounting(TrafficCounter.milliSecondFromNano());
        }
    }

    /**
     * Change the check interval.
     *
     * @param newCheckInterval The new check interval (in milliseconds)
     */
    public void configure(long newCheckInterval) {
        checkInterval = newCheckInterval;
        if (trafficCounter != null) {
            trafficCounter.configure(checkInterval);
        }
    }

    /**
     * @return the writeLimit
     */
    public long getWriteLimit() {
        return writeLimit;
    }

    /**
     * <p>Note the change will be taken as best effort, meaning
     * that all already scheduled traffics will not be
     * changed, but only applied to new traffics.</p>
     * <p>So the expected usage of this method is to be used not too often,
     * accordingly to the traffic shaping configuration.</p>
     *
     * @param writeLimit the writeLimit to set
     */
    public void setWriteLimit(long writeLimit) {
        this.writeLimit = writeLimit;
        if (trafficCounter != null) {
            trafficCounter.resetAccounting(TrafficCounter.milliSecondFromNano());
        }
    }

    /**
     * @return the readLimit
     */
    public long getReadLimit() {
        return readLimit;
    }

    /**
     * <p>Note the change will be taken as best effort, meaning
     * that all already scheduled traffics will not be
     * changed, but only applied to new traffics.</p>
     * <p>So the expected usage of this method is to be used not too often,
     * accordingly to the traffic shaping configuration.</p>
     *
     * @param readLimit the readLimit to set
     */
    public void setReadLimit(long readLimit) {
        this.readLimit = readLimit;
        if (trafficCounter != null) {
            trafficCounter.resetAccounting(TrafficCounter.milliSecondFromNano());
        }
    }

    /**
     * @return the checkInterval
     */
    public long getCheckInterval() {
        return checkInterval;
    }

    /**
     * @param checkInterval the interval in ms between each step check to set, default value being 1000 ms.
     */
    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
        if (trafficCounter != null) {
            trafficCounter.configure(checkInterval);
        }
    }

    /**
     * <p>Note the change will be taken as best effort, meaning
     * that all already scheduled traffics will not be
     * changed, but only applied to new traffics.</p>
     * <p>So the expected usage of this method is to be used not too often,
     * accordingly to the traffic shaping configuration.</p>
     *
     * @param maxTime
     *            Max delay in wait, shall be less than TIME OUT in related protocol.
     *            Must be positive.
     */
    public void setMaxTimeWait(long maxTime) {
        this.maxTime = checkPositive(maxTime, "maxTime");
    }

    /**
     * @return the max delay in wait to prevent TIME OUT
     */
    public long getMaxTimeWait() {
        return maxTime;
    }

    /**
     * @return the maxWriteDelay
     */
    public long getMaxWriteDelay() {
        return maxWriteDelay;
    }

    /**
     * <p>Note the change will be taken as best effort, meaning
     * that all already scheduled traffics will not be
     * changed, but only applied to new traffics.</p>
     * <p>So the expected usage of this method is to be used not too often,
     * accordingly to the traffic shaping configuration.</p>
     *
     * @param maxWriteDelay the maximum Write Delay in ms in the buffer allowed before write suspension is set.
     *              Must be positive.
     */
    public void setMaxWriteDelay(long maxWriteDelay) {
        this.maxWriteDelay = checkPositive(maxWriteDelay, "maxWriteDelay");
    }

    /**
     * @return the maxWriteSize default being {@value #DEFAULT_MAX_SIZE} bytes.
     */
    public long getMaxWriteSize() {
        return maxWriteSize;
    }

    /**
     * <p>Note that this limit is a best effort on memory limitation to prevent Out Of
     * Memory Exception. To ensure it works, the handler generating the write should
     * use one of the way provided by Netty to handle the capacity:</p>
     * <p>- the {@code Channel.isWritable()} property and the corresponding
     * {@code channelWritabilityChanged()}</p>
     * <p>- the {@code ChannelFuture.addListener(new GenericFutureListener())}</p>
     *
     * @param maxWriteSize the maximum Write Size allowed in the buffer
     *            per channel before write suspended is set,
     *            default being {@value #DEFAULT_MAX_SIZE} bytes.
     */
    public void setMaxWriteSize(long maxWriteSize) {
        this.maxWriteSize = maxWriteSize;
    }

    /**
     * {@link TrafficCounter} 完成一次统计后回调；子类可在此做近实时监控。
     *
     * @param counter
     *            the TrafficCounter that computes its performance
     */
    protected void doAccounting(TrafficCounter counter) {
        // NOOP by default
    }

    /**
     * 定时恢复 AutoRead 的任务：读整形暂停后到期重新开启读。
     */
    static final class ReopenReadTimerTask implements Runnable {
        final ChannelHandlerContext ctx;
        ReopenReadTimerTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            Channel channel = ctx.channel();
            ChannelConfig config = channel.config();
            if (!config.isAutoRead() && isHandlerActive(ctx)) {
                // 用户手动 setAutoRead(false) 且 handler 仍 active：仅清 READ_SUSPENDED
                if (logger.isDebugEnabled()) {
                    logger.debug("Not unsuspend: " + config.isAutoRead() + ':' +
                            isHandlerActive(ctx));
                }
                channel.attr(READ_SUSPENDED).set(false);
            } else {
                // 否则恢复 AutoRead 并触发 read()
                if (logger.isDebugEnabled()) {
                    if (config.isAutoRead() && !isHandlerActive(ctx)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Unsuspend: " + config.isAutoRead() + ':' +
                                    isHandlerActive(ctx));
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Normal unsuspend: " + config.isAutoRead() + ':'
                                    + isHandlerActive(ctx));
                        }
                    }
                }
                channel.attr(READ_SUSPENDED).set(false);
                config.setAutoRead(true);
                channel.read();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Unsuspend final status => " + config.isAutoRead() + ':'
                        + isHandlerActive(ctx));
            }
        }
    }

    /** 解除读暂停并恢复 AutoRead。 */
    void releaseReadSuspended(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        channel.attr(READ_SUSPENDED).set(false);
        channel.config().setAutoRead(true);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        long size = calculateSize(msg);
        long now = TrafficCounter.milliSecondFromNano();
        if (size > 0) {
            // 计算恢复读之前需等待的毫秒数
            long wait = trafficCounter.readTimeToWait(size, readLimit, maxTime, now);
            wait = checkWaitReadTime(ctx, wait, now);
            if (wait >= MINIMAL_WAIT) { // 至少 10ms 才暂停，避免过于频繁
                // AutoRead 且 handler active 才视为可暂停
                Channel channel = ctx.channel();
                ChannelConfig config = channel.config();
                if (logger.isDebugEnabled()) {
                    logger.debug("Read suspend: " + wait + ':' + config.isAutoRead() + ':'
                            + isHandlerActive(ctx));
                }
                if (config.isAutoRead() && isHandlerActive(ctx)) {
                    config.setAutoRead(false);
                    channel.attr(READ_SUSPENDED).set(true);
                    // 复用 REOPEN_TASK，减少 Runnable 分配
                    Attribute<Runnable> attr = channel.attr(REOPEN_TASK);
                    Runnable reopenTask = attr.get();
                    if (reopenTask == null) {
                        reopenTask = new ReopenReadTimerTask(ctx);
                        attr.set(reopenTask);
                    }
                    ctx.executor().schedule(reopenTask, wait, TimeUnit.MILLISECONDS);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Suspend final status => " + config.isAutoRead() + ':'
                                + isHandlerActive(ctx) + " will reopened at: " + wait);
                    }
                }
            }
        }
        informReadOperation(ctx, now);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        if (channel.hasAttr(REOPEN_TASK)) {
            // 释放 reopen 任务引用
            channel.attr(REOPEN_TASK).set(null);
        }
        super.handlerRemoved(ctx);
    }

    /**
     * GTSH 可覆盖：按 channel 调整读等待时间。
     * @param wait the wait delay computed in ms
     * @param now the relative now time in ms
     * @return the wait to use according to the context
     */
    long checkWaitReadTime(final ChannelHandlerContext ctx, long wait, final long now) {
        // 默认不调整
        return wait;
    }

    /**
     * GTSH 可覆盖：记录读操作时间戳。
     * @param now the relative now time in ms
     */
    void informReadOperation(final ChannelHandlerContext ctx, final long now) {
        // 默认无操作
    }

    protected static boolean isHandlerActive(ChannelHandlerContext ctx) {
        Boolean suspended = ctx.channel().attr(READ_SUSPENDED).get();
        return suspended == null || Boolean.FALSE.equals(suspended);
    }

    @Override
    public void read(ChannelHandlerContext ctx) {
        if (isHandlerActive(ctx)) {
            // Global/Read 场景：READ_SUSPENDED 为 false 时才继续 read
            ctx.read();
        }
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise)
            throws Exception {
        long size = calculateSize(msg);
        long now = TrafficCounter.milliSecondFromNano();
        if (size > 0) {
            // 计算继续写之前需等待的毫秒数
            long wait = trafficCounter.writeTimeToWait(size, writeLimit, maxTime, now);
            if (wait >= MINIMAL_WAIT) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Write suspend: " + wait + ':' + ctx.channel().config().isAutoRead() + ':'
                            + isHandlerActive(ctx));
                }
                submitWrite(ctx, msg, size, wait, now, promise);
                return;
            }
        }
        // 保持写顺序：即使无延迟也走 submitWrite
        submitWrite(ctx, msg, size, 0, now, promise);
    }

    @Deprecated
    protected void submitWrite(final ChannelHandlerContext ctx, final Object msg,
            final long delay, final ChannelPromise promise) {
        submitWrite(ctx, msg, calculateSize(msg),
                delay, TrafficCounter.milliSecondFromNano(), promise);
    }

    abstract void submitWrite(
            ChannelHandlerContext ctx, Object msg, long size, long delay, long now, ChannelPromise promise);

    /**
     * 释放排队写消息并失败 promise。
     */
    static void releaseAndFailQueuedWrite(Object msg, ChannelPromise promise, Throwable cause) {
        ReferenceCountUtil.safeRelease(msg);
        promise.tryFailure(cause);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        setUserDefinedWritability(ctx, true);
        super.channelRegistered(ctx);
    }

    void setUserDefinedWritability(ChannelHandlerContext ctx, boolean writable) {
        ChannelOutboundBuffer cob = ctx.channel().unsafe().outboundBuffer();
        if (cob != null) {
            cob.setUserDefinedWritability(userDefinedWritabilityIndex, writable);
        }
    }

    /**
     * 根据延迟与队列大小决定是否暂停上游 write（userDefinedWritability=false）。
     * @param delay the computed delay
     * @param queueSize the current queueSize
     */
    void checkWriteSuspend(ChannelHandlerContext ctx, long delay, long queueSize) {
        if (queueSize > maxWriteSize || delay > maxWriteDelay) {
            setUserDefinedWritability(ctx, false);
        }
    }
    /** 显式恢复写可写状态。 */
    void releaseWriteSuspended(ChannelHandlerContext ctx) {
        setUserDefinedWritability(ctx, true);
    }

    /**
     * @return the current TrafficCounter (if
     *         channel is still connected)
     */
    public TrafficCounter trafficCounter() {
        return trafficCounter;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(290)
            .append("TrafficShaping with Write Limit: ").append(writeLimit)
            .append(" Read Limit: ").append(readLimit)
            .append(" CheckInterval: ").append(checkInterval)
            .append(" maxDelay: ").append(maxWriteDelay)
            .append(" maxSize: ").append(maxWriteSize)
            .append(" and Counter: ");
        if (trafficCounter != null) {
            builder.append(trafficCounter);
        } else {
            builder.append("none");
        }
        return builder.toString();
    }

    /**
     * 计算消息字节大小；支持 {@link ByteBuf}、{@link ByteBufHolder}、{@link FileRegion}。
     * @param msg the msg for which the size should be calculated.
     * @return size the size of the msg or {@code -1} if unknown.
     */
    protected long calculateSize(Object msg) {
        if (msg instanceof ByteBuf) {
            return ((ByteBuf) msg).readableBytes();
        }
        if (msg instanceof ByteBufHolder) {
            return ((ByteBufHolder) msg).content().readableBytes();
        }
        if (msg instanceof FileRegion) {
            return ((FileRegion) msg).count();
        }
        return -1;
    }
}
