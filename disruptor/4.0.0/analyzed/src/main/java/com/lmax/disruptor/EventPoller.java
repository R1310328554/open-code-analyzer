package com.lmax.disruptor;

/**
 * Disruptor 的实验性轮询式接口。与 {@link BatchEventProcessor} 不同，
 * 事件轮询器允许用户自行控制执行流程，适合与生命周期不由 Disruptor DSL 管理的现有线程集成。
 *
 * @param <T> 事件类型
 */
public class EventPoller<T>
{
    private final DataProvider<T> dataProvider;
    private final Sequencer sequencer;
    private final Sequence sequence;
    private final Sequence gatingSequence;

    /**
     * 处理事件的回调接口。
     *
     * @param <T> 事件类型
     */
    public interface Handler<T>
    {
        /**
         * 每消费一个事件时调用。
         *
         * @param event 事件对象
         * @param sequence 事件序号
         * @param endOfBatch 是否为当前批次的最后一个事件
         * @return 是否继续消费事件；若为 {@code false}，轮询器将不再投递事件，
         *         直至再次调用 {@link EventPoller#poll(Handler)}
         * @throws Exception 处理器抛出的异常将传播给 {@code poll} 的调用方
         */
        boolean onEvent(T event, long sequence, boolean endOfBatch) throws Exception;
    }

    /**
     * 表示 {@link #poll(Handler)} 的调用结果。
     */
    public enum PollState
    {
        /**
         * 轮询器已处理一个或多个事件
         */
        PROCESSING,
        /**
         * 轮询器正在等待门控序号推进，事件尚不可用
         */
        GATING,
        /**
         * 无需处理任何事件
         */
        IDLE
    }

    /**
     * 创建事件轮询器。多数用户应使用 {@link RingBuffer#newPoller(Sequence...)}，
     * 由框架自动完成配置。
     *
     * @param dataProvider 事件数据来源
     * @param sequencer 负责事件排序的主序号器
     * @param sequence 本轮询器使用的消费序号
     * @param gatingSequence 门控序号
     */
    public EventPoller(
        final DataProvider<T> dataProvider,
        final Sequencer sequencer,
        final Sequence sequence,
        final Sequence gatingSequence)
    {
        this.dataProvider = dataProvider;
        this.sequencer = sequencer;
        this.sequence = sequence;
        this.gatingSequence = gatingSequence;
    }

    /**
     * 使用给定处理器轮询事件。<br>
     * <br>
     * 轮询器会持续向处理器投递事件，直至已知可用事件全部消费完毕，
     * 或 {@link Handler#onEvent(Object, long, boolean)} 返回 false。<br>
     * <br>
     * 注意：处理当前事件期间可能有新事件变为可用，再次调用本方法即可处理。
     *
     * @param eventHandler 消费事件的处理器
     * @return 本次轮询尝试后轮询器的状态
     * @throws Exception 事件处理器抛出的异常将传播给调用方
     */
    public PollState poll(final Handler<T> eventHandler) throws Exception
    {
        final long currentSequence = sequence.get();
        long nextSequence = currentSequence + 1;
        final long availableSequence = sequencer.getHighestPublishedSequence(nextSequence, gatingSequence.get());

        if (nextSequence <= availableSequence)
        {
            boolean processNextEvent;
            long processedSequence = currentSequence;

            try
            {
                do
                {
                    final T event = dataProvider.get(nextSequence);
                    processNextEvent = eventHandler.onEvent(event, nextSequence, nextSequence == availableSequence);
                    processedSequence = nextSequence;
                    nextSequence++;

                }
                while (nextSequence <= availableSequence && processNextEvent);
            }
            finally
            {
                sequence.set(processedSequence);
            }

            return PollState.PROCESSING;
        }
        else if (sequencer.getCursor() >= nextSequence)
        {
            return PollState.GATING;
        }
        else
        {
            return PollState.IDLE;
        }
    }

    /**
     * 创建事件轮询器。多数用户应使用 {@link RingBuffer#newPoller(Sequence...)}，
     * 由框架自动完成配置。
     *
     * @param dataProvider 事件数据来源
     * @param sequencer 负责事件排序的主序号器
     * @param sequence 本轮询器使用的消费序号
     * @param cursorSequence 游标序号，通常为环形缓冲区的游标
     * @param gatingSequences 额外需要门控的序号
     * @param <T> 事件类型
     * @return 构造完成的事件轮询器
     */
    public static <T> EventPoller<T> newInstance(
        final DataProvider<T> dataProvider,
        final Sequencer sequencer,
        final Sequence sequence,
        final Sequence cursorSequence,
        final Sequence... gatingSequences)
    {
        Sequence gatingSequence;
        if (gatingSequences.length == 0)
        {
            gatingSequence = cursorSequence;
        }
        else if (gatingSequences.length == 1)
        {
            gatingSequence = gatingSequences[0];
        }
        else
        {
            gatingSequence = new FixedSequenceGroup(gatingSequences);
        }

        return new EventPoller<>(dataProvider, sequencer, sequence, gatingSequence);
    }

    /**
     * 获取本事件轮询器使用的 {@link Sequence}。
     *
     * @return 轮询器使用的消费序号
     */
    public Sequence getSequence()
    {
        return sequence;
    }
}
