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
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

/**
 * 查询 ConsumeQueue 索引响应：订阅信息、过滤数据及队列索引条目列表。
 */
public class QueryConsumeQueueResponseBody extends RemotingSerializable {

    /** 关联订阅数据。 */
    private SubscriptionData subscriptionData;
    /** 过滤表达式序列化数据。 */
    private String filterData;
    /** ConsumeQueue 索引条目列表。 */
    private List<ConsumeQueueData> queueData;
    /** 返回结果中最大队列逻辑索引。 */
    private long maxQueueIndex;
    /** 返回结果中最小队列逻辑索引。 */
    private long minQueueIndex;

    /** 返回订阅数据。 */
    public SubscriptionData getSubscriptionData() {
        return subscriptionData;
    }

    public void setSubscriptionData(SubscriptionData subscriptionData) {
        this.subscriptionData = subscriptionData;
    }

    public String getFilterData() {
        return filterData;
    }

    public void setFilterData(String filterData) {
        this.filterData = filterData;
    }

    /** 返回队列索引列表。 */
    public List<ConsumeQueueData> getQueueData() {
        return queueData;
    }

    public void setQueueData(List<ConsumeQueueData> queueData) {
        this.queueData = queueData;
    }

    /** 返回最大队列索引。 */
    public long getMaxQueueIndex() {
        return maxQueueIndex;
    }

    public void setMaxQueueIndex(long maxQueueIndex) {
        this.maxQueueIndex = maxQueueIndex;
    }

    /** 返回最小队列索引。 */
    public long getMinQueueIndex() {
        return minQueueIndex;
    }

    public void setMinQueueIndex(long minQueueIndex) {
        this.minQueueIndex = minQueueIndex;
    }
}
