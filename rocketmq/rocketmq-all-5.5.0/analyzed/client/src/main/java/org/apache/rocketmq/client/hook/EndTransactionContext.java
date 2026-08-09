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
package org.apache.rocketmq.client.hook;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.common.message.Message;

/**
 * 事务消息结束钩子上下文：记录半消息提交/回滚时的 Producer 组、消息体、
 * Broker 地址及本地事务状态，供 {@link EndTransactionHook} 使用。
 */
public class EndTransactionContext {
    /** 事务 Producer 组名。 */
    private String producerGroup;
    /** 半消息或事务消息体。 */
    private Message message;
    /** 处理事务的 Broker 地址。 */
    private String brokerAddr;
    /** 消息 ID。 */
    private String msgId;
    /** 事务 ID。 */
    private String transactionId;
    /** 本地事务最终状态（COMMIT/ROLLBACK/UNKNOWN）。 */
    private LocalTransactionState transactionState;
    /** 是否由 Broker 事务回查触发（而非 Producer 主动提交）。 */
    private boolean fromTransactionCheck;

    /** 返回 Producer 组。 */
    public String getProducerGroup() {
        return producerGroup;
    }

    /** 设置 Producer 组。 */
    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    /** 返回消息体。 */
    public Message getMessage() {
        return message;
    }

    /** 设置消息体。 */
    public void setMessage(Message message) {
        this.message = message;
    }

    /** 返回 Broker 地址。 */
    public String getBrokerAddr() {
        return brokerAddr;
    }

    /** 设置 Broker 地址。 */
    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    /** 返回消息 ID。 */
    public String getMsgId() {
        return msgId;
    }

    /** 设置消息 ID。 */
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    /** 返回事务 ID。 */
    public String getTransactionId() {
        return transactionId;
    }

    /** 设置事务 ID。 */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /** 返回本地事务状态。 */
    public LocalTransactionState getTransactionState() {
        return transactionState;
    }

    /** 设置本地事务状态。 */
    public void setTransactionState(LocalTransactionState transactionState) {
        this.transactionState = transactionState;
    }

    /** 是否来自 Broker 事务回查。 */
    public boolean isFromTransactionCheck() {
        return fromTransactionCheck;
    }

    /** 设置是否来自事务回查。 */
    public void setFromTransactionCheck(boolean fromTransactionCheck) {
        this.fromTransactionCheck = fromTransactionCheck;
    }
}
