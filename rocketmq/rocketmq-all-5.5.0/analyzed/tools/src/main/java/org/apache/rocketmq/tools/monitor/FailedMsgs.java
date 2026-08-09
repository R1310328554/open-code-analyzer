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
 * 消费失败消息统计快照。
 * <p>按消费组与 Topic 维度汇总近期失败条数，供 {@link MonitorListener} 上报。
 */
public class FailedMsgs {
    /** 消费组名称。 */
    private String consumerGroup;
    /** 业务 Topic 名称。 */
    private String topic;
    /** 近期消费失败消息总数。 */
    private long failedMsgsTotalRecently;

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

    /** @return 近期失败消息总数 */
    public long getFailedMsgsTotalRecently() {
        return failedMsgsTotalRecently;
    }

    /** @param failedMsgsTotalRecently 近期失败消息总数 */
    public void setFailedMsgsTotalRecently(long failedMsgsTotalRecently) {
        this.failedMsgsTotalRecently = failedMsgsTotalRecently;
    }

    /** @return 便于日志输出的字符串表示 */
    @Override
    public String toString() {
        return "FailedMsgs [consumerGroup=" + consumerGroup + ", topic=" + topic
            + ", failedMsgsTotalRecently=" + failedMsgsTotalRecently + "]";
    }
}
