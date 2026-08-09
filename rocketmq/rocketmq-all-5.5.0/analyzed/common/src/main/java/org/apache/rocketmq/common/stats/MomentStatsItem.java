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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.logging.org.slf4j.Logger;

/**
 * 瞬时统计项：维护单个 key 的当前累计值，每 5 分钟打印并重置。
 */
public class MomentStatsItem {

    /** 当前统计周期内的累计值。 */
    private final AtomicLong value = new AtomicLong(0);

    /** 统计类别名称（如 TOPIC_PUT_NUMS）。 */
    private final String statsName;
    /** 统计维度 key（如 topic 或 group 名）。 */
    private final String statsKey;
    /** 驱动定时打印与清零的调度线程池。 */
    private final ScheduledExecutorService scheduledExecutorService;
    /** 输出统计日志的 Logger。 */
    private final Logger log;
    /** 最近一次写入统计值的时间戳，用于空闲清理。 */
    private long lastUpdateTimestamp = System.currentTimeMillis();

    /** 构造瞬时统计项并绑定调度器与日志。 */
    public MomentStatsItem(String statsName, String statsKey,
        ScheduledExecutorService scheduledExecutorService, Logger log) {
        this.statsName = statsName;
        this.statsKey = statsKey;
        this.scheduledExecutorService = scheduledExecutorService;
        this.log = log;
    }

    /** 注册每 5 分钟打印并重置 value 的定时任务。 */
    public void init() {
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    printAtMinutes();

                    MomentStatsItem.this.value.set(0);
                } catch (Throwable e) {
                }
            }
        }, Math.abs(UtilAll.computeNextMinutesTimeMillis() - System.currentTimeMillis()), 1000 * 60 * 5, TimeUnit.MILLISECONDS);
    }

    /** 打印当前 5 分钟窗口内的统计值。 */
    public void printAtMinutes() {
        log.info("[{}] [{}] Stats Every 5 Minutes, Value: {}",
            this.statsName,
            this.statsKey,
            this.value.get());
    }

    /** 返回累计值原子计数器。 */
    public AtomicLong getValue() {
        return value;
    }

    /** 返回统计 key。 */
    public String getStatsKey() {
        return statsKey;
    }

    /** 返回统计类别名。 */
    public String getStatsName() {
        return statsName;
    }

    /** 返回最后更新时间戳。 */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /** 更新最后写入时间戳。 */
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
}
