/**
 * 提前释放序号示例：在逻辑工作块完成时手动推进 sequenceCallback。
 */

package com.lmax.disruptor.examples;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.examples.support.LongEvent;

@SuppressWarnings("unused")
// tag::example[]
public class EarlyReleaseHandler implements EventHandler<LongEvent>
{
    private Sequence sequenceCallback;
    private int batchRemaining = 20;

    @Override
    public void setSequenceCallback(final Sequence sequenceCallback)
    {
        this.sequenceCallback = sequenceCallback;
    }

    @Override
    public void onEvent(final LongEvent event, final long sequence, final boolean endOfBatch)
    {
        processEvent(event);

        boolean logicalChunkOfWorkComplete = isLogicalChunkOfWorkComplete();
        if (logicalChunkOfWorkComplete)
        {
            // 步骤：逻辑块完成时提前释放已处理序号
            sequenceCallback.set(sequence);
        }

        batchRemaining = logicalChunkOfWorkComplete || endOfBatch ? 20 : batchRemaining;
    }

    private boolean isLogicalChunkOfWorkComplete()
    {
        // 按更小工作块的完成条件返回 true/false，例如刷盘、DB 提交或自定义批大小。

        return --batchRemaining == -1;
    }

    private void processEvent(final LongEvent event)
    {
        // 处理事件
    }
}
// end::example[]