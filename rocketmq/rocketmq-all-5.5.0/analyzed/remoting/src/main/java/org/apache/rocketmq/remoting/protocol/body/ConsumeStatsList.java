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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;

/**
 * 多 Broker 消费统计列表：按订阅组聚合 {@link ConsumeStats} 及总堆积量。
 */
public class ConsumeStatsList extends RemotingSerializable {
    /** 订阅组 → 消费统计列表。 */
    private List<Map<String/*subscriptionGroupName*/, List<ConsumeStats>>> consumeStatsList = new ArrayList<>();
    /** 统计来源 Broker 地址。 */
    private String brokerAddr;
    /** 全部队列总堆积量。 */
    private long totalDiff;
    /** 在途（已拉取未 Ack）总堆积量。 */
    private long totalInflightDiff;

    /** 返回消费统计列表。 */
    public List<Map<String, List<ConsumeStats>>> getConsumeStatsList() {
        return consumeStatsList;
    }

    /** 设置消费统计列表。 */
    public void setConsumeStatsList(List<Map<String, List<ConsumeStats>>> consumeStatsList) {
        this.consumeStatsList = consumeStatsList;
    }

    /** 返回 Broker 地址。 */
    public String getBrokerAddr() {
        return brokerAddr;
    }

    /** 设置 Broker 地址。 */
    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    /** 返回总堆积量。 */
    public long getTotalDiff() {
        return totalDiff;
    }

    /** 设置总堆积量。 */
    public void setTotalDiff(long totalDiff) {
        this.totalDiff = totalDiff;
    }

    /** 返回在途堆积量。 */
    public long getTotalInflightDiff() {
        return totalInflightDiff;
    }

    /** 设置在途堆积量。 */
    public void setTotalInflightDiff(long totalInflightDiff) {
        this.totalInflightDiff = totalInflightDiff;
    }
}
