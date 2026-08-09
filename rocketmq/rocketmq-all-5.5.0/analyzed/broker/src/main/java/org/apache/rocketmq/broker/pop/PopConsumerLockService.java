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
package org.apache.rocketmq.broker.pop;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.rocketmq.common.KeyBuilder;
import org.apache.rocketmq.common.PopAckConstants;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.utils.ConcurrentHashMapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * POP 消费组-Topic 粒度互斥锁：防止同一 group@topic 并发 POP 导致状态错乱。
 * 锁超时后由 {@link #removeTimeout()} 清理。
 */
public class PopConsumerLockService {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.ROCKETMQ_POP_LOGGER_NAME);

    private final long timeout;
    private final ConcurrentMap<String /* groupId@topicId */, TimedLock> lockTable;

    /** @param timeout 锁超时时间（毫秒），超时视为可重入 POP */
    public PopConsumerLockService(long timeout) {
        this.timeout = timeout;
        this.lockTable = new ConcurrentHashMap<>();
    }

    /** 按 groupId@topicId 复合键尝试加锁。 */
    public boolean tryLock(String key) {
        return Objects.requireNonNull(ConcurrentHashMapUtils.computeIfAbsent(lockTable,
            key, s -> new TimedLock())).tryLock();
    }

    /** 按消费组与 topic 尝试加锁。 */
    public boolean tryLock(String groupId, String topicId) {
        return tryLock(groupId + PopAckConstants.SPLIT + topicId);
    }

    /** 释放指定复合键上的锁。 */
    public void unlock(String key) {
        TimedLock lock = lockTable.get(key);
        if (lock != null) {
            lock.unlock();
        }
    }

    /** 释放指定消费组与 topic 上的锁。 */
    public void unlock(String groupId, String topicId) {
        unlock(groupId + PopAckConstants.SPLIT + topicId);
    }

    // 重试 topic 需解析为原始 group/topic 再判断锁是否超时
    /** 判断锁是否已超时（不存在或持锁时间超过 timeout）。 */
    public boolean isLockTimeout(String groupId, String topicId) {
        topicId = KeyBuilder.parseNormalTopic(topicId, groupId);
        TimedLock lock = lockTable.get(groupId + PopAckConstants.SPLIT + topicId);
        return lock == null || System.currentTimeMillis() - lock.getLockTime() > timeout;
    }

    /** 扫描 lockTable，移除已超时的锁条目。 */
    public void removeTimeout() {
        Iterator<Map.Entry<String, TimedLock>> iterator = lockTable.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TimedLock> entry = iterator.next();
            if (System.currentTimeMillis() - entry.getValue().getLockTime() > timeout) {
                log.info("PopConsumerLockService remove timeout lock, " +
                    "key={}, locked={}", entry.getKey(), entry.getValue().lock.get());
                iterator.remove();
            }
        }
    }

    /** 带时间戳的可重入互斥锁，记录最近一次成功加锁时刻。 */
    static class TimedLock {
        private volatile long lockTime;
        private final AtomicBoolean lock;

        public TimedLock() {
            this.lockTime = System.currentTimeMillis();
            this.lock = new AtomicBoolean(false);
        }

        public boolean tryLock() {
            if (lock.compareAndSet(false, true)) {
                this.lockTime = System.currentTimeMillis();
                return true;
            }
            return false;
        }

        public void unlock() {
            lock.set(false);
        }

        public long getLockTime() {
            return lockTime;
        }
    }
}