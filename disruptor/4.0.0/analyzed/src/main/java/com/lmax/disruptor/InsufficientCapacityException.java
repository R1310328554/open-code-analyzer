/*
 * Copyright 2012 LMAX Ltd.
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
package com.lmax.disruptor;

/**
 * 在插入值会导致环形缓冲区环绕消费序号时抛出。
 * 专用于 {@link RingBuffer#tryNext()} 申领序号时的容量不足场景。
 *
 * <p>为提升效率，本异常不填充堆栈跟踪。
 */
@SuppressWarnings({"serial", "lgtm[java/non-sync-override]"})
public final class InsufficientCapacityException extends Exception
{
    /**
     * 为效率考虑提供的单例实例
     */
    public static final InsufficientCapacityException INSTANCE = new InsufficientCapacityException();

    private InsufficientCapacityException()
    {
        // Singleton
    }

    @Override
    public Throwable fillInStackTrace()
    {
        return this;
    }
}
