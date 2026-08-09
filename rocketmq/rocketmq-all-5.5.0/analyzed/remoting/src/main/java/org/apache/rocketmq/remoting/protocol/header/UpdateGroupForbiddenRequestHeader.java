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
 * $Id: CreateTopicRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header;

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.rpc.TopicRequestHeader;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 更新并查询消费组禁读状态的请求头：设置某消费组对 Topic 的可读权限。
 * readable 为 false 时禁止该组消费指定 Topic。
 */
@RocketMQAction(value = RequestCode.UPDATE_AND_GET_GROUP_FORBIDDEN, action = Action.UPDATE)
public class UpdateGroupForbiddenRequestHeader extends TopicRequestHeader {
    /** 目标消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String  group;
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String  topic;

    /** 是否允许读取，false 表示禁读，可为空。 */
    private Boolean readable;

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

    /** 返回消费组名称。 */
    public String getGroup() {
        return group;
    }

    /** 设置消费组名称。 */
    public void setGroup(String group) {
        this.group = group;
    }

    /** 返回是否可读。 */
    public Boolean getReadable() {
        return readable;
    }

    /** 设置是否可读。 */
    public void setReadable(Boolean readable) {
        this.readable = readable;
    }

}
