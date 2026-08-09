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

package org.apache.rocketmq.common.stats;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 瞬时统计项集合：按 statsKey 管理多个 {@link MomentStatsItem}，统一调度打印与清理。
 */
public class MomentStatsItemSet {
    private static final Logger COMMERCIAL_LOG = LoggerFactory.getLogger(LoggerName.COMMERCIAL_LOGGER_NAME);
    /** statsKey → 瞬时统计项映射表。 */
    private final ConcurrentMap<String/* key */, MomentStatsItem> statsItemTable =
        new ConcurrentHashMap<>(128);
    private final String statsName;
    private final ScheduledExecutorService scheduledExecutorService;
    private final Logger log;

    /** 构造集合并立即初始化定时打印任务。 */
    public MomentStatsItemSet(String statsName, ScheduledExecutorService scheduledExecutorService, Logger log) {
        this.statsName = statsName;
        this.scheduledExecutorService = scheduledExecutorService;
        this.log = log;
        this.init();
    }

    /** 返回底层统计项表（只读用途）。 */
    public ConcurrentMap<String, MomentStatsItem> getStatsItemTable() {
        return statsItemTable;
    }

    /** 返回统计类别名。 */
    public String getStatsName() {
        return statsName;
    }

    /** 注册每 5 分钟遍历打印全部子项的定时任务。 */
    public void init() {

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    printAtMinutes();
                } catch (Throwable ignored) {
                }
            }
        }, Math.abs(UtilAll.computeNextMinutesTimeMillis() - System.currentTimeMillis()), 1000 * 60 * 5, TimeUnit.MILLISECONDS);
    }

    /** 遍历所有子项执行分钟级打印。 */
    private void printAtMinutes() {
        Iterator<Entry<String, MomentStatsItem>> it = this.statsItemTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, MomentStatsItem> next = it.next();
            next.getValue().printAtMinutes();
        }
    }

    /** 设置指定 key 的 int 统计值并刷新时间戳。 */
    public void setValue(final String statsKey, final int value) {
        MomentStatsItem statsItem = this.getAndCreateStatsItem(statsKey);
        statsItem.getValue().set(value);
        statsItem.setLastUpdateTimestamp(System.currentTimeMillis());
    }

    /** 设置指定 key 的 long 统计值并刷新时间戳。 */
    public void setValue(final String statsKey, final long value) {
        MomentStatsItem statsItem = this.getAndCreateStatsItem(statsKey);
        statsItem.getValue().set(value);
        statsItem.setLastUpdateTimestamp(System.currentTimeMillis());
    }

    /** 删除 key 中包含 separator+statsKey+separator 的统计项。 */
    public void delValueByInfixKey(final String statsKey, String separator) {
        Iterator<Entry<String, MomentStatsItem>> it = this.statsItemTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, MomentStatsItem> next = it.next();
            if (next.getKey().contains(separator + statsKey + separator)) {
                it.remove();
            }
        }
    }

    /** 删除 key 以 separator+statsKey 结尾的统计项。 */
    public void delValueBySuffixKey(final String statsKey, String separator) {
        Iterator<Entry<String, MomentStatsItem>> it = this.statsItemTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, MomentStatsItem> next = it.next();
            if (next.getKey().endsWith(separator + statsKey)) {
                it.remove();
            }
        }
    }

    /** 获取或懒创建指定 key 的 {@link MomentStatsItem}。 */
    public MomentStatsItem getAndCreateStatsItem(final String statsKey) {
        MomentStatsItem statsItem = this.statsItemTable.get(statsKey);
        if (null == statsItem) {
            statsItem =
                new MomentStatsItem(this.statsName, statsKey, this.scheduledExecutorService, this.log);
            MomentStatsItem prev = this.statsItemTable.putIfAbsent(statsKey, statsItem);

            if (null != prev) {
                statsItem = prev;
                // statsItem.init();
            }
        }

        return statsItem;
    }

    /** 移除超过 maxStatsIdleTimeInMinutes 未更新的空闲统计项。 */
    public void cleanResource(int maxStatsIdleTimeInMinutes) {
        COMMERCIAL_LOG.info("CleanStatisticItem: kind:{}, size:{}", statsName, this.statsItemTable.size());
        Iterator<Entry<String, MomentStatsItem>> it = this.statsItemTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, MomentStatsItem> next = it.next();
            MomentStatsItem statsItem = next.getValue();
            if (System.currentTimeMillis() - statsItem.getLastUpdateTimestamp() > maxStatsIdleTimeInMinutes * 60 * 1000L) {
                it.remove();
                COMMERCIAL_LOG.info("CleanStatisticItem: removeKind:{}, removeKey:{}", statsName, statsItem.getStatsKey());
            }
        }
    }
}
