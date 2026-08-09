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

import com.google.common.base.MoreObjects;
import org.apache.rocketmq.common.action.Action;
import org.apache.rocketmq.common.action.RocketMQAction;
import org.apache.rocketmq.common.resource.ResourceType;
import org.apache.rocketmq.common.resource.RocketMQResource;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.annotation.CFNullable;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.rpc.RpcRequestHeader;

/**
 * 结束事务请求头：Producer 提交或回滚半消息，完成两阶段事务。
 */
@RocketMQAction(value = RequestCode.END_TRANSACTION, action = Action.PUB)
public class EndTransactionRequestHeader extends RpcRequestHeader {
    /** 半消息所属 Topic。 */
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    /** 生产者组名称。 */
    @CFNotNull
    private String producerGroup;
    /** 事务状态表位点。 */
    @CFNotNull
    private Long tranStateTableOffset;
    /** CommitLog 物理位点。 */
    @CFNotNull
    private Long commitLogOffset;
    /** 提交/回滚/未知标志（TRANSACTION_COMMIT_TYPE / ROLLBACK / NOT）。 */
    @CFNotNull
    private Integer commitOrRollback; // TRANSACTION_COMMIT_TYPE
    // TRANSACTION_ROLLBACK_TYPE
    // TRANSACTION_NOT_TYPE

    /** 是否由 Broker 事务回查触发。 */
    @CFNullable
    private Boolean fromTransactionCheck = false;

    /** 半消息 ID。 */
    @CFNotNull
    private String msgId;

    /** 事务 ID。 */
    private String transactionId;

    @Override
    /** 校验 commitOrRollback 必须为合法事务类型。 */
    public void checkFields() throws RemotingCommandException {
        if (MessageSysFlag.TRANSACTION_NOT_TYPE == this.commitOrRollback) {
            return;
        }

        if (MessageSysFlag.TRANSACTION_COMMIT_TYPE == this.commitOrRollback) {
            return;
        }

        if (MessageSysFlag.TRANSACTION_ROLLBACK_TYPE == this.commitOrRollback) {
            return;
        }

        throw new RemotingCommandException("commitOrRollback field wrong");
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回生产者组。 */
    public String getProducerGroup() {
        return producerGroup;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public Long getTranStateTableOffset() {
        return tranStateTableOffset;
    }

    public void setTranStateTableOffset(Long tranStateTableOffset) {
        this.tranStateTableOffset = tranStateTableOffset;
    }

    public Long getCommitLogOffset() {
        return commitLogOffset;
    }

    public void setCommitLogOffset(Long commitLogOffset) {
        this.commitLogOffset = commitLogOffset;
    }

    /** 返回提交/回滚标志。 */
    public Integer getCommitOrRollback() {
        return commitOrRollback;
    }

    public void setCommitOrRollback(Integer commitOrRollback) {
        this.commitOrRollback = commitOrRollback;
    }

    /** 返回是否由回查触发。 */
    public Boolean getFromTransactionCheck() {
        return fromTransactionCheck;
    }

    public void setFromTransactionCheck(Boolean fromTransactionCheck) {
        this.fromTransactionCheck = fromTransactionCheck;
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

    /** 返回便于诊断的可读字符串。 */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("producerGroup", producerGroup)
            .add("tranStateTableOffset", tranStateTableOffset)
            .add("commitLogOffset", commitLogOffset)
            .add("commitOrRollback", commitOrRollback)
            .add("fromTransactionCheck", fromTransactionCheck)
            .add("msgId", msgId)
            .add("transactionId", transactionId)
            .toString();
    }
}
