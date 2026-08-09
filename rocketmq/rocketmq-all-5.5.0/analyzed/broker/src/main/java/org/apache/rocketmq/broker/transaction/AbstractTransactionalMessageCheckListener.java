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

import io.netty.channel.Channel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.protocol.header.CheckTransactionStateRequestHeader;

/**
 * 事务消息回查监听器抽象基类：异步向 Producer 发送 CHECK_TRANSACTION_STATE 请求，
 * 子类实现 resolveDiscardMsg 处理超限回查消息。
 */
public abstract class AbstractTransactionalMessageCheckListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.TRANSACTION_LOGGER_NAME);

    private BrokerController brokerController;

    // TRANS_CHECK_MAX_TIME_TOPIC 队列数
    protected final static int TCMT_QUEUE_NUMS = 1;

    private volatile ExecutorService executorService;

    public AbstractTransactionalMessageCheckListener() {
    }

    public AbstractTransactionalMessageCheckListener(BrokerController brokerController) {
        this.brokerController = brokerController;
    }

    /** 构造回查请求头，恢复真实 topic/queueId 并向 Producer 通道发送。 */
    public void sendCheckMessage(MessageExt msgExt) throws Exception {
        CheckTransactionStateRequestHeader checkTransactionStateRequestHeader = new CheckTransactionStateRequestHeader();
        checkTransactionStateRequestHeader.setTopic(msgExt.getTopic());
        checkTransactionStateRequestHeader.setCommitLogOffset(msgExt.getCommitLogOffset());
        checkTransactionStateRequestHeader.setOffsetMsgId(msgExt.getMsgId());
        checkTransactionStateRequestHeader.setMsgId(msgExt.getUserProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));
        checkTransactionStateRequestHeader.setTransactionId(checkTransactionStateRequestHeader.getMsgId());
        checkTransactionStateRequestHeader.setTranStateTableOffset(msgExt.getQueueOffset());
        checkTransactionStateRequestHeader.setBrokerName(brokerController.getBrokerConfig().getBrokerName());
        msgExt.setTopic(msgExt.getUserProperty(MessageConst.PROPERTY_REAL_TOPIC));
        msgExt.setQueueId(Integer.parseInt(msgExt.getUserProperty(MessageConst.PROPERTY_REAL_QUEUE_ID)));
        msgExt.setStoreSize(0);
        String groupId = msgExt.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP);
        Channel channel = brokerController.getProducerManager().getAvailableChannel(groupId);
        if (channel != null) {
            brokerController.getBroker2Client().checkProducerTransactionState(groupId, channel, checkTransactionStateRequestHeader, msgExt);
        } else {
            LOGGER.warn("Check transaction failed, channel is null. groupId={}", groupId);
        }
    }

    /** 在线程池中异步执行半消息回查。 */
    public void resolveHalfMsg(final MessageExt msgExt) {
        if (executorService != null) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendCheckMessage(msgExt);
                    } catch (Exception e) {
                        LOGGER.error("Send check message error!", e);
                    }
                }
            });
        } else {
            LOGGER.error("TransactionalMessageCheckListener not init");
        }
    }

    public BrokerController getBrokerController() {
        return brokerController;
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public synchronized void initExecutorService() {
        if (executorService == null) {
            executorService = ThreadUtils.newThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2000),
                new ThreadFactoryImpl("Transaction-msg-check-thread", brokerController.getBrokerIdentity()), new CallerRunsPolicy());
        }
    }

    /**
     * 注入 BrokerController 并初始化回查线程池
     *
     * @param brokerController
     */
    public void setBrokerController(BrokerController brokerController) {
        this.brokerController = brokerController;
        initExecutorService();
    }

    /**
     * 避免无限回查：超过最大回查次数时丢弃半消息（由子类实现）。
     *
     * @param msgExt 待丢弃的半消息
     */
    /** 回查次数超限时丢弃半消息的具体实现。 */
    public abstract void resolveDiscardMsg(MessageExt msgExt);
}
