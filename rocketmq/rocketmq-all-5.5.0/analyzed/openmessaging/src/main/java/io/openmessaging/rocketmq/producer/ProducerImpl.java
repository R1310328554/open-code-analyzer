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
package io.openmessaging.rocketmq.producer;

import io.openmessaging.BytesMessage;
import io.openmessaging.KeyValue;
import io.openmessaging.Message;
import io.openmessaging.Promise;
import io.openmessaging.exception.OMSRuntimeException;
import io.openmessaging.interceptor.ProducerInterceptor;
import io.openmessaging.producer.BatchMessageSender;
import io.openmessaging.producer.LocalTransactionExecutor;
import io.openmessaging.producer.Producer;
import io.openmessaging.producer.SendResult;
import io.openmessaging.rocketmq.promise.DefaultPromise;
import io.openmessaging.rocketmq.utils.OMSUtil;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

import static io.openmessaging.rocketmq.utils.OMSUtil.msgConvert;

/**
 * OMS Producer 实现：提供同步、异步与单向发送能力。
 */
public class ProducerImpl extends AbstractOMSProducer implements Producer {

    private static final Logger log = LoggerFactory.getLogger(ProducerImpl.class);

    /** 使用 OMS 属性构造 Producer。 */
    public ProducerImpl(final KeyValue properties) {
        super(properties);
    }

    /** 返回 Producer 配置属性（含 PRODUCER_ID）。 */
    @Override
    public KeyValue attributes() {
        return properties;
    }

    /** 同步发送，使用默认超时。 */
    @Override
    public SendResult send(final Message message) {
        return send(message, this.rocketmqProducer.getSendMsgTimeout());
    }

    /** 同步发送，可从 properties 读取 TIMEOUT 覆盖默认超时。 */
    @Override
    public SendResult send(final Message message, final KeyValue properties) {
        long timeout = properties.containsKey(Message.BuiltinKeys.TIMEOUT)
            ? properties.getInt(Message.BuiltinKeys.TIMEOUT) : this.rocketmqProducer.getSendMsgTimeout();
        return send(message, timeout);
    }

    /** 本地事务发送（当前未实现）。 */
    @Override
    public SendResult send(Message message, LocalTransactionExecutor branchExecutor, KeyValue attributes) {
        return null;
    }

    /** 同步发送核心逻辑：转换消息、调用 RocketMQ 并校验 SEND_OK。 */
    private SendResult send(final Message message, long timeout) {
        checkMessageType(message);
        org.apache.rocketmq.common.message.Message rmqMessage = msgConvert((BytesMessage) message);
        try {
            org.apache.rocketmq.client.producer.SendResult rmqResult = this.rocketmqProducer.send(rmqMessage, timeout);
            if (!rmqResult.getSendStatus().equals(SendStatus.SEND_OK)) {
                log.error(String.format("Send message to RocketMQ failed, %s", message));
                throw new OMSRuntimeException("-1", "Send message to RocketMQ broker failed.");
            }
            message.sysHeaders().put(Message.BuiltinKeys.MESSAGE_ID, rmqResult.getMsgId());
            return OMSUtil.sendResultConvert(rmqResult);
        } catch (Exception e) {
            log.error(String.format("Send message to RocketMQ failed, %s", message), e);
            throw checkProducerException(rmqMessage.getTopic(), message.sysHeaders().getString(Message.BuiltinKeys.MESSAGE_ID), e);
        }
    }

    /** 异步发送，使用默认超时。 */
    @Override
    public Promise<SendResult> sendAsync(final Message message) {
        return sendAsync(message, this.rocketmqProducer.getSendMsgTimeout());
    }

    @Override
    public Promise<SendResult> sendAsync(final Message message, final KeyValue properties) {
        long timeout = properties.containsKey(Message.BuiltinKeys.TIMEOUT)
            ? properties.getInt(Message.BuiltinKeys.TIMEOUT) : this.rocketmqProducer.getSendMsgTimeout();
        return sendAsync(message, timeout);
    }

    /** 异步发送：通过 {@link DefaultPromise} 包装 SendCallback 结果。 */
    private Promise<SendResult> sendAsync(final Message message, long timeout) {
        checkMessageType(message);
        org.apache.rocketmq.common.message.Message rmqMessage = msgConvert((BytesMessage) message);
        final Promise<SendResult> promise = new DefaultPromise<>();
        try {
            this.rocketmqProducer.send(rmqMessage, new SendCallback() {
                @Override
                public void onSuccess(final org.apache.rocketmq.client.producer.SendResult rmqResult) {
                    message.sysHeaders().put(Message.BuiltinKeys.MESSAGE_ID, rmqResult.getMsgId());
                    promise.set(OMSUtil.sendResultConvert(rmqResult));
                }

                @Override
                public void onException(final Throwable e) {
                    promise.setFailure(e);
                }
            }, timeout);
        } catch (Exception e) {
            promise.setFailure(e);
        }
        return promise;
    }

    /** 单向发送，不等待 Broker 应答（异常被忽略）。 */
    @Override
    public void sendOneway(final Message message) {
        checkMessageType(message);
        org.apache.rocketmq.common.message.Message rmqMessage = msgConvert((BytesMessage) message);
        try {
            this.rocketmqProducer.sendOneway(rmqMessage);
        } catch (Exception ignore) { // 单向发送忽略异常
        }
    }

    @Override
    public void sendOneway(final Message message, final KeyValue properties) {
        sendOneway(message);
    }

    /** 批量发送器（当前未实现）。 */
    @Override
    public BatchMessageSender createBatchMessageSender() {
        return null;
    }

    @Override
    public void addInterceptor(ProducerInterceptor interceptor) {

    }

    @Override
    public void removeInterceptor(ProducerInterceptor interceptor) {

    }
}
