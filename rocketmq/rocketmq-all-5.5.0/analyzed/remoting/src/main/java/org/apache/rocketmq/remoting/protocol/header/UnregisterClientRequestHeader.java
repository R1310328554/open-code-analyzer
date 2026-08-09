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

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.rpc.RpcRequestHeader;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 注销客户端的请求头：Broker 移除指定 clientID 的生产者/消费者注册信息。
 * producerGroup 与 consumerGroup 可选，用于区分注销范围。
 */
@RocketMQAction(value = RequestCode.UNREGISTER_CLIENT, action = {Action.PUB, Action.SUB})
public class UnregisterClientRequestHeader extends RpcRequestHeader {
    /** 待注销的客户端唯一 ID。 */
    @CFNotNull
    private String clientID;

    /** 生产者组，为空则不限定生产端。 */
    @CFNullable
    private String producerGroup;
    /** 消费组，为空则不限定消费端。 */
    @CFNullable
    @RocketMQResource(ResourceType.GROUP)
    private String consumerGroup;

    /** 返回客户端 ID。 */
    public String getClientID() {
        return clientID;
    }

    /** 设置客户端 ID。 */
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    /** 返回生产者组。 */
    public String getProducerGroup() {
        return producerGroup;
    }

    /** 设置生产者组。 */
    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    /** 返回消费组。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /** 设置消费组。 */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }
}
