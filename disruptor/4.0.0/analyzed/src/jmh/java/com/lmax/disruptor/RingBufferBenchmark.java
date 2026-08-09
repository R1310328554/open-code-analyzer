package com.lmax.disruptor;

import com.lmax.disruptor.alternatives.RingBufferArray;
import com.lmax.disruptor.alternatives.RingBufferUnsafe;
import com.lmax.disruptor.support.DummyWaitStrategy;
import com.lmax.disruptor.support.StubEvent;
import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * 对比 Unsafe 与纯数组两种 {@link RingBuffer} 实现读写性能的 JMH 基准。
 */
@SuppressWarnings("ALL")
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(1)
public class RingBufferBenchmark
{
    // 在已调优系统上运行、并将基准线程绑定到隔离 CPU 时：
    // 启动 JMH 进程时设置环境变量指定隔离 CPU 列表，例如 ISOLATED_CPUS=38,40,42,44,46,48 java -jar disruptor-jmh.jar
    /** 从环境变量 ISOLATED_CPUS 解析的隔离 CPU 列表。 */
    private static final List<Integer> ISOLATED_CPUS = Arrays.stream(System.getenv().getOrDefault("ISOLATED_CPUS", "").split(","))
            .map(String::trim)
            .filter(not(String::isBlank))
            .map(Integer::valueOf)
            .collect(Collectors.toList());

    /** 为各基准线程分配递增 ID。 */
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    /**
     * 线程 CPU 亲和性绑定状态。
     */
    @State(Scope.Thread)
    public static class ThreadPinningState
    {
        /** 当前线程在基准中的序号。 */
        int threadId = THREAD_COUNTER.getAndIncrement();
        /** CPU 亲和性锁。 */
        private AffinityLock affinityLock;

        /**
         * 若配置了 ISOLATED_CPUS，则将当前线程绑定到对应 CPU。
         */
        @Setup
        public void setup()
        {
            if (ISOLATED_CPUS.size() > 0)
            {
                if (threadId > ISOLATED_CPUS.size())
                {
                    throw new IllegalArgumentException(
                            String.format("Benchmark uses at least %d threads, only defined %d isolated cpus",
                                    threadId,
                                    ISOLATED_CPUS.size()
                            ));
                }

                final Integer cpuId = ISOLATED_CPUS.get(threadId);
                affinityLock = AffinityLock.acquireLock(cpuId);
                System.out.printf("Attempted to set thread affinity for %s to %d, success = %b%n",
                        Thread.currentThread().getName(),
                        cpuId,
                        affinityLock.isAllocated()
                );
            }
            else
            {
                System.err.printf("ISOLATED_CPUS environment variable not defined, running thread %s (id=%d) on scheduler-defined CPU:%d%n ",
                        Thread.currentThread().getName(),
                        threadId,
                        Affinity.getCpu());
            }
        }

        /**
         * 释放 CPU 亲和性锁。
         */
        @TearDown
        public void teardown()
        {
            if (ISOLATED_CPUS.size() > 0)
            {
                affinityLock.release();
            }
        }
    }

    /*
     * 方案 1：RingBufferUnsafe — 使用 Unsafe API 跳过边界检查，与 Disruptor 3.x 行为一致
     */

    /** Unsafe 版 RingBuffer 的 JMH 组状态。 */
    @State(Scope.Group)
    public static class StateRingBufferUnsafe
    {
        /** Unsafe 环形缓冲区实例。 */
        RingBufferUnsafe<Object> ringBufferUnsafe = new RingBufferUnsafe<>(
                () -> new StubEvent(-1),
                new SingleProducerSequencer(128, new DummyWaitStrategy()));
    }

    /** 并发读取 Unsafe 环形缓冲区槽位 64。 */
    @Benchmark
    @Group("RingBufferUnsafe")
    public Object readUnsafe(final StateRingBufferUnsafe ringBufferUnsafe, final ThreadPinningState t)
    {
        return ringBufferUnsafe.ringBufferUnsafe.get(64);
    }

    /** 并发向 Unsafe 环形缓冲区发布序号 64。 */
    @Benchmark
    @Group("RingBufferUnsafe")
    public void writeUnsafe(final StateRingBufferUnsafe ringBufferUnsafe, final ThreadPinningState t)
    {
        ringBufferUnsafe.ringBufferUnsafe.publish(64);
    }

    /*
     * 方案 2：RingBufferArray — 无 Unsafe 式无边界检查访问，回退为普通数组索引
     */

    /** 纯数组版 RingBuffer 的 JMH 组状态。 */
    @State(Scope.Group)
    public static class StateRingBufferArray
    {
        /** 数组版环形缓冲区实例。 */
        RingBufferArray<Object> ringBufferVarHandle = new RingBufferArray<>(
                () -> new StubEvent(-1),
                new SingleProducerSequencer(128, new DummyWaitStrategy())
        );
    }

    /** 并发读取数组版环形缓冲区槽位 64。 */
    @Benchmark
    @Group("RingBufferArray")
    public Object readArray(final StateRingBufferArray ringBufferVarHandle, final ThreadPinningState t)
    {
        return ringBufferVarHandle.ringBufferVarHandle.get(64);
    }

    /** 并发向数组版环形缓冲区发布序号 64。 */
    @Benchmark
    @Group("RingBufferArray")
    public void writeArray(final StateRingBufferArray ringBufferVarHandle, final ThreadPinningState t)
    {
        ringBufferVarHandle.ringBufferVarHandle.publish(64);
    }

    /**
     * 独立运行本基准。
     */
    public static void main(final String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(RingBufferBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
