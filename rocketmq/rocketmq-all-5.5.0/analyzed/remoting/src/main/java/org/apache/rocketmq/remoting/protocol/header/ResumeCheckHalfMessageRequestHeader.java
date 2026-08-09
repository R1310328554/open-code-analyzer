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
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;

/**
 * 恢复半消息回查的请求头：事务消息二阶段提交/回滚后，通知 Broker 恢复对该半消息的回查。
 */
@RocketMQAction(value = RequestCode.RESUME_CHECK_HALF_MESSAGE, action = Action.UPDATE)
public class ResumeCheckHalfMessageRequestHeader implements CommandCustomHeader {

    /** 半消息所在 Topic 名称。 */
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 半消息 ID，可为空。 */
    @CFNullable
    private String msgId;

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

    /** 返回半消息 ID。 */
    public String getMsgId() {
        return msgId;
    }

    /** 设置半消息 ID。 */
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    /** 返回含消息 ID 的调试字符串。 */
    @Override
    public String toString() {
        return "ResumeCheckHalfMessageRequestHeader [msgId=" + msgId + "]";
    }
}
