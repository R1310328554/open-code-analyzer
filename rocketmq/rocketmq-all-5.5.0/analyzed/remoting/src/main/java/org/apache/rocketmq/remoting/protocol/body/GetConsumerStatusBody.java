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

import java.util.HashMap;
import java.util.Map;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 消费者消费进度快照（已废弃）：全局队列位点与各消费者实例位点。
 *
 * @deprecated 请改用新版消费状态查询接口。
 */
@Deprecated
public class GetConsumerStatusBody extends RemotingSerializable {
    /** 全局 MessageQueue → 消费位点。 */
    private Map<MessageQueue, Long> messageQueueTable = new HashMap<>();
    /** clientId →（MessageQueue → 位点）映射。 */
    private Map<String, Map<MessageQueue, Long>> consumerTable =
        new HashMap<>();

    /** 返回全局队列位点表。 */
    public Map<MessageQueue, Long> getMessageQueueTable() {
        return messageQueueTable;
    }

    public void setMessageQueueTable(Map<MessageQueue, Long> messageQueueTable) {
        this.messageQueueTable = messageQueueTable;
    }

    /** 返回各消费者实例位点表。 */
    public Map<String, Map<MessageQueue, Long>> getConsumerTable() {
        return consumerTable;
    }

    public void setConsumerTable(Map<String, Map<MessageQueue, Long>> consumerTable) {
        this.consumerTable = consumerTable;
    }
}
