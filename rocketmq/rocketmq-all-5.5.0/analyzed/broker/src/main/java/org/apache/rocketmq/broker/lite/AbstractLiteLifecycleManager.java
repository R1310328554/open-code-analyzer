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

import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.lite.LiteUtil;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.MessageStore;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.apache.rocketmq.broker.offset.ConsumerOffsetManager.TOPIC_GROUP_SEPARATOR;

/**
 * Lite 主题生命周期管理抽象基类：负责 lite topic 的 TTL 过期清理与订阅有效性判定。
 * 子类分别基于文件 ConsumeQueue 与 RocksDB ConsumeQueue 实现具体扫描逻辑。
 */
public abstract class AbstractLiteLifecycleManager extends ServiceThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.ROCKETMQ_POP_LITE_LOGGER_NAME);
    /** maxOffset 异常时连续扫描超过该次数才判定过期，避免并发读写 transient 状态。 */
    private static final int MAX_INVALID_SCAN_COUNT = 5;

    protected final BrokerController brokerController;
    protected final String brokerName;
    protected final LiteSharding liteSharding;
    protected MessageStore messageStore;
    protected Map<String, Integer> ttlMap = Collections.emptyMap();
    protected Map<String, Set<String>> subscriberGroupMap = Collections.emptyMap();
    protected Map<String, Integer> invalidScanCountMap = new ConcurrentHashMap<>();

    /** 绑定 Broker 控制器与 lite 分片策略。 */
    public AbstractLiteLifecycleManager(BrokerController brokerController, LiteSharding liteSharding) {
        this.brokerController = brokerController;
        this.brokerName = brokerController.getBrokerConfig().getBrokerName();
        this.liteSharding = liteSharding;
    }

    /** 初始化 MessageStore 引用，启动前必须调用。 */
    public boolean init() {
        this.messageStore = brokerController.getMessageStore();
        assert messageStore != null;
        return true;
    }

    /**
     * 返回指定 LMQ 队列下一个可写 slot 索引（从 0 起算）。
     */
    public abstract long getMaxOffsetInQueue(String lmqName);

    /**
     * 收集已过期的 lite LMQ，附带父 topic 名；返回 (parentTopic, lmqName) 列表，非 null。
     */
    public abstract List<Pair<String, String>> collectExpiredLiteTopic();

    /**
     * 按父 topic 收集其下所有 LMQ 名称；返回列表非 null。
     */
    public abstract List<String> collectByParentTopic(String parentTopic);

    /**
     * 高频遍历 lite topic；Triple 为 (lmqName, maxOffset, lastStoreTimestamp)，后者暂为 null。
     * 回调返回 true 继续，false 中断。
     *
     * @param function 遍历回调
     */
    public abstract void forEachLiteTopic(Function<Triple<String, Long, Long>, Boolean> function);

    /**
     * 判断给定 LMQ 的订阅是否仍有效：当前 broker 负责该 LMQ，或 MessageStore 中仍有消息。
     */
    public boolean isSubscriptionActive(String parentTopic, String lmqName) {
        return brokerName.equals(liteSharding.shardingByLmqName(parentTopic, lmqName)) || isLmqExist(lmqName);
    }

    /** 统计父 topic 下 lite LMQ 数量；非 lite 类型 topic 返回 0。 */
    public int getLiteTopicCount(String parentTopic) {
        if (!LiteMetadataUtil.isLiteMessageType(parentTopic, brokerController)) {
            return 0;
        }
        return collectByParentTopic(parentTopic).size();
    }

    /** LMQ 队列 maxOffset > 0 即视为存在。 */
    public boolean isLmqExist(String lmqName) {
        return getMaxOffsetInQueue(lmqName) > 0;
    }

    /** 刷新元数据后扫描并删除所有 TTL 过期的 lite LMQ。 */
    public void cleanExpiredLiteTopic() {
        try {
            updateMetadata(); // necessary
            List<Pair<String, String>> lmqToDelete = collectExpiredLiteTopic();
            LOGGER.info("collect expired topic, size:{}", lmqToDelete.size());
            lmqToDelete.forEach(pair -> deleteLmq(pair.getObject1(), pair.getObject2()));
            if (!lmqToDelete.isEmpty()) {
                brokerController.getMessageStore().getQueueStore().flush();
            }
        } catch (Exception e) {
            LOGGER.error("cleanExpiredLiteTopic error", e);
        }
    }

    /** 按父 topic 批量清理其下全部 lite LMQ。 */
    public void cleanByParentTopic(String parentTopic) {
        try {
            if (!LiteMetadataUtil.isLiteMessageType(parentTopic, brokerController)) {
                return;
            }
            updateMetadata(); // necessary
            List<String> lmqToDelete = collectByParentTopic(parentTopic);
            LOGGER.info("clean by parent topic, {}, size:{}", parentTopic, lmqToDelete.size());
            lmqToDelete.forEach(lmqName -> deleteLmq(parentTopic, lmqName));
        } catch (Exception e) {
            LOGGER.error("cleanByParentTopic error", e);
        }
    }

    @Override
    public void run() {
        LOGGER.info("Start checking lite ttl.");
        while (!this.isStopped()) {
            long runningTime = System.currentTimeMillis() - brokerController.getShouldStartTime();
            if (runningTime < brokerController.getBrokerConfig().getMinLiteTTl()) { // base protection for restart
                this.waitForRunning(20 * 1000);
                continue;
            }

            cleanExpiredLiteTopic();
            long checkInterval = brokerController.getBrokerConfig().getLiteTtlCheckInterval();
            this.waitForRunning(checkInterval);
        }
        LOGGER.info("End checking lite ttl.");
    }

    /** 从 Topic/Subscription 配置刷新 TTL 映射与订阅 group 映射。 */
    public void updateMetadata() {
        ttlMap = LiteMetadataUtil.getTopicTtlMap(brokerController);
        subscriberGroupMap = LiteMetadataUtil.getSubscriberGroupMap(brokerController);
    }

    /** 综合 maxOffset、最后写入时间与 topic TTL 判定 LMQ 是否过期。 */
    public boolean isLiteTopicExpired(String parentTopic, String lmqName, long maxOffset) {
        if (!LiteUtil.isLiteTopicQueue(lmqName)) {
            return false;
        }
        if (maxOffset <= 0) {
            int invalidCount = invalidScanCountMap.getOrDefault(lmqName, 0) + 1;
            LOGGER.warn("unexpected condition, max offset <= 0, {}, {}, scanCount:{}", lmqName, maxOffset, invalidCount);
            if (invalidCount > MAX_INVALID_SCAN_COUNT) { // check more times in case of  concurrent issue
                invalidScanCountMap.remove(lmqName);
                return true;
            }
            invalidScanCountMap.put(lmqName, invalidCount);
            return false;
        } else {
            invalidScanCountMap.remove(lmqName);
        }
        long latestStoreTime =
            this.brokerController.getMessageStore().getMessageStoreTimeStamp(lmqName, 0, maxOffset - 1);
        long inactiveTime = System.currentTimeMillis() - latestStoreTime;
        if (inactiveTime < brokerController.getBrokerConfig().getMinLiteTTl()) {
            return false;
        }
        Integer minutes = ttlMap.get(parentTopic);
        if (null == minutes) {
            LOGGER.warn("unexpected condition, topic ttl not found. {}", lmqName);
            return false;
        }
        if (minutes <= 0) {
            return false;
        }
        if (hasConsumerLag(lmqName, maxOffset, latestStoreTime, parentTopic)) {
            return false;
        }
        return inactiveTime > minutes * 60 * 1000;
    }

    /** 删除 LMQ：清理消费位点、订阅注册及 MessageStore 中的 topic 数据。 */
    public void deleteLmq(String parentTopic, String lmqName) {
        try {
            Set<String> groups = subscriberGroupMap.getOrDefault(parentTopic, Collections.emptySet());
            groups.forEach(group -> {
                String topicAtGroup = lmqName + TOPIC_GROUP_SEPARATOR + group;
                brokerController.getConsumerOffsetManager().getOffsetTable().remove(topicAtGroup);
                brokerController.getConsumerOffsetManager().removeConsumerOffset(topicAtGroup); // no iteration
                brokerController.getPopLiteMessageProcessor().getConsumerOrderInfoManager().remove(lmqName, group);
            });
            brokerController.getMessageStore().deleteTopics(Sets.newHashSet(lmqName));
            boolean sharding = brokerName.equals(liteSharding.shardingByLmqName(parentTopic, lmqName));
            brokerController.getLiteSubscriptionRegistry().cleanSubscription(lmqName, false);
            brokerController.getConsumerOffsetManager().getPullOffsetTable().remove(
                lmqName + TOPIC_GROUP_SEPARATOR + MixAll.TOOLS_CONSUMER_GROUP);
            LOGGER.info("delete lmq finish. {}, sharding:{}", lmqName, sharding);
        } catch (Exception e) {
            LOGGER.error("delete lmq error. {}", lmqName, e);
        }
    }

    /**
     * 当前未做消费滞后检查，TTL 过期仅依据消息发送静默时长。
     */
    public boolean hasConsumerLag(String lmqName, long maxOffset, long latestStoreTime, String parentTopic) {
        return false;
    }
}
