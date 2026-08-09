/**
 * LongEvent 的 EventFactory 实现。
 */

package com.lmax.disruptor.examples.longevent;

import com.lmax.disruptor.EventFactory;

// tag::example[]
public class LongEventFactory implements EventFactory<LongEvent>
{
    /** 预分配 RingBuffer 槽位时创建新的 LongEvent。 */
    @Override
    public LongEvent newInstance()
    {
        return new LongEvent();
    }
}
// end::example[]