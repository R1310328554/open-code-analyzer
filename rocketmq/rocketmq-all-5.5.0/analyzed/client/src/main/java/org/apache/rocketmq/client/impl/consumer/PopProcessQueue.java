/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.client.impl.consumer;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.rocketmq.remoting.protocol.body.PopProcessQueueInfo;

/**
 * POP 消费队列快照：跟踪待 Ack 消息数、最近 pop 时间与丢弃状态。
 */
public class PopProcessQueue {

    /** POP 空闲超时阈值（毫秒），超时视为 pull 过期。 */
    private final static long PULL_MAX_IDLE_TIME = Long.parseLong(System.getProperty("rocketmq.client.pull.pullMaxIdleTime", "120000"));

    /** 最近一次 pop 时间戳。 */
    private long lastPopTimestamp = System.currentTimeMillis();
    /** 待 Ack 消息计数。 */
    private AtomicInteger waitAckCounter = new AtomicInteger(0);
    /** 队列是否已丢弃（rebalance 移除等）。 */
    private volatile boolean dropped = false;

    /** 返回最近 pop 时间戳。 */
    public long getLastPopTimestamp() {
        return lastPopTimestamp;
    }

    /** 设置最近 pop 时间戳。 */
    public void setLastPopTimestamp(long lastPopTimestamp) {
        this.lastPopTimestamp = lastPopTimestamp;
    }

    /** 增加待 Ack 计数（pop 到新消息时）。 */
    public void incFoundMsg(int count) {
        this.waitAckCounter.getAndAdd(count);
    }

    /**
     * 消息 Ack 后递减计数。
     *
     * @return 递减前的计数值
     */
    public int ack() {
        return this.waitAckCounter.getAndDecrement();
    }

    /** 减少待 Ack 计数。 */
    public void decFoundMsg(int count) {
        this.waitAckCounter.addAndGet(count);
    }

    /** 返回当前待 Ack 消息数。 */
    public int getWaiAckMsgCount() {
        return this.waitAckCounter.get();
    }

    /** 队列是否已丢弃。 */
    public boolean isDropped() {
        return dropped;
    }

    /** 设置丢弃标志。 */
    public void setDropped(boolean dropped) {
        this.dropped = dropped;
    }

    /** 填充运行时监控信息结构体。 */
    public void fillPopProcessQueueInfo(final PopProcessQueueInfo info) {
        info.setWaitAckCount(getWaiAckMsgCount());
        info.setDroped(isDropped());
        info.setLastPopTimestamp(getLastPopTimestamp());
    }

    /** 判断是否超过 POP 空闲超时未 pop。 */
    public boolean isPullExpired() {
        return (System.currentTimeMillis() - this.lastPopTimestamp) > PULL_MAX_IDLE_TIME;
    }

    @Override
    public String toString() {
        return "PopProcessQueue[waitAckCounter:" + this.waitAckCounter.get()
                + ", lastPopTimestamp:" + getLastPopTimestamp()
                + ", drop:" + dropped +  "]";
    }
}
