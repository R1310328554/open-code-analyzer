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
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 分页拉取全部 Topic 配置的请求头：含序号、数据版本及单次最大条数。
 */
@RocketMQAction(value = RequestCode.GET_ALL_TOPIC_CONFIG, resource = ResourceType.TOPIC, action = Action.GET)
public class GetAllTopicConfigRequestHeader implements CommandCustomHeader {
    @Override
    public void checkFields() throws RemotingCommandException {
        // nothing
    }

    /** 当前分页序号，从 0 起。 */
    @CFNotNull
    private Integer topicSeq;

    /** 客户端已知的数据版本，用于增量同步。 */
    private String dataVersion;

    /** 单次返回的最大 Topic 数量。 */
    private Integer maxTopicNum;

    /** 返回分页序号。 */
    public Integer getTopicSeq() {
        return topicSeq;
    }

    /** 设置分页序号。 */
    public void setTopicSeq(Integer topicSeq) {
        this.topicSeq = topicSeq;
    }

    /** 返回数据版本。 */
    public String getDataVersion() {
        return dataVersion;
    }

    /** 设置数据版本。 */
    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    /** 返回单次最大条数。 */
    public Integer getMaxTopicNum() {
        return maxTopicNum;
    }

    /** 设置单次最大条数。 */
    public void setMaxTopicNum(Integer maxTopicNum) {
        this.maxTopicNum = maxTopicNum;
    }
}
