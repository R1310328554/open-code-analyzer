/**
 * 对象事件处理占位处理器。
 */

package com.lmax.disruptor.examples.objectevent;

import com.lmax.disruptor.EventHandler;

public class ProcessingEventHandler<T> implements EventHandler<ObjectEvent<T>>
{
    @Override
    public void onEvent(ObjectEvent<T> event, long sequence, boolean endOfBatch) throws Exception
    {
    }
}
