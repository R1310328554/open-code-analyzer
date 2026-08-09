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
import org.apache.rocketmq.remoting.rpc.TopicRequestHeader;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 查询消费者消费进度状态的请求头：指定 Topic、消费组及可选客户端地址。
 */
@RocketMQAction(value = RequestCode.INVOKE_BROKER_TO_GET_CONSUMER_STATUS, action = Action.GET)
public class GetConsumerStatusRequestHeader extends TopicRequestHeader {
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 目标消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String group;
    /** 指定 Consumer 客户端地址，可为空表示查询全组。 */
    @CFNullable
    private String clientAddr;

    /** 校验请求头字段（空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回消费组名称。 */
    public String getGroup() {
        return group;
    }

    /** 设置消费组名称。 */
    public void setGroup(String group) {
        this.group = group;
    }

    /** 返回客户端地址。 */
    public String getClientAddr() {
        return clientAddr;
    }

    /** 设置客户端地址。 */
    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    /** 返回含 Topic、消费组与客户端地址的调试字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("topic", topic)
            .add("group", group)
            .add("clientAddr", clientAddr)
            .toString();
    }
}
