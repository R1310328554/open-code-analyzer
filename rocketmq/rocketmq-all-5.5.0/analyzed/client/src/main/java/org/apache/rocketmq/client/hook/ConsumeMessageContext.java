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

import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 消费消息钩子上下文：记录一次消费批次的消息列表、队列、成功标志及
 * 链路追踪/命名空间等元数据，供 {@link ConsumeMessageHook} 前后回调使用。
 */
public class ConsumeMessageContext {
    /** 消费者组名。 */
    private String consumerGroup;
    /** 本批次待消费或已消费的消息列表。 */
    private List<MessageExt> msgList;
    /** 消息来源队列。 */
    private MessageQueue mq;
    /** 消费是否成功（After 钩子中回填）。 */
    private boolean success;
    /** 消费状态描述（如 RECONSUME_LATER）。 */
    private String status;
    /** 消息轨迹追踪上下文。 */
    private Object mqTraceContext;
    /** 扩展属性键值对。 */
    private Map<String, String> props;
    /** 命名空间（多租户隔离）。 */
    private String namespace;
    /** 接入通道类型（LOCAL/CLOUD 等）。 */
    private AccessChannel accessChannel;

    /** 返回消费者组。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /** 设置消费者组。 */
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 返回消息列表。 */
    public List<MessageExt> getMsgList() {
        return msgList;
    }

    /** 设置消息列表。 */
    public void setMsgList(List<MessageExt> msgList) {
        this.msgList = msgList;
    }

    /** 返回消息队列。 */
    public MessageQueue getMq() {
        return mq;
    }

    /** 设置消息队列。 */
    public void setMq(MessageQueue mq) {
        this.mq = mq;
    }

    /** 消费是否成功。 */
    public boolean isSuccess() {
        return success;
    }

    /** 设置消费成功标志。 */
    public void setSuccess(boolean success) {
        this.success = success;
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

    /** 返回消费状态。 */
    public String getStatus() {
        return status;
    }

    /** 设置消费状态。 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 返回命名空间。 */
    public String getNamespace() {
        return namespace;
    }

    /** 设置命名空间。 */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /** 返回接入通道。 */
    public AccessChannel getAccessChannel() {
        return accessChannel;
    }

    /** 设置接入通道。 */
    public void setAccessChannel(AccessChannel accessChannel) {
        this.accessChannel = accessChannel;
    }
}
