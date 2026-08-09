package com.lmax.disruptor;

import com.lmax.disruptor.alternatives.MultiProducerSequencerUnsafe;
import com.lmax.disruptor.alternatives.MultiProducerSequencerVarHandle;
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
 * 对比 Unsafe 与 VarHandle 实现的多生产者 {@link Sequencer} 读写性能的 JMH 基准。
 */
@SuppressWarnings("unused")
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(1)
public class MultiProducerSequencerBenchmark
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
     * com.lmax.disruptor.alternatives.MultiProducerSequencerUnsafe（disruptor v3.4.2 实现）
     */
    /** Unsafe 版多生产者 Sequencer 的 JMH 组状态。 */
    @State(Scope.Group)
    public static class StateMultiProducerSequencerUnsafe
    {
        /** 第一个 Sequencer 实例。 */
        Sequencer value1 = new MultiProducerSequencerUnsafe(64, new BlockingWaitStrategy());
        /** 第二个 Sequencer 实例。 */
        Sequencer value2 = new MultiProducerSequencerUnsafe(64, new BlockingWaitStrategy());
    }

    /** 并发读取 value1 可用性。 */
    @Benchmark
    @Group("SequenceUnsafe")
    public boolean read1(final StateMultiProducerSequencerUnsafe s, final ThreadPinningState t)
    {
        return s.value1.isAvailable(1);
    }

    /** 并发读取 value1 可用性（第二读线程）。 */
    @Benchmark
    @Group("SequenceUnsafe")
    public boolean read2(final StateMultiProducerSequencerUnsafe s, final ThreadPinningState t)
    {
        return s.value1.isAvailable(1);
    }

    /** 向 value1 发布序号 1。 */
    @Benchmark
    @Group("SequenceUnsafe")
    public void setValue1A(final StateMultiProducerSequencerUnsafe s, final ThreadPinningState t)
    {
        s.value1.publish(1L);
    }

    /** 向 value1 发布序号 2。 */
    @Benchmark
    @Group("SequenceUnsafe")
    public void setValue1B(final StateMultiProducerSequencerUnsafe s, final ThreadPinningState t)
    {
        s.value1.publish(2L);
    }

    /** 向 value2 发布序号 1。 */
    @Benchmark
    @Group("SequenceUnsafe")
    public void setValue2A(final StateMultiProducerSequencerUnsafe s, final ThreadPinningState t)
    {
        s.value2.publish(1L);
    }

    /** 向 value2 发布序号 2。 */
    @Benchmark
    @Group("SequenceUnsafe")
    public void setValue2B(final StateMultiProducerSequencerUnsafe s, final ThreadPinningState t)
    {
        s.value2.publish(2L);
    }

    /*
     * com.lmax.disruptor.alternatives.StateSequenceVarHandle（disruptor v3.4.2 实现）
     */
    /** VarHandle 版多生产者 Sequencer 的 JMH 组状态。 */
    @State(Scope.Group)
    public static class StateMultiProducerSequencerVarHandle
    {
        /** 第一个 Sequencer 实例。 */
        Sequencer value1 = new MultiProducerSequencerVarHandle(64, new BlockingWaitStrategy());
        /** 第二个 Sequencer 实例。 */
        Sequencer value2 = new MultiProducerSequencerVarHandle(64, new BlockingWaitStrategy());
    }

    /** 并发读取 value1 可用性。 */
    @Benchmark
    @Group("StateMultiProducerSequencerVarHandle")
    public boolean read1(final StateMultiProducerSequencerVarHandle s, final ThreadPinningState t)
    {
        return s.value1.isAvailable(1);
    }

    /** 并发读取 value1 可用性（第二读线程）。 */
    @Benchmark
    @Group("StateMultiProducerSequencerVarHandle")
    public boolean read2(final StateMultiProducerSequencerVarHandle s, final ThreadPinningState t)
    {
        return s.value1.isAvailable(1);
    }

    /** 向 value1 发布序号 1。 */
    @Benchmark
    @Group("StateMultiProducerSequencerVarHandle")
    public void setValue1A(final StateMultiProducerSequencerVarHandle s, final ThreadPinningState t)
    {
        s.value1.publish(1L);
    }

    /** 向 value1 发布序号 2。 */
    @Benchmark
    @Group("StateMultiProducerSequencerVarHandle")
    public void setValue1B(final StateMultiProducerSequencerVarHandle s, final ThreadPinningState t)
    {
        s.value1.publish(2L);
    }

    /** 向 value2 发布序号 1。 */
    @Benchmark
    @Group("StateMultiProducerSequencerVarHandle")
    public void setValue2A(final StateMultiProducerSequencerVarHandle s, final ThreadPinningState t)
    {
        s.value2.publish(1L);
    }

    /** 向 value2 发布序号 2。 */
    @Benchmark
    @Group("StateMultiProducerSequencerVarHandle")
    public void setValue2B(final StateMultiProducerSequencerVarHandle s, final ThreadPinningState t)
    {
        s.value2.publish(2L);
    }

    /**
     * 独立运行本基准。
     */
    public static void main(final String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(MultiProducerSequencerBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
