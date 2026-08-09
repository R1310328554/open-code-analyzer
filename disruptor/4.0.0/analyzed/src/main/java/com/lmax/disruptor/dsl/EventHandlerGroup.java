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

import com.lmax.disruptor.BatchRewindStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RewindableEventHandler;
import com.lmax.disruptor.RewindableException;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;

import java.util.Arrays;

/**
 * 作为 {@link Disruptor} 组成部分的 {@link EventProcessor} 组。
 *
 * @param <T> 事件处理器处理的事件类型
 */
public class EventHandlerGroup<T>
{
    private final Disruptor<T> disruptor;
    private final ConsumerRepository consumerRepository;
    private final Sequence[] sequences;

    EventHandlerGroup(
        final Disruptor<T> disruptor,
        final ConsumerRepository consumerRepository,
        final Sequence[] sequences)
    {
        this.disruptor = disruptor;
        this.consumerRepository = consumerRepository;
        this.sequences = Arrays.copyOf(sequences, sequences.length);
    }

    /**
     * 将本组消费者与 {@code otherHandlerGroup} 合并为新的处理器组。
     *
     * @param otherHandlerGroup 要合并的事件处理器组
     * @return 合并现有与新消费者后的 {@link EventHandlerGroup}
     */
    public EventHandlerGroup<T> and(final EventHandlerGroup<T> otherHandlerGroup)
    {
        final Sequence[] combinedSequences = new Sequence[this.sequences.length + otherHandlerGroup.sequences.length];
        System.arraycopy(this.sequences, 0, combinedSequences, 0, this.sequences.length);
        System.arraycopy(
            otherHandlerGroup.sequences, 0,
            combinedSequences, this.sequences.length, otherHandlerGroup.sequences.length);
        return new EventHandlerGroup<>(disruptor, consumerRepository, combinedSequences);
    }

    /**
     * 将本组处理器与 {@code processors} 合并为新的处理器组。
     *
     * @param processors 要合并的处理器
     * @return 合并现有与新处理器后的 {@link EventHandlerGroup}
     */
    public EventHandlerGroup<T> and(final EventProcessor... processors)
    {
        Sequence[] combinedSequences = new Sequence[sequences.length + processors.length];

        for (int i = 0; i < processors.length; i++)
        {
            consumerRepository.add(processors[i]);
            combinedSequences[i] = processors[i].getSequence();
        }
        System.arraycopy(sequences, 0, combinedSequences, processors.length, sequences.length);

        return new EventHandlerGroup<>(disruptor, consumerRepository, combinedSequences);
    }

    /**
     * <p>配置批处理处理器消费 RingBuffer 事件。仅当本组中每个 {@link EventProcessor}
     * 都已处理完该事件后，这些处理器才会开始处理。</p>
     *
     * <p>通常作为链式调用的一部分。例如处理器 {@code A} 必须在 {@code B} 之前处理事件：</p>
     *
     * <pre><code>dw.handleEventsWith(A).then(B);</code></pre>
     *
     * @param handlers 将处理事件的批处理器
     * @return 可用于在新建事件处理器上设置屏障的 {@link EventHandlerGroup}
     */
    @SafeVarargs
    public final EventHandlerGroup<T> then(final EventHandler<? super T>... handlers)
    {
        return handleEventsWith(handlers);
    }

    /**
     * <p>配置可回卷批处理处理器消费 RingBuffer 事件。仅当本组中每个 {@link EventProcessor}
     * 都已处理完该事件后，这些处理器才会开始处理。</p>
     *
     * <p>通常作为链式调用的一部分。例如处理器 {@code A} 必须在 {@code B} 之前处理事件：</p>
     *
     * <pre><code>dw.handleEventsWith(A).then(B);</code></pre>
     *
     * @param batchRewindStrategy 自定义 {@link RewindableException} 处理方式的 {@link BatchRewindStrategy}
     * @param handlers 将处理事件的可回卷处理器
     * @return 可用于在新建事件处理器上设置屏障的 {@link EventHandlerGroup}
     */
    @SafeVarargs
    public final EventHandlerGroup<T> then(final BatchRewindStrategy batchRewindStrategy,
                                           final RewindableEventHandler<? super T>... handlers)
    {
        return handleEventsWith(batchRewindStrategy, handlers);
    }

    /**
     * <p>配置自定义事件处理器消费 RingBuffer 事件。调用 {@link Disruptor#start()} 时
     * Disruptor 会自动启动这些处理器。</p>
     *
     * <p>通常作为链式调用的一部分。例如处理器 {@code A} 必须在 {@code B} 之前处理事件。</p>
     *
     * @param eventProcessorFactories 用于创建事件处理器的工厂
     * @return 可用于继续链接依赖关系的 {@link EventHandlerGroup}
     */
    @SafeVarargs
    public final EventHandlerGroup<T> then(final EventProcessorFactory<T>... eventProcessorFactories)
    {
        return handleEventsWith(eventProcessorFactories);
    }

    /**
     * <p>配置批处理处理器处理 RingBuffer 事件。仅当本组中每个 {@link EventProcessor}
     * 都已处理完该事件后，这些处理器才会开始处理。</p>
     *
     * <p>通常作为链式调用的一部分。例如 {@code A} 必须在 {@code B} 之前处理事件：</p>
     *
     * <pre><code>dw.after(A).handleEventsWith(B);</code></pre>
     *
     * @param handlers 将处理事件的批处理器
     * @return 可用于在新建事件处理器上设置屏障的 {@link EventHandlerGroup}
     */
    @SafeVarargs
    public final EventHandlerGroup<T> handleEventsWith(final EventHandler<? super T>... handlers)
    {
        return disruptor.createEventProcessors(sequences, handlers);
    }

    /**
     * <p>配置可回卷批处理处理器处理 RingBuffer 事件。仅当本组中每个 {@link EventProcessor}
     * 都已处理完该事件后，这些处理器才会开始处理。</p>
     *
     * <p>通常作为链式调用的一部分。例如 {@code A} 必须在 {@code B} 之前处理事件：</p>
     *
     * <pre><code>dw.after(A).handleEventsWith(B);</code></pre>
     *
     * @param batchRewindStrategy 自定义 {@link RewindableException} 处理方式的 {@link BatchRewindStrategy}
     * @param handlers 将处理事件的可回卷处理器
     * @return 可用于在新建事件处理器上设置屏障的 {@link EventHandlerGroup}
     */
    @SafeVarargs
    public final EventHandlerGroup<T> handleEventsWith(final BatchRewindStrategy batchRewindStrategy,
                                                       final RewindableEventHandler<? super T>... handlers)
    {
        return disruptor.createEventProcessors(sequences, batchRewindStrategy, handlers);
    }

    /**
     * <p>配置自定义事件处理器处理 RingBuffer 事件。调用 {@link Disruptor#start()} 时
     * Disruptor 会自动启动这些处理器。</p>
     *
     * <p>通常作为链式调用的一部分。例如 {@code A} 必须在 {@code B} 之前处理事件：</p>
     *
     * <pre><code>dw.after(A).handleEventsWith(B);</code></pre>
     *
     * @param eventProcessorFactories 用于创建事件处理器的工厂
     * @return 可用于继续链接依赖关系的 {@link EventHandlerGroup}
     */
    @SafeVarargs
    public final EventHandlerGroup<T> handleEventsWith(final EventProcessorFactory<T>... eventProcessorFactories)
    {
        return disruptor.createEventProcessors(sequences, eventProcessorFactories);
    }

    /**
     * 为本组处理器创建依赖屏障，使自定义事件处理器可依赖
     * Disruptor 创建的 {@link com.lmax.disruptor.BatchEventProcessor}。
     *
     * @return 包含本组所有处理器的 {@link SequenceBarrier}
     */
    public SequenceBarrier asSequenceBarrier()
    {
        return disruptor.getRingBuffer().newBarrier(sequences);
    }
}
