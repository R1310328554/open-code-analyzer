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

import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.logging.org.slf4j.Logger;

/**
 * 滑动窗口统计项：秒/分/时/日多级采样，计算 TPS、SUM 与平均耗时。
 */
public class StatsItem {
    /** 累计统计值（如字节数或耗时总和）。 */
    private final LongAdder value = new LongAdder();

    /** 累计调用次数。 */
    private final LongAdder times = new LongAdder();

    /** 分钟级采样快照链表（约 70 秒窗口）。 */
    private final LinkedList<CallSnapshot> csListMinute = new LinkedList<>();

    /** 小时级采样快照链表。 */
    private final LinkedList<CallSnapshot> csListHour = new LinkedList<>();

    /** 日级采样快照链表。 */
    private final LinkedList<CallSnapshot> csListDay = new LinkedList<>();

    private final String statsName;
    private final String statsKey;
    private long lastUpdateTimestamp = System.currentTimeMillis();
    private final ScheduledExecutorService scheduledExecutorService;

    private final Logger logger;

    /** 构造统计项并绑定调度器与日志。 */
    public StatsItem(String statsName, String statsKey, ScheduledExecutorService scheduledExecutorService, Logger logger) {
        this.statsName = statsName;
        this.statsKey = statsKey;
        this.scheduledExecutorService = scheduledExecutorService;
        this.logger = logger;
    }

    /** 根据快照链表首尾差值计算 SUM、TPS、AVGPT 与 TIMES。 */
    private static StatsSnapshot computeStatsData(final LinkedList<CallSnapshot> csList) {
        StatsSnapshot statsSnapshot = new StatsSnapshot();
        synchronized (csList) {
            double tps = 0;
            double avgpt = 0;
            long sum = 0;
            long timesDiff = 0;
            if (!csList.isEmpty()) {
                CallSnapshot first = csList.getFirst();
                CallSnapshot last = csList.getLast();
                sum = last.getValue() - first.getValue();
                tps = (sum * 1000.0d) / (last.getTimestamp() - first.getTimestamp());

                timesDiff = last.getTimes() - first.getTimes();
                if (timesDiff > 0) {
                    avgpt = (sum * 1.0d) / timesDiff;
                }
            }

            statsSnapshot.setSum(sum);
            statsSnapshot.setTps(tps);
            statsSnapshot.setAvgpt(avgpt);
            statsSnapshot.setTimes(timesDiff);
        }

        return statsSnapshot;
    }

    /** 返回分钟窗口聚合快照。 */
    public StatsSnapshot getStatsDataInMinute() {
        return computeStatsData(this.csListMinute);
    }

    /** 返回小时窗口聚合快照。 */
    public StatsSnapshot getStatsDataInHour() {
        return computeStatsData(this.csListHour);
    }

    /** 返回日窗口聚合快照。 */
    public StatsSnapshot getStatsDataInDay() {
        return computeStatsData(this.csListDay);
    }

    /** 注册秒/分/时采样及分钟/小时/日打印的定时任务。 */
    public void init() {

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    samplingInSeconds();
                } catch (Throwable ignored) {
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    samplingInMinutes();
                } catch (Throwable ignored) {
                }
            }
        }, 0, 10, TimeUnit.MINUTES);

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    samplingInHour();
                } catch (Throwable ignored) {
                }
            }
        }, 0, 1, TimeUnit.HOURS);

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    printAtMinutes();
                } catch (Throwable ignored) {
                }
            }
        }, Math.abs(UtilAll.computeNextMinutesTimeMillis() - System.currentTimeMillis()), 1000 * 60, TimeUnit.MILLISECONDS);

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    printAtHour();
                } catch (Throwable ignored) {
                }
            }
        }, Math.abs(UtilAll.computeNextHourTimeMillis() - System.currentTimeMillis()), 1000 * 60 * 60, TimeUnit.MILLISECONDS);

        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    printAtDay();
                } catch (Throwable ignored) {
                }
            }
        }, Math.abs(UtilAll.computeNextMorningTimeMillis() - System.currentTimeMillis()) - 2000, 1000 * 60 * 60 * 24, TimeUnit.MILLISECONDS);
    }

    /** 每 10 秒向分钟链表追加一次快照。 */
    public void samplingInSeconds() {
        synchronized (this.csListMinute) {
            if (this.csListMinute.size() == 0) {
                this.csListMinute.add(new CallSnapshot(System.currentTimeMillis() - 10 * 1000, 0, 0));
            }
            this.csListMinute.add(new CallSnapshot(System.currentTimeMillis(), this.times.sum(), this.value
                .sum()));
            if (this.csListMinute.size() > 7) {
                this.csListMinute.removeFirst();
            }
        }
    }

    /** 每 10 分钟向小时链表追加一次快照。 */
    public void samplingInMinutes() {
        synchronized (this.csListHour) {
            if (this.csListHour.size() == 0) {
                this.csListHour.add(new CallSnapshot(System.currentTimeMillis() - 10 * 60 * 1000, 0, 0));
            }
            this.csListHour.add(new CallSnapshot(System.currentTimeMillis(), this.times.sum(), this.value
                .sum()));
            if (this.csListHour.size() > 7) {
                this.csListHour.removeFirst();
            }
        }
    }

    /** 每小时向日链表追加一次快照。 */
    public void samplingInHour() {
        synchronized (this.csListDay) {
            if (this.csListDay.size() == 0) {
                this.csListDay.add(new CallSnapshot(System.currentTimeMillis() - 1 * 60 * 60 * 1000, 0, 0));
            }
            this.csListDay.add(new CallSnapshot(System.currentTimeMillis(), this.times.sum(), this.value
                .sum()));
            if (this.csListDay.size() > 25) {
                this.csListDay.removeFirst();
            }
        }
    }

    /** 打印分钟级统计日志。 */
    public void printAtMinutes() {
        StatsSnapshot ss = computeStatsData(this.csListMinute);
        logger.info(String.format("[%s] [%s] Stats In One Minute, ", this.statsName, this.statsKey) + statPrintDetail(ss));
    }

    /** 打印小时级统计日志。 */
    public void printAtHour() {
        StatsSnapshot ss = computeStatsData(this.csListHour);
        logger.info(String.format("[%s] [%s] Stats In One Hour, ", this.statsName, this.statsKey) + statPrintDetail(ss));

    }

    /** 打印日级统计日志。 */
    public void printAtDay() {
        StatsSnapshot ss = computeStatsData(this.csListDay);
        logger.info(String.format("[%s] [%s] Stats In One Day, ", this.statsName, this.statsKey) + statPrintDetail(ss));
    }

    /** 格式化 SUM/TPS/AVGPT 明细，子类可覆盖。 */
    protected String statPrintDetail(StatsSnapshot ss) {
        return String.format("SUM: %d TPS: %.2f AVGPT: %.2f",
                ss.getSum(),
                ss.getTps(),
                ss.getAvgpt());
    }

    public LongAdder getValue() {
        return value;
    }

    public String getStatsKey() {
        return statsKey;
    }

    public String getStatsName() {
        return statsName;
    }

    public LongAdder getTimes() {
        return times;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
}

/** 某一时刻的 times 与 value 快照，用于滑动窗口差分计算。 */
class CallSnapshot {
    private final long timestamp;
    private final long times;

    private final long value;

    public CallSnapshot(long timestamp, long times, long value) {
        super();
        this.timestamp = timestamp;
        this.times = times;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTimes() {
        return times;
    }

    public long getValue() {
        return value;
    }
}
