/**
 * 按序列号从缓冲槽取出事件的访问器。
 */

package com.lmax.disruptor.immutable;

public interface EventAccessor<T>
{
    T take(long sequence);
}
