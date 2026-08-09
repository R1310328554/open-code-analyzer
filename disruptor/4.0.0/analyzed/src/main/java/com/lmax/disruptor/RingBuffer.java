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
package com.lmax.disruptor;


import com.lmax.disruptor.dsl.ProducerType;

abstract class RingBufferPad
{
    protected byte
        p10, p11, p12, p13, p14, p15, p16, p17,
        p20, p21, p22, p23, p24, p25, p26, p27,
        p30, p31, p32, p33, p34, p35, p36, p37,
        p40, p41, p42, p43, p44, p45, p46, p47,
        p50, p51, p52, p53, p54, p55, p56, p57,
        p60, p61, p62, p63, p64, p65, p66, p67,
        p70, p71, p72, p73, p74, p75, p76, p77;
}

abstract class RingBufferFields<E> extends RingBufferPad
{
    private static final int BUFFER_PAD = 32;

    private final long indexMask;
    private final E[] entries;
    protected final int bufferSize;
    protected final Sequencer sequencer;

    @SuppressWarnings("unchecked")
    RingBufferFields(
        final EventFactory<E> eventFactory,
        final Sequencer sequencer)
    {
        this.sequencer = sequencer;
        this.bufferSize = sequencer.getBufferSize();

        if (bufferSize < 1)
        {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }
        if (Integer.bitCount(bufferSize) != 1)
        {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }

        this.indexMask = bufferSize - 1;
        this.entries = (E[]) new Object[bufferSize + 2 * BUFFER_PAD];
        fill(eventFactory);
    }

    private void fill(final EventFactory<E> eventFactory)
    {
        // 步骤：预分配每个槽位的事件对象，跳过头尾 BUFFER_PAD 填充区
        for (int i = 0; i < bufferSize; i++)
        {
            entries[BUFFER_PAD + i] = eventFactory.newInstance();
        }
    }

    protected final E elementAt(final long sequence)
    {
        // 步骤：用序号掩码映射到物理槽位（2 的幂大小，等价于取模）
        return entries[BUFFER_PAD + (int) (sequence & indexMask)];
    }
}

/**
 * 基于环形数组的可复用事件槽位存储，在发布者与 {@link EventProcessor} 之间交换事件数据。
 *
 * @param <E> 事件实现类型，在交换或并行协调过程中承载共享数据
 */
public final class RingBuffer<E> extends RingBufferFields<E> implements Cursored, EventSequencer<E>, EventSink<E>
{
    /**
     * 游标初始值
     */
    public static final long INITIAL_CURSOR_VALUE = Sequence.INITIAL_VALUE;
    protected byte
        p10, p11, p12, p13, p14, p15, p16, p17,
        p20, p21, p22, p23, p24, p25, p26, p27,
        p30, p31, p32, p33, p34, p35, p36, p37,
        p40, p41, p42, p43, p44, p45, p46, p47,
        p50, p51, p52, p53, p54, p55, p56, p57,
        p60, p61, p62, p63, p64, p65, p66, p67,
        p70, p71, p72, p73, p74, p75, p76, p77;

    /**
     * 以完整参数集构造 {@link RingBuffer}。
     *
     * @param eventFactory 用于预填充环形缓冲区槽位的事件工厂
     * @param sequencer    管理事件在环形缓冲区中流转顺序的序号器
     * @throws IllegalArgumentException if bufferSize is less than 1 or not a power of 2
     */
    RingBuffer(
        final EventFactory<E> eventFactory,
        final Sequencer sequencer)
    {
        super(eventFactory, sequencer);
    }

    /**
     * 创建多生产者环形缓冲区，并指定等待策略。
     *
     * @param <E> 环形缓冲区中存储的事件类型
     * @param factory      在环形缓冲区中创建事件的工厂
     * @param bufferSize   环形缓冲区元素个数
     * @param waitStrategy 等待新元素可用的策略
     * @return 构造完成的环形缓冲区
     * @throws IllegalArgumentException if bufferSize is less than 1 or not a power of 2
     * @see MultiProducerSequencer
     */
    public static <E> RingBuffer<E> createMultiProducer(
        final EventFactory<E> factory,
        final int bufferSize,
        final WaitStrategy waitStrategy)
    {
        MultiProducerSequencer sequencer = new MultiProducerSequencer(bufferSize, waitStrategy);

        return new RingBuffer<>(factory, sequencer);
    }

    /**
     * 创建多生产者环形缓冲区，使用默认等待策略  {@link BlockingWaitStrategy}.
     *
     * @param <E> 环形缓冲区中存储的事件类型
     * @param factory    used to create the events within the ring buffer.
     * @param bufferSize number of elements to create within the ring buffer.
     * @return 构造完成的环形缓冲区
     * @throws IllegalArgumentException if <code>bufferSize</code> is less than 1 or not a power of 2
     * @see MultiProducerSequencer
     */
    public static <E> RingBuffer<E> createMultiProducer(final EventFactory<E> factory, final int bufferSize)
    {
        return createMultiProducer(factory, bufferSize, new BlockingWaitStrategy());
    }

    /**
     * 创建单生产者环形缓冲区，并指定等待策略。
     *
     * @param <E> 环形缓冲区中存储的事件类型
     * @param factory      在环形缓冲区中创建事件的工厂
     * @param bufferSize   环形缓冲区元素个数
     * @param waitStrategy 等待新元素可用的策略
     * @return 构造完成的环形缓冲区
     * @throws IllegalArgumentException if bufferSize is less than 1 or not a power of 2
     * @see SingleProducerSequencer
     */
    public static <E> RingBuffer<E> createSingleProducer(
        final EventFactory<E> factory,
        final int bufferSize,
        final WaitStrategy waitStrategy)
    {
        SingleProducerSequencer sequencer = new SingleProducerSequencer(bufferSize, waitStrategy);

        return new RingBuffer<>(factory, sequencer);
    }

    /**
     * 创建单生产者环形缓冲区，使用默认等待策略  {@link BlockingWaitStrategy}.
     *
     * @param <E> 环形缓冲区中存储的事件类型
     * @param factory    used to create the events within the ring buffer.
     * @param bufferSize number of elements to create within the ring buffer.
     * @return 构造完成的环形缓冲区
     * @throws IllegalArgumentException if <code>bufferSize</code> is less than 1 or not a power of 2
     * @see MultiProducerSequencer
     */
    public static <E> RingBuffer<E> createSingleProducer(final EventFactory<E> factory, final int bufferSize)
    {
        return createSingleProducer(factory, bufferSize, new BlockingWaitStrategy());
    }

    /**
     * 按指定生产者类型（SINGLE 或 MULTI）创建环形缓冲区
     *
     * @param <E> 环形缓冲区中存储的事件类型
     * @param producerType 生产者类型 {@link ProducerType}
     * @param factory      在环形缓冲区中创建事件的工厂
     * @param bufferSize   环形缓冲区元素个数
     * @param waitStrategy 等待新元素可用的策略
     * @return 构造完成的环形缓冲区
     * @throws IllegalArgumentException if bufferSize is less than 1 or not a power of 2
     */
    public static <E> RingBuffer<E> create(
        final ProducerType producerType,
        final EventFactory<E> factory,
        final int bufferSize,
        final WaitStrategy waitStrategy)
    {
        switch (producerType)
        {
            case SINGLE:
                return createSingleProducer(factory, bufferSize, waitStrategy);
            case MULTI:
                return createMultiProducer(factory, bufferSize, waitStrategy);
            default:
                throw new IllegalStateException(producerType.toString());
        }
    }

    /**
     * <p>按序号获取环形缓冲区中的事件。</p>
     *
     * <p>This call has 2 uses.  Firstly use this call when publishing to a ring buffer.
     * After calling {@link RingBuffer#next()} use this call to get hold of the
     * preallocated event to fill with data before calling {@link RingBuffer#publish(long)}.</p>
     *
     * <p>Secondly use this call when consuming data from the ring buffer.  After calling
     * {@link SequenceBarrier#waitFor(long)} call this method with any value greater than
     * that your current consumer sequence and less than or equal to the value returned from
     * the {@link SequenceBarrier#waitFor(long)} method.</p>
     *
     * @param sequence for the event
     * @return the event for the given sequence
     */
    @Override
    public E get(final long sequence)
    {
        return elementAt(sequence);
    }

    /**
     * 申领并返回下一个可写序号；调用方必须在完成后发布该序号，例如：
     * <pre>
     * long sequence = ringBuffer.next();
     * try {
     *     Event e = ringBuffer.get(sequence);
     *     // Do some work with the event.
     * } finally {
     *     ringBuffer.publish(sequence);
     * }
     * </pre>
     *
     * @return 下一个待发布的序号
     * @see RingBuffer#publish(long)
     * @see RingBuffer#get(long)
     */
    @Override
    public long next()
    {
        return sequencer.next();
    }

    /**
     * 与 {@link RingBuffer#next()} 类似，但允许一次申领 n 个连续序号。
     *
     * @param n 要申领的槽位数
     * @return 已申领的最高序号
     * @see Sequencer#next(int)
     */
    @Override
    public long next(final int n)
    {
        return sequencer.next(n);
    }

    /**
     * <p>申领并返回下一个可写序号；调用方必须在完成后发布该序号，例如：</p>
     * <pre>
     * long sequence = ringBuffer.next();
     * try {
     *     Event e = ringBuffer.get(sequence);
     *     // Do some work with the event.
     * } finally {
     *     ringBuffer.publish(sequence);
     * }
     * </pre>
     * <p>若环形缓冲区无可用空间，本方法不阻塞，而是抛出 {@link InsufficientCapacityException}。</p>
     *
     * @return 下一个待发布的序号
     * @throws InsufficientCapacityException if the necessary space in the ring buffer is not available
     * @see RingBuffer#publish(long)
     * @see RingBuffer#get(long)
     */
    @Override
    public long tryNext() throws InsufficientCapacityException
    {
        return sequencer.tryNext();
    }

    /**
     * 与 {@link RingBuffer#tryNext()} 类似，但尝试一次申领 n 个连续序号。
     *
     * @param n 要申领的槽位数
     * @return 已申领的最高序号
     * @throws InsufficientCapacityException if the necessary space in the ring buffer is not available
     */
    @Override
    public long tryNext(final int n) throws InsufficientCapacityException
    {
        return sequencer.tryNext(n);
    }

    /**
     * 将游标设到指定序号并返回该槽位的预分配事件；可能引发数据竞争，仅应在受控场景（如初始化）使用。
     *
     * @param sequence 要申领的序号
     * @return 预分配的事件对象
     */
    public E claimAndGetPreallocated(final long sequence)
    {
        sequencer.claim(sequence);
        return get(sequence);
    }

    /**
     * 判断指定序号的事件当前是否可读。
     *
     * <p>Note that this does not guarantee that event will still be available
     * on the next interaction with the RingBuffer. For example, it is not
     * necessarily safe to write code like this:
     *
     * <pre>{@code
     * if (ringBuffer.isAvailable(sequence))
     * {
     *     final E e = ringBuffer.get(sequence);
     *     // ...do something with e
     * }
     * }</pre>
     *
     * <p>because there is a race between the reading thread and the writing thread.
     *
     * <p>This method will also return false when querying for sequences that are
     * behind the ring buffer's wrap point.
     *
     * @param sequence The sequence to identify the entry.
     * @return If the event published with the given sequence number is currently available.
     */
    public boolean isAvailable(final long sequence)
    {
        return sequencer.isAvailable(sequence);
    }

    /**
     * 将门控序号安全、原子地加入本 Disruptor 实例。
     *
     * @param gatingSequences 要添加的序号
     */
    public void addGatingSequences(final Sequence... gatingSequences)
    {
        sequencer.addGatingSequences(gatingSequences);
    }

    /**
     * 获取已添加到此环形缓冲区的全部门控序号中的最小值。
     *
     * @return The minimum gating sequence or the cursor sequence if
     * no sequences have been added.
     */
    public long getMinimumGatingSequence()
    {
        return sequencer.getMinimumSequence();
    }

    /**
     * 从本环形缓冲区移除指定门控序号。
     *
     * @param sequence 要移除的序号
     * @return <code>true</code> if this sequence was found, <code>false</code> otherwise.
     */
    public boolean removeGatingSequence(final Sequence sequence)
    {
        return sequencer.removeGatingSequence(sequence);
    }

    /**
     * 创建供 {@link EventProcessor} 使用的 {@link SequenceBarrier}，根据依赖序号判断哪些消息已可读。
     *
     * @param sequencesToTrack 需要额外跟踪的序号
     * @return 跟踪指定序号的序列屏障
     * @see SequenceBarrier
     */
    public SequenceBarrier newBarrier(final Sequence... sequencesToTrack)
    {
        return sequencer.newBarrier(sequencesToTrack);
    }

    /**
     * 创建绑定到指定门控序号的事件轮询器。
     *
     * @param gatingSequences 门控序号
     * @return 绑定本环形缓冲区与指定序号的轮询器
     */
    public EventPoller<E> newPoller(final Sequence... gatingSequences)
    {
        return sequencer.newPoller(this, gatingSequences);
    }

    /**
     * 获取环形缓冲区当前游标值；具体语义取决于所用 {@link Sequencer} 类型。
     *
     * @see MultiProducerSequencer
     * @see SingleProducerSequencer
     */
    @Override
    public long getCursor()
    {
        return sequencer.getCursor();
    }

    /**
     * 缓冲区大小。
     *
     * @return 缓冲区槽位数
     */
    @Override
    public int getBufferSize()
    {
        return bufferSize;
    }

    /**
     * 检查是否具有 <code>requiredCapacity</code> 指定的可用空间。返回 <code>true</code> 并不保证随后调用 {@link RingBuffer#next()} 不会阻塞，多生产者场景尤其如此。
     *
     * @param requiredCapacity 需要检查的容量
     * @return <code>true</code> If the specified <code>requiredCapacity</code> is available
     * <code>false</code> if not.
     */
    @Override
    public boolean hasAvailableCapacity(final int requiredCapacity)
    {
        return sequencer.hasAvailableCapacity(requiredCapacity);
    }


    /**
     * @see com.lmax.disruptor.EventSink#publishEvent(com.lmax.disruptor.EventTranslator)
     */
    @Override
    public void publishEvent(final EventTranslator<E> translator)
    {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvent(com.lmax.disruptor.EventTranslator)
     */
    @Override
    public boolean tryPublishEvent(final EventTranslator<E> translator)
    {
        try
        {
            final long sequence = sequencer.tryNext();
            translateAndPublish(translator, sequence);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvent(com.lmax.disruptor.EventTranslatorOneArg, Object)
     * com.lmax.disruptor.EventSink#publishEvent(com.lmax.disruptor.EventTranslatorOneArg, A)
     */
    @Override
    public <A> void publishEvent(final EventTranslatorOneArg<E, A> translator, final A arg0)
    {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence, arg0);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvent(com.lmax.disruptor.EventTranslatorOneArg, Object)
     * com.lmax.disruptor.EventSink#tryPublishEvent(com.lmax.disruptor.EventTranslatorOneArg, A)
     */
    @Override
    public <A> boolean tryPublishEvent(final EventTranslatorOneArg<E, A> translator, final A arg0)
    {
        try
        {
            final long sequence = sequencer.tryNext();
            translateAndPublish(translator, sequence, arg0);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvent(com.lmax.disruptor.EventTranslatorTwoArg, Object, Object)
     * com.lmax.disruptor.EventSink#publishEvent(com.lmax.disruptor.EventTranslatorTwoArg, A, B)
     */
    @Override
    public <A, B> void publishEvent(final EventTranslatorTwoArg<E, A, B> translator, final A arg0, final B arg1)
    {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence, arg0, arg1);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvent(com.lmax.disruptor.EventTranslatorTwoArg, Object, Object)
     * com.lmax.disruptor.EventSink#tryPublishEvent(com.lmax.disruptor.EventTranslatorTwoArg, A, B)
     */
    @Override
    public <A, B> boolean tryPublishEvent(final EventTranslatorTwoArg<E, A, B> translator, final A arg0, final B arg1)
    {
        try
        {
            final long sequence = sequencer.tryNext();
            translateAndPublish(translator, sequence, arg0, arg1);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvent(com.lmax.disruptor.EventTranslatorThreeArg, Object, Object, Object)
     * com.lmax.disruptor.EventSink#publishEvent(com.lmax.disruptor.EventTranslatorThreeArg, A, B, C)
     */
    @Override
    public <A, B, C> void publishEvent(final EventTranslatorThreeArg<E, A, B, C> translator, final A arg0, final B arg1, final C arg2)
    {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence, arg0, arg1, arg2);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvent(com.lmax.disruptor.EventTranslatorThreeArg, Object, Object, Object)
     * com.lmax.disruptor.EventSink#tryPublishEvent(com.lmax.disruptor.EventTranslatorThreeArg, A, B, C)
     */
    @Override
    public <A, B, C> boolean tryPublishEvent(final EventTranslatorThreeArg<E, A, B, C> translator, final A arg0, final B arg1, final C arg2)
    {
        try
        {
            final long sequence = sequencer.tryNext();
            translateAndPublish(translator, sequence, arg0, arg1, arg2);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvent(com.lmax.disruptor.EventTranslatorVararg, java.lang.Object...)
     */
    @Override
    public void publishEvent(final EventTranslatorVararg<E> translator, final Object... args)
    {
        final long sequence = sequencer.next();
        translateAndPublish(translator, sequence, args);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvent(com.lmax.disruptor.EventTranslatorVararg, java.lang.Object...)
     */
    @Override
    public boolean tryPublishEvent(final EventTranslatorVararg<E> translator, final Object... args)
    {
        try
        {
            final long sequence = sequencer.tryNext();
            translateAndPublish(translator, sequence, args);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }


    /**
     * @see com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslator[])
     */
    @Override
    public void publishEvents(final EventTranslator<E>[] translators)
    {
        publishEvents(translators, 0, translators.length);
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslator[], int, int)
     */
    @Override
    public void publishEvents(final EventTranslator<E>[] translators, final int batchStartsAt, final int batchSize)
    {
        checkBounds(translators, batchStartsAt, batchSize);
        final long finalSequence = sequencer.next(batchSize);
        translateAndPublishBatch(translators, batchStartsAt, batchSize, finalSequence);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslator[])
     */
    @Override
    public boolean tryPublishEvents(final EventTranslator<E>[] translators)
    {
        return tryPublishEvents(translators, 0, translators.length);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslator[], int, int)
     */
    @Override
    public boolean tryPublishEvents(final EventTranslator<E>[] translators, final int batchStartsAt, final int batchSize)
    {
        checkBounds(translators, batchStartsAt, batchSize);
        try
        {
            final long finalSequence = sequencer.tryNext(batchSize);
            translateAndPublishBatch(translators, batchStartsAt, batchSize, finalSequence);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorOneArg, Object[])
     * com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorOneArg, A[])
     */
    @Override
    public <A> void publishEvents(final EventTranslatorOneArg<E, A> translator, final A[] arg0)
    {
        publishEvents(translator, 0, arg0.length, arg0);
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorOneArg, int, int, Object[])
     * com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorOneArg, int, int, A[])
     */
    @Override
    public <A> void publishEvents(final EventTranslatorOneArg<E, A> translator, final int batchStartsAt, final int batchSize, final A[] arg0)
    {
        checkBounds(arg0, batchStartsAt, batchSize);
        final long finalSequence = sequencer.next(batchSize);
        translateAndPublishBatch(translator, arg0, batchStartsAt, batchSize, finalSequence);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorOneArg, Object[])
     * com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorOneArg, A[])
     */
    @Override
    public <A> boolean tryPublishEvents(final EventTranslatorOneArg<E, A> translator, final A[] arg0)
    {
        return tryPublishEvents(translator, 0, arg0.length, arg0);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorOneArg, int, int, Object[])
     * com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorOneArg, int, int, A[])
     */
    @Override
    public <A> boolean tryPublishEvents(
            final EventTranslatorOneArg<E, A> translator, final int batchStartsAt, final int batchSize, final A[] arg0)
    {
        checkBounds(arg0, batchStartsAt, batchSize);
        try
        {
            final long finalSequence = sequencer.tryNext(batchSize);
            translateAndPublishBatch(translator, arg0, batchStartsAt, batchSize, finalSequence);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorTwoArg, Object[], Object[])
     * com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorTwoArg, A[], B[])
     */
    @Override
    public <A, B> void publishEvents(final EventTranslatorTwoArg<E, A, B> translator, final A[] arg0, final B[] arg1)
    {
        publishEvents(translator, 0, arg0.length, arg0, arg1);
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorTwoArg, int, int, Object[], Object[])
     * com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorTwoArg, int, int, A[], B[])
     */
    @Override
    public <A, B> void publishEvents(
        final EventTranslatorTwoArg<E, A, B> translator, final int batchStartsAt, final int batchSize, final A[] arg0, final B[] arg1)
    {
        checkBounds(arg0, arg1, batchStartsAt, batchSize);
        final long finalSequence = sequencer.next(batchSize);
        translateAndPublishBatch(translator, arg0, arg1, batchStartsAt, batchSize, finalSequence);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorTwoArg, Object[], Object[])
     * com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorTwoArg, A[], B[])
     */
    @Override
    public <A, B> boolean tryPublishEvents(final EventTranslatorTwoArg<E, A, B> translator, final A[] arg0, final B[] arg1)
    {
        return tryPublishEvents(translator, 0, arg0.length, arg0, arg1);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorTwoArg, int, int, Object[], Object[])
     * com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorTwoArg, int, int, A[], B[])
     */
    @Override
    public <A, B> boolean tryPublishEvents(
        final EventTranslatorTwoArg<E, A, B> translator, final int batchStartsAt, final int batchSize, final A[] arg0, final B[] arg1)
    {
        checkBounds(arg0, arg1, batchStartsAt, batchSize);
        try
        {
            final long finalSequence = sequencer.tryNext(batchSize);
            translateAndPublishBatch(translator, arg0, arg1, batchStartsAt, batchSize, finalSequence);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorThreeArg, Object[], Object[], Object[])
     * com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorThreeArg, A[], B[], C[])
     */
    @Override
    public <A, B, C> void publishEvents(final EventTranslatorThreeArg<E, A, B, C> translator, final A[] arg0, final B[] arg1, final C[] arg2)
    {
        publishEvents(translator, 0, arg0.length, arg0, arg1, arg2);
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorThreeArg, int, int, Object[], Object[], Object[])
     * com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorThreeArg, int, int, A[], B[], C[])
     */
    @Override
    public <A, B, C> void publishEvents(
        final EventTranslatorThreeArg<E, A, B, C> translator, final int batchStartsAt, final int batchSize, final A[] arg0, final B[] arg1, final C[] arg2)
    {
        checkBounds(arg0, arg1, arg2, batchStartsAt, batchSize);
        final long finalSequence = sequencer.next(batchSize);
        translateAndPublishBatch(translator, arg0, arg1, arg2, batchStartsAt, batchSize, finalSequence);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorThreeArg, Object[], Object[], Object[])
     * com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorThreeArg, A[], B[], C[])
     */
    @Override
    public <A, B, C> boolean tryPublishEvents(
        final EventTranslatorThreeArg<E, A, B, C> translator, final A[] arg0, final B[] arg1, final C[] arg2)
    {
        return tryPublishEvents(translator, 0, arg0.length, arg0, arg1, arg2);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorThreeArg, int, int, Object[], Object[], Object[])
     * com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorThreeArg, int, int, A[], B[], C[])
     */
    @Override
    public <A, B, C> boolean tryPublishEvents(
        final EventTranslatorThreeArg<E, A, B, C> translator, final int batchStartsAt, final int batchSize, final A[] arg0, final B[] arg1, final C[] arg2)
    {
        checkBounds(arg0, arg1, arg2, batchStartsAt, batchSize);
        try
        {
            final long finalSequence = sequencer.tryNext(batchSize);
            translateAndPublishBatch(translator, arg0, arg1, arg2, batchStartsAt, batchSize, finalSequence);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorVararg, java.lang.Object[][])
     */
    @Override
    public void publishEvents(final EventTranslatorVararg<E> translator, final Object[]... args)
    {
        publishEvents(translator, 0, args.length, args);
    }

    /**
     * @see com.lmax.disruptor.EventSink#publishEvents(com.lmax.disruptor.EventTranslatorVararg, int, int, java.lang.Object[][])
     */
    @Override
    public void publishEvents(final EventTranslatorVararg<E> translator, final int batchStartsAt, final int batchSize, final Object[]... args)
    {
        checkBounds(batchStartsAt, batchSize, args);
        final long finalSequence = sequencer.next(batchSize);
        translateAndPublishBatch(translator, batchStartsAt, batchSize, finalSequence, args);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorVararg, java.lang.Object[][])
     */
    @Override
    public boolean tryPublishEvents(final EventTranslatorVararg<E> translator, final Object[]... args)
    {
        return tryPublishEvents(translator, 0, args.length, args);
    }

    /**
     * @see com.lmax.disruptor.EventSink#tryPublishEvents(com.lmax.disruptor.EventTranslatorVararg, int, int, java.lang.Object[][])
     */
    @Override
    public boolean tryPublishEvents(
        final EventTranslatorVararg<E> translator, final int batchStartsAt, final int batchSize, final Object[]... args)
    {
        checkBounds(args, batchStartsAt, batchSize);
        try
        {
            final long finalSequence = sequencer.tryNext(batchSize);
            translateAndPublishBatch(translator, batchStartsAt, batchSize, finalSequence, args);
            return true;
        }
        catch (InsufficientCapacityException e)
        {
            return false;
        }
    }

    /**
     * 发布指定序号，将该消息标记为可供消费者读取。
     *
     * @param sequence 要发布的序号
     */
    @Override
    public void publish(final long sequence)
    {
        sequencer.publish(sequence);
    }

    /**
     * 批量发布序号区间，将区间内消息标记为可读。
     *
     * @param lo 要发布的最低序号
     * @param hi 要发布的最高序号
     * @see Sequencer#next(int)
     */
    @Override
    public void publish(final long lo, final long hi)
    {
        sequencer.publish(lo, hi);
    }

    /**
     * 获取环形缓冲区剩余可用容量。
     *
     * @return 剩余槽位数
     */
    @Override
    public long remainingCapacity()
    {
        return sequencer.remainingCapacity();
    }

    private void checkBounds(final EventTranslator<E>[] translators, final int batchStartsAt, final int batchSize)
    {
        checkBatchSizing(batchStartsAt, batchSize);
        batchOverRuns(translators, batchStartsAt, batchSize);
    }

    private void checkBatchSizing(final int batchStartsAt, final int batchSize)
    {
        if (batchStartsAt < 0 || batchSize < 0)
        {
            throw new IllegalArgumentException("Both batchStartsAt and batchSize must be positive but got: batchStartsAt " + batchStartsAt + " and batchSize " + batchSize);
        }
        else if (batchSize > bufferSize)
        {
            throw new IllegalArgumentException("The ring buffer cannot accommodate " + batchSize + " it only has space for " + bufferSize + " entities.");
        }
    }

    private <A> void checkBounds(final A[] arg0, final int batchStartsAt, final int batchSize)
    {
        checkBatchSizing(batchStartsAt, batchSize);
        batchOverRuns(arg0, batchStartsAt, batchSize);
    }

    private <A, B> void checkBounds(final A[] arg0, final B[] arg1, final int batchStartsAt, final int batchSize)
    {
        checkBatchSizing(batchStartsAt, batchSize);
        batchOverRuns(arg0, batchStartsAt, batchSize);
        batchOverRuns(arg1, batchStartsAt, batchSize);
    }

    private <A, B, C> void checkBounds(
        final A[] arg0, final B[] arg1, final C[] arg2, final int batchStartsAt, final int batchSize)
    {
        checkBatchSizing(batchStartsAt, batchSize);
        batchOverRuns(arg0, batchStartsAt, batchSize);
        batchOverRuns(arg1, batchStartsAt, batchSize);
        batchOverRuns(arg2, batchStartsAt, batchSize);
    }

    private void checkBounds(final int batchStartsAt, final int batchSize, final Object[][] args)
    {
        checkBatchSizing(batchStartsAt, batchSize);
        batchOverRuns(args, batchStartsAt, batchSize);
    }

    private <A> void batchOverRuns(final A[] arg0, final int batchStartsAt, final int batchSize)
    {
        if (batchStartsAt + batchSize > arg0.length)
        {
            throw new IllegalArgumentException(
                "A batchSize of: " + batchSize +
                    " with batchStatsAt of: " + batchStartsAt +
                    " will overrun the available number of arguments: " + (arg0.length - batchStartsAt));
        }
    }

    private void translateAndPublish(final EventTranslator<E> translator, final long sequence)
    {
        try
        {
            // 步骤 1：将数据写入已申领槽位
            translator.translateTo(get(sequence), sequence);
        }
        finally
        {
            // 步骤 2：发布序号，使消费者可见
            sequencer.publish(sequence);
        }
    }

    private <A> void translateAndPublish(final EventTranslatorOneArg<E, A> translator, final long sequence, final A arg0)
    {
        try
        {
            translator.translateTo(get(sequence), sequence, arg0);
        }
        finally
        {
            sequencer.publish(sequence);
        }
    }

    private <A, B> void translateAndPublish(final EventTranslatorTwoArg<E, A, B> translator, final long sequence, final A arg0, final B arg1)
    {
        try
        {
            translator.translateTo(get(sequence), sequence, arg0, arg1);
        }
        finally
        {
            sequencer.publish(sequence);
        }
    }

    private <A, B, C> void translateAndPublish(
        final EventTranslatorThreeArg<E, A, B, C> translator, final long sequence,
        final A arg0, final B arg1, final C arg2)
    {
        try
        {
            translator.translateTo(get(sequence), sequence, arg0, arg1, arg2);
        }
        finally
        {
            sequencer.publish(sequence);
        }
    }

    private void translateAndPublish(final EventTranslatorVararg<E> translator, final long sequence, final Object... args)
    {
        try
        {
            translator.translateTo(get(sequence), sequence, args);
        }
        finally
        {
            sequencer.publish(sequence);
        }
    }

    private void translateAndPublishBatch(
        final EventTranslator<E>[] translators, final int batchStartsAt,
        final int batchSize, final long finalSequence)
    {
        final long initialSequence = finalSequence - (batchSize - 1);
        try
        {
            // 步骤 1：从批次起始序号起，逐个转换并写入槽位
            long sequence = initialSequence;
            final int batchEndsAt = batchStartsAt + batchSize;
            for (int i = batchStartsAt; i < batchEndsAt; i++)
            {
                final EventTranslator<E> translator = translators[i];
                translator.translateTo(get(sequence), sequence++);
            }
        }
        finally
        {
            // 步骤 2：批量发布整个序号区间
            sequencer.publish(initialSequence, finalSequence);
        }
    }

    private <A> void translateAndPublishBatch(
        final EventTranslatorOneArg<E, A> translator, final A[] arg0,
        final int batchStartsAt, final int batchSize, final long finalSequence)
    {
        final long initialSequence = finalSequence - (batchSize - 1);
        try
        {
            long sequence = initialSequence;
            final int batchEndsAt = batchStartsAt + batchSize;
            for (int i = batchStartsAt; i < batchEndsAt; i++)
            {
                translator.translateTo(get(sequence), sequence++, arg0[i]);
            }
        }
        finally
        {
            sequencer.publish(initialSequence, finalSequence);
        }
    }

    private <A, B> void translateAndPublishBatch(
        final EventTranslatorTwoArg<E, A, B> translator, final A[] arg0,
        final B[] arg1, final int batchStartsAt, final int batchSize,
        final long finalSequence)
    {
        final long initialSequence = finalSequence - (batchSize - 1);
        try
        {
            long sequence = initialSequence;
            final int batchEndsAt = batchStartsAt + batchSize;
            for (int i = batchStartsAt; i < batchEndsAt; i++)
            {
                translator.translateTo(get(sequence), sequence++, arg0[i], arg1[i]);
            }
        }
        finally
        {
            sequencer.publish(initialSequence, finalSequence);
        }
    }

    private <A, B, C> void translateAndPublishBatch(
        final EventTranslatorThreeArg<E, A, B, C> translator,
        final A[] arg0, final B[] arg1, final C[] arg2, final int batchStartsAt,
        final int batchSize, final long finalSequence)
    {
        final long initialSequence = finalSequence - (batchSize - 1);
        try
        {
            long sequence = initialSequence;
            final int batchEndsAt = batchStartsAt + batchSize;
            for (int i = batchStartsAt; i < batchEndsAt; i++)
            {
                translator.translateTo(get(sequence), sequence++, arg0[i], arg1[i], arg2[i]);
            }
        }
        finally
        {
            sequencer.publish(initialSequence, finalSequence);
        }
    }

    private void translateAndPublishBatch(
        final EventTranslatorVararg<E> translator, final int batchStartsAt,
        final int batchSize, final long finalSequence, final Object[][] args)
    {
        final long initialSequence = finalSequence - (batchSize - 1);
        try
        {
            long sequence = initialSequence;
            final int batchEndsAt = batchStartsAt + batchSize;
            for (int i = batchStartsAt; i < batchEndsAt; i++)
            {
                translator.translateTo(get(sequence), sequence++, args[i]);
            }
        }
        finally
        {
            sequencer.publish(initialSequence, finalSequence);
        }
    }

    @Override
    public String toString()
    {
        return "RingBuffer{" +
            "bufferSize=" + bufferSize +
            ", sequencer=" + sequencer +
            "}";
    }
}
