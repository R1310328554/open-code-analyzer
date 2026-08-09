/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor.dsl;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BatchEventProcessorBuilder;
import com.lmax.disruptor.BatchRewindStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventHandlerIdentity;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.EventTranslatorThreeArg;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RewindableEventHandler;
import com.lmax.disruptor.RewindableException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.util.Util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 以 DSL 风格配置围绕环形缓冲区的 Disruptor 模式（建造者模式）。
 *
 * <p>设置两个必须按序处理事件的处理器的简单示例：
 *
 * <pre>
 * <code>Disruptor&lt;MyEvent&gt; disruptor = new Disruptor&lt;MyEvent&gt;(MyEvent.FACTORY, 32, Executors.newCachedThreadPool());
 * EventHandler&lt;MyEvent&gt; handler1 = new EventHandler&lt;MyEvent&gt;() { ... };
 * EventHandler&lt;MyEvent&gt; handler2 = new EventHandler&lt;MyEvent&gt;() { ... };
 * disruptor.handleEventsWith(handler1);
 * disruptor.after(handler1).handleEventsWith(handler2);
 *
 * RingBuffer ringBuffer = disruptor.start();</code>
 * </pre>
 *
 * @param <T> 事件类型
 */
public class Disruptor<T>
{
    private final RingBuffer<T> ringBuffer;
    private final ThreadFactory threadFactory;
    private final ConsumerRepository consumerRepository = new ConsumerRepository();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private ExceptionHandler<? super T> exceptionHandler = new ExceptionHandlerWrapper<>();

    /**
     * 创建 Disruptor，默认使用 {@link com.lmax.disruptor.BlockingWaitStrategy} and
     * {@link ProducerType}.MULTI
     *
     * @param eventFactory   在环形缓冲区中创建事件的工厂
     * @param ringBufferSize 环形缓冲区大小
     * @param threadFactory  为处理器创建线程的 {@link ThreadFactory}
     */
    public Disruptor(final EventFactory<T> eventFactory, final int ringBufferSize, final ThreadFactory threadFactory)
    {
        this(RingBuffer.createMultiProducer(eventFactory, ringBufferSize), threadFactory);
    }

    /**
     * 创建 Disruptor。
     *
     * @param eventFactory   在环形缓冲区中创建事件的工厂
     * @param ringBufferSize 环形缓冲区大小，须为 2 的幂
     * @param threadFactory  为处理器创建线程的 {@link ThreadFactory}
     * @param producerType   环形缓冲区的申领策略
     * @param waitStrategy   环形缓冲区的等待策略
     */
    public Disruptor(
            final EventFactory<T> eventFactory,
            final int ringBufferSize,
            final ThreadFactory threadFactory,
            final ProducerType producerType,
            final WaitStrategy waitStrategy)
    {
        this(
            RingBuffer.create(producerType, eventFactory, ringBufferSize, waitStrategy),
            threadFactory);
    }

    /**
     * 私有构造辅助方法
     */
    private Disruptor(final RingBuffer<T> ringBuffer, final ThreadFactory threadFactory)
    {
        this.ringBuffer = ringBuffer;
        this.threadFactory = threadFactory;
    }

    /**
     * <p>配置事件处理器，一旦有事件可用即并行处理</p>
     *
     * <p>可作为依赖链的起点，例如处理器 <code>A</code> 必须在 <code>B</code> 之前处理：</p>
     * <pre><code>dw.handleEventsWith(A).then(B);</code></pre>
     *
     * <p>可多次调用（累加），但通常在搭建 Disruptor 时只调用一次</p>
     *
     * @param handlers 将处理事件的处理器
     * @return 可用于链接依赖关系的 {@link EventHandlerGroup}
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public final EventHandlerGroup<T> handleEventsWith(final EventHandler<? super T>... handlers)
    {
        return createEventProcessors(new Sequence[0], handlers);
    }

    /**
     * <p>配置事件处理器，一旦有事件可用即并行处理</p>
     *
     * <p>可作为依赖链的起点，例如处理器 <code>A</code> 必须在 <code>B</code> 之前处理：</p>
     * <pre><code>dw.handleEventsWith(A).then(B);</code></pre>
     *
     * <p>可多次调用（累加），但通常在搭建 Disruptor 时只调用一次</p>
     *
     * @param batchRewindStrategy 处理 {@link RewindableException} 的策略
     * @param handlers            将处理事件的可回退处理器
     * @return 可用于链接依赖关系的 {@link EventHandlerGroup}
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public final EventHandlerGroup<T> handleEventsWith(final BatchRewindStrategy batchRewindStrategy,
                                                       final RewindableEventHandler<? super T>... handlers)
    {
        return createEventProcessors(new Sequence[0], batchRewindStrategy, handlers);
    }

    /**
     * <p>配置自定义事件处理器；调用 {@link #start()} 时 Disruptor 会自动启动它们</p>
     *
     * <p>可作为依赖链的起点，例如处理器 <code>A</code> 必须在 <code>B</code> 之前处理：</p>
     * <pre><code>dw.handleEventsWith(A).then(B);</code></pre>
     *
     * <p>Since this is the start of the chain, the processor factories will always be passed an empty <code>Sequence</code>
     * array, so the factory isn't necessary in this case. This method is provided for consistency with
     * {@link EventHandlerGroup#handleEventsWith(EventProcessorFactory...)} and {@link EventHandlerGroup#then(EventProcessorFactory...)}
     * which do have barrier sequences to provide.</p>
     *
     * <p>可多次调用（累加），但通常在搭建 Disruptor 时只调用一次</p>
     *
     * @param eventProcessorFactories 用于创建事件处理器的工厂
     * @return 可用于链接依赖关系的 {@link EventHandlerGroup}
     */
    @SafeVarargs
    public final EventHandlerGroup<T> handleEventsWith(final EventProcessorFactory<T>... eventProcessorFactories)
    {
        final Sequence[] barrierSequences = new Sequence[0];
        return createEventProcessors(barrierSequences, eventProcessorFactories);
    }

    /**
     * <p>Set up custom event processors to handle events from the ring buffer. The Disruptor will
     * 在 {@link #start()} is called.</p>
     *
     * <p>This method can be used as the start of a chain. For example if the processor <code>A</code> must
     * process events before handler <code>B</code>:</p>
     * <pre><code>dw.handleEventsWith(A).then(B);</code></pre>
     *
     * @param processors 将处理事件的处理器
     * @return 可用于链接依赖关系的 {@link EventHandlerGroup}
     */
    public EventHandlerGroup<T> handleEventsWith(final EventProcessor... processors)
    {
        for (final EventProcessor processor : processors)
        {
            consumerRepository.add(processor);
        }

        final Sequence[] sequences = Util.getSequencesFor(processors);

        ringBuffer.addGatingSequences(sequences);

        return new EventHandlerGroup<>(this, consumerRepository, sequences);
    }


    /**
     * <p>为后续创建的事件处理器指定异常处理器。</p>
     *
     * <p>仅在此方法之后注册的处理器会使用该异常处理器。</p>
     *
     * @param exceptionHandler 供后续 {@link EventProcessor} 使用的异常处理器
     * @deprecated 仅作用于后续处理器；请改用 setDefaultExceptionHandler，其对已有与新增处理器均生效
     */
    @Deprecated
    public void handleExceptionsWith(final ExceptionHandler<? super T> exceptionHandler)
    {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * <p>为本 Disruptor 创建的事件处理器与工作池指定异常处理器。</p>
     *
     * <p>该处理器对本实例已有及后续创建的处理器与工作池均生效。</p>
     *
     * @param exceptionHandler 要使用的异常处理器
     */
    @SuppressWarnings("unchecked")
    public void setDefaultExceptionHandler(final ExceptionHandler<? super T> exceptionHandler)
    {
        checkNotStarted();
        if (!(this.exceptionHandler instanceof ExceptionHandlerWrapper))
        {
            throw new IllegalStateException("setDefaultExceptionHandler can not be used after handleExceptionsWith");
        }
        ((ExceptionHandlerWrapper<T>) this.exceptionHandler).switchTo(exceptionHandler);
    }

    /**
     * 为特定处理器覆盖默认异常处理器。
     * <pre>disruptorWizard.handleExceptionsIn(eventHandler).with(exceptionHandler);</pre>
     *
     * @param eventHandler 要单独设置异常处理器的处理器
     * @return 用于链式调用 with 的 DSL 设置对象
     */
    public ExceptionHandlerSetting<T> handleExceptionsFor(final EventHandlerIdentity eventHandler)
    {
        return new ExceptionHandlerSetting<>(eventHandler, consumerRepository);
    }

    /**
     * <p>创建一组事件处理器作为下游依赖。
     * For example if the handler <code>A</code> must process events before handler <code>B</code>:</p>
     *
     * <pre><code>dw.after(A).handleEventsWith(B);</code></pre>
     *
     * @param handlers 先前通过 {@link #handleEventsWith(EventHandler[])} 注册的处理器，
     *                 将作为后续处理器屏障的序号来源
     * @return 可用于在指定处理器上建立依赖屏障的 {@link EventHandlerGroup}
     */
    public final EventHandlerGroup<T> after(final EventHandlerIdentity... handlers)
    {
        final Sequence[] sequences = new Sequence[handlers.length];
        for (int i = 0, handlersLength = handlers.length; i < handlersLength; i++)
        {
            sequences[i] = consumerRepository.getSequenceFor(handlers[i]);
        }

        return new EventHandlerGroup<>(this, consumerRepository, sequences);
    }

    /**
     * 创建一组事件处理器作为下游依赖。
     *
     * @param processors 先前通过 {@link #handleEventsWith(com.lmax.disruptor.EventProcessor...)} 注册的处理器，
     *                   将作为后续处理器屏障的序号来源
     * @return 可用于在指定处理器上建立 {@link SequenceBarrier} 的 {@link EventHandlerGroup}
     * @see #after(EventHandlerIdentity[])
     */
    public EventHandlerGroup<T> after(final EventProcessor... processors)
    {
        return new EventHandlerGroup<>(this, consumerRepository, Util.getSequencesFor(processors));
    }

    /**
     * 向环形缓冲区发布单个事件。
     *
     * @param eventTranslator 将数据写入事件的转换器
     */
    public void publishEvent(final EventTranslator<T> eventTranslator)
    {
        ringBuffer.publishEvent(eventTranslator);
    }

    /**
     * 向环形缓冲区发布单个事件。
     *
     * @param <A>             用户提供的参数类型
     * @param eventTranslator 将数据写入事件的转换器
     * @param arg             写入事件的单个参数
     */
    public <A> void publishEvent(final EventTranslatorOneArg<T, A> eventTranslator, final A arg)
    {
        ringBuffer.publishEvent(eventTranslator, arg);
    }

    /**
     * 向环形缓冲区批量发布事件。
     *
     * @param <A>             用户提供的参数类型
     * @param eventTranslator 将数据写入事件的转换器
     * @param arg             写入各事件的参数数组，每个事件一个
     */
    public <A> void publishEvents(final EventTranslatorOneArg<T, A> eventTranslator, final A[] arg)
    {
        ringBuffer.publishEvents(eventTranslator, arg);
    }

    /**
     * 向环形缓冲区发布单个事件。
     *
     * @param <A>             用户提供的参数类型
     * @param <B>             用户提供的参数类型
     * @param eventTranslator 将数据写入事件的转换器
     * @param arg0            写入事件的第一个参数
     * @param arg1            写入事件的第二个参数
     */
    public <A, B> void publishEvent(final EventTranslatorTwoArg<T, A, B> eventTranslator, final A arg0, final B arg1)
    {
        ringBuffer.publishEvent(eventTranslator, arg0, arg1);
    }

    /**
     * 向环形缓冲区发布单个事件。
     *
     * @param eventTranslator 将数据写入事件的转换器
     * @param <A>             用户提供的参数类型
     * @param <B>             用户提供的参数类型
     * @param <C>             用户提供的参数类型
     * @param arg0            写入事件的第一个参数
     * @param arg1            写入事件的第二个参数
     * @param arg2            写入事件的第三个参数
     */
    public <A, B, C> void publishEvent(final EventTranslatorThreeArg<T, A, B, C> eventTranslator, final A arg0, final B arg1, final C arg2)
    {
        ringBuffer.publishEvent(eventTranslator, arg0, arg1, arg2);
    }

    /**
     * <p>启动事件处理器并返回配置完成的环形缓冲区。</p>
     *
     * <p>环形缓冲区已配置为不会覆盖最慢处理器尚未消费完的槽位。</p>
     *
     * <p>所有处理器注册完毕后，本方法只能调用一次。</p>
     *
     * @return 配置完成的环形缓冲区
     */
    public RingBuffer<T> start()
    {
        checkOnlyStartedOnce();
        consumerRepository.startAll(threadFactory);

        return ringBuffer;
    }

    /**
     * 对本 Disruptor 创建的所有 {@link com.lmax.disruptor.EventProcessor} 调用 halt()。
     */
    public void halt()
    {
        consumerRepository.haltAll();
    }

    /**
     * <p>等待当前队列中的事件全部被处理完毕后停止处理器。调用前必须已停止发布，否则可能永不返回。</p>
     *
     * <p>本方法不会关闭线程池，也不会等待处理器线程完全终止。</p>
     */
    public void shutdown()
    {
        try
        {
            shutdown(-1, TimeUnit.MILLISECONDS);
        }
        catch (final TimeoutException e)
        {
            exceptionHandler.handleOnShutdownException(e);
        }
    }

    /**
     * <p>Waits until all events currently in the disruptor have been processed by all event processors
     * and then halts the processors.</p>
     *
     * <p>本方法不会关闭线程池，也不会等待处理器线程完全终止。</p>
     *
     * @param timeout  等待全部事件处理完毕的超时时间；<code>-1</code> 表示无限等待
     * @param timeUnit 超时时间单位
     * @throws TimeoutException 超时前未完成关闭
     */
    public void shutdown(final long timeout, final TimeUnit timeUnit) throws TimeoutException
    {
        final long timeOutAt = System.nanoTime() + timeUnit.toNanos(timeout);
        while (hasBacklog())
        {
            if (timeout >= 0 && System.nanoTime() > timeOutAt)
            {
                throw TimeoutException.INSTANCE;
            }
            // Busy spin
        }
        halt();
    }

    /**
     * 本 Disruptor 使用的 {@link RingBuffer}。当 {@link BatchEventProcessor} 行为不满足需求时，可用于创建自定义处理器。
     *
     * @return 本 Disruptor 使用的环形缓冲区
     */
    public RingBuffer<T> getRingBuffer()
    {
        return ringBuffer;
    }

    /**
     * 获取表示已发布序号的游标值。
     *
     * @return 已发布事件的游标值
     */
    public long getCursor()
    {
        return ringBuffer.getCursor();
    }

    /**
     * 数据结构可容纳的条目容量。
     *
     * @return {@link RingBuffer} 的大小
     * @see com.lmax.disruptor.Sequencer#getBufferSize()
     */
    public long getBufferSize()
    {
        return ringBuffer.getBufferSize();
    }

    /**
     * 按序号获取环形缓冲区中的事件。
     *
     * @param sequence 事件序号
     * @return 对应序号的事件
     * @see RingBuffer#get(long)
     */
    public T get(final long sequence)
    {
        return ringBuffer.get(sequence);
    }

    /**
     * 获取特定处理器使用的 {@link SequenceBarrier}；注意多个处理器可能共享同一屏障。
     *
     * @param handler 目标处理器
     * @return <i>handler</i> 使用的 {@link SequenceBarrier}
     */
    public SequenceBarrier getBarrierFor(final EventHandlerIdentity handler)
    {
        return consumerRepository.getBarrierFor(handler);
    }

    /**
     * 获取指定事件处理器的当前序号值。
     *
     * @param handler 目标处理器
     * @return 处理器的当前序号
     */
    public long getSequenceValueFor(final EventHandlerIdentity handler)
    {
        return consumerRepository.getSequenceFor(handler).get();
    }

    /**
     * 确认所有事件处理器是否已消费全部消息
     */
    private boolean hasBacklog()
    {
        final long cursor = ringBuffer.getCursor();

        return consumerRepository.hasBacklog(cursor, false);
    }

    /**
     * 检查 Disruptor 是否已启动
     *
     * @return 若已调用 start 则返回 true，否则 false
     */
    public boolean hasStarted()
    {
        return started.get();
    }

    EventHandlerGroup<T> createEventProcessors(
            final Sequence[] barrierSequences,
            final EventHandler<? super T>[] eventHandlers)
    {
        checkNotStarted();

        final Sequence[] processorSequences = new Sequence[eventHandlers.length];
        final SequenceBarrier barrier = ringBuffer.newBarrier(barrierSequences);

        for (int i = 0, eventHandlersLength = eventHandlers.length; i < eventHandlersLength; i++)
        {
            final EventHandler<? super T> eventHandler = eventHandlers[i];

            final BatchEventProcessor<T> batchEventProcessor =
                    new BatchEventProcessorBuilder().build(ringBuffer, barrier, eventHandler);

            if (exceptionHandler != null)
            {
                batchEventProcessor.setExceptionHandler(exceptionHandler);
            }

            consumerRepository.add(batchEventProcessor, eventHandler, barrier);
            processorSequences[i] = batchEventProcessor.getSequence();
        }

        updateGatingSequencesForNextInChain(barrierSequences, processorSequences);

        return new EventHandlerGroup<>(this, consumerRepository, processorSequences);
    }

    EventHandlerGroup<T> createEventProcessors(
            final Sequence[] barrierSequences,
            final BatchRewindStrategy batchRewindStrategy,
            final RewindableEventHandler<? super T>[] eventHandlers)
    {
        checkNotStarted();

        final Sequence[] processorSequences = new Sequence[eventHandlers.length];
        final SequenceBarrier barrier = ringBuffer.newBarrier(barrierSequences);

        for (int i = 0, eventHandlersLength = eventHandlers.length; i < eventHandlersLength; i++)
        {
            final RewindableEventHandler<? super T> eventHandler = eventHandlers[i];

            final BatchEventProcessor<T> batchEventProcessor =
                    new BatchEventProcessorBuilder().build(ringBuffer, barrier, eventHandler, batchRewindStrategy);

            if (exceptionHandler != null)
            {
                batchEventProcessor.setExceptionHandler(exceptionHandler);
            }

            consumerRepository.add(batchEventProcessor, eventHandler, barrier);
            processorSequences[i] = batchEventProcessor.getSequence();
        }

        updateGatingSequencesForNextInChain(barrierSequences, processorSequences);

        return new EventHandlerGroup<>(this, consumerRepository, processorSequences);
    }

    private void updateGatingSequencesForNextInChain(final Sequence[] barrierSequences, final Sequence[] processorSequences)
    {
        if (processorSequences.length > 0)
        {
            ringBuffer.addGatingSequences(processorSequences);
            for (final Sequence barrierSequence : barrierSequences)
            {
                ringBuffer.removeGatingSequence(barrierSequence);
            }
            consumerRepository.unMarkEventProcessorsAsEndOfChain(barrierSequences);
        }
    }

    EventHandlerGroup<T> createEventProcessors(
            final Sequence[] barrierSequences, final EventProcessorFactory<T>[] processorFactories)
    {
        final EventProcessor[] eventProcessors = new EventProcessor[processorFactories.length];
        for (int i = 0; i < processorFactories.length; i++)
        {
            eventProcessors[i] = processorFactories[i].createEventProcessor(ringBuffer, barrierSequences);
        }

        return handleEventsWith(eventProcessors);
    }

    private void checkNotStarted()
    {
        if (started.get())
        {
            throw new IllegalStateException("All event handlers must be added before calling starts.");
        }
    }

    private void checkOnlyStartedOnce()
    {
        if (!started.compareAndSet(false, true))
        {
            throw new IllegalStateException("Disruptor.start() must only be called once.");
        }
    }

    @Override
    public String toString()
    {
        return "Disruptor{" +
                "ringBuffer=" + ringBuffer +
                ", started=" + started +
                ", threadFactory=" + threadFactory +
                '}';
    }
}
