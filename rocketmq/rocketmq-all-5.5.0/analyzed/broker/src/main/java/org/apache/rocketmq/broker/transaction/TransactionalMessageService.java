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
package org.apache.rocketmq.broker.transaction;

import java.util.concurrent.CompletableFuture;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageExtBrokerInner;
import org.apache.rocketmq.remoting.protocol.header.EndTransactionRequestHeader;
import org.apache.rocketmq.store.PutMessageResult;

/**
 * Broker 端事务消息服务抽象：半消息写入、提交/回滚、回查与指标管理。
 * 队列模式见 {@link org.apache.rocketmq.broker.transaction.queue.TransactionalMessageServiceImpl}，
 * RocksDB 模式见 {@link org.apache.rocketmq.broker.transaction.rocksdb.TransactionalMessageRocksDBService}。
 */
public interface TransactionalMessageService {

    /**
     * 同步写入半消息（Prepare）到存储。
     *
     * @param messageInner 半消息体
     * @return 写入结果
     */
    PutMessageResult prepareMessage(MessageExtBrokerInner messageInner);

    /**
     * 异步写入半消息；Future 在刷盘与副本同步完成后完成。
     *
     * @param messageInner 半消息体
     * @return 异步写入结果
     */
    CompletableFuture<PutMessageResult> asyncPrepareMessage(MessageExtBrokerInner messageInner);

    /**
     * 提交或回滚后删除半消息。
     *
     * @param messageExt 待删除的半消息
     * @return 是否删除成功
     */
    boolean deletePrepareMessage(MessageExt messageExt);

    /**
     * 处理事务提交：将半消息转为正式消息。
     *
     * @param requestHeader 结束事务请求头
     * @return 操作结果（含半消息与错误码）
     */
    OperationResult commitMessage(EndTransactionRequestHeader requestHeader);

    /**
     * 处理事务回滚：丢弃半消息。
     *
     * @param requestHeader 结束事务请求头
     * @return 操作结果（含半消息与错误码）
     */
    OperationResult rollbackMessage(EndTransactionRequestHeader requestHeader);

    /**
     * 扫描未决半消息并向 Producer 发送回查请求。
     *
     * @param transactionTimeout 首次回查最小等待时间（毫秒）
     * @param transactionCheckMax 最大回查次数，超限则丢弃
     * @param listener 回查或丢弃时的回调
     */
    void check(long transactionTimeout, int transactionCheckMax, AbstractTransactionalMessageCheckListener listener);

    /**
     * 启动事务服务。
     *
     * @return 启动成功返回 true
     */
    boolean open();

    /** 关闭事务服务。 */
    void close();

    TransactionMetrics getTransactionMetrics();

    void setTransactionMetrics(TransactionMetrics transactionMetrics);
}
