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

import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 触发 Lite 消息派发的请求头：指定消费组及可选客户端 ID。
 * Broker 据此向 Lite 消费端推送待消费消息。
 */
public class TriggerLiteDispatchRequestHeader implements CommandCustomHeader {

    /** 目标消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String group;

    /** 指定客户端 ID，为空则对该组全部客户端触发。 */
    private String clientId;

    /** 返回消费组名称。 */
    public String getGroup() {
        return group;
    }

    /** 设置消费组名称。 */
    public void setGroup(String group) {
        this.group = group;
    }

    /** 返回客户端 ID。 */
    public String getClientId() {
        return clientId;
    }

    /** 设置客户端 ID。 */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
