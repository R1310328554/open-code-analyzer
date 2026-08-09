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
package org.apache.rocketmq.store.pop;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;


/**
 * 批量 Pop 确认消息：在 {@link AckMsg} 基础上携带多条 ack 偏移列表。
 */
public class BatchAckMsg extends AckMsg {
    /** 批量 ack 的队列偏移列表。 */
    @JSONField(name = "aol", alternateNames = {"ackOffsetList"})
    private List<Long> ackOffsetList = new ArrayList(32);


    /** 返回 ack 偏移列表。 */
    public List<Long> getAckOffsetList() {
        return ackOffsetList;
    }

    /** 设置 ack 偏移列表。 */
    public void setAckOffsetList(List<Long> ackOffsetList) {
        this.ackOffsetList = ackOffsetList;
    }

    /** 返回包含批量 ack 字段的可读字符串。 */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BatchAckMsg{");
        sb.append("ackOffsetList=").append(ackOffsetList);
        sb.append(", startOffset=").append(getStartOffset());
        sb.append(", consumerGroup='").append(getConsumerGroup()).append('\'');
        sb.append(", topic='").append(getTopic()).append('\'');
        sb.append(", queueId=").append(getQueueId());
        sb.append(", popTime=").append(getPopTime());
        sb.append(", brokerName=").append(getBrokerName());
        sb.append('}');
        return sb.toString();
    }
}
