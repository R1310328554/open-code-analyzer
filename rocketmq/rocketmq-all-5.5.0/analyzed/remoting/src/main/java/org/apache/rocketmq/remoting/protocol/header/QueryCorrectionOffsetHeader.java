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
 * $Id: GetMinOffsetRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
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
 * 查询消费位点校正信息的请求头：对比基准消费组与过滤组在 Topic 上的位点差异。
 * filterGroups 支持逗号分隔的多个消费组。
 */
@RocketMQAction(value = RequestCode.QUERY_CORRECTION_OFFSET, action = Action.GET)
public class QueryCorrectionOffsetHeader extends TopicRequestHeader {
    /** 待对比的消费组列表，逗号分隔，可为空。 */
    @RocketMQResource(value = ResourceType.GROUP, splitter = ",")
    private String filterGroups;
    /** 基准消费组名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String compareGroup;
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;

    /** 校验请求头字段（本类无额外约束，空实现）。 */
    @Override
    public void checkFields() throws RemotingCommandException {

    }

    /** 返回过滤消费组列表。 */
    public String getFilterGroups() {
        return filterGroups;
    }

    /** 设置过滤消费组列表。 */
    public void setFilterGroups(String filterGroups) {
        this.filterGroups = filterGroups;
    }

    /** 返回基准消费组名称。 */
    public String getCompareGroup() {
        return compareGroup;
    }

    /** 设置基准消费组名称。 */
    public void setCompareGroup(String compareGroup) {
        this.compareGroup = compareGroup;
    }

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }
}
