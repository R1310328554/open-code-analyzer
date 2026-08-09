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
package org.apache.rocketmq.client.impl.consumer;

import java.util.List;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;

/**
 * 消息消费线程池服务接口：管理消费线程生命周期，提交 pull/pop 拉到的消息进行消费。
 */
public interface ConsumeMessageService {
    /** 启动消费线程池。 */
    void start();

    /** 关闭服务并等待线程终止。 */
    void shutdown(long awaitTerminateMillis);

    /** 更新核心线程数。 */
    void updateCorePoolSize(int corePoolSize);

    /** 核心线程数加一。 */
    void incCorePoolSize();

    /** 核心线程数减一。 */
    void decCorePoolSize();

    /** 返回当前核心线程数。 */
    int getCorePoolSize();

    /** 直接消费单条消息（管理/调试用途）。 */
    ConsumeMessageDirectlyResult consumeMessageDirectly(final MessageExt msg, final String brokerName);

    /**
     * 提交 pull 模式消费请求。
     *
     * @param msgs 待消费消息列表
     * @param processQueue 对应 ProcessQueue
     * @param messageQueue 消息队列
     * @param dispathToConsume 是否立即分派到消费线程
     */
    void submitConsumeRequest(
        final List<MessageExt> msgs,
        final ProcessQueue processQueue,
        final MessageQueue messageQueue,
        final boolean dispathToConsume);

    /**
     * 提交 POP 模式消费请求。
     *
     * @param msgs 待消费消息列表
     * @param processQueue 对应 PopProcessQueue
     * @param messageQueue 消息队列
     */
    void submitPopConsumeRequest(
        final List<MessageExt> msgs,
        final PopProcessQueue processQueue,
        final MessageQueue messageQueue);
}
