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
 * 事务半消息回查调度线程：按 {@code transactionCheckInterval} 唤醒，
 * 在 {@link #onWaitEnd()} 中触发 {@link TransactionalMessageService#check}。
 */
public class TransactionalMessageCheckService extends ServiceThread {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.TRANSACTION_LOGGER_NAME);

    private BrokerController brokerController;

    /** @param brokerController 所属 Broker 控制器 */
    public TransactionalMessageCheckService(BrokerController brokerController) {
        this.brokerController = brokerController;
    }

    /** 容器模式下返回 broker 标识前缀 + 类名。 */
    @Override
    public String getServiceName() {
        if (brokerController != null && brokerController.getBrokerConfig().isInBrokerContainer()) {
            return brokerController.getBrokerIdentity().getIdentifier() + TransactionalMessageCheckService.class.getSimpleName();
        }
        return TransactionalMessageCheckService.class.getSimpleName();
    }

    /** 按配置间隔 {@link #waitForRunning}，等待结束时执行回查。 */
    @Override
    public void run() {
        log.info("Start transaction check service thread!");
        while (!this.isStopped()) {
            long checkInterval = brokerController.getBrokerConfig().getTransactionCheckInterval();
            this.waitForRunning(checkInterval);
        }
        log.info("End transaction check service thread!");
    }

    /** 读取超时与最大回查次数，调用事务服务扫描半消息。 */
    @Override
    protected void onWaitEnd() {
        long timeout = brokerController.getBrokerConfig().getTransactionTimeOut();
        int checkMax = brokerController.getBrokerConfig().getTransactionCheckMax();
        long begin = System.currentTimeMillis();
        log.info("Begin to check prepare message, begin time:{}", begin);
        this.brokerController.getTransactionalMessageService().check(timeout, checkMax, this.brokerController.getTransactionalMessageCheckListener());
        log.info("End to check prepare message, consumed time:{}", System.currentTimeMillis() - begin);
    }

}
