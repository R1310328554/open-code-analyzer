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
package org.apache.rocketmq.proxy.processor;

import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.common.sysflag.MessageSysFlag;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.common.ProxyException;
import org.apache.rocketmq.proxy.common.ProxyExceptionCode;
import org.apache.rocketmq.proxy.service.ServiceManager;
import org.apache.rocketmq.proxy.service.transaction.EndTransactionRequestData;

/**
 * 事务消息处理器：负责半消息提交/回滚及事务订阅注册。
 */
public class TransactionProcessor extends AbstractProcessor {

    /** 构造事务处理器。 */
    public TransactionProcessor(MessagingProcessor messagingProcessor,
        ServiceManager serviceManager) {
        super(messagingProcessor, serviceManager);
    }

    /** 结束事务（提交或回滚），向 Broker 发送 EndTransaction 单向请求。 */
    public CompletableFuture<Void> endTransaction(ProxyContext ctx, String topic, String transactionId, String messageId, String producerGroup,
        TransactionStatus transactionStatus, boolean fromTransactionCheck, long timeoutMillis) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            // 从事务服务生成 EndTransaction 请求头
            EndTransactionRequestData headerData = serviceManager.getTransactionService().genEndTransactionRequestHeader(
                ctx,
                topic,
                producerGroup,
                buildCommitOrRollback(transactionStatus),
                fromTransactionCheck,
                messageId,
                transactionId
            );
            if (headerData == null) {
                future.completeExceptionally(new ProxyException(ProxyExceptionCode.TRANSACTION_DATA_NOT_FOUND, "cannot found transaction data"));
                return future;
            }
            return this.serviceManager.getMessageService().endTransactionOneway(
                ctx,
                headerData.getBrokerName(),
                headerData.getRequestHeader(),
                timeoutMillis
            );
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    /** 将 {@link TransactionStatus} 映射为 MessageSysFlag 提交/回滚类型。 */
    protected int buildCommitOrRollback(TransactionStatus transactionStatus) {
        switch (transactionStatus) {
            case COMMIT: // 提交
                return MessageSysFlag.TRANSACTION_COMMIT_TYPE;
            case ROLLBACK: // 回滚
                return MessageSysFlag.TRANSACTION_ROLLBACK_TYPE;
            default:
                return MessageSysFlag.TRANSACTION_NOT_TYPE;
        }
    }

    /** 注册生产者组对 Topic 的事务订阅关系。 */
    public void addTransactionSubscription(ProxyContext ctx, String producerGroup, String topic) {
        this.serviceManager.getTransactionService().addTransactionSubscription(ctx, producerGroup, topic);
    }
}
