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
package org.apache.rocketmq.client;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.Map;

/**
 * MQ 管理基础接口：Topic 创建、队列 offset 查询与按 key 检索消息等。
 */
public interface MQAdmin {
    /**
     * 创建 Topic。
     *
     * @param key accessKey
     * @param newTopic Topic 名称
     * @param queueNum 队列数
     * @param attributes 扩展属性
     */
    void createTopic(final String key, final String newTopic, final int queueNum, Map<String, String> attributes)
        throws MQClientException;

    /**
     * 创建 Topic（指定系统标志位）。
     *
     * @param topicSysFlag Topic 系统标志
     */
    void createTopic(String key, String newTopic, int queueNum, int topicSysFlag, Map<String, String> attributes)
        throws MQClientException;

    /**
     * 按时间戳查找队列消费位点；涉及 Broker IO，调用需谨慎。
     */
    long searchOffset(final MessageQueue mq, final long timestamp) throws MQClientException;

    /** 获取队列最大 offset。 */
    long maxOffset(final MessageQueue mq) throws MQClientException;

    /** 获取队列最小 offset。 */
    long minOffset(final MessageQueue mq) throws MQClientException;

    /** 获取队列最早消息存储时间（微秒）。 */
    long earliestMsgStoreTime(final MessageQueue mq) throws MQClientException;

    /** 按 Topic 与 key 在时间范围内索引查询消息。 */
    QueryResult queryMessage(final String topic, final String key, final int maxNum, final long begin,
        final long end) throws MQClientException, InterruptedException;

    /** 按 msgId 查看单条消息详情。 */
    MessageExt viewMessage(String topic,
        String msgId) throws RemotingException, MQBrokerException, InterruptedException, MQClientException;

}