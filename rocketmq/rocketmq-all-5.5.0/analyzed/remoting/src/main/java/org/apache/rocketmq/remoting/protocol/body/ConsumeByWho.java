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

import java.util.HashSet;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 消息被哪些消费组消费/未消费的诊断结果：按 Topic、队列与位点定位。
 */
public class ConsumeByWho extends RemotingSerializable {
    /** 已消费该消息的消费组集合。 */
    private HashSet<String> consumedGroup = new HashSet<>();
    /** 尚未消费该消息的消费组集合。 */
    private HashSet<String> notConsumedGroup = new HashSet<>();
    /** 目标 Topic。 */
    private String topic;
    /** 队列 ID。 */
    private int queueId;
    /** CommitLog 逻辑位点。 */
    private long offset;

    /** 返回已消费组集合。 */
    public HashSet<String> getConsumedGroup() {
        return consumedGroup;
    }

    /** 设置已消费组集合。 */
    public void setConsumedGroup(HashSet<String> consumedGroup) {
        this.consumedGroup = consumedGroup;
    }

    /** 返回未消费组集合。 */
    public HashSet<String> getNotConsumedGroup() {
        return notConsumedGroup;
    }

    /** 设置未消费组集合。 */
    public void setNotConsumedGroup(HashSet<String> notConsumedGroup) {
        this.notConsumedGroup = notConsumedGroup;
    }

    /** 返回 Topic。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回队列 ID。 */
    public int getQueueId() {
        return queueId;
    }

    /** 设置队列 ID。 */
    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    /** 返回位点。 */
    public long getOffset() {
        return offset;
    }

    /** 设置位点。 */
    public void setOffset(long offset) {
        this.offset = offset;
    }
}
