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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * Topic 写入统计表：各队列 {@link TopicOffset} 与 Topic 级写入 TPS。
 */
public class TopicStatsTable extends RemotingSerializable {
    /** Topic 写入 TPS（条/秒）。 */
    private double topicPutTps;

    /** 队列 → 偏移范围快照。 */
    private Map<MessageQueue, TopicOffset> offsetTable = new ConcurrentHashMap<>();

    /** 返回偏移表。 */
    public Map<MessageQueue, TopicOffset> getOffsetTable() {
        return offsetTable;
    }

    /** 设置偏移表。 */
    public void setOffsetTable(Map<MessageQueue, TopicOffset> offsetTable) {
        this.offsetTable = offsetTable;
    }

    /** 返回写入 TPS。 */
    public double getTopicPutTps() {
        return topicPutTps;
    }

    /** 设置写入 TPS。 */
    public void setTopicPutTps(double topicPutTps) {
        this.topicPutTps = topicPutTps;
    }
}
