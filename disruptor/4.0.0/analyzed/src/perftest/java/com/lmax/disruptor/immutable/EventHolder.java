/**
 * 环形缓冲槽位：持有待处理的 {@link SimpleEvent} 引用。
 */

package com.lmax.disruptor.immutable;

import com.lmax.disruptor.EventFactory;

public class EventHolder
{

    public static final EventFactory<EventHolder> FACTORY = EventHolder::new;

    public SimpleEvent event;
}
