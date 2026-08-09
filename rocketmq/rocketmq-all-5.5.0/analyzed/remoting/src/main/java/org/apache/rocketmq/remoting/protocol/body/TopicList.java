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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * Broker 上 Topic 名称列表及所属 Broker 地址，用于路由查询响应。
 */
public class TopicList extends RemotingSerializable {
    /** Topic 名称集合。 */
    private Set<String> topicList = ConcurrentHashMap.newKeySet();
    /** 承载上述 Topic 的 Broker 地址。 */
    private String brokerAddr;

    /** 返回 Topic 名称集合。 */
    public Set<String> getTopicList() {
        return topicList;
    }

    public void setTopicList(Set<String> topicList) {
        this.topicList = topicList;
    }

    /** 返回 Broker 地址。 */
    public String getBrokerAddr() {
        return brokerAddr;
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }
}
