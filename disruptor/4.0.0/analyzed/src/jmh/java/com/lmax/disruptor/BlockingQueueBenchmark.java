package com.lmax.disruptor;

import com.lmax.disruptor.util.Constants;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.lmax.disruptor.util.SimpleEvent;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 对比 {@link ArrayBlockingQueue} 与 Disruptor 生产侧性能的 JMH 基准。
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
public class BlockingQueueBenchmark
{
    /** 有界阻塞队列，作为对照组。 */
    private BlockingQueue<SimpleEvent> arrayBlockingQueue;
    /** 消费者线程是否继续运行。 */
    private volatile boolean consumerRunning;
    /** 复用的测试事件对象。 */
    private SimpleEvent simpleEvent;

    /**
     * 启动后台消费者线程并准备测试事件。
     */
    @Setup
    public void setup(final Blackhole bh) throws InterruptedException
    {
        arrayBlockingQueue = new ArrayBlockingQueue<>(Constants.RINGBUFFER_SIZE);

        final CountDownLatch consumerStartedLatch = new CountDownLatch(1);
        final Thread eventHandler = DaemonThreadFactory.INSTANCE.newThread(() ->
        {
            consumerStartedLatch.countDown();
            while (consumerRunning)
            {
                SimpleEvent event = arrayBlockingQueue.poll();
                if (event != null)
                {
                    bh.consume(event);
                }
            }
        });
        consumerRunning = true;
        eventHandler.start();
        consumerStartedLatch.await();

        simpleEvent = new SimpleEvent();
        simpleEvent.setValue(0);
    }

    /**
     * 测量向阻塞队列 {@code offer} 事件的平均耗时。
     */
    @Benchmark
    public void producing() throws InterruptedException
    {
        if (!arrayBlockingQueue.offer(simpleEvent, 1, TimeUnit.SECONDS))
        {
            throw new IllegalStateException("Queue full, benchmark should not experience backpressure");
        }
    }

    /**
     * 停止消费者线程。
     */
    @TearDown
    public void tearDown()
    {
        consumerRunning = false;
    }

    /**
     * 独立运行本基准。
     */
    public static void main(final String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(BlockingQueueBenchmark.class.getSimpleName())
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
