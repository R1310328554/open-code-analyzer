/*
 * Copyright 2023 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lmax.disruptor;

/**
 * {@link BatchEventProcessor} 的构建器，支持配置批次大小等参数。
 */
public final class BatchEventProcessorBuilder
{
    private int maxBatchSize = Integer.MAX_VALUE;

    /**
     * 设置单次批次在更新序号前最多处理的事件数。
     *
     * @param maxBatchSize 单批最大事件数
     * @return 本构建器
     */
    public BatchEventProcessorBuilder setMaxBatchSize(final int maxBatchSize)
    {
        this.maxBatchSize = maxBatchSize;
        return this;
    }

    /**
     * 构建 {@link EventProcessor}：在 {@link EventHandler#onEvent(Object, long, boolean)} 返回时自动推进序号。
     *
     * <p>所创建的 {@link BatchEventProcessor} 不支持批次回退，
     * 但支持 {@link EventHandler#setSequenceCallback(Sequence)}。
     *
     * @param dataProvider    事件数据来源
     * @param sequenceBarrier 处理器等待的屏障
     * @param eventHandler    事件委托处理器
     * @param <T>             事件实现类型
     * @return 配置完成的 {@link BatchEventProcessor}
     */
    public <T> BatchEventProcessor<T> build(
            final DataProvider<T> dataProvider,
            final SequenceBarrier sequenceBarrier,
            final EventHandler<? super T> eventHandler)
    {
        final BatchEventProcessor<T> processor = new BatchEventProcessor<>(
                dataProvider, sequenceBarrier, eventHandler, maxBatchSize, null
        );
        eventHandler.setSequenceCallback(processor.getSequence());

        return processor;
    }

    /**
     * 构建支持批次回退的 {@link EventProcessor}：
     * 在 {@link EventHandler#onEvent(Object, long, boolean)} 返回时自动推进序号。
     *
     * @param dataProvider           事件数据来源
     * @param sequenceBarrier        处理器等待的屏障
     * @param rewindableEventHandler 支持回退的事件处理器
     * @param batchRewindStrategy    处理 {@link RewindableException} 的策略
     * @param <T>                    事件实现类型
     * @return 配置完成的 {@link BatchEventProcessor}
     */
    public <T> BatchEventProcessor<T> build(
            final DataProvider<T> dataProvider,
            final SequenceBarrier sequenceBarrier,
            final RewindableEventHandler<? super T> rewindableEventHandler,
            final BatchRewindStrategy batchRewindStrategy)
    {
        if (null == batchRewindStrategy)
        {
            throw new NullPointerException("batchRewindStrategy cannot be null when building a BatchEventProcessor");
        }

        return new BatchEventProcessor<>(
                dataProvider, sequenceBarrier, rewindableEventHandler, maxBatchSize, batchRewindStrategy
        );
    }
}
