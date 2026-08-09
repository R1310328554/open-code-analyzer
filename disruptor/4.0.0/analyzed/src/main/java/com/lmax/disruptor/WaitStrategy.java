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
 * 供 {@link EventProcessor} 在游标 {@link Sequence} 上等待时采用的策略。
 */
public interface WaitStrategy
{
    /**
     * 等待指定序号可用。具体实现可能返回小于请求序号的值，例如用于表示超时。
     * 使用 {@link WaitStrategy} 获取新事件通知的 {@link EventProcessor} 必须处理这种情况；
     * {@link BatchEventProcessor} 已显式处理并在需要时触发超时回调。
     *
     * @param sequence          要等待的序号
     * @param cursor            环形缓冲区主游标；阻塞/通知类策略需要它，因为它是唯一在更新时会被唤醒的序号
     * @param dependentSequence 依赖的序号，实际等待其追上目标
     * @param barrier           处理器正在等待的屏障
     * @return 当前可用的序号，可能大于请求的序号
     * @throws AlertException       Disruptor 状态发生变化
     * @throws InterruptedException 线程被中断
     * @throws TimeoutException     等待超时（部分策略不使用）
     */
    long waitFor(long sequence, Sequence cursor, Sequence dependentSequence, SequenceBarrier barrier)
        throws AlertException, InterruptedException, TimeoutException;

    /**
     * 实现类应在游标推进时唤醒正在阻塞等待的 {@link EventProcessor}。
     */
    void signalAllWhenBlocking();
}
