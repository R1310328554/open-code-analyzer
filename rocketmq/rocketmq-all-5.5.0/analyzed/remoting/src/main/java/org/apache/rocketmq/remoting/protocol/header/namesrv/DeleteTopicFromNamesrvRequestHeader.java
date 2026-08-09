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
package org.apache.rocketmq.remoting.protocol.header.namesrv;

import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.TopicRequestHeader;

/**
 * 从 NameServer 路由表删除 Topic 的请求头：指定 Topic 与可选集群名。
 */
@RocketMQAction(value = RequestCode.DELETE_TOPIC_IN_NAMESRV, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class DeleteTopicFromNamesrvRequestHeader extends TopicRequestHeader {
    /** 待删除的 Topic 名称。 */
    @CFNotNull
    private String topic;

    /** 目标集群名称，可为空。 */
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;

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

    /** 返回集群名称。 */
    public String getClusterName() {
        return clusterName;
    }

    /** 设置集群名称。 */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
