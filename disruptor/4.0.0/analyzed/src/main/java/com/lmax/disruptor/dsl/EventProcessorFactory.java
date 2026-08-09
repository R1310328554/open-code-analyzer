package com.lmax.disruptor.dsl;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;

/**
 * 工厂接口，用于在处理器链中插入自定义 {@link EventProcessor}：
 *
 * <pre><code>
 * disruptor.handleEventsWith(handler1).then((ringBuffer, barrierSequences) -&gt; new CustomEventProcessor(ringBuffer, barrierSequences));
 * </code></pre>
 *
 * @param <T> 事件中承载的数据类型
 */
public interface EventProcessorFactory<T>
{
    /**
     * 创建以 {@code barrierSequences} 为门控的新事件处理器。
     *
     * @param ringBuffer 事件来源 RingBuffer
     * @param barrierSequences 要门控的序号
     * @return 在处理事件前需等待 {@code barrierSequences} 的新 {@link EventProcessor}
     */
    EventProcessor createEventProcessor(RingBuffer<T> ringBuffer, Sequence[] barrierSequences);
}
