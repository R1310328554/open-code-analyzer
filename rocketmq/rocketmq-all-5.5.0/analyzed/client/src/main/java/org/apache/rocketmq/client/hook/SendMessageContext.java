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

import java.util.Map;
import org.apache.rocketmq.client.impl.CommunicationMode;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.message.MessageType;

/**
 * 发送消息钩子上下文：记录 Producer 单次 send 的目标队列、Broker 地址、
 * 通信模式、发送结果及轨迹信息，供 {@link SendMessageHook} 前后回调。
 */
public class SendMessageContext {
    /** Producer 组名。 */
    private String producerGroup;
    /** 待发送消息。 */
    private Message message;
    /** 目标消息队列。 */
    private MessageQueue mq;
    /** 目标 Broker 地址。 */
    private String brokerAddr;
    /** 消息产生主机地址。 */
    private String bornHost;
    /** 发送通信模式。 */
    private CommunicationMode communicationMode;
    /** 发送结果（After 钩子中回填）。 */
    private SendResult sendResult;
    /** 发送异常（若有）。 */
    private Exception exception;
    /** 消息轨迹追踪上下文。 */
    private Object mqTraceContext;
    /** 扩展属性键值对。 */
    private Map<String, String> props;
    /** 关联的 Producer 实现实例。 */
    private DefaultMQProducerImpl producer;
    /** 消息类型（普通/事务/延迟等）。 */
    private MessageType msgType = MessageType.Normal_Msg;
    /** 命名空间。 */
    private String namespace;

    /** 返回消息类型。 */
    public MessageType getMsgType() {
        return msgType;
    }

    /** 设置消息类型。 */
    public void setMsgType(final MessageType msgType) {
        this.msgType = msgType;
    }

    /** 返回 Producer 实现。 */
    public DefaultMQProducerImpl getProducer() {
        return producer;
    }

    /** 设置 Producer 实现。 */
    public void setProducer(final DefaultMQProducerImpl producer) {
        this.producer = producer;
    }

    /** 返回 Producer 组。 */
    public String getProducerGroup() {
        return producerGroup;
    }

    /** 设置 Producer 组。 */
    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    /** 返回待发送消息。 */
    public Message getMessage() {
        return message;
    }

    /** 设置待发送消息。 */
    public void setMessage(Message message) {
        this.message = message;
    }

    /** 返回目标队列。 */
    public MessageQueue getMq() {
        return mq;
    }

    /** 设置目标队列。 */
    public void setMq(MessageQueue mq) {
        this.mq = mq;
    }

    /** 返回 Broker 地址。 */
    public String getBrokerAddr() {
        return brokerAddr;
    }

    /** 设置 Broker 地址。 */
    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }

    /** 返回通信模式。 */
    public CommunicationMode getCommunicationMode() {
        return communicationMode;
    }

    /** 设置通信模式。 */
    public void setCommunicationMode(CommunicationMode communicationMode) {
        this.communicationMode = communicationMode;
    }

    /** 返回发送结果。 */
    public SendResult getSendResult() {
        return sendResult;
    }

    /** 设置发送结果。 */
    public void setSendResult(SendResult sendResult) {
        this.sendResult = sendResult;
    }

    /** 返回发送异常。 */
    public Exception getException() {
        return exception;
    }

    /** 设置发送异常。 */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /** 返回轨迹追踪上下文。 */
    public Object getMqTraceContext() {
        return mqTraceContext;
    }

    /** 设置轨迹追踪上下文。 */
    public void setMqTraceContext(Object mqTraceContext) {
        this.mqTraceContext = mqTraceContext;
    }

    /** 返回扩展属性。 */
    public Map<String, String> getProps() {
        return props;
    }

    /** 设置扩展属性。 */
    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    /** 返回消息产生主机。 */
    public String getBornHost() {
        return bornHost;
    }

    /** 设置消息产生主机。 */
    public void setBornHost(String bornHost) {
        this.bornHost = bornHost;
    }

    /** 返回命名空间。 */
    public String getNamespace() {
        return namespace;
    }

    /** 设置命名空间。 */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
