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

import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 事务指标持久化后台线程：按 {@link BrokerController} 配置的间隔
 * 调用 {@link TransactionalMessageService#getTransactionMetrics()} 落盘统计。
 */
public class TransactionMetricsFlushService extends ServiceThread {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.TRANSACTION_LOGGER_NAME);
    private BrokerController brokerController;
    /** @param brokerController 所属 Broker 控制器 */
    public TransactionMetricsFlushService(BrokerController brokerController) {
        this.brokerController = brokerController;
    }

    /** 返回线程服务名 {@code TransactionFlushService}。 */
    @Override
    public String getServiceName() {
        return "TransactionFlushService";
    }

    /** 循环等待 flush 间隔到期后持久化事务指标。 */
    @Override
    public void run() {
        log.info(this.getServiceName() + " service start");
        long start = System.currentTimeMillis();
        while (!this.isStopped()) {
            try {
                if (System.currentTimeMillis() - start > brokerController.getBrokerConfig().getTransactionMetricFlushInterval()) {
                    start = System.currentTimeMillis();
                    brokerController.getTransactionalMessageService().getTransactionMetrics().persist();
                    waitForRunning(brokerController.getBrokerConfig().getTransactionMetricFlushInterval());
                }
            } catch (Throwable e) {
                log.error("Error occurred in " + getServiceName(), e);
            }
        }
        log.info(this.getServiceName() + " service end");
    }
}