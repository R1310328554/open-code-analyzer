/**
 * 按 key 分组批处理示例：key 变化或达到批大小时刷新批次。
 */

package com.lmax.disruptor.examples;

import com.lmax.disruptor.EventHandler;

import java.util.ArrayList;
import java.util.List;

public class KeyedBatching implements EventHandler<KeyedBatching.KeyedEvent>
{
    private static final int MAX_BATCH_SIZE = 100;
    private final List<Object> batch = new ArrayList<>();
    private long key = 0;

    @Override
    public void onEvent(final KeyedEvent event, final long sequence, final boolean endOfBatch)
    {
        // 步骤：key 变化时先处理已有批次
        if (!batch.isEmpty() && event.key != key)
        {
            processBatch(batch);
        }

        batch.add(event.data);
        key = event.key;

        // 步骤：批末或达到上限时刷新
        if (endOfBatch || batch.size() >= MAX_BATCH_SIZE)
        {
            processBatch(batch);
        }
    }

    private void processBatch(final List<Object> batch)
    {
        // 处理当前批次
        batch.clear();
    }

    /** 带 key 与 data 的分组事件。 */
    public static class KeyedEvent
    {
        long key;
        Object data;
    }
}
