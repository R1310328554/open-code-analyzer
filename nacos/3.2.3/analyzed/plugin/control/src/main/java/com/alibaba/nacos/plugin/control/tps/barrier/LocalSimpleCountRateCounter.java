/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.control.tps.barrier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地简单计数速率计数器，基于固定大小环形槽位窗口统计 TPS。
 *
 * <p>按秒/分/时对齐时间窗口，每个槽位记录一个周期内的请求计数与拦截计数；
 * 窗口满时自动复用最旧槽位。</p>
 *
 * @author shiyiyue
 */
public class LocalSimpleCountRateCounter extends RateCounter {
    
    /** 环形槽位数量，决定可回溯的时间窗口长度。 */
    private static final int DEFAULT_RECORD_SIZE = 10;
    
    /** 窗口起始对齐时间（毫秒）。 */
    long startTime = System.currentTimeMillis();
    
    /** 环形槽位列表。 */
    private List<TpsSlot> slotList;
    
    public LocalSimpleCountRateCounter(String name, TimeUnit period) {
        super(name, period);
        slotList = new ArrayList<>(DEFAULT_RECORD_SIZE);
        for (int i = 0; i < DEFAULT_RECORD_SIZE; i++) {
            slotList.add(new TpsSlot());
        }
        long now = System.currentTimeMillis();
        
        if (period == TimeUnit.SECONDS) {
            startTime = RateCounter.getTrimMillsOfSecond(now);
        } else if (period == TimeUnit.MINUTES) {
            startTime = RateCounter.getTrimMillsOfMinute(now);
        } else if (period == TimeUnit.HOURS) {
            startTime = RateCounter.getTrimMillsOfHour(now);
        } else {
            // 默认按秒对齐
            startTime = RateCounter.getTrimMillsOfSecond(now);
        }
    }
    
    /** {@inheritDoc} 累加计数，不做上限校验。 */
    @Override
    public long add(long timestamp, long count) {
        return createSlotIfAbsent(timestamp).countHolder.count.addAndGet(count);
    }
    
    /** {@inheritDoc} 尝试累加，超上限时记录拦截计数并返回 {@code false}。 */
    @Override
    public boolean tryAdd(long timestamp, long countDelta, long upperLimit) {
        if (createSlotIfAbsent(timestamp).countHolder.count.addAndGet(countDelta) <= upperLimit) {
            return true;
        } else {
            createSlotIfAbsent(timestamp).countHolder.interceptedCount.addAndGet(countDelta);
            return false;
        }
    }
    
    /**
     * 扣减指定时间窗口内的计数（用于补偿场景）。
     *
     * @param timestamp 时间戳（毫秒）
     * @param count     扣减数量
     */
    public void minus(long timestamp, long count) {
        AtomicLong currentCount = createSlotIfAbsent(timestamp).countHolder.count;
        currentCount.addAndGet(count * -1);
    }
    
    /** {@inheritDoc} 获取指定时间窗口的当前计数，槽位不存在时返回 0。 */
    public long getCount(long timestamp) {
        TpsSlot point = getPoint(timestamp);
        return point == null ? 0L : point.countHolder.count.longValue();
    }
    
    /**
     * 只读获取指定时间戳对应的槽位，不存在时返回 {@code null}。
     *
     * @param timeStamp 时间戳（毫秒）
     * @return 对应槽位，不存在时为 {@code null}
     */
    private TpsSlot getPoint(long timeStamp) {
        long distance = timeStamp - startTime;
        long diff =
            (distance < 0 ? distance + getPeriod().toMillis(1) * DEFAULT_RECORD_SIZE : distance)
                / getPeriod()
                    .toMillis(1);
        long currentWindowTime = startTime + diff * getPeriod().toMillis(1);
        int index = (int) diff % DEFAULT_RECORD_SIZE;
        TpsSlot tpsSlot = slotList.get(index);
        if (tpsSlot.time != currentWindowTime) {
            return null;
        }
        return tpsSlot;
    }
    
    /**
     * 获取或创建指定时间戳对应的槽位。
     *
     * @param timeStamp 时间戳（毫秒）
     * @return 对应槽位（不存在时自动创建并重置）
     */
    public TpsSlot createSlotIfAbsent(long timeStamp) {
        long distance = timeStamp - startTime;
        
        long diff =
            (distance < 0 ? distance + getPeriod().toMillis(1) * DEFAULT_RECORD_SIZE : distance)
                / getPeriod()
                    .toMillis(1);
        long currentWindowTime = startTime + diff * getPeriod().toMillis(1);
        int index = (int) diff % DEFAULT_RECORD_SIZE;
        TpsSlot tpsSlot = slotList.get(index);
        if (tpsSlot.time != currentWindowTime) {
            tpsSlot.reset(currentWindowTime);
        }
        return slotList.get(index);
    }
    
    /** 单个时间窗口槽位，持有该窗口的请求与拦截计数。 */
    static class TpsSlot {
        
        /** 槽位对应的时间窗口起始时间（毫秒）。 */
        long time = 0L;
        
        /** 计数持有者。 */
        private SlotCountHolder countHolder = new SlotCountHolder();
        
        /** 重置槽位到新的时间窗口并清零计数。 */
        public void reset(long second) {
            synchronized (this) {
                if (this.time != second) {
                    this.time = second;
                    countHolder.count.set(0L);
                    countHolder.interceptedCount.set(0);
                }
            }
        }
        
        @Override
        public String toString() {
            return "TpsSlot{" + "time=" + time + ", countHolder=" + countHolder + '}';
        }
        
    }
    
    /** 槽位内的原子计数持有者。 */
    static class SlotCountHolder {
        
        /** 通过请求计数。 */
        AtomicLong count = new AtomicLong();
        
        /** 被拦截请求计数。 */
        AtomicLong interceptedCount = new AtomicLong();
        
        @Override
        public String toString() {
            return "{" + count + "|" + interceptedCount + '}';
        }
    }
}
