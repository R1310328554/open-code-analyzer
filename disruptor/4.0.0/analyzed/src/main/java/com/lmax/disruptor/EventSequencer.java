package com.lmax.disruptor;

/**
 * 将 {@link RingBuffer} 的底层数据访问与序号编排能力聚合为统一接口。
 *
 * @param <T> 事件类型
 */
public interface EventSequencer<T> extends DataProvider<T>, Sequenced
{

}
