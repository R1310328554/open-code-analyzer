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

package org.apache.rocketmq.broker.lite;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.lite.LiteUtil;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.queue.ConsumeQueueInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * 基于内存 ConsumeQueue 表的 lite 生命周期管理器：扫描、过期判定与遍历均直接访问 MessageStore 队列索引。
 */
public class LiteLifecycleManager extends AbstractLiteLifecycleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.ROCKETMQ_POP_LITE_LOGGER_NAME);

    /** 绑定 Broker 与 lite 分片组件。 */
    public LiteLifecycleManager(BrokerController brokerController, LiteSharding liteSharding) {
        super(brokerController, liteSharding);
    }

    @Override
    /** 从 ConsumeQueue 表读取 LMQ queueId=0 的 maxOffset。 */
    public long getMaxOffsetInQueue(String lmqName) {
        ConsumeQueueInterface consumeQueue = messageStore.getConsumeQueue(lmqName, 0);
        return consumeQueue != null ? consumeQueue.getMaxOffsetInQueue() : 0L;
    }

    @Override
    /** 遍历 ConsumeQueue 表，筛选属于 parentTopic 的 LMQ 名。 */
    public List<String> collectByParentTopic(String parentTopic) {
        if (StringUtils.isEmpty(parentTopic)) {
            return Collections.emptyList();
        }
        List<String> resultList = new ArrayList<>();
        Iterator<Map.Entry<String, ConcurrentMap<Integer, ConsumeQueueInterface>>> iterator =
            messageStore.getQueueStore().getConsumeQueueTable().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ConcurrentMap<Integer, ConsumeQueueInterface>> entry = iterator.next();
            if (LiteUtil.belongsTo(entry.getKey(), parentTopic)) {
                resultList.add(entry.getKey());
            }
        }
        return resultList;
    }

    @Override
    /** 全表扫描 ConsumeQueue，收集 TTL 已过期的 (parentTopic, lmqName) 对。 */
    public List<Pair<String, String>> collectExpiredLiteTopic() {
        List<Pair<String, String>> lmqToDelete = new ArrayList<>();
        Iterator<Map.Entry<String, ConcurrentMap<Integer, ConsumeQueueInterface>>> iterator =
            messageStore.getQueueStore().getConsumeQueueTable().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ConcurrentMap<Integer, ConsumeQueueInterface>> entry = iterator.next();
            String lmqName =  entry.getKey();
            String parentTopic = LiteUtil.getParentTopic(lmqName);
            if (null == parentTopic) {
                continue;
            }
            Map<Integer, ConsumeQueueInterface> map = entry.getValue();
            if (map.size() != 1 || null == map.get(0)) {
                LOGGER.warn("unexpected lmq count. {}", lmqName);
                continue;
            }
            if (isLiteTopicExpired(parentTopic, entry.getKey(), map.get(0).getMaxOffsetInQueue())) {
                lmqToDelete.add(new Pair<>(parentTopic, lmqName));
            }
        }
        return lmqToDelete;
    }

    @Override
    /** 遍历所有 lite LMQ 并回调 (lmqName, maxOffset, null)。 */
    public void forEachLiteTopic(Function<Triple<String, Long, Long>, Boolean> function) {
        Iterator<Map.Entry<String, ConcurrentMap<Integer, ConsumeQueueInterface>>> iterator =
            messageStore.getQueueStore().getConsumeQueueTable().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ConcurrentMap<Integer, ConsumeQueueInterface>> entry = iterator.next();
            if (!LiteUtil.isLiteTopicQueue(entry.getKey())) {
                continue;
            }
            ConsumeQueueInterface consumeQueueInterface = entry.getValue().get(0);
            if (null == consumeQueueInterface) {
                continue;
            }
            Triple<String, Long, Long> triple = Triple.of(entry.getKey(), consumeQueueInterface.getMaxOffsetInQueue(), null);
            try {
                if (!function.apply(triple)) {
                    break;
                }
            } catch (Throwable e) {
                LOGGER.error("forEachLiteTopic error. {}", entry.getKey(), e);
                break;
            }
        }
    }
}
