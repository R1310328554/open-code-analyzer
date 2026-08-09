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
package org.apache.rocketmq.common.statistics;

import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 分桶直方图统计摘要：维护 max/min/avg/total 及 TP 分位值。
 * 桶边界由 {@code topPercentileMeta} 二维数组定义。
 */
public class StatisticsBrief {
    /** {@code topPercentileMeta} 每行中区间上界索引。 */
    public static final int META_RANGE_INDEX = 0;
    /** {@code topPercentileMeta} 每行中桶数量索引。 */
    public static final int META_SLOT_NUM_INDEX = 1;

    // 分位直方图元数据与计数
    /** 分位桶元数据：每行 [rangeMax, slotNum]。 */
    private long[][] topPercentileMeta;
    /** 各桶采样计数。 */
    private AtomicInteger[] counts;
    /** 总采样次数。 */
    private AtomicLong totalCount;

    // 全局 max/min/total 聚合
    /** 采样最大值。 */
    private long max;
    /** 采样最小值。 */
    private long min;
    /** 采样值累加和。 */
    private long total;

    /** 以分位桶元数据构造摘要对象。 */
    public StatisticsBrief(long[][] topPercentileMeta) {
        if (!isLegalMeta(topPercentileMeta)) {
            throw new IllegalArgumentException("illegal topPercentileMeta");
        }

        this.topPercentileMeta = topPercentileMeta;
        this.counts = new AtomicInteger[slotNum(topPercentileMeta)];
        this.totalCount = new AtomicLong(0);
        reset();
    }

    /** 清零各桶计数与 max/min/total。 */
    public void reset() {
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] == null) {
                counts[i] = new AtomicInteger(0);
            } else {
                counts[i].set(0);
            }
        }
        totalCount.set(0);

        synchronized (this) {
            max = 0;
            min = Long.MAX_VALUE;
            total = 0;
        }
    }

    /** 校验元数据非空且每行长度为 2。 */
    private static boolean isLegalMeta(long[][] meta) {
        if (ArrayUtils.isEmpty(meta)) {
            return false;
        }

        for (long[] line : meta) {
            if (ArrayUtils.isEmpty(line) || line.length != 2) {
                return false;
            }
        }

        return true;
    }

    /** 根据元数据计算总桶数。 */
    private static int slotNum(long[][] meta) {
        int ret = 1;
        for (long[] line : meta) {
            ret += line[META_SLOT_NUM_INDEX];
        }
        return ret;
    }

    /** 记录一次采样并更新桶计数与 max/min/total。 */
    public void sample(long value) {
        int index = getSlotIndex(value);
        counts[index].incrementAndGet();
        totalCount.incrementAndGet();

        synchronized (this) {
            max = Math.max(max, value);
            min = Math.min(min, value);
            total += value;
        }
    }

    /** 返回 TP99.9 分位估计值。 */
    public long tp999() {
        return getTPValue(0.999f);
    }

    /** 按给定比例（0~1）估算 TP 分位值。 */
    public long getTPValue(float ratio) {
        if (ratio <= 0 || ratio >= 1) {
            ratio = 0.99f;
        }
        long count = totalCount.get();
        long excludes = (long)(count - count * ratio);
        if (excludes == 0) {
            return getMax();
        }

        int tmp = 0;
        for (int i = counts.length - 1; i > 0; i--) {
            tmp += counts[i].get();
            if (tmp > excludes) {
                return Math.min(getSlotTPValue(i), getMax());
            }
        }
        return 0;
    }

    /** 由桶索引反推该桶代表的分位值。 */
    private long getSlotTPValue(int index) {
        int slotNumLeft = index;
        for (int i = 0; i < topPercentileMeta.length; i++) {
            int slotNum = (int)topPercentileMeta[i][META_SLOT_NUM_INDEX];
            if (slotNumLeft < slotNum) {
                long metaRangeMax = topPercentileMeta[i][META_RANGE_INDEX];
                long metaRangeMin = 0;
                if (i > 0) {
                    metaRangeMin = topPercentileMeta[i - 1][META_RANGE_INDEX];
                }

                return metaRangeMin + (metaRangeMax - metaRangeMin) / slotNum * (slotNumLeft + 1);
            } else {
                slotNumLeft -= slotNum;
            }
        }
        // 末桶上界为 Integer.MAX_VALUE
        return Integer.MAX_VALUE;
    }

    /** 将采样值映射到桶索引。 */
    private int getSlotIndex(long num) {
        int index = 0;
        for (int i = 0; i < topPercentileMeta.length; i++) {
            long rangeMax = topPercentileMeta[i][META_RANGE_INDEX];
            int slotNum = (int)topPercentileMeta[i][META_SLOT_NUM_INDEX];
            long rangeMin = (i > 0) ? topPercentileMeta[i - 1][META_RANGE_INDEX] : 0;
            if (rangeMin <= num && num < rangeMax) {
                index += (num - rangeMin) / ((rangeMax - rangeMin) / slotNum);
                break;
            }

            index += slotNum;
        }
        return index;
    }

    /** 返回采样最大值。 */
    public long getMax() {
        return max;
    }

    /** 无采样时返回 0，否则返回最小值。 */
    public long getMin() {
        return totalCount.get() > 0 ? min : 0;
    }

    /** 返回采样值累加和。 */
    public long getTotal() {
        return total;
    }

    /** 返回总采样次数。 */
    public long getCnt() {
        return totalCount.get();
    }

    /** 返回采样平均值，无采样时为 0。 */
    public double getAvg() {
        return totalCount.get() != 0 ? ((double)total) / totalCount.get() : 0;
    }
}
