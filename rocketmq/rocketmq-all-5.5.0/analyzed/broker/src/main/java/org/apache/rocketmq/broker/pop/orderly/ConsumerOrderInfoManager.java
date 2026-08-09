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
package org.apache.rocketmq.broker.pop.orderly;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.common.OrderedConsumptionLevel;
import org.apache.rocketmq.store.GetMessageResult;

/**
 * 顺序消费控制器顶层接口：封装 POP 顺序消费的完整生命周期管理，
 * 支持不同并发策略实现。
 * <p>
 * 设计目标：
 * 1. 队列级顺序消费（现有实现）
 * 2. 消息组级顺序消费（提升并发度）
 * 3. 可插拔的自定义顺序消费策略
 * </p>
 */
public interface ConsumerOrderInfoManager {

    /**
     * POP 成功后更新消息接收状态，记录各 offset 的可见性信息并构建 orderInfo。
     *
     * @param attemptId          区分不同 POP 请求
     * @param isRetry            是否为重试 topic
     * @param topic              Topic 名
     * @param group              消费组名
     * @param queueId            队列 ID
     * @param popTime            POP 时刻
     * @param invisibleTime      消息不可见时长
     * @param msgQueueOffsetList 消息队列 offset 列表
     * @param orderInfoBuilder   构建 orderInfo 的 StringBuilder
     * @param getMessageResult   返回给客户端的消息结果
     */
    void update(String attemptId, boolean isRetry, String topic, String group, int queueId,
        long popTime, long invisibleTime, List<Long> msgQueueOffsetList,
        StringBuilder orderInfoBuilder, GetMessageResult getMessageResult);

    /**
     * 检查当前 POP 是否应被阻塞，以保证顺序 topic 的严格有序消费。
     *
     * @param attemptId     请求 attemptId
     * @param topic         Topic 名
     * @param group         消费组名
     * @param queueId       队列 ID
     * @param invisibleTime 不可见时长
     * @return true 表示需阻塞，false 表示可继续 POP
     */
    boolean checkBlock(String attemptId, String topic, String group, int queueId, long invisibleTime);

    /**
     * 删除指定 topic@group 的顺序消费状态（topic 删除时调用）。
     *
     * @param topic Topic 名
     * @param group 消费组名
     */
    void remove(String topic, String group);

    /** 返回当前维护的 orderInfo 条目数。 */
    int getOrderInfoCount();

    /**
     * ACK 时提交消费进度并计算下一可消费 offset。
     *
     * @param topic       Topic 名
     * @param group       消费组名
     * @param queueId     队列 ID
     * @param queueOffset 消息队列 offset
     * @param popTime     POP 时刻，用于校验
     * @return -1 无效；-2 无需提交；>=0 应提交的 offset
     */
    long commitAndNext(String topic, String group, int queueId, long queueOffset, long popTime);

    /**
     * 更新消息下次可见时间（延迟重消费场景）。
     *
     * @param topic           Topic 名
     * @param group           消费组名
     * @param queueId         队列 ID
     * @param queueOffset     消息 offset
     * @param popTime         POP 时刻
     * @param nextVisibleTime 下次可见时间戳
     */
    void updateNextVisibleTime(String topic, String group, int queueId, long queueOffset,
        long popTime, long nextVisibleTime);

    /**
     * 清除指定队列的阻塞状态（重平衡或队列迁移时调用）。
     *
     * @param topic   Topic 名
     * @param group   消费组名
     * @param queueId 队列 ID
     */
    void clearBlock(String topic, String group, int queueId);

    /**
     * 返回顺序消费粒度（QUEUE、MESSAGE_GROUP 等）。
     *
     * @return 顺序消费级别枚举
     */
    OrderedConsumptionLevel getOrderedConsumptionLevel();

    /** 启动控制器，初始化定时器、线程池等资源。 */
    void start();

    /** 关闭控制器并释放资源。 */
    void shutdown();

    /** 持久化顺序消费状态到磁盘。 */
    void persist();

    boolean load();

    /** 从缓存中获取可立即 POP 的消息结果。 */
    CompletableFuture<GetMessageResult> getAvailableMessageResult(String attemptId, long popTime, long invisibleTime,
        String groupId,
        String topicId, int queueId, int batchSize, StringBuilder orderCountInfoBuilder);
}
