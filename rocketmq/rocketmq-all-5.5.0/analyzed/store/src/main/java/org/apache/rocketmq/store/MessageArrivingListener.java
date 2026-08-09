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

package org.apache.rocketmq.store;

import java.util.Map;

/**
 * 消息到达监听器：ConsumeQueue 写入新索引时通知上层（如长轮询挂起服务）。
 */
public interface MessageArrivingListener {

    /**
     * 消费队列有新消息到达时的回调通知。
     * @param topic Topic 名称
     * @param queueId 消费队列 ID
     * @param logicOffset 消费队列逻辑 offset
     * @param tagsCode 消息 Tag 哈希码
     * @param msgStoreTime 消息存储时间戳
     * @param filterBitMap 消息布隆过滤器位图
     * @param properties 消息属性
     */
    void arriving(String topic, int queueId, long logicOffset, long tagsCode,
        long msgStoreTime, byte[] filterBitMap, Map<String, String> properties);
}
