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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Strings;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.broker.BrokerPathConfigHelper;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.protocol.RemotingSerializable;

/**
 * Lite/LMQ 消费位点管理器：LMQ group 使用 topic@group 单键存储位点，
 * 与普通多队列位点表分离持久化。
 */
public class LmqConsumerOffsetManager extends ConsumerOffsetManager {
    /** LMQ 位点表：key 为 topic@group，value 为队列 0 的 committed offset。 */
    private ConcurrentHashMap<String, Long> lmqOffsetTable = new ConcurrentHashMap<>(512);

    public LmqConsumerOffsetManager() {

    }

    public LmqConsumerOffsetManager(BrokerController brokerController) {
        super(brokerController);
    }

    @Override
    public long queryOffset(final String group, final String topic, final int queueId) {
        if (!MixAll.isLmq(group)) {
            return super.queryOffset(group, topic, queueId);
        }
        // LMQ 位点 key：topic@group
        String key = topic + TOPIC_GROUP_SEPARATOR + group;
        Long offset = lmqOffsetTable.get(key);
        if (offset != null) {
            return offset;
        }
        return -1;
    }

    @Override
    public Map<Integer, Long> queryOffset(final String group, final String topic) {
        if (!MixAll.isLmq(group)) {
            return super.queryOffset(group, topic);
        }
        Map<Integer, Long> map = new HashMap<>();
        // topic@group
        String key = topic + TOPIC_GROUP_SEPARATOR + group;
        Long offset = lmqOffsetTable.get(key);
        if (offset != null) {
            map.put(0, offset);
        }
        return map;
    }

    @Override
    public void commitOffset(final String clientHost, final String group, final String topic, final int queueId,
        final long offset) {
        if (!MixAll.isLmq(group)) {
            super.commitOffset(clientHost, group, topic, queueId, offset);
            return;
        }
        // topic@group
        String key = topic + TOPIC_GROUP_SEPARATOR + group;
        lmqOffsetTable.put(key, offset);
    }

    @Override
    public String encode() {
        return this.encode(false);
    }

    @Override
    public String configFilePath() {
        return BrokerPathConfigHelper.getLmqConsumerOffsetPath(brokerController.getMessageStoreConfig().getStorePathRootDir());
    }

    @Override
    public void decode(String jsonString) {
        if (jsonString != null) {
            LmqConsumerOffsetManager obj = RemotingSerializable.fromJson(jsonString, LmqConsumerOffsetManager.class);
            if (obj != null) {
                super.setOffsetTable(obj.getOffsetTable());
                this.lmqOffsetTable = obj.lmqOffsetTable;
            }
        }
    }

    @Override
    public String encode(final boolean prettyFormat) {
        return RemotingSerializable.toJson(this, prettyFormat);
    }

    public ConcurrentHashMap<String, Long> getLmqOffsetTable() {
        return lmqOffsetTable;
    }

    public void setLmqOffsetTable(ConcurrentHashMap<String, Long> lmqOffsetTable) {
        this.lmqOffsetTable = lmqOffsetTable;
    }

    @Override
    /** 删除指定 LMQ group 的全部 topic@group 位点条目。 */
    public void removeOffset(String group) {
        if (!MixAll.isLmq(group)) {
            super.removeOffset(group);
            return;
        }
        Iterator<Map.Entry<String, Long>> it = this.lmqOffsetTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> next = it.next();
            String topicAtGroup = next.getKey();
            if (topicAtGroup.contains(group)) {
                String[] arrays = topicAtGroup.split(TOPIC_GROUP_SEPARATOR);
                if (arrays.length == 2 && group.equals(arrays[1])) {
                    it.remove();
                    removeConsumerOffset(topicAtGroup);
                    LOG.warn("clean lmq group offset {}", topicAtGroup);
                }
            }
        }
    }

    @Override
    /** 为 LMQ topic/group 分配重置位点，同步更新 lmqOffsetTable 与 resetOffsetTable。 */
    public void assignResetOffset(String topic, String group, int queueId, long offset) {
        if (Strings.isNullOrEmpty(topic) || Strings.isNullOrEmpty(group) || queueId < 0 || offset < 0) {
            LOG.warn("Illegal arguments when assigning reset offset. Topic={}, group={}, queueId={}, offset={}",
                    topic, group, queueId, offset);
            return;
        }
        if (!MixAll.isLmq(topic) || !MixAll.isLmq(group)) {
            super.assignResetOffset(topic, group, queueId, offset);
            return;
        }

        String key = topic + TOPIC_GROUP_SEPARATOR + group;
        ConcurrentMap<Integer, Long> map = resetOffsetTable.get(key);
        if (null == map) {
            map = new ConcurrentHashMap<>();
            ConcurrentMap<Integer, Long> previous = resetOffsetTable.putIfAbsent(key, map);
            if (null != previous) {
                map = previous;
            }
        }
        map.put(queueId, offset);

        lmqOffsetTable.computeIfPresent(key, (k, oldValue) -> offset);
    }
}
