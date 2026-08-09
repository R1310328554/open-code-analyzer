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
package org.apache.rocketmq.client.consumer.store;

import java.util.Map;
import java.util.Set;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;

/**
 * 消费偏移量存储接口：定义加载、更新、读取、持久化等操作。
 */
public interface OffsetStore {
    /** 加载偏移量（从本地或远程）。 */
    void load() throws MQClientException;

    /** 更新偏移量并写入内存。 */
    void updateOffset(final MessageQueue mq, final long offset, final boolean increaseOnly);

    /**
     * 更新并冻结指定队列偏移量，防止并发更新。
     *
     * @param mq 目标消息队列
     * @param offset 期望更新的偏移量
     */
    void updateAndFreezeOffset(final MessageQueue mq, final long offset);

    /**
     * 按指定方式读取偏移量。
     *
     * @return 读取到的偏移量，未找到时返回负值
     */
    long readOffset(final MessageQueue mq, final ReadOffsetType type);

    /** 批量持久化指定队列的偏移量（本地文件或 Broker）。 */
    void persistAll(final Set<MessageQueue> mqs);

    /** 持久化单个队列的偏移量。 */
    void persist(final MessageQueue mq);

    /** 移除指定队列的偏移量记录。 */
    void removeOffset(MessageQueue mq);

    /**
     * 克隆指定 Topic 的偏移量表副本。
     *
     * @return 偏移量映射副本
     */
    Map<MessageQueue, Long> cloneOffsetTable(String topic);

    /**
     * 将消费偏移量同步到 Broker。
     *
     * @param mq 消息队列
     * @param offset 偏移量
     * @param isOneway 是否单向发送（不等待响应）
     */
    void updateConsumeOffsetToBroker(MessageQueue mq, long offset, boolean isOneway) throws RemotingException,
        MQBrokerException, InterruptedException, MQClientException;
}
