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
package org.apache.rocketmq.client.producer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.NamespaceUtil;

/**
 * 事务消息 Producer：发送半消息后由 {@link TransactionListener} 执行本地事务，
 * 并管理回查线程池与事务环境生命周期。
 */
public class TransactionMQProducer extends DefaultMQProducer {
    /** 旧版回查监听器（已废弃）。 */
    private TransactionCheckListener transactionCheckListener;
    /** 回查线程池最小线程数（已废弃，建议自定义 ExecutorService）。 */
    private int checkThreadPoolMinSize = 1;
    /** 回查线程池最大线程数（已废弃）。 */
    private int checkThreadPoolMaxSize = 1;
    /** 回查请求最大排队数（已废弃）。 */
    private int checkRequestHoldMax = 2000;

    /** 执行本地事务与回查的自定义线程池。 */
    private ExecutorService executorService;

    /** 新版事务监听器，发送前必须设置。 */
    private TransactionListener transactionListener;

    /** 默认构造。 */
    public TransactionMQProducer() {
    }

    public TransactionMQProducer(final String producerGroup) {
        super(producerGroup);
    }

    public TransactionMQProducer(final String producerGroup, final List<String> topics) {
        super(producerGroup, null, topics);
    }

    public TransactionMQProducer(final String producerGroup, RPCHook rpcHook) {
        super(producerGroup, rpcHook, null);
    }

    public TransactionMQProducer(final String producerGroup, RPCHook rpcHook, final List<String> topics) {
        super(producerGroup, rpcHook, topics);
    }

    public TransactionMQProducer(final String producerGroup, RPCHook rpcHook, boolean enableMsgTrace, final String customizedTraceTopic) {
        super(producerGroup, rpcHook, enableMsgTrace, customizedTraceTopic);
    }

    @Deprecated
    public TransactionMQProducer(final String namespace, final String producerGroup) {
        super(namespace, producerGroup);
    }

    @Deprecated
    public TransactionMQProducer(final String namespace, final String producerGroup, RPCHook rpcHook, boolean enableMsgTrace, final String customizedTraceTopic) {
        super(namespace, producerGroup, rpcHook, enableMsgTrace, customizedTraceTopic);
    }

    /** 初始化事务环境后启动 Producer。 */
    @Override
    public void start() throws MQClientException {
        this.defaultMQProducerImpl.initTransactionEnv();
        super.start();
    }

    /** 关闭 Producer 并销毁事务环境。 */
    @Override
    public void shutdown() {
        super.shutdown();
        this.defaultMQProducerImpl.destroyTransactionEnv();
    }

    /** 发送事务半消息并触发本地事务；Topic 会自动加上 namespace 前缀。 */
    @Override
    public TransactionSendResult sendMessageInTransaction(final Message msg,
        final Object arg) throws MQClientException {
        if (null == this.transactionListener) {
            throw new MQClientException("TransactionListener is null", null);
        }

        msg.setTopic(NamespaceUtil.wrapNamespace(this.getNamespace(), msg.getTopic()));
        return this.defaultMQProducerImpl.sendMessageInTransaction(msg, null, arg);
    }

    /** 返回旧版回查监听器。 */
    public TransactionCheckListener getTransactionCheckListener() {
        return transactionCheckListener;
    }

    /** @deprecated 5.0.0 起移除，请改用 {@link TransactionListener}。 */
    @Deprecated
    public void setTransactionCheckListener(TransactionCheckListener transactionCheckListener) {
        this.transactionCheckListener = transactionCheckListener;
    }

    /** 返回回查线程池最小线程数。 */
    public int getCheckThreadPoolMinSize() {
        return checkThreadPoolMinSize;
    }

    /** @deprecated 5.0.0 起移除，请改用 {@link #setExecutorService} 自定义线程池。 */
    @Deprecated
    public void setCheckThreadPoolMinSize(int checkThreadPoolMinSize) {
        this.checkThreadPoolMinSize = checkThreadPoolMinSize;
    }

    /** 返回回查线程池最大线程数。 */
    public int getCheckThreadPoolMaxSize() {
        return checkThreadPoolMaxSize;
    }

    /** @deprecated 5.0.0 起移除，请改用 {@link #setExecutorService} 自定义线程池。 */
    @Deprecated
    public void setCheckThreadPoolMaxSize(int checkThreadPoolMaxSize) {
        this.checkThreadPoolMaxSize = checkThreadPoolMaxSize;
    }

    /** 返回回查请求最大排队数。 */
    public int getCheckRequestHoldMax() {
        return checkRequestHoldMax;
    }

    /** @deprecated 5.0.0 起移除，请改用 {@link #setExecutorService} 自定义线程池。 */
    @Deprecated
    public void setCheckRequestHoldMax(int checkRequestHoldMax) {
        this.checkRequestHoldMax = checkRequestHoldMax;
    }

    /** 返回事务执行线程池。 */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /** 设置执行本地事务与回查的线程池。 */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /** 返回事务监听器。 */
    public TransactionListener getTransactionListener() {
        return transactionListener;
    }

    /** 设置事务监听器（发送事务消息前必填）。 */
    public void setTransactionListener(TransactionListener transactionListener) {
        this.transactionListener = transactionListener;
    }
}
