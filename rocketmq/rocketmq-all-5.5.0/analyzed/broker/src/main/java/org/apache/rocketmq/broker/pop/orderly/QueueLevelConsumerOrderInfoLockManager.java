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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.lite.LiteUtil;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 队列级顺序消费锁释放通知管理器：在 lockFreeTimestamp 到达时
 * 唤醒长轮询或 Lite 事件分发，避免顺序 POP 长时间空等。
 */
public class QueueLevelConsumerOrderInfoLockManager {
    private static final Logger POP_LOGGER = LoggerFactory.getLogger(LoggerName.ROCKETMQ_POP_LOGGER_NAME);
    private ConsumerOrderInfoManager consumerOrderInfoManager;

    private final BrokerController brokerController;
    private final Map<Key, Timeout> timeoutMap = new ConcurrentHashMap<>();
    private final Timer timer;
    private static final int TIMER_TICK_MS = 100;

    public QueueLevelConsumerOrderInfoLockManager(BrokerController brokerController) {
        this.brokerController = brokerController;
        this.timer = new HashedWheelTimer(
            new ThreadFactoryImpl("ConsumerOrderInfoLockManager_"),
            TIMER_TICK_MS, TimeUnit.MILLISECONDS);
    }

    /** 从磁盘恢复 orderInfo 后，为尚未到期的 lockFreeTimestamp 重建定时任务。 */
    /** 遍历持久化表，为每个未到期的 lockFreeTimestamp 注册 HashedWheel 定时器。 */
    public void recover(Map<String/* topic@group*/, ConcurrentHashMap<Integer/*queueId*/, QueueLevelConsumerManager.OrderInfo>> table) {
        if (!this.brokerController.getBrokerConfig().isEnableNotifyAfterPopOrderLockRelease()) {
            return;
        }
        for (Map.Entry<String, ConcurrentHashMap<Integer, QueueLevelConsumerManager.OrderInfo>> entry : table.entrySet()) {
            String topicAtGroup = entry.getKey();
            ConcurrentHashMap<Integer/*queueId*/, QueueLevelConsumerManager.OrderInfo> qs = entry.getValue();
            String[] arrays = QueueLevelConsumerManager.decodeKey(topicAtGroup);
            if (arrays.length != 2) {
                continue;
            }
            String topic = arrays[0];
            String group = arrays[1];
            for (Map.Entry<Integer, QueueLevelConsumerManager.OrderInfo> qsEntry : qs.entrySet()) {
                Long lockFreeTimestamp = qsEntry.getValue().getLockFreeTimestamp();
                if (lockFreeTimestamp == null || lockFreeTimestamp <= System.currentTimeMillis()) {
                    continue;
                }
                this.updateLockFreeTimestamp(topic, group, qsEntry.getKey(), lockFreeTimestamp);
            }
        }
    }

    /** 从 OrderInfo 提取 lockFreeTimestamp 并更新定时任务。 */
    public void updateLockFreeTimestamp(String topic, String group, int queueId, QueueLevelConsumerManager.OrderInfo orderInfo) {
        this.updateLockFreeTimestamp(topic, group, queueId, orderInfo.getLockFreeTimestamp());
    }

    /** 注册/刷新 lockFreeTimestamp 到期后的通知定时器，新任务会取消旧任务。 */
    public void updateLockFreeTimestamp(String topic, String group, int queueId, Long lockFreeTimestamp) {
        if (!this.brokerController.getBrokerConfig().isEnableNotifyAfterPopOrderLockRelease()) {
            return;
        }
        if (lockFreeTimestamp == null) {
            return;
        }
        try {
            this.timeoutMap.compute(new Key(topic, group, queueId), (key, oldTimeout) -> {
                try {
                    long delay = lockFreeTimestamp - System.currentTimeMillis();
                    Timeout newTimeout = this.timer.newTimeout(new NotifyLockFreeTimerTask(key), delay, TimeUnit.MILLISECONDS);
                    if (oldTimeout != null) {
                        // cancel prev timerTask
                        oldTimeout.cancel();
                    }
                    return newTimeout;
                } catch (Exception e) {
                    POP_LOGGER.warn("add timeout task failed. key:{}, lockFreeTimestamp:{}", key, lockFreeTimestamp, e);
                    return oldTimeout;
                }
            });
        } catch (Exception e) {
            POP_LOGGER.error("unexpect error when updateLockFreeTimestamp. topic:{}, group:{}, queueId:{}, lockFreeTimestamp:{}",
                topic, group, queueId, lockFreeTimestamp, e);
        }
    }

    /** 锁释放到期：Lite topic 走事件分发，普通 topic 唤醒 POP 长轮询。 */
    protected void notifyLockIsFree(Key key) {
        try {
            if (LiteUtil.isLiteTopicQueue(key.topic)) {
                this.brokerController.getLiteEventDispatcher().dispatch(key.group, key.topic, key.queueId, -1, -1);
                return;
            }
            this.brokerController.getPopMessageProcessor().notifyLongPollingRequestIfNeed(key.topic, key.group, key.queueId);
        } catch (Exception e) {
            POP_LOGGER.error("unexpect error when notifyLockIsFree. key:{}", key, e);
        }
    }

    /** 停止 HashedWheelTimer。 */
    public void shutdown() {
        this.timer.stop();
    }

    @VisibleForTesting
    protected Map<Key, Timeout> getTimeoutMap() {
        return timeoutMap;
    }

    private class NotifyLockFreeTimerTask implements TimerTask {

        private final Key key;

        private NotifyLockFreeTimerTask(Key key) {
            this.key = key;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled() || !brokerController.getBrokerConfig().isEnableNotifyAfterPopOrderLockRelease()) {
                return;
            }
            notifyLockIsFree(key);
            timeoutMap.computeIfPresent(key, (key1, curTimeout) -> {
                if (curTimeout == timeout) {
                    // remove from map
                    return null;
                }
                return curTimeout;
            });
        }
    }

    private static class Key {
        private final String topic;
        private final String group;
        private final int queueId;

        public Key(String topic, String group, int queueId) {
            this.topic = topic;
            this.group = group;
            this.queueId = queueId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return queueId == key.queueId && Objects.equal(topic, key.topic) && Objects.equal(group, key.group);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(topic, group, queueId);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("topic", topic)
                .add("group", group)
                .add("queueId", queueId)
                .toString();
        }
    }
}
