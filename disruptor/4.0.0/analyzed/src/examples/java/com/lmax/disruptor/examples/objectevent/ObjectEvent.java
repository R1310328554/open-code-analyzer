/**
 * 通用对象事件载体。
 */

package com.lmax.disruptor.examples.objectevent;

// tag::example[]
class ObjectEvent<T>
{
    T val;

    void clear()
    {
        val = null;
    }
}
// end::example[]