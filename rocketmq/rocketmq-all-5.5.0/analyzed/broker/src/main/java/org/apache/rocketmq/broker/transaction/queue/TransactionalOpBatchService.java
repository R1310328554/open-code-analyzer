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
package org.apache.rocketmq.broker.transaction.queue;

import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 事务 Op 消息批量发送线程：按 {@code transactionOpBatchInterval}
 * 唤醒 {@link TransactionalMessageServiceImpl#batchSendOpMessage()}。
 */
public class TransactionalOpBatchService extends ServiceThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.TRANSACTION_LOGGER_NAME);

    private BrokerController brokerController;
    private TransactionalMessageServiceImpl transactionalMessageService;

    private long wakeupTimestamp = 0;


    /** @param brokerController Broker 控制器
     *  @param transactionalMessageService 队列模式事务服务实现 */
    public TransactionalOpBatchService(BrokerController brokerController,
                                       TransactionalMessageServiceImpl transactionalMessageService) {
        this.brokerController = brokerController;
        this.transactionalMessageService = transactionalMessageService;
    }

    /** 返回本服务线程类名。 */
    @Override
    public String getServiceName() {
        return TransactionalOpBatchService.class.getSimpleName();
    }

    /** 按 batch 间隔休眠，到期或提前唤醒时批量发送 Op。 */
    @Override
    public void run() {
        LOGGER.info("Start transaction op batch thread!");
        long checkInterval = brokerController.getBrokerConfig().getTransactionOpBatchInterval();
        wakeupTimestamp = System.currentTimeMillis() + checkInterval;
        while (!this.isStopped()) {
            long interval = wakeupTimestamp - System.currentTimeMillis();
            if (interval <= 0) {
                interval = 0;
                wakeup();
            }
            this.waitForRunning(interval);
        }
        LOGGER.info("End transaction op batch thread!");
    }

    /** 调用事务服务批量刷写 Op 消息并更新下次唤醒时间。 */
    @Override
    protected void onWaitEnd() {
        wakeupTimestamp = transactionalMessageService.batchSendOpMessage();
    }
}
