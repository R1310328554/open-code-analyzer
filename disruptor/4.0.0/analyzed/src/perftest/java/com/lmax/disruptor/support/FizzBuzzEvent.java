/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lmax.disruptor.support;

import com.lmax.disruptor.EventFactory;

/**
 * FizzBuzz 性能测试事件：携带数值及 fizz/buzz 标记。
 */
public final class FizzBuzzEvent
{
    private boolean fizz = false;
    private boolean buzz = false;
    private long value = 0;

    public long getValue()
    {
        return value;
    }

    /** 设置新数值并重置 fizz/buzz 标记。 */
    public void setValue(final long value)
    {
        fizz = false;
        buzz = false;
        this.value = value;
    }

    public boolean isFizz()
    {
        return fizz;
    }

    public void setFizz(final boolean fizz)
    {
        this.fizz = fizz;
    }

    public boolean isBuzz()
    {
        return buzz;
    }

    public void setBuzz(final boolean buzz)
    {
        this.buzz = buzz;
    }

    /** RingBuffer 预分配事件工厂。 */
    public static final EventFactory<FizzBuzzEvent> EVENT_FACTORY = () -> new FizzBuzzEvent();
}
