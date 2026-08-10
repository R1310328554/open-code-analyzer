/*
 * Copyright 2012 The Netty Project
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

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkNotNullWithIAE;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 流量计数器：统计读写字节并周期性计算带宽，回调 {@link AbstractTrafficShapingHandler#doAccounting}。
 * <p>{@code checkInterval} 为 0 时不启动定时统计，仅在每次读/写时更新；
 * 整形精度随统计间隔增大而降低。</p>
 */
public class TrafficCounter {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(TrafficCounter.class);

    /**
     * 基于 {@link System#nanoTime()} 的毫秒时间（非 EPOCH，用于间隔计算）。
     * @return the time in ms using nanoTime, so not real EPOCH time but elapsed time in ms.
     */
    public static long milliSecondFromNano() {
        return System.nanoTime() / 1000000;
    }

    /** 当前统计周期内已写字节数 */
    private final AtomicLong currentWrittenBytes = new AtomicLong();

    /** 当前统计周期内已读字节数 */
    private final AtomicLong currentReadBytes = new AtomicLong();

    /** 本周期内写整形等待结束时间 */
    private long writingTime;

    /** 本周期内读整形等待结束时间 */
    private long readingTime;

    /** 生命周期累计写字节 */
    private final AtomicLong cumulativeWrittenBytes = new AtomicLong();

    /** 生命周期累计读字节 */
    private final AtomicLong cumulativeReadBytes = new AtomicLong();

    /** 累计计数上次清零的 EPOCH 时间（毫秒） */
    private long lastCumulativeTime;

    /** 上一周期写吞吐量（字节/秒） */
    private long lastWriteThroughput;

    /** 上一周期读吞吐量（字节/秒） */
    private long lastReadThroughput;

    /** 上次统计快照时间 */
    final AtomicLong lastTime = new AtomicLong();

    /** 上一周期写字节数 */
    private volatile long lastWrittenBytes;

    /** 上一周期读字节数 */
    private volatile long lastReadBytes;

    /** 上一周期写整形结束时间快照 */
    private volatile long lastWritingTime;

    /** 上一周期读整形结束时间快照 */
    private volatile long lastReadingTime;

    /** 实际已 flush 写字节 */
    private final AtomicLong realWrittenBytes = new AtomicLong();

    /** 实际写吞吐量（字节/秒） */
    private long realWriteThroughput;

    /** 两次统计间隔（毫秒） */
    final AtomicLong checkInterval = new AtomicLong(
            AbstractTrafficShapingHandler.DEFAULT_CHECK_INTERVAL);

    // 默认 1 秒

    /** 计数器名称 */
    final String name;

    /** 宿主 TrafficShapingHandler */
    final AbstractTrafficShapingHandler trafficShapingHandler;

    /** 定时统计调度器 */
    final ScheduledExecutorService executor;
    /** start() 创建的监控任务 */
    Runnable monitor;
    /** stop() 时 cancel 用 */
    volatile ScheduledFuture<?> scheduledFuture;

    /** 监控是否激活 */
    volatile boolean monitorActive;

    /** 固定间隔执行的统计任务 */
    private final class TrafficMonitoringTask implements Runnable {
        @Override
        public void run() {
            if (!monitorActive) {
                return;
            }
            resetAccounting(milliSecondFromNano());
            if (trafficShapingHandler != null) {
                trafficShapingHandler.doAccounting(TrafficCounter.this);
            }
        }
    }

    /**
     * 启动周期性统计；{@code checkInterval > 0} 且 {@code executor != null} 时生效。
     */
    public synchronized void start() {
        if (monitorActive) {
            return;
        }
        lastTime.set(milliSecondFromNano());
        long localCheckInterval = checkInterval.get();
        // executor 为 null 时由 GlobalChannelTrafficCounter 统一调度 per-channel 计数器
        if (localCheckInterval > 0 && executor != null) {
            monitorActive = true;
            monitor = new TrafficMonitoringTask();
            scheduledFuture =
                executor.scheduleAtFixedRate(monitor, 0, localCheckInterval, TimeUnit.MILLISECONDS);
        }
    }

    /** 停止统计并做最后一次 accounting。 */
    public synchronized void stop() {
        if (!monitorActive) {
            return;
        }
        monitorActive = false;
        resetAccounting(milliSecondFromNano());
        if (trafficShapingHandler != null) {
            trafficShapingHandler.doAccounting(this);
        }
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * 重置本周期读/写计数并计算吞吐量。
     *
     * @param newLastTime the milliseconds unix timestamp that we should be considered up-to-date for.
     */
    synchronized void resetAccounting(long newLastTime) {
        long interval = newLastTime - lastTime.getAndSet(newLastTime);
        if (interval == 0) {
            // 无间隔，跳过
            return;
        }
        if (logger.isDebugEnabled() && interval > checkInterval() << 1) {
            logger.debug("Acct schedule not ok: " + interval + " > 2*" + checkInterval() + " from " + name);
        }
        lastReadBytes = currentReadBytes.getAndSet(0);
        lastWrittenBytes = currentWrittenBytes.getAndSet(0);
        lastReadThroughput = lastReadBytes * 1000 / interval;
        // 字节 / 间隔(ms) * 1000 => 字节/秒
        lastWriteThroughput = lastWrittenBytes * 1000 / interval;
        realWriteThroughput = realWrittenBytes.getAndSet(0) * 1000 / interval;
        lastWritingTime = Math.max(lastWritingTime, writingTime);
        lastReadingTime = Math.max(lastReadingTime, readingTime);
    }

    /**
     * Constructor with the {@link AbstractTrafficShapingHandler} that hosts it, the {@link ScheduledExecutorService}
     * to use, its name, the checkInterval between two computations in milliseconds.
     *
     * @param executor
     *            the underlying executor service for scheduling checks, might be null when used
     * from {@link GlobalChannelTrafficCounter}.
     * @param name
     *            the name given to this monitor.
     * @param checkInterval
     *            the checkInterval in millisecond between two computations.
     */
    public TrafficCounter(ScheduledExecutorService executor, String name, long checkInterval) {

        this.name = checkNotNull(name, "name");
        trafficShapingHandler = null;
        this.executor = executor;

        init(checkInterval);
    }

    /**
     * Constructor with the {@link AbstractTrafficShapingHandler} that hosts it, the Timer to use, its
     * name, the checkInterval between two computations in millisecond.
     *
     * @param trafficShapingHandler
     *            the associated AbstractTrafficShapingHandler.
     * @param executor
     *            the underlying executor service for scheduling checks, might be null when used
     * from {@link GlobalChannelTrafficCounter}.
     * @param name
     *            the name given to this monitor.
     * @param checkInterval
     *            the checkInterval in millisecond between two computations.
     */
    public TrafficCounter(
            AbstractTrafficShapingHandler trafficShapingHandler, ScheduledExecutorService executor,
            String name, long checkInterval) {
        this.name = checkNotNull(name, "name");
        this.trafficShapingHandler = checkNotNullWithIAE(trafficShapingHandler, "trafficShapingHandler");
        this.executor = executor;

        init(checkInterval);
    }

    private void init(long checkInterval) {
        // 绝对时间戳，仅作信息展示
        lastCumulativeTime = System.currentTimeMillis();
        writingTime = milliSecondFromNano();
        readingTime = writingTime;
        lastWritingTime = writingTime;
        lastReadingTime = writingTime;
        configure(checkInterval);
    }

    /**
     * Change checkInterval between two computations in millisecond.
     *
     * @param newCheckInterval The new check interval (in milliseconds)
     */
    public void configure(long newCheckInterval) {
        long newInterval = newCheckInterval / 10 * 10;
        if (checkInterval.getAndSet(newInterval) != newInterval) {
            if (newInterval <= 0) {
                stop();
                // 不再定时统计
                lastTime.set(milliSecondFromNano());
            } else {
                // 间隔变化时重启定时器
                stop();
                start();
            }
        }
    }

    /**
     * Computes counters for Read.
     *
     * @param recv
     *            the size in bytes to read
     */
    void bytesRecvFlowControl(long recv) {
        currentReadBytes.addAndGet(recv);
        cumulativeReadBytes.addAndGet(recv);
    }

    /**
     * Computes counters for Write.
     *
     * @param write
     *            the size in bytes to write
     */
    void bytesWriteFlowControl(long write) {
        currentWrittenBytes.addAndGet(write);
        cumulativeWrittenBytes.addAndGet(write);
    }

    /**
     * Computes counters for Real Write.
     *
     * @param write
     *            the size in bytes to write
     */
    void bytesRealWriteFlowControl(long write) {
        realWrittenBytes.addAndGet(write);
    }

    /**
     * @return the current checkInterval between two computations of traffic counter
     *         in millisecond.
     */
    public long checkInterval() {
        return checkInterval.get();
    }

    /**
     * @return the Read Throughput in bytes/s computes in the last check interval.
     */
    public long lastReadThroughput() {
        return lastReadThroughput;
    }

    /**
     * @return the Write Throughput in bytes/s computes in the last check interval.
     */
    public long lastWriteThroughput() {
        return lastWriteThroughput;
    }

    /**
     * @return the number of bytes read during the last check Interval.
     */
    public long lastReadBytes() {
        return lastReadBytes;
    }

    /**
     * @return the number of bytes written during the last check Interval.
     */
    public long lastWrittenBytes() {
        return lastWrittenBytes;
    }

    /**
     * @return the current number of bytes read since the last checkInterval.
     */
    public long currentReadBytes() {
        return currentReadBytes.get();
    }

    /**
     * @return the current number of bytes written since the last check Interval.
     */
    public long currentWrittenBytes() {
        return currentWrittenBytes.get();
    }

    /**
     * @return the Time in millisecond of the last check as of System.currentTimeMillis().
     */
    public long lastTime() {
        return lastTime.get();
    }

    /**
     * @return the cumulativeWrittenBytes
     */
    public long cumulativeWrittenBytes() {
        return cumulativeWrittenBytes.get();
    }

    /**
     * @return the cumulativeReadBytes
     */
    public long cumulativeReadBytes() {
        return cumulativeReadBytes.get();
    }

    /**
     * @return the lastCumulativeTime in millisecond as of System.currentTimeMillis()
     * when the cumulative counters were reset to 0.
     */
    public long lastCumulativeTime() {
        return lastCumulativeTime;
    }

    /**
     * @return the realWrittenBytes
     */
    public AtomicLong getRealWrittenBytes() {
        return realWrittenBytes;
    }

    /**
     * @return the realWriteThroughput
     */
    public long getRealWriteThroughput() {
        return realWriteThroughput;
    }

    /**
     * Reset both read and written cumulative bytes counters and the associated absolute time
     * from System.currentTimeMillis().
     */
    public void resetCumulativeTime() {
        lastCumulativeTime = System.currentTimeMillis();
        cumulativeReadBytes.set(0);
        cumulativeWrittenBytes.set(0);
    }

    /**
     * @return the name of this TrafficCounter.
     */
    public String name() {
        return name;
    }

    /**
     * Returns the time to wait (if any) for the given length message, using the given limitTraffic and the max wait
     * time.
     *
     * @param size
     *            the recv size
     * @param limitTraffic
     *            the traffic limit in bytes per second.
     * @param maxTime
     *            the max time in ms to wait in case of excess of traffic.
     * @return the current time to wait (in ms) if needed for Read operation.
     */
    @Deprecated
    public long readTimeToWait(final long size, final long limitTraffic, final long maxTime) {
        return readTimeToWait(size, limitTraffic, maxTime, milliSecondFromNano());
    }

    /**
     * 根据当前读速率与限额，计算读操作前应等待的毫秒数（并更新读计数）。
     *
     * @param size
     *            the recv size
     * @param limitTraffic
     *            the traffic limit in bytes per second
     * @param maxTime
     *            the max time in ms to wait in case of excess of traffic.
     * @param now the current time
     * @return the current time to wait (in ms) if needed for Read operation.
     */
    public long readTimeToWait(final long size, final long limitTraffic, final long maxTime, final long now) {
        bytesRecvFlowControl(size);
        if (size == 0 || limitTraffic == 0) {
            return 0;
        }
        final long lastTimeCheck = lastTime.get();
        long sum = currentReadBytes.get();
        long localReadingTime = readingTime;
        long lastRB = lastReadBytes;
        final long interval = now - lastTimeCheck;
        long pastDelay = Math.max(lastReadingTime - lastTimeCheck, 0);
        if (interval > AbstractTrafficShapingHandler.MINIMAL_WAIT) {
            // Enough interval time to compute shaping
            long time = sum * 1000 / limitTraffic - interval + pastDelay;
            if (time > AbstractTrafficShapingHandler.MINIMAL_WAIT) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Time: " + time + ':' + sum + ':' + interval + ':' + pastDelay);
                }
                if (time > maxTime && now + time - localReadingTime > maxTime) {
                    time = maxTime;
                }
                readingTime = Math.max(localReadingTime, now + time);
                return time;
            }
            readingTime = Math.max(localReadingTime, now);
            return 0;
        }
        // 间隔过短：合并上一周期字节数以估算
        long lastsum = sum + lastRB;
        long lastinterval = interval + checkInterval.get();
        long time = lastsum * 1000 / limitTraffic - lastinterval + pastDelay;
        if (time > AbstractTrafficShapingHandler.MINIMAL_WAIT) {
            if (logger.isDebugEnabled()) {
                logger.debug("Time: " + time + ':' + lastsum + ':' + lastinterval + ':' + pastDelay);
            }
            if (time > maxTime && now + time - localReadingTime > maxTime) {
                time = maxTime;
            }
            readingTime = Math.max(localReadingTime, now + time);
            return time;
        }
        readingTime = Math.max(localReadingTime, now);
        return 0;
    }

    /**
     * Returns the time to wait (if any) for the given length message, using the given limitTraffic and
     * the max wait time.
     *
     * @param size
     *            the write size
     * @param limitTraffic
     *            the traffic limit in bytes per second.
     * @param maxTime
     *            the max time in ms to wait in case of excess of traffic.
     * @return the current time to wait (in ms) if needed for Write operation.
     */
    @Deprecated
    public long writeTimeToWait(final long size, final long limitTraffic, final long maxTime) {
        return writeTimeToWait(size, limitTraffic, maxTime, milliSecondFromNano());
    }

    /**
     * 根据当前写速率与限额，计算写操作前应等待的毫秒数（并更新写计数）。
     *
     * @param size
     *            the write size
     * @param limitTraffic
     *            the traffic limit in bytes per second.
     * @param maxTime
     *            the max time in ms to wait in case of excess of traffic.
     * @param now the current time
     * @return the current time to wait (in ms) if needed for Write operation.
     */
    public long writeTimeToWait(final long size, final long limitTraffic, final long maxTime, final long now) {
        bytesWriteFlowControl(size);
        if (size == 0 || limitTraffic == 0) {
            return 0;
        }
        final long lastTimeCheck = lastTime.get();
        long sum = currentWrittenBytes.get();
        long lastWB = lastWrittenBytes;
        long localWritingTime = writingTime;
        long pastDelay = Math.max(lastWritingTime - lastTimeCheck, 0);
        final long interval = now - lastTimeCheck;
        if (interval > AbstractTrafficShapingHandler.MINIMAL_WAIT) {
            // Enough interval time to compute shaping
            long time = sum * 1000 / limitTraffic - interval + pastDelay;
            if (time > AbstractTrafficShapingHandler.MINIMAL_WAIT) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Time: " + time + ':' + sum + ':' + interval + ':' + pastDelay);
                }
                if (time > maxTime && now + time - localWritingTime > maxTime) {
                    time = maxTime;
                }
                writingTime = Math.max(localWritingTime, now + time);
                return time;
            }
            writingTime = Math.max(localWritingTime, now);
            return 0;
        }
        // take the last write interval check to get enough interval time
        long lastsum = sum + lastWB;
        long lastinterval = interval + checkInterval.get();
        long time = lastsum * 1000 / limitTraffic - lastinterval + pastDelay;
        if (time > AbstractTrafficShapingHandler.MINIMAL_WAIT) {
            if (logger.isDebugEnabled()) {
                logger.debug("Time: " + time + ':' + lastsum + ':' + lastinterval + ':' + pastDelay);
            }
            if (time > maxTime && now + time - localWritingTime > maxTime) {
                time = maxTime;
            }
            writingTime = Math.max(localWritingTime, now + time);
            return time;
        }
        writingTime = Math.max(localWritingTime, now);
        return 0;
    }

    @Override
    public String toString() {
        return new StringBuilder(165).append("Monitor ").append(name)
                .append(" Current Speed Read: ").append(lastReadThroughput >> 10).append(" KB/s, ")
                .append("Asked Write: ").append(lastWriteThroughput >> 10).append(" KB/s, ")
                .append("Real Write: ").append(realWriteThroughput >> 10).append(" KB/s, ")
                .append("Current Read: ").append(currentReadBytes.get() >> 10).append(" KB, ")
                .append("Current asked Write: ").append(currentWrittenBytes.get() >> 10).append(" KB, ")
                .append("Current real Write: ").append(realWrittenBytes.get() >> 10).append(" KB").toString();
    }
}
