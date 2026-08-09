/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.proxy.service.transaction;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.remoting.protocol.header.EndTransactionRequestHeader;

/**
 * 事务服务抽象基类：管理 {@link TransactionData} 并生成结束事务请求。
 */
public abstract class AbstractTransactionService implements TransactionService, StartAndShutdown {

    /** 本地事务元数据管理器。 */
    protected TransactionDataManager transactionDataManager = new TransactionDataManager();

    @Override
    public TransactionData addTransactionDataByBrokerAddr(ProxyContext ctx, String brokerAddr, String topic, String producerGroup, long tranStateTableOffset, long commitLogOffset, String transactionId,
        Message message) {
        return this.addTransactionDataByBrokerName(ctx, this.getBrokerNameByAddr(brokerAddr), topic, producerGroup, tranStateTableOffset, commitLogOffset, transactionId, message);
    }

    @Override
    /** 按 brokerName 记录半消息事务元数据。 */
    public TransactionData addTransactionDataByBrokerName(ProxyContext ctx, String brokerName, String topic, String producerGroup, long tranStateTableOffset, long commitLogOffset, String transactionId,
        Message message) {
        if (StringUtils.isBlank(brokerName)) {
            return null;
        }
        TransactionData transactionData = new TransactionData(
            brokerName,
            topic,
            tranStateTableOffset, commitLogOffset, transactionId,
            System.currentTimeMillis(),
            ConfigurationManager.getProxyConfig().getTransactionDataExpireMillis());

        this.transactionDataManager.addTransactionData(
            producerGroup,
            transactionId,
            transactionData
        );
        return transactionData;
    }

    @Override
    /** 根据本地缓存的事务数据生成 {@link EndTransactionRequestData}。 */
    public EndTransactionRequestData genEndTransactionRequestHeader(ProxyContext ctx, String topic, String producerGroup, Integer commitOrRollback,
        boolean fromTransactionCheck, String msgId, String transactionId) {
        TransactionData transactionData = this.transactionDataManager.pollNoExpireTransactionData(producerGroup, transactionId);
        if (transactionData == null) {
            return null;
        }
        EndTransactionRequestHeader header = new EndTransactionRequestHeader();
        header.setTopic(topic);
        header.setProducerGroup(producerGroup);
        header.setCommitOrRollback(commitOrRollback);
        header.setFromTransactionCheck(fromTransactionCheck);
        header.setMsgId(msgId);
        header.setTransactionId(transactionId);
        header.setTranStateTableOffset(transactionData.getTranStateTableOffset());
        header.setCommitLogOffset(transactionData.getCommitLogOffset());
        return new EndTransactionRequestData(transactionData.getBrokerName(), header);
    }

    @Override
    /** 回查发送失败时移除本地事务记录。 */
    public void onSendCheckTransactionStateFailed(ProxyContext context, String producerGroup, TransactionData transactionData) {
        this.transactionDataManager.removeTransactionData(producerGroup, transactionData.getTransactionId(), transactionData);
    }

    /** 将 Broker 地址解析为 brokerName（子类实现）。 */
    protected abstract String getBrokerNameByAddr(String brokerAddr);

    @Override
    public void shutdown() throws Exception {
        this.transactionDataManager.shutdown();
    }

    @Override
    public void start() throws Exception {
        this.transactionDataManager.start();
    }
}
