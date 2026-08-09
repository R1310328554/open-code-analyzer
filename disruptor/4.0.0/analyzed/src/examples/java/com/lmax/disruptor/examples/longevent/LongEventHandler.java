/**
 * 长整型事件处理器示例。
 */

package com.lmax.disruptor.examples.longevent;

import com.lmax.disruptor.EventHandler;

// tag::example[]
public class LongEventHandler implements EventHandler<LongEvent>
{
    @Override
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch)
    {
        System.out.println("Event: " + event);
    }
}
// end::example[]