package com.lmax.disruptor;

import com.lmax.disruptor.util.SimpleEvent;
import com.lmax.disruptor.util.UnsafeAccess;
import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * 对比多种数组元素访问方式（标准索引、Unsafe、VarHandle、MethodHandle）的 JMH 基准。
 */
@SuppressWarnings("unused")
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(1)
@State(Scope.Thread)
public class ArrayAccessBenchmark
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
     * 线程 CPU 亲和性绑定状态：setup 时锁定到指定核心，teardown 时释放。
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

    /** 环形数组槽位数（须为 2 的幂）。 */
    private static final int EVENT_COUNT = 64;
    /** 序号掩码，用于环形索引。 */
    private static final int INDEX_MASK = EVENT_COUNT - 1;
    /** 预填充的对象数组。 */
    private final Object[] entries = new Object[EVENT_COUNT];
    /** 当前访问序号。 */
    public int sequence;

    /** Unsafe 实例，用于无边界检查访问。 */
    private static final Unsafe UNSAFE = UnsafeAccess.getUnsafe();
    /** 数组元素缩放因子。 */
    private final int scale = UNSAFE.arrayIndexScale(Object[].class);
    /** 数组基址偏移。 */
    private final int offset = UNSAFE.arrayBaseOffset(Object[].class);

    /** VarHandle 数组元素访问器。 */
    private final VarHandle varHandle = MethodHandles.arrayElementVarHandle(Object[].class);

    /** MethodHandle 数组元素 getter。 */
    private final MethodHandle methodHandle = MethodHandles.arrayElementGetter(Object[].class);

    /**
     * 预填充数组并重置序号。
     */
    @Setup
    public void setup()
    {
        for (int i = 0; i < EVENT_COUNT; i++)
        {
            SimpleEvent simpleEvent = new SimpleEvent();
            simpleEvent.setValue(i);
            entries[i] = simpleEvent;
        }

        sequence = 0;
    }

    /**
     * 标准 Java 数组索引访问。
     */
    @Benchmark
    public Object standardArrayAccess(final ThreadPinningState t)
    {
        return entries[getNextSequence()];
    }

    /**
     * 通过 Unsafe 直接内存偏移访问。
     */
    @Benchmark
    public Object unsafeArrayAccess(final ThreadPinningState t)
    {
        return UNSAFE.getObject(entries, offset + ((long) (getNextSequence()) * scale));
    }

    /**
     * 通过 VarHandle 访问。
     */
    @Benchmark
    public Object varHandleArrayAccess(final ThreadPinningState t)
    {
        return varHandle.get(entries, getNextSequence());
    }

    /**
     * 通过 MethodHandle {@code invoke} 访问。
     */
    @Benchmark
    public Object getterMethodHandleInvokeArrayAccess(final ThreadPinningState t) throws Throwable
    {
        return methodHandle.invoke(entries, getNextSequence());
    }

    /**
     * 通过 MethodHandle {@code invokeExact} 访问。
     */
    @Benchmark
    public Object getterMethodHandleInvokeExactArrayAccess(final ThreadPinningState t) throws Throwable
    {
        return methodHandle.invokeExact(entries, getNextSequence());
    }

    /**
     * 递增序号并返回环形索引。
     */
    private int getNextSequence()
    {
        return sequence++ & INDEX_MASK;
    }

    /**
     * 独立运行本基准。
     */
    public static void main(final String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(ArrayAccessBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
