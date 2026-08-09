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

/**
 * $Id: GetRouteInfoRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header.namesrv;

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.TopicRequestHeader;

/**
 * 按 Topic 查询路由信息的请求头：NameServer 返回 Broker 队列分布。
 * acceptStandardJsonOnly 控制是否仅接受标准 JSON 格式路由数据。
 */
@RocketMQAction(value = RequestCode.GET_ROUTEINFO_BY_TOPIC, resource = ResourceType.CLUSTER, action = Action.GET)
public class GetRouteInfoRequestHeader extends TopicRequestHeader {

    /** 目标 Topic 名称。 */
    @CFNotNull
    private String topic;

    /** 是否仅接受标准 JSON 路由格式，可为空。 */
    @CFNullable
    private Boolean acceptStandardJsonOnly;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
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

    /** 返回是否仅接受标准 JSON 路由格式。 */
    public Boolean getAcceptStandardJsonOnly() {
        return acceptStandardJsonOnly;
    }

    /** 设置是否仅接受标准 JSON 路由格式。 */
    public void setAcceptStandardJsonOnly(Boolean acceptStandardJsonOnly) {
        this.acceptStandardJsonOnly = acceptStandardJsonOnly;
    }
}
