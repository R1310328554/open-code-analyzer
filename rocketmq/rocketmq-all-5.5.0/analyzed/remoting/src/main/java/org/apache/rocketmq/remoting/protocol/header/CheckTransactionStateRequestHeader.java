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
 * $Id: EndTransactionRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header;

import com.google.common.base.MoreObjects;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.RpcRequestHeader;

/**
 * 事务状态回查请求头：Broker 向 Producer 发起半消息提交/回滚状态确认。
 */
@RocketMQAction(value = RequestCode.CHECK_TRANSACTION_STATE, action = Action.PUB)
public class CheckTransactionStateRequestHeader extends RpcRequestHeader {
    /** 半消息所属 Topic。 */
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 事务状态表中的逻辑位点。 */
    @CFNotNull
    private Long tranStateTableOffset;
    /** CommitLog 中的物理位点。 */
    @CFNotNull
    private Long commitLogOffset;
    /** 消息唯一标识。 */
    private String msgId;
    /** 事务 ID（Producer 端生成）。 */
    private String transactionId;
    /** 基于位点生成的 MsgId。 */
    private String offsetMsgId;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回事务状态表位点。 */
    public Long getTranStateTableOffset() {
        return tranStateTableOffset;
    }

    public void setTranStateTableOffset(Long tranStateTableOffset) {
        this.tranStateTableOffset = tranStateTableOffset;
    }

    /** 返回 CommitLog 位点。 */
    public Long getCommitLogOffset() {
        return commitLogOffset;
    }

    public void setCommitLogOffset(Long commitLogOffset) {
        this.commitLogOffset = commitLogOffset;
    }

    /** 返回消息 ID。 */
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    /** 返回事务 ID。 */
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /** 返回位点 MsgId。 */
    public String getOffsetMsgId() {
        return offsetMsgId;
    }

    public void setOffsetMsgId(String offsetMsgId) {
        this.offsetMsgId = offsetMsgId;
    }

    /** 返回便于诊断的可读字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tranStateTableOffset", tranStateTableOffset)
            .add("commitLogOffset", commitLogOffset)
            .add("msgId", msgId)
            .add("transactionId", transactionId)
            .add("offsetMsgId", offsetMsgId)
            .toString();
    }
}
