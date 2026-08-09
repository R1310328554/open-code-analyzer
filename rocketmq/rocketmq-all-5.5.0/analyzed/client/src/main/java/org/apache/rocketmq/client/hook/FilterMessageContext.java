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
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 消息过滤钩子上下文：Pull 消费在业务监听器之前，向 {@link FilterMessageHook}
 * 传递待过滤的消息批次与队列信息，支持自定义二次过滤逻辑。
 */
public class FilterMessageContext {
    /** 消费者组名。 */
    private String consumerGroup;
    /** 待过滤的消息列表（可被钩子修改）。 */
    private List<MessageExt> msgList;
    /** 消息来源队列。 */
    private MessageQueue mq;
    /** 用户自定义扩展参数。 */
    private Object arg;
    /** 是否单元化部署模式。 */
    private boolean unitMode;

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

    /** 返回便于日志排查的字符串表示。 */
    @Override
    public String toString() {
        return "ConsumeMessageContext [consumerGroup=" + consumerGroup + ", msgList=" + msgList + ", mq="
            + mq + ", arg=" + arg + "]";
    }
}
