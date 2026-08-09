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

import org.apache.rocketmq.client.impl.CommunicationMode;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 发送禁发校验钩子上下文：在消息真正发往 Broker 前，向 {@link CheckForbiddenHook}
 * 传递 NameServer 地址、Producer 组、目标队列及通信模式等信息。
 */
public class CheckForbiddenContext {
    /** NameServer 地址。 */
    private String nameSrvAddr;
    /** Producer 组名。 */
    private String group;
    /** 待发送消息。 */
    private Message message;
    /** 目标消息队列。 */
    private MessageQueue mq;
    /** 目标 Broker 地址。 */
    private String brokerAddr;
    /** 发送通信模式（同步/异步/单向）。 */
    private CommunicationMode communicationMode;
    /** 发送结果（钩子执行后可能回填）。 */
    private SendResult sendResult;
    /** 发送过程异常（若有）。 */
    private Exception exception;
    /** 用户自定义扩展参数。 */
    private Object arg;
    /** 是否单元化部署模式。 */
    private boolean unitMode = false;

    /** 返回 Producer 组名。 */
    public String getGroup() {
        return group;
    }

    /** 设置 Producer 组名。 */
    public void setGroup(String group) {
        this.group = group;
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

    /** 返回扩展参数。 */
    public Object getArg() {
        return arg;
    }

    /** 设置扩展参数。 */
    public void setArg(Object arg) {
        this.arg = arg;
    }

    /** 是否单元化模式。 */
    public boolean isUnitMode() {
        return unitMode;
    }

    /** 设置是否单元化模式。 */
    public void setUnitMode(boolean isUnitMode) {
        this.unitMode = isUnitMode;
    }

    /** 返回 NameServer 地址。 */
    public String getNameSrvAddr() {
        return nameSrvAddr;
    }

    /** 设置 NameServer 地址。 */
    public void setNameSrvAddr(String nameSrvAddr) {
        this.nameSrvAddr = nameSrvAddr;
    }

    /** 返回便于日志排查的字符串表示。 */
    @Override
    public String toString() {
        return "SendMessageContext [nameSrvAddr=" + nameSrvAddr + ", group=" + group + ", message=" + message
            + ", mq=" + mq + ", brokerAddr=" + brokerAddr + ", communicationMode=" + communicationMode
            + ", sendResult=" + sendResult + ", exception=" + exception + ", unitMode=" + unitMode
            + ", arg=" + arg + "]";
    }
}
