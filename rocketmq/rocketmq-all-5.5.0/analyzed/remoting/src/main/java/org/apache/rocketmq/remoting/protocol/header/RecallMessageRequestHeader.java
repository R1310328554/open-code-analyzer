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
package org.apache.rocketmq.remoting.protocol.header;

import com.google.common.base.MoreObjects;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.TopicRequestHeader;

/**
 * 撤回已发送消息的请求头：Producer 通过 recallHandle 标识撤回指定 Topic 下的消息。
 * producerGroup 可为空，由 Broker 从连接上下文推断。
 */
@RocketMQAction(value = RequestCode.RECALL_MESSAGE, action = Action.PUB)
public class RecallMessageRequestHeader extends TopicRequestHeader {
    /** 生产者组名称，可为空。 */
    @CFNullable
    private String producerGroup;

    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;

    /** 消息撤回句柄，由发送响应返回。 */
    @CFNotNull
    private String recallHandle;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回生产者组名称。 */
    public String getProducerGroup() {
        return producerGroup;
    }

    /** 设置生产者组名称。 */
    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回消息撤回句柄。 */
    public String getRecallHandle() {
        return recallHandle;
    }

    /** 设置消息撤回句柄。 */
    public void setRecallHandle(String recallHandle) {
        this.recallHandle = recallHandle;
    }

    /** 返回含生产者组、Topic 与撤回句柄的调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("producerGroup", producerGroup)
                .add("topic", topic)
                .add("recallHandle", recallHandle)
                .toString();
    }
}
