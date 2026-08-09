/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.proxy.service.transaction;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.proxy.config.ConfigurationManager;

/**
 * 事务消息数据管理器：按生产者组与事务 ID 缓存 {@link TransactionData}，
 * 并定时清理过期条目。
 */
public class TransactionDataManager implements StartAndShutdown {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);

    /** 当前缓存中事务数据的最大过期时间戳。 */
    protected final AtomicLong maxTransactionDataExpireTime = new AtomicLong(System.currentTimeMillis());
    /** 键为 producerGroup@transactionId，值为按过期时间排序的事务数据集合。 */
    protected final Map<String /* producerGroup@transactionId */, NavigableSet<TransactionData>> transactionIdDataMap = new ConcurrentHashMap<>();
    /** 后台扫描线程，周期性清理过期事务数据。 */
    protected final TransactionDataCleaner transactionDataCleaner = new TransactionDataCleaner();

    /** 拼接 producerGroup 与 transactionId 作为缓存键。 */
    protected String buildKey(String producerGroup, String transactionId) {
        return producerGroup + "@" + transactionId;
    }

    /** 追加事务数据；超出上限时移除最早条目。 */
    public void addTransactionData(String producerGroup, String transactionId, TransactionData transactionData) {
        this.transactionIdDataMap.compute(buildKey(producerGroup, transactionId), (key, dataSet) -> {
            if (dataSet == null) {
                dataSet = new ConcurrentSkipListSet<>();
            }
            dataSet.add(transactionData);
            if (dataSet.size() > ConfigurationManager.getProxyConfig().getTransactionDataMaxNum()) {
                dataSet.pollFirst();
            }
            return dataSet;
        });
    }

    /** 弹出最新且未过期的事务数据，跳过已过期项。 */
    public TransactionData pollNoExpireTransactionData(String producerGroup, String transactionId) {
        AtomicReference<TransactionData> res = new AtomicReference<>();
        long currTimestamp = System.currentTimeMillis();
        this.transactionIdDataMap.computeIfPresent(buildKey(producerGroup, transactionId), (key, dataSet) -> {
            TransactionData data = dataSet.pollLast();
            while (data != null && data.getExpireTime() < currTimestamp) {
                data = dataSet.pollLast();
            }
            if (data != null) {
                res.set(data);
            }
            if (dataSet.isEmpty()) {
                return null;
            }
            return dataSet;
        });
        return res.get();
    }

    /** 移除指定事务数据；集合为空时删除键。 */
    public void removeTransactionData(String producerGroup, String transactionId, TransactionData transactionData) {
        this.transactionIdDataMap.computeIfPresent(buildKey(producerGroup, transactionId), (key, dataSet) -> {
            dataSet.remove(transactionData);
            if (dataSet.isEmpty()) {
                return null;
            }
            return dataSet;
        });
    }

    /** 遍历全部键，删除过期事务数据并更新最大过期时间。 */
    protected void cleanExpireTransactionData() {
        long currTimestamp = System.currentTimeMillis();
        Set<String> transactionIdSet = this.transactionIdDataMap.keySet();
        for (String transactionId : transactionIdSet) {
            this.transactionIdDataMap.computeIfPresent(transactionId, (transactionIdKey, dataSet) -> {
                Iterator<TransactionData> iterator = dataSet.iterator();
                while (iterator.hasNext()) {
                    try {
                        TransactionData data = iterator.next();
                        if (data.getExpireTime() < currTimestamp) {
                            iterator.remove();
                        } else {
                            break;
                        }
                    } catch (NoSuchElementException ignore) {
                        break;
                    }
                }
                if (dataSet.isEmpty()) {
                    return null;
                }
                try {
                    TransactionData maxData = dataSet.last();
                    maxTransactionDataExpireTime.set(Math.max(maxTransactionDataExpireTime.get(), maxData.getExpireTime()));
                } catch (NoSuchElementException ignore) {
                }
                return dataSet;
            });
        }
    }

    /** 定时触发 {@link #cleanExpireTransactionData()} 的后台服务线程。 */
    protected class TransactionDataCleaner extends ServiceThread {

        @Override
        public String getServiceName() {
            return "TransactionDataCleaner";
        }

        @Override
        public void run() {
            log.info(this.getServiceName() + " service started");
            while (!this.isStopped()) {
                this.waitForRunning(ConfigurationManager.getProxyConfig().getTransactionDataExpireScanPeriodMillis());
            }
            log.info(this.getServiceName() + " service stopped");
        }

        @Override
        /** 等待周期结束后执行过期数据清理。 */
        protected void onWaitEnd() {
            cleanExpireTransactionData();
        }
    }

    /** 关闭前同步清理并等待缓存内数据自然过期。 */
    protected void waitTransactionDataClear() throws InterruptedException {
        this.cleanExpireTransactionData();
        long waitMs = Math.max(this.maxTransactionDataExpireTime.get() - System.currentTimeMillis(), 0);
        waitMs = Math.min(waitMs, ConfigurationManager.getProxyConfig().getTransactionDataMaxWaitClearMillis());

        if (waitMs > 0) {
            TimeUnit.MILLISECONDS.sleep(waitMs);
        }
    }

    @Override
    /** 停止清理线程并等待事务数据清空。 */
    public void shutdown() throws Exception {
        this.transactionDataCleaner.shutdown();
        this.waitTransactionDataClear();
    }

    @Override
    /** 启动事务数据过期扫描线程。 */
    public void start() throws Exception {
        this.transactionDataCleaner.start();
    }
}
