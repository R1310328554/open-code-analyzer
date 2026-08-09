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
 * $Id: QueryMessageRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
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
 * 按索引查询消息的请求头：支持按 Key、时间范围及索引类型检索 Topic 中的消息。
 * 用于运维排查与消息追踪场景。
 */
@RocketMQAction(value = RequestCode.QUERY_MESSAGE, action = {Action.SUB, Action.GET})
public class QueryMessageRequestHeader extends TopicRequestHeader {
    /** 目标 Topic 名称。 */
    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 消息索引 Key（业务键或消息 ID）。 */
    @CFNotNull
    private String key;
    /** 单次查询返回的最大消息条数。 */
    @CFNotNull
    private Integer maxNum;
    /** 查询起始时间戳（毫秒）。 */
    @CFNotNull
    private Long beginTimestamp;
    /** 查询结束时间戳（毫秒）。 */
    @CFNotNull
    private Long endTimestamp;
    /** 索引类型（如 UNIQ_KEY、TIME），可为空。 */
    private String indexType;
    /** 分页游标：上一批最后一条消息的 Key，可为空。 */
    private String lastKey;

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

    /** 返回索引 Key。 */
    public String getKey() {
        return key;
    }

    /** 设置索引 Key。 */
    public void setKey(String key) {
        this.key = key;
    }

    /** 返回最大返回条数。 */
    public Integer getMaxNum() {
        return maxNum;
    }

    /** 设置最大返回条数。 */
    public void setMaxNum(Integer maxNum) {
        this.maxNum = maxNum;
    }

    /** 返回起始时间戳。 */
    public Long getBeginTimestamp() {
        return beginTimestamp;
    }

    /** 设置起始时间戳。 */
    public void setBeginTimestamp(Long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
    }

    /** 返回结束时间戳。 */
    public Long getEndTimestamp() {
        return endTimestamp;
    }

    /** 设置结束时间戳。 */
    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    /** 返回索引类型。 */
    public String getIndexType() {
        return indexType;
    }

    /** 设置索引类型。 */
    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    /** 返回分页游标 Key。 */
    public String getLastKey() {
        return lastKey;
    }

    /** 设置分页游标 Key。 */
    public void setLastKey(String lastKey) {
        this.lastKey = lastKey;
    }
}
