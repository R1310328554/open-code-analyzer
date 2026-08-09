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
package org.apache.rocketmq.remoting.protocol.body;

/**
 * Pop 消费模式下 ProcessQueue 运行态：待 ACK 数、是否丢弃及最近 Pop 时间。
 */
public class PopProcessQueueInfo {
    /** 等待 ACK 的消息条数。 */
    private int waitAckCount;
    /** 队列是否已被丢弃（如 rebalance 回收）。 */
    private boolean droped;
    /** 最近一次 Pop 拉取时间戳。 */
    private long lastPopTimestamp;


    /** 返回待 ACK 条数。 */
    public int getWaitAckCount() {
        return waitAckCount;
    }


    public void setWaitAckCount(int waitAckCount) {
        this.waitAckCount = waitAckCount;
    }


    /** 返回队列是否已丢弃。 */
    public boolean isDroped() {
        return droped;
    }


    public void setDroped(boolean droped) {
        this.droped = droped;
    }


    /** 返回最近 Pop 时间戳。 */
    public long getLastPopTimestamp() {
        return lastPopTimestamp;
    }


    public void setLastPopTimestamp(long lastPopTimestamp) {
        this.lastPopTimestamp = lastPopTimestamp;
    }

    /** 返回 Pop 队列运行态可读字符串。 */
    @Override
    public String toString() {
        return "PopProcessQueueInfo [waitAckCount:" + waitAckCount +
                ", droped:" + droped + ", lastPopTimestamp:" + lastPopTimestamp + "]";
    }
}
