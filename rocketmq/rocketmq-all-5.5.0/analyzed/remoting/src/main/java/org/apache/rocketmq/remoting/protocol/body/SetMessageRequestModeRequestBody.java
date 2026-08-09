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

import org.apache.rocketmq.common.message.MessageRequestMode;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * 设置 Topic 消息请求模式（Pull/Pop）的请求体：指定消费组、模式及 Pop 队列共享数。
 */
public class SetMessageRequestModeRequestBody extends RemotingSerializable {

    /** 目标 Topic 名称。 */
    private String topic;

    /** 消费组名称。 */
    private String consumerGroup;

    /** 消息请求模式，默认 Pull。 */
    private MessageRequestMode mode = MessageRequestMode.PULL;

    /** Pop 模式下，当前消费者可与 cid 列表中后续 N（N=popShareQueueNum）个消费者共享已分配的 MessageQueue。 */
    /** Pop 队列共享数量，0 表示不共享。 */
    private int popShareQueueNum = 0;

    public SetMessageRequestModeRequestBody() {
    }

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 返回消息请求模式。 */
    public MessageRequestMode getMode() {
        return mode;
    }

    public void setMode(MessageRequestMode mode) {
        this.mode = mode;
    }

    /** 返回 Pop 队列共享数。 */
    public int getPopShareQueueNum() {
        return popShareQueueNum;
    }

    public void setPopShareQueueNum(int popShareQueueNum) {
        this.popShareQueueNum = popShareQueueNum;
    }
}
