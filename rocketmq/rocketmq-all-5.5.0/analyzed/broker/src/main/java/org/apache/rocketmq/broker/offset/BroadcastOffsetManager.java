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
package org.apache.rocketmq.broker.offset;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.BrokerConfig;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.store.exception.ConsumeQueueException;

/**
 * 广播消费位点管理器：按 clientId 维护各队列拉取位点，并汇总提交 group@broadcast 位点。
 * 同时支持 Proxy 与 Broker 直连切换时的初始位点协商。
 */
public class BroadcastOffsetManager extends ServiceThread {
    private static final String TOPIC_GROUP_SEPARATOR = "@";
    private final BrokerController brokerController;
    private final BrokerConfig brokerConfig;

    /** key 为 topic@groupId，value 为该组下所有 client 各队列拉取位点。 */
    protected final ConcurrentHashMap<String /* topic@groupId */, BroadcastOffsetData> offsetStoreMap =
        new ConcurrentHashMap<>();

    public BroadcastOffsetManager(BrokerController brokerController) {
        this.brokerController = brokerController;
        this.brokerConfig = brokerController.getBrokerConfig();
    }

    /** 更新指定 client 在某队列上的广播拉取位点；fromProxy 标记请求是否经 Proxy 转发。 */
    public void updateOffset(String topic, String group, int queueId, long offset, String clientId, boolean fromProxy) {
        BroadcastOffsetData broadcastOffsetData = offsetStoreMap.computeIfAbsent(
            buildKey(topic, group), key -> new BroadcastOffsetData(topic, group));

        broadcastOffsetData.clientOffsetStore.compute(clientId, (clientIdKey, broadcastTimedOffsetStore) -> {
            if (broadcastTimedOffsetStore == null) {
                broadcastTimedOffsetStore = new BroadcastTimedOffsetStore(fromProxy);
            }

            broadcastTimedOffsetStore.timestamp = System.currentTimeMillis();
            broadcastTimedOffsetStore.fromProxy = fromProxy;
            broadcastTimedOffsetStore.offsetStore.updateOffset(queueId, offset, true);
            return broadcastTimedOffsetStore;
        });
    }

    /**
     * 查询是否需要初始化拉取位点，典型场景：
     * 1. Proxy 切 Broker；2. Broker 切 Proxy；3. 首次经 Proxy 拉取。
     *
     * @return -1 表示无需初始化，沿用 Pull 请求头中的 queueOffset
     */
    public Long queryInitOffset(String topic, String groupId, int queueId, String clientId, long requestOffset,
        boolean fromProxy) throws ConsumeQueueException {

        BroadcastOffsetData broadcastOffsetData = offsetStoreMap.get(buildKey(topic, groupId));
        if (broadcastOffsetData == null) {
            if (fromProxy && requestOffset < 0) {
                return getOffset(null, topic, groupId, queueId);
            } else {
                return -1L;
            }
        }

        final AtomicLong offset = new AtomicLong(-1L);
        BroadcastTimedOffsetStore offsetStore = broadcastOffsetData.clientOffsetStore.get(clientId);
        if (offsetStore == null) {
            offsetStore = new BroadcastTimedOffsetStore(fromProxy);
            broadcastOffsetData.clientOffsetStore.put(clientId, offsetStore);
        }

        if (offsetStore.fromProxy && requestOffset < 0) {
            // when from proxy and requestOffset is -1
            // means proxy need a init offset to pull message
            offset.set(getOffset(offsetStore, topic, groupId, queueId));
        } else {
            if (offsetStore.fromProxy != fromProxy) {
                offset.set(getOffset(offsetStore, topic, groupId, queueId));
            }
        }
        return offset.get();
    }

    /** 依次从本地缓存、ConsumerOffsetManager、MessageStore 解析可用起始位点。 */
    private long getOffset(BroadcastTimedOffsetStore offsetStore, String topic, String groupId, int queueId)
        throws ConsumeQueueException {
        long storeOffset = -1;
        if (offsetStore != null) {
            storeOffset = offsetStore.offsetStore.readOffset(queueId);
        }
        if (storeOffset < 0) {
            storeOffset =
                brokerController.getConsumerOffsetManager().queryOffset(broadcastGroupId(groupId), topic, queueId);
        }
        if (storeOffset < 0) {
            if (this.brokerController.getMessageStore().checkInMemByConsumeOffset(topic, queueId, 0, 1)) {
                storeOffset = 0;
            } else {
                storeOffset = brokerController.getMessageStore().getMaxOffsetInQueue(topic, queueId, true);
            }
        }
        return storeOffset;
    }

    /**
     * 定时扫描：1) 清理过期 client 位点；2) 取各队列最小位点并以 group@broadcast 提交。
     */
    protected void scanOffsetData() {
        for (String k : offsetStoreMap.keySet()) {
            BroadcastOffsetData broadcastOffsetData = offsetStoreMap.get(k);
            if (broadcastOffsetData == null) {
                continue;
            }

            Map<Integer, Long> queueMinOffset = new HashMap<>();

            for (String clientId : broadcastOffsetData.clientOffsetStore.keySet()) {
                broadcastOffsetData.clientOffsetStore
                    .computeIfPresent(clientId, (clientIdKey, broadcastTimedOffsetStore) -> {
                        long interval = System.currentTimeMillis() - broadcastTimedOffsetStore.timestamp;
                        boolean clientIsOnline = brokerController.getConsumerManager().findChannel(broadcastOffsetData.group, clientId) != null;
                        if (clientIsOnline || interval < Duration.ofSeconds(brokerConfig.getBroadcastOffsetExpireSecond()).toMillis()) {
                            Set<Integer> queueSet = broadcastTimedOffsetStore.offsetStore.queueList();
                            for (Integer queue : queueSet) {
                                long offset = broadcastTimedOffsetStore.offsetStore.readOffset(queue);
                                offset = Math.min(queueMinOffset.getOrDefault(queue, offset), offset);
                                queueMinOffset.put(queue, offset);
                            }
                        }
                        if (clientIsOnline && interval >= Duration.ofSeconds(brokerConfig.getBroadcastOffsetExpireMaxSecond()).toMillis()) {
                            return null;
                        }
                        if (!clientIsOnline && interval >= Duration.ofSeconds(brokerConfig.getBroadcastOffsetExpireSecond()).toMillis()) {
                            return null;
                        }
                        return broadcastTimedOffsetStore;
                    });
            }

            offsetStoreMap.computeIfPresent(k, (key, broadcastOffsetDataVal) -> {
                if (broadcastOffsetDataVal.clientOffsetStore.isEmpty()) {
                    return null;
                }
                return broadcastOffsetDataVal;
            });

            queueMinOffset.forEach((queueId, offset) ->
                this.brokerController.getConsumerOffsetManager().commitOffset("BroadcastOffset",
                broadcastGroupId(broadcastOffsetData.group), broadcastOffsetData.topic, queueId, offset));
        }
    }

    private String buildKey(String topic, String group) {
        return topic + TOPIC_GROUP_SEPARATOR + group;
    }

    /**
     * @param group 用户消费组
     * @return 用于提交位点的 broadcast 专用 groupId（group@broadcast）
     */
    private static String broadcastGroupId(String group) {
        return group + TOPIC_GROUP_SEPARATOR + "broadcast";
    }

    @Override
    public String getServiceName() {
        return "BroadcastOffsetManager";
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            this.waitForRunning(Duration.ofSeconds(5).toMillis());
        }
    }

    @Override
    protected void onWaitEnd() {
        this.scanOffsetData();
    }

    public static class BroadcastOffsetData {
        private final String topic;
        private final String group;
        private final ConcurrentHashMap<String /* clientId */, BroadcastTimedOffsetStore> clientOffsetStore;

        public BroadcastOffsetData(String topic, String group) {
            this.topic = topic;
            this.group = group;
            this.clientOffsetStore = new ConcurrentHashMap<>();
        }
    }

    public static class BroadcastTimedOffsetStore {

        /** 该 client 位点最后一次更新时间戳。 */
        private volatile long timestamp;

        /** 标记该 client 位点是否由 Proxy 侧更新。 */
        private volatile boolean fromProxy;

        /** 各队列已拉取位点存储。 */
        private final BroadcastOffsetStore offsetStore;

        public BroadcastTimedOffsetStore(boolean fromProxy) {
            this.timestamp = System.currentTimeMillis();
            this.fromProxy = fromProxy;
            this.offsetStore = new BroadcastOffsetStore();
        }
    }
}
