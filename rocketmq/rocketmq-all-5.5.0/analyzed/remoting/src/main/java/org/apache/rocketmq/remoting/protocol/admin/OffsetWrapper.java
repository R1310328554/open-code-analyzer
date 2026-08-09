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
package org.apache.rocketmq.remoting.protocol.admin;

/**
 * 单队列偏移量快照：Broker 最大位点、消费者已提交位点、Pull 位点及更新时间。
 */
public class OffsetWrapper {
    /** Broker 端该队列最大逻辑偏移。 */
    private long brokerOffset;
    /** 消费组已提交消费位点。 */
    private long consumerOffset;
    /** 最近一次 Pull 请求的位点（在途消息上界）。 */
    private long pullOffset;
    /** 偏移量最后更新时间戳（毫秒）。 */
    private long lastTimestamp;

    /** 返回 Broker 偏移。 */
    public long getBrokerOffset() {
        return brokerOffset;
    }

    /** 设置 Broker 偏移。 */
    public void setBrokerOffset(long brokerOffset) {
        this.brokerOffset = brokerOffset;
    }

    /** 返回消费者偏移。 */
    public long getConsumerOffset() {
        return consumerOffset;
    }

    /** 设置消费者偏移。 */
    public void setConsumerOffset(long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }

    /** 返回 Pull 偏移。 */
    public long getPullOffset() {
        return pullOffset;
    }

    /** 设置 Pull 偏移。 */
    public void setPullOffset(long pullOffset) {
        this.pullOffset = pullOffset;
    }

    /** 返回最后更新时间。 */
    public long getLastTimestamp() {
        return lastTimestamp;
    }

    /** 设置最后更新时间。 */
    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }
}
