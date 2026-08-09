/*
 * Copyright 2022 LMAX Ltd.
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

import java.util.concurrent.atomic.AtomicInteger;

import static com.lmax.disruptor.RewindAction.REWIND;
import static java.lang.Math.min;


/**
 * 封装从 {@link RingBuffer} 批量消费事件并委托给 {@link EventHandler} 的语义。
 *
 * @param <T> 事件实现类型，在交换或并行协调过程中承载共享数据
 */
public final class BatchEventProcessor<T>
        implements EventProcessor
{
    private static final int IDLE = 0;
    private static final int HALTED = IDLE + 1;
    private static final int RUNNING = HALTED + 1;

    private final AtomicInteger running = new AtomicInteger(IDLE);
    private ExceptionHandler<? super T> exceptionHandler;
    private final DataProvider<T> dataProvider;
    private final SequenceBarrier sequenceBarrier;
    private final EventHandlerBase<? super T> eventHandler;
    private final int batchLimitOffset;
    private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    private final RewindHandler rewindHandler;
    private int retriesAttempted = 0;

    BatchEventProcessor(
            final DataProvider<T> dataProvider,
            final SequenceBarrier sequenceBarrier,
            final EventHandlerBase<? super T> eventHandler,
            final int maxBatchSize,
            final BatchRewindStrategy batchRewindStrategy
    )
    {
        this.dataProvider = dataProvider;
        this.sequenceBarrier = sequenceBarrier;
        this.eventHandler = eventHandler;

        if (maxBatchSize < 1)
        {
            throw new IllegalArgumentException("maxBatchSize must be greater than 0");
        }
        this.batchLimitOffset = maxBatchSize - 1;

        this.rewindHandler = eventHandler instanceof RewindableEventHandler
                ? new TryRewindHandler(batchRewindStrategy)
                : new NoRewindHandler();
    }

    @Override
    public Sequence getSequence()
    {
        return sequence;
    }

    @Override
    public void halt()
    {
        running.set(HALTED);
        sequenceBarrier.alert();
    }

    @Override
    public boolean isRunning()
    {
        return running.get() != IDLE;
    }

    /**
     * 设置新的 {@link ExceptionHandler}，用于处理从本处理器传播出的异常。
     *
     * @param exceptionHandler 替换现有异常处理器
     */
    public void setExceptionHandler(final ExceptionHandler<? super T> exceptionHandler)
    {
        if (null == exceptionHandler)
        {
            throw new NullPointerException();
        }

        this.exceptionHandler = exceptionHandler;
    }

    /**
     * 可在 {@link #halt()} 之后由另一线程再次调用以重新运行。
     *
     * @throws IllegalStateException 若本实例已在其他线程中运行
     */
    @Override
    public void run()
    {
        int witnessValue = running.compareAndExchange(IDLE, RUNNING);
        if (witnessValue == IDLE) // CAS 成功，获得运行权
        {
            sequenceBarrier.clearAlert();

            notifyStart();
            try
            {
                if (running.get() == RUNNING)
                {
                    processEvents();
                }
            }
            finally
            {
                notifyShutdown();
                running.set(IDLE);
            }
        }
        else
        {
            if (witnessValue == RUNNING)
            {
                throw new IllegalStateException("Thread is already running");
            }
            else
            {
                earlyExit();
            }
        }
    }

    private void processEvents()
    {
        T event = null;
        long nextSequence = sequence.get() + 1L;

        while (true)
        {
            final long startOfBatchSequence = nextSequence;
            try
            {
                try
                {
                    // 步骤 1：在屏障上等待，直到 nextSequence 可用
                    final long availableSequence = sequenceBarrier.waitFor(nextSequence);
                    // 步骤 2：计算本批上界（受 maxBatchSize 与可用序号约束）
                    final long endOfBatchSequence = min(nextSequence + batchLimitOffset, availableSequence);

                    if (nextSequence <= endOfBatchSequence)
                    {
                        eventHandler.onBatchStart(endOfBatchSequence - nextSequence + 1, availableSequence - nextSequence + 1);
                    }

                    // 步骤 3：逐事件回调处理器
                    while (nextSequence <= endOfBatchSequence)
                    {
                        event = dataProvider.get(nextSequence);
                        eventHandler.onEvent(event, nextSequence, nextSequence == endOfBatchSequence);
                        nextSequence++;
                    }

                    retriesAttempted = 0;

                    // 步骤 4：批次成功后推进消费序号
                    sequence.set(endOfBatchSequence);
                }
                catch (final RewindableException e)
                {
                    nextSequence = rewindHandler.attemptRewindGetNextSequence(e, startOfBatchSequence);
                }
            }
            catch (final TimeoutException e)
            {
                notifyTimeout(sequence.get());
            }
            catch (final AlertException ex)
            {
                if (running.get() != RUNNING)
                {
                    break;
                }
            }
            catch (final Throwable ex)
            {
                handleEventException(ex, nextSequence, event);
                sequence.set(nextSequence);
                nextSequence++;
            }
        }
    }

    private void earlyExit()
    {
        notifyStart();
        notifyShutdown();
    }

    private void notifyTimeout(final long availableSequence)
    {
        try
        {
            eventHandler.onTimeout(availableSequence);
        }
        catch (Throwable e)
        {
            handleEventException(e, availableSequence, null);
        }
    }

    /**
     * 处理器启动时通知 {@link EventHandler}。
     */
    private void notifyStart()
    {
        try
        {
            eventHandler.onStart();
        }
        catch (final Throwable ex)
        {
            handleOnStartException(ex);
        }
    }

    /**
     * 处理器关闭前通知 {@link EventHandler}。
     */
    private void notifyShutdown()
    {
        try
        {
            eventHandler.onShutdown();
        }
        catch (final Throwable ex)
        {
            handleOnShutdownException(ex);
        }
    }

    /**
     * 委托给已配置的 {@link ExceptionHandler#handleEventException(Throwable, long, Object)}，
     * 若未配置则使用默认 {@link ExceptionHandler}。
     */
    private void handleEventException(final Throwable ex, final long sequence, final T event)
    {
        getExceptionHandler().handleEventException(ex, sequence, event);
    }

    /**
     * 委托给 {@link ExceptionHandler#handleOnStartException(Throwable)}。
     */
    private void handleOnStartException(final Throwable ex)
    {
        getExceptionHandler().handleOnStartException(ex);
    }

    /**
     * 委托给 {@link ExceptionHandler#handleOnShutdownException(Throwable)}。
     */
    private void handleOnShutdownException(final Throwable ex)
    {
        getExceptionHandler().handleOnShutdownException(ex);
    }

    private ExceptionHandler<? super T> getExceptionHandler()
    {
        ExceptionHandler<? super T> handler = exceptionHandler;
        return handler == null ? ExceptionHandlers.defaultHandler() : handler;
    }

    private class TryRewindHandler implements RewindHandler
    {
        private final BatchRewindStrategy batchRewindStrategy;

        TryRewindHandler(final BatchRewindStrategy batchRewindStrategy)
        {
            this.batchRewindStrategy = batchRewindStrategy;
        }

        @Override
        public long attemptRewindGetNextSequence(final RewindableException e, final long startOfBatchSequence) throws RewindableException
        {
            if (batchRewindStrategy.handleRewindException(e, ++retriesAttempted) == REWIND)
            {
                return startOfBatchSequence;
            }
            else
            {
                retriesAttempted = 0;
                throw e;
            }
        }
    }

    private static class NoRewindHandler implements RewindHandler
    {
        @Override
        public long attemptRewindGetNextSequence(final RewindableException e, final long startOfBatchSequence)
        {
            throw new UnsupportedOperationException("Rewindable Exception thrown from a non-rewindable event handler", e);
        }
    }
}
