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
package com.lmax.disruptor;

/**
 * 用于通知在 {@link SequenceBarrier} 上等待的 {@link EventProcessor}：Disruptor 状态已发生变化。
 *
 * <p>出于性能考虑，本异常不填充堆栈跟踪。
 */
@SuppressWarnings({"serial", "lgtm[java/non-sync-override]"})
public final class AlertException extends Exception
{
    /**
     * 预分配的单例异常，避免每次告警产生垃圾对象。
     */
    public static final AlertException INSTANCE = new AlertException();

    /**
     * 私有构造器，保证全局仅存在一个实例。
     */
    private AlertException()
    {
    }

    /**
     * 覆写以跳过堆栈填充，降低热路径开销。
     *
     * @return 本实例。
     */
    @Override
    public Throwable fillInStackTrace()
    {
        return this;
    }
}
