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
 * 协调屏障：跟踪发布者游标，以及依赖本屏障的 {@link EventProcessor} 的消费序号。
 */
public interface SequenceBarrier
{
    /**
     * 等待指定序号可供消费。
     *
     * @param sequence 要等待的序号
     * @return 当前可读的最高序号
     * @throws AlertException Disruptor 状态已变更时抛出
     * @throws InterruptedException 线程在条件变量上被唤醒时抛出
     * @throws TimeoutException 等待超时
     */
    long waitFor(long sequence) throws AlertException, InterruptedException, TimeoutException;

    /**
     * 获取当前可读的游标值。
     *
     * @return 已发布条目的游标值
     */
    long getCursor();

    /**
     * 屏障当前的告警状态。
     *
     * @return 若处于告警状态则为 {@code true}，否则为 {@code false}
     */
    boolean isAlerted();

    /**
     * 向 {@link EventProcessor} 发出状态变更告警，并保持该状态直至清除。
     */
    void alert();

    /**
     * 清除当前告警状态。
     */
    void clearAlert();

    /**
     * 检查是否已触发告警，若已触发则抛出 {@link AlertException}。
     *
     * @throws AlertException 已触发告警时抛出
     */
    void checkAlert() throws AlertException;
}
