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

import java.util.List;
import org.apache.rocketmq.common.lite.LiteLagInfo;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.admin.OffsetWrapper;

/**
 * Lite 消费组积压与位点信息响应：汇总 lag、TopK 明细及可选 Lite Topic 位点。
 */
public class GetLiteGroupInfoResponseBody extends RemotingSerializable {
    /** 消费者 Group。 */
    private String group;
    /** 父 Topic 名。 */
    private String parentTopic;
    /** 指定查询的 Lite Topic（可为空表示全组汇总）。 */
    private String liteTopic;
    // 全组汇总 lag 信息
    /** 最早未消费消息时间戳，-1 表示无积压。 */
    private long earliestUnconsumedTimestamp = -1;
    /** 全组消息积压条数。 */
    private long totalLagCount;
    // 单 Lite Topic 位点明细（指定 liteTopic 时填充）
    /** 指定 Lite Topic 的位点包装（min/max/consumer 位点）。 */
    private OffsetWrapper liteTopicOffsetWrapper; // if lite topic specified
    // 积压 TopK 排行
    /** 按积压条数降序的 TopK Lite Topic。 */
    private List<LiteLagInfo> lagCountTopK;
    /** 按最早未消费时间排序的 TopK Lite Topic。 */
    private List<LiteLagInfo> lagTimestampTopK;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getParentTopic() {
        return parentTopic;
    }

    public void setParentTopic(String parentTopic) {
        this.parentTopic = parentTopic;
    }

    public String getLiteTopic() {
        return liteTopic;
    }

    public void setLiteTopic(String liteTopic) {
        this.liteTopic = liteTopic;
    }

    public long getEarliestUnconsumedTimestamp() {
        return earliestUnconsumedTimestamp;
    }

    public void setEarliestUnconsumedTimestamp(long earliestUnconsumedTimestamp) {
        this.earliestUnconsumedTimestamp = earliestUnconsumedTimestamp;
    }

    /** 返回全组积压条数。 */
    public long getTotalLagCount() {
        return totalLagCount;
    }

    public void setTotalLagCount(long totalLagCount) {
        this.totalLagCount = totalLagCount;
    }

    public OffsetWrapper getLiteTopicOffsetWrapper() {
        return liteTopicOffsetWrapper;
    }

    public void setLiteTopicOffsetWrapper(OffsetWrapper liteTopicOffsetWrapper) {
        this.liteTopicOffsetWrapper = liteTopicOffsetWrapper;
    }

    public List<LiteLagInfo> getLagCountTopK() {
        return lagCountTopK;
    }

    public void setLagCountTopK(List<LiteLagInfo> lagCountTopK) {
        this.lagCountTopK = lagCountTopK;
    }

    public List<LiteLagInfo> getLagTimestampTopK() {
        return lagTimestampTopK;
    }

    public void setLagTimestampTopK(List<LiteLagInfo> lagTimestampTopK) {
        this.lagTimestampTopK = lagTimestampTopK;
    }
}
