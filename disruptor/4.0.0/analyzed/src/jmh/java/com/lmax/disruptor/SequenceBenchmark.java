/**
 * JMH：对比 AtomicLong 与多种 Sequence 实现的并发读写吞吐。
 */

package com.lmax.disruptor;

import com.lmax.disruptor.alternatives.SequenceDoublePadded;
import com.lmax.disruptor.alternatives.SequenceUnsafe;
import com.lmax.disruptor.alternatives.SequenceVarHandle;
import com.lmax.disruptor.alternatives.SequenceVarHandleArray;
import com.lmax.disruptor.alternatives.SequenceVarHandleBarrier;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@SuppressWarnings("ALL")
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(1)
public class SequenceBenchmark
{
    // 在隔离 CPU 上运行基准：
    // 通过环境变量 ISOLATED_CPUS 指定 CPU，例如 ISOLATED_CPUS=38,40,42,44,46,48 java -jar disruptor-jmh.jar
    private static final List<Integer> ISOLATED_CPUS = Arrays.stream(System.getenv().getOrDefault("ISOLATED_CPUS", "").split(","))
            .map(String::trim)
            .filter(not(String::isBlank))
            .map(Integer::valueOf)
            .collect(Collectors.toList());

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    /** 将基准线程绑定到 ISOLATED_CPUS 列表中的 CPU。 */
    @State(Scope.Thread)
    public static class ThreadPinningState
    {
        int threadId = THREAD_COUNTER.getAndIncrement();
        private AffinityLock affinityLock;

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
     * 方案 1：AtomicLong
     *
     * 线程安全？是。原子更新？是。
     */
    @State(Scope.Group)
    public static class StateAtomic
    {
        AtomicLong value1 = new AtomicLong(0);
        AtomicLong value2 = new AtomicLong(0);
    }

    @Benchmark
    @Group("AtomicLong")
    public long read1(final StateAtomic s, final ThreadPinningState t)
    {
        return s.value1.get();
    }

    @Benchmark
    @Group("AtomicLong")
    public long read2(final StateAtomic s, final ThreadPinningState t)
    {
        return s.value2.get();
    }

    @Benchmark
    @Group("AtomicLong")
    public void setValue1Opaque(final StateAtomic s, final ThreadPinningState t)
    {
        // 有序写入 long（opaque）
        s.value1.setOpaque(1234L);
    }

    @Benchmark
    @Group("AtomicLong")
    public void setValue1Volatile(final StateAtomic s, final ThreadPinningState t)
    {
        // volatile 写入 long
        s.value1.set(5678L);
    }

    @Benchmark
    @Group("AtomicLong")
    public long incrementValue2(final StateAtomic s, final ThreadPinningState t)
    {
        return s.value2.getAndIncrement();
    }

    /*
     * 方案 2：com.lmax.disruptor.Sequence（Disruptor v3.4.2 风格）
     *
     * 与 AtomicLong 类似，但带填充以避免伪共享。
     * 使用 UNSAFE 以更细粒度控制字段内存语义；并非总需完整 volatile 保证，原子更新依赖 compareAndSwap。
     */
    @State(Scope.Group)
    public static class StateSequenceUnsafe
    {
        SequenceUnsafe value1 = new SequenceUnsafe(0);
        SequenceUnsafe value2 = new SequenceUnsafe(0);
    }

    @Benchmark
    @Group("SequenceUnsafe")
    public long read1(final StateSequenceUnsafe s, final ThreadPinningState t)
    {
        return s.value1.get();
    }

    @Benchmark
    @Group("SequenceUnsafe")
    public long read2(final StateSequenceUnsafe s, final ThreadPinningState t)
    {
        return s.value2.get();
    }

    @Benchmark
    @Group("SequenceUnsafe")
    public void setValue1(final StateSequenceUnsafe s, final ThreadPinningState t)
    {
        // 有序写入 long
        s.value1.set(1234L);
    }

    @Benchmark
    @Group("SequenceUnsafe")
    public void setValue1Volatile(final StateSequenceUnsafe s, final ThreadPinningState t)
    {
        // volatile 写入 long
        s.value1.setVolatile(5678L);
    }

    @Benchmark
    @Group("SequenceUnsafe")
    public long incrementValue2(final StateSequenceUnsafe s, final ThreadPinningState t)
    {
        return s.value2.incrementAndGet();
    }

    /*
     * 方案 2.5：com.lmax.disruptor.alternatives.SequenceDoublePadded
     *
     * 与 Disruptor 3.4.2 的 Sequence 相同，但填充量加倍。
     * https://github.com/LMAX-Exchange/disruptor/issues/231 指出 Intel CPU 可能（默认可选）预取 2 条缓存行。
     *
     * 本基准用于对比额外填充相对常规 Sequence 是否有性能差异。
     */
    @State(Scope.Group)
    public static class StateSequenceDoublePadded
    {
        SequenceDoublePadded value1 = new SequenceDoublePadded(0);
        SequenceDoublePadded value2 = new SequenceDoublePadded(0);
    }

    @Benchmark
    @Group("SequenceDoublePadded")
    public long read1(final StateSequenceDoublePadded s, final ThreadPinningState t)
    {
        return s.value1.get();
    }

    @Benchmark
    @Group("SequenceDoublePadded")
    public long read2(final StateSequenceDoublePadded s, final ThreadPinningState t)
    {
        return s.value2.get();
    }

    @Benchmark
    @Group("SequenceDoublePadded")
    public void setValue1(final StateSequenceDoublePadded s, final ThreadPinningState t)
    {
        // 有序写入 long
        s.value1.set(1234L);
    }

    @Benchmark
    @Group("SequenceDoublePadded")
    public void setValue1Volatile(final StateSequenceDoublePadded s, final ThreadPinningState t)
    {
        // volatile 写入 long
        s.value1.setVolatile(5678L);
    }

    @Benchmark
    @Group("SequenceDoublePadded")
    public long incrementValue2(final StateSequenceDoublePadded s, final ThreadPinningState t)
    {
        return s.value2.incrementAndGet();
    }

    /*
     * 方案 3：com.lmax.disruptor.alternatives.SequenceVarHandle
     *
     * 使用 VarHandle 替代 UNSAFE 实现内存序的 Sequence 更新版本，可能是 Disruptor 4.0 的方向。
     */
    @State(Scope.Group)
    public static class StateSequenceVarHandle
    {
        SequenceVarHandle value1 = new SequenceVarHandle(0);
        SequenceVarHandle value2 = new SequenceVarHandle(0);
    }

    @Benchmark
    @Group("SequenceVarHandle")
    public long read1(final StateSequenceVarHandle s, final ThreadPinningState t)
    {
        return s.value1.get();
    }

    @Benchmark
    @Group("SequenceVarHandle")
    public long read2(final StateSequenceVarHandle s, final ThreadPinningState t)
    {
        return s.value2.get();
    }

    @Benchmark
    @Group("SequenceVarHandle")
    public void setValue1(final StateSequenceVarHandle s, final ThreadPinningState t)
    {
        // 有序写入 long
        s.value1.set(1234L);
    }

    @Benchmark
    @Group("SequenceVarHandle")
    public void setValue1Volatile(final StateSequenceVarHandle s, final ThreadPinningState t)
    {
        // volatile 写入 long
        s.value1.setVolatile(5678L);
    }

    @Benchmark
    @Group("SequenceVarHandle")
    public long incrementValue2(final StateSequenceVarHandle s, final ThreadPinningState t)
    {
        return s.value2.incrementAndGet();
    }

    /*
     * 方案 3.5：com.lmax.disruptor.alternatives.SequenceVarHandleBarrier
     *
     * 与 VarHandle 版类似，但使用手动内存屏障；可能减少装箱并提供更多灵活性。
     */
    @State(Scope.Group)
    public static class StateSequenceVarHandleBarrier
    {
        SequenceVarHandleBarrier value1 = new SequenceVarHandleBarrier(0);
        SequenceVarHandleBarrier value2 = new SequenceVarHandleBarrier(0);
    }

    @Benchmark
    @Group("SequenceVarHandleBarrier")
    public long read1(final StateSequenceVarHandleBarrier s, final ThreadPinningState t)
    {
        return s.value1.get();
    }

    @Benchmark
    @Group("SequenceVarHandleBarrier")
    public long read2(final StateSequenceVarHandleBarrier s, final ThreadPinningState t)
    {
        return s.value2.get();
    }

    @Benchmark
    @Group("SequenceVarHandleBarrier")
    public void setValue1(final StateSequenceVarHandleBarrier s, final ThreadPinningState t)
    {
        // 有序写入 long
        s.value1.set(1234L);
    }

    @Benchmark
    @Group("SequenceVarHandleBarrier")
    public void setValue1Volatile(final StateSequenceVarHandleBarrier s, final ThreadPinningState t)
    {
        // volatile 写入 long
        s.value1.setVolatile(5678L);
    }

    @Benchmark
    @Group("SequenceVarHandleBarrier")
    public long incrementValue2(final StateSequenceVarHandleBarrier s, final ThreadPinningState t)
    {
        return s.value2.incrementAndGet();
    }

    /*
     * 方案 4：com.lmax.disruptor.alternatives.SequenceVarHandleArray
     *
     * 与 SequenceVarHandle 类似，但用 long 数组而非类层次做填充。
     * 数组边界检查会拖慢所有操作；保留此方案以警示：数组填充可能不是好选择。
     */
    @State(Scope.Group)
    public static class StateSequenceVarHandleArray
    {
        SequenceVarHandleArray value1 = new SequenceVarHandleArray(0);
        SequenceVarHandleArray value2 = new SequenceVarHandleArray(0);
    }

    @Benchmark
    @Group("SequenceVarHandleArray")
    public long read1(final StateSequenceVarHandleArray s, final ThreadPinningState t)
    {
        return s.value1.get();
    }

    @Benchmark
    @Group("SequenceVarHandleArray")
    public long read2(final StateSequenceVarHandleArray s, final ThreadPinningState t)
    {
        return s.value2.get();
    }

    @Benchmark
    @Group("SequenceVarHandleArray")
    public void setValue1(final StateSequenceVarHandleArray s, final ThreadPinningState t)
    {
        // 有序写入 long
        s.value1.set(1234L);
    }

    @Benchmark
    @Group("SequenceVarHandleArray")
    public void setValue1Volatile(final StateSequenceVarHandleArray s, final ThreadPinningState t)
    {
        // volatile 写入 long
        s.value1.setVolatile(5678L);
    }

    @Benchmark
    @Group("SequenceVarHandleArray")
    public long incrementValue2(final StateSequenceVarHandleArray s, final ThreadPinningState t)
    {
        return s.value2.incrementAndGet();
    }

    public static void main(final String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(SequenceBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
