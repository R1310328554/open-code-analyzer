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
package org.apache.rocketmq.client.consumer.listener;

import java.util.List;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 并发消息监听器：同一消费组内多线程并行处理不同队列的消息。
 */
public interface MessageListenerConcurrently extends MessageListener {
    /**
     * 处理一批消息；不建议抛异常，失败时应返回 {@link ConsumeConcurrentlyStatus#RECONSUME_LATER}。
     *
     * @param msgs    消息列表，size &gt;= 1；批量大小由 consumeMessageBatchMaxSize 控制
     * @param context 并发消费上下文
     * @return 消费状态
     */
    ConsumeConcurrentlyStatus consumeMessage(final List<MessageExt> msgs,
        final ConsumeConcurrentlyContext context);
}
