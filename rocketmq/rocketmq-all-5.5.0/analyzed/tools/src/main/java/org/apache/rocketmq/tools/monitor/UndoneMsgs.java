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

package org.apache.rocketmq.tools.monitor;

/**
 * 未消费消息积压统计快照。
 * <p>由 {@link MonitorService#computeUndoneMsgs} 填充后通过 {@link MonitorListener} 上报。
 */
public class UndoneMsgs {
    /** 消费组名称。 */
    private String consumerGroup;
    /** 业务 Topic 名称。 */
    private String topic;

    /** 全部队列积压消息总数（brokerOffset - consumerOffset 之和）。 */
    private long undoneMsgsTotal;

    /** 单个 MessageQueue 上的最大积压条数。 */
    private long undoneMsgsSingleMQ;

    /** 估算的最大消费延迟（毫秒）。 */
    private long undoneMsgsDelayTimeMills;

    /** @return 消费组名称 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /** @param consumerGroup 消费组名称 */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** @return Topic 名称 */
    public String getTopic() {
        return topic;
    }

    /** @param topic Topic 名称 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** @return 总积压条数 */
    public long getUndoneMsgsTotal() {
        return undoneMsgsTotal;
    }

    /** @param undoneMsgsTotal 总积压条数 */
    public void setUndoneMsgsTotal(long undoneMsgsTotal) {
        this.undoneMsgsTotal = undoneMsgsTotal;
    }

    /** @return 单队列最大积压 */
    public long getUndoneMsgsSingleMQ() {
        return undoneMsgsSingleMQ;
    }

    /** @param undoneMsgsSingleMQ 单队列最大积压 */
    public void setUndoneMsgsSingleMQ(long undoneMsgsSingleMQ) {
        this.undoneMsgsSingleMQ = undoneMsgsSingleMQ;
    }

    /** @return 最大消费延迟（毫秒） */
    public long getUndoneMsgsDelayTimeMills() {
        return undoneMsgsDelayTimeMills;
    }

    /** @param undoneMsgsDelayTimeMills 最大消费延迟（毫秒） */
    public void setUndoneMsgsDelayTimeMills(long undoneMsgsDelayTimeMills) {
        this.undoneMsgsDelayTimeMills = undoneMsgsDelayTimeMills;
    }

    /** @return 便于日志输出的字符串表示 */
    @Override
    public String toString() {
        return "UndoneMsgs [consumerGroup=" + consumerGroup + ", topic=" + topic + ", undoneMsgsTotal="
            + undoneMsgsTotal + ", undoneMsgsSingleMQ=" + undoneMsgsSingleMQ
            + ", undoneMsgsDelayTimeMills=" + undoneMsgsDelayTimeMills + "]";
    }
}
