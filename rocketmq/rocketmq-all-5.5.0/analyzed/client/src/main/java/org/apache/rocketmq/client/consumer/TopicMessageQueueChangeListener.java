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
package org.apache.rocketmq.client.consumer;

import java.util.Set;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * Topic 队列数变更监听器：Topic 扩缩容导致 {@link MessageQueue} 集合变化时触发。
 */
public interface TopicMessageQueueChangeListener {
    /**
     * Topic 队列数量变更时回调（扩缩容场景）。
     *
     * @param topic           发生变更的 Topic
     * @param messageQueues   变更后的队列集合
     */
    void onChanged(String topic, Set<MessageQueue> messageQueues);
}
