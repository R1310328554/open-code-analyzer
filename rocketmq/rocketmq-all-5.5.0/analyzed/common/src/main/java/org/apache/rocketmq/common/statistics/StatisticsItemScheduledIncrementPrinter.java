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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 定时打印 {@link StatisticsItem} 区间增量（非累计值），并采样 TPS 相关 brief。
 */
public class StatisticsItemScheduledIncrementPrinter extends StatisticsItemScheduledPrinter {

    /** 参与 TPS 采样的子项名称。 */
    private String[] tpsItemNames;

    /** TPS 采样任务初始延迟（毫秒）。 */
    public static final int TPS_INITIAL_DELAY = 0;
    /** TPS 采样间隔（毫秒）。 */
    public static final int TPS_INTREVAL = 1000;
    /** brief 输出字段分隔符。 */
    public static final String SEPARATOR = "|";

    /** 各 kind/object 上次打印时的快照，用于计算增量。 */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItem>> lastItemSnapshots
        = new ConcurrentHashMap<>();

    /** kind -> object -> TPS 采样 brief。 */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItemSampleBrief>> sampleBriefs
        = new ConcurrentHashMap<>();

    /** 构造增量定时打印机，并指定 TPS 采样项名。 */
    public StatisticsItemScheduledIncrementPrinter(String name, StatisticsItemPrinter printer,
                                                   ScheduledExecutorService executor, InitialDelay initialDelay,
                                                   long interval, String[] tpsItemNames, Valve valve) {
        super(name, printer, executor, initialDelay, interval, valve);
        this.tpsItemNames = tpsItemNames;
    }

    /** 注册统计项：按 interval 打印增量，并按 TPS_INTREVAL 采样 brief。 */
    @Override
    public void schedule(final StatisticsItem item) {
        setItemSampleBrief(item.getStatKind(), item.getStatObject(), new StatisticsItemSampleBrief(item, tpsItemNames));

        // 每 interval 毫秒打印一次区间增量
        ScheduledFuture future = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!enabled()) {
                    return;
                }

                StatisticsItem snapshot = item.snapshot();
                StatisticsItem lastSnapshot = getItemSnapshot(lastItemSnapshots, item.getStatKind(),
                    item.getStatObject());
                StatisticsItem increment = snapshot.subtract(lastSnapshot);

                Interceptor interceptor = item.getInterceptor();
                String interceptorStr = formatInterceptor(interceptor);
                if (interceptor != null) {
                    interceptor.reset();
                }

                StatisticsItemSampleBrief brief = getSampleBrief(item.getStatKind(), item.getStatObject());
                if (brief != null && (!increment.allZeros() || printZeroLine())) {
                    printer.print(name, increment, interceptorStr, brief.toString());
                }

                setItemSnapshot(lastItemSnapshots, snapshot);

                if (brief != null) {
                    brief.reset();
                }
            }
        }, getInitialDelay(), interval, TimeUnit.MILLISECONDS);
        addFuture(item, future);

        // 每 TPS_INTREVAL 毫秒采样一次 TPS brief
        ScheduledFuture futureSample = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!enabled()) {
                    return;
                }

                StatisticsItem snapshot = item.snapshot();
                StatisticsItemSampleBrief brief = getSampleBrief(item.getStatKind(), item.getStatObject());
                if (brief != null) {
                    brief.sample(snapshot);
                }
            }
        }, TPS_INTREVAL, TPS_INTREVAL, TimeUnit.MILLISECONDS);
        addFuture(item, futureSample);
    }

    /** 取消定时任务并清理快照与 brief 缓存。 */
    @Override
    public void remove(StatisticsItem item) {
        // 取消关联的 Future 任务
        removeAllFuture(item);

        String kind = item.getStatKind();
        String key = item.getStatObject();

        ConcurrentHashMap<String, StatisticsItem> lastItemMap = lastItemSnapshots.get(kind);
        if (lastItemMap != null) {
            lastItemMap.remove(key);
        }

        ConcurrentHashMap<String, StatisticsItemSampleBrief> briefMap = sampleBriefs.get(kind);
        if (briefMap != null) {
            briefMap.remove(key);
        }
    }

    /** 从快照表取指定 kind/key 的上次快照。 */
    private StatisticsItem getItemSnapshot(
        ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItem>> snapshots,
        String kind, String key) {
        ConcurrentHashMap<String, StatisticsItem> itemMap = snapshots.get(kind);
        return (itemMap != null) ? itemMap.get(key) : null;
    }

    /** 取 TPS 采样 brief。 */
    private StatisticsItemSampleBrief getSampleBrief(String kind, String key) {
        ConcurrentHashMap<String, StatisticsItemSampleBrief> itemMap = sampleBriefs.get(kind);
        return (itemMap != null) ? itemMap.get(key) : null;
    }

    /** 更新 kind/key 的快照缓存。 */
    private void setItemSnapshot(ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItem>> snapshots,
                                 StatisticsItem item) {
        String kind = item.getStatKind();
        String key = item.getStatObject();
        ConcurrentHashMap<String, StatisticsItem> itemMap = snapshots.get(kind);
        if (itemMap == null) {
            itemMap = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, StatisticsItem> oldItemMap = snapshots.putIfAbsent(kind, itemMap);
            if (oldItemMap != null) {
                itemMap = oldItemMap;
            }
        }

        itemMap.put(key, item);
    }

    /** 注册 kind/key 的 TPS 采样 brief。 */
    private void setItemSampleBrief(String kind, String key,
                                    StatisticsItemSampleBrief brief) {
        ConcurrentHashMap<String, StatisticsItemSampleBrief> itemMap = sampleBriefs.get(kind);
        if (itemMap == null) {
            itemMap = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, StatisticsItemSampleBrief> oldItemMap = sampleBriefs.putIfAbsent(kind, itemMap);
            if (oldItemMap != null) {
                itemMap = oldItemMap;
            }
        }

        itemMap.put(key, brief);
    }

    /** 将 {@link StatisticsBriefInterceptor} 格式化为 max|avg|tp999 后缀串。 */
    private String formatInterceptor(Interceptor interceptor) {
        if (interceptor == null) {
            return "";
        }

        if (interceptor instanceof StatisticsBriefInterceptor) {
            StringBuilder sb = new StringBuilder();
            StatisticsBriefInterceptor briefInterceptor = (StatisticsBriefInterceptor)interceptor;
            for (StatisticsBrief brief : briefInterceptor.getStatisticsBriefs()) {
                long max = brief.getMax();
                long tp999 = Math.min(brief.tp999(), max);
                // 可选输出 total/min
                sb.append(SEPARATOR).append(max);
                // 可选输出 min
                sb.append(SEPARATOR).append(String.format("%.2f", brief.getAvg()));
                sb.append(SEPARATOR).append(tp999);
            }
            return sb.toString();
        }
        return "";
    }

    /** 对指定 TPS 子项在采样周期内的 max/avg 摘要。 */
    public static class StatisticsItemSampleBrief {
        /** 上次采样时的统计项快照。 */
        private StatisticsItem lastSnapshot;

        /** TPS 子项名称。 */
        public String[] itemNames;
        /** 与各 itemNames 对应的周期 brief。 */
        public ItemSampleBrief[] briefs;

        /** 初始化并建立与 statItem 子项的 brief 数组。 */
        public StatisticsItemSampleBrief(StatisticsItem statItem, String[] itemNames) {
            this.lastSnapshot = statItem.snapshot();
            this.itemNames = itemNames;
            this.briefs = new ItemSampleBrief[itemNames.length];
            for (int i = 0; i < itemNames.length; i++) {
                this.briefs[i] = new ItemSampleBrief();
            }
        }

        /** 重置各子项 brief。 */
        public synchronized void reset() {
            for (ItemSampleBrief brief : briefs) {
                brief.reset();
            }
        }

        /** 根据与上次快照的差分采样各 TPS 子项增量。 */
        public synchronized void sample(StatisticsItem snapshot) {
            if (snapshot == null) {
                return;
            }

            for (int i = 0; i < itemNames.length; i++) {
                String name = itemNames[i];

                long lastValue = lastSnapshot != null ? lastSnapshot.getItemAccumulate(name).get() : 0;
                long increment = snapshot.getItemAccumulate(name).get() - lastValue;
                briefs[i].sample(increment);
            }
            lastSnapshot = snapshot;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < briefs.length; i++) {
                ItemSampleBrief brief = briefs[i];
                sb.append(SEPARATOR).append(brief.getMax());
                // 可选输出 min
                sb.append(SEPARATOR).append(String.format("%.2f", brief.getAvg()));
            }
            return sb.toString();
        }
    }

    /** 单个子项在一段时间内的 max/min/avg 采样摘要。 */
    public static class ItemSampleBrief {
        /** 周期内增量最大值。 */
        private long max;
        /** 周期内增量最小值。 */
        private long min;
        /** 增量累加和。 */
        private long total;
        /** 采样次数。 */
        private long cnt;

        /** 构造并重置计数。 */
        public ItemSampleBrief() {
            reset();
        }

        /** 记录一次增量采样。 */
        public void sample(long value) {
            max = Math.max(max, value);
            min = Math.min(min, value);
            total += value;
            cnt++;
        }

        /** 清零 max/min/total/cnt。 */
        public void reset() {
            max = 0;
            min = Long.MAX_VALUE;
            total = 0;
            cnt = 0;
        }

        /** 返回周期内增量最大值。 */
        public long getMax() {
            return max;
        }

        /** 无采样时返回 0，否则返回最小增量。 */
        public long getMin() {
            return cnt > 0 ? min : 0;
        }

        /** 返回增量累加和。 */
        public long getTotal() {
            return total;
        }

        /** 返回采样次数。 */
        public long getCnt() {
            return cnt;
        }

        /** 返回增量平均值。 */
        public double getAvg() {
            return cnt != 0 ? ((double)total) / cnt : 0;
        }
    }
}
