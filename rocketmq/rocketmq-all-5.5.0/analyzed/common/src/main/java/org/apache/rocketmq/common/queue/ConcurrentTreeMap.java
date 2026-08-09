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

package org.apache.rocketmq.common.queue;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 线程安全的 TreeMap 包装：结合 {@link RoundQueue} 限制可跟踪键的数量。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class ConcurrentTreeMap<K, V> {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);
    /** 保护 tree 与 roundQueue 的可重入锁（公平模式）。 */
    private final ReentrantLock lock;
    /** 有序键值存储。 */
    private TreeMap<K, V> tree;
    /** 最近访问键的环形队列，用于容量控制。 */
    private RoundQueue<K> roundQueue;

    /**
     * @param capacity roundQueue 容量上限
     * @param comparator 键比较器
     */
        tree = new TreeMap<>(comparator);
        roundQueue = new RoundQueue<>(capacity);
        lock = new ReentrantLock(true);
    }

    /** 移除并返回最小键对应的 Map.Entry。 */
    public Map.Entry<K, V> pollFirstEntry() {
        lock.lock();
        try {
            return tree.pollFirstEntry();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 若 roundQueue 接受该键则尝试 putIfAbsent，并返回已存在或新写入的值；
     * 键已在 roundQueue 中则仅返回 tree 中现有值。
     */
        lock.lock();
        try {
            if (roundQueue.put(key)) {
                V exist = tree.get(key);
                if (null == exist) {
                    tree.put(key, value);
                    exist = value;
                }
                log.warn("putIfAbsentAndRetExsit success. " + key);
                return exist;
            } else {
                V exist = tree.get(key);
                return exist;
            }
        } finally {
            lock.unlock();
        }
    }

}
