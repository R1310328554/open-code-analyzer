/**
 * 长整型事件载体示例。
 */

package com.lmax.disruptor.examples.longevent;

// tag::example[]
public class LongEvent
{
    private long value;

    /** 设置事件值。 */
    public void set(long value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "LongEvent{" + "value=" + value + '}';
    }
}
// end::example[]