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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.rocketmq.common.utils.ThreadUtils;

/**
 * 统计中心：按 kind/key 维护 {@link StatisticsItem}，自动调度打印与空闲清理。
 */
public class StatisticsManager {

    /** 统计类别名 -> 元数据。 */
    private Map<String, StatisticsKindMeta> kindMetaMap;

    /** 分位 brief 配置：项名 -> topPercentileMeta。 */
    private Pair<String, long[][]>[] briefMetas;

    /** kind -> objectKey -> 统计项实例。 */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, StatisticsItem>> statsTable
        = new ConcurrentHashMap<>();

    /** 统计项最大空闲毫秒数，超时且非 online 则移除。 */
    private static final int MAX_IDLE_TIME = 10 * 60 * 1000;
    /** 后台清理空闲统计项的单线程调度器。 */
    private final ScheduledExecutorService executor = ThreadUtils.newSingleThreadScheduledExecutor(
        "StatisticsManagerCleaner", true);

    /** 可选：判定统计项是否仍在线。 */
    private StatisticsItemStateGetter statisticsItemStateGetter;

    /** 空元数据表并启动清理任务。 */
    public StatisticsManager() {
        kindMetaMap = new HashMap<>();
        start();
    }

    /** 以给定 kind 元数据构造并启动清理。 */
    public StatisticsManager(Map<String, StatisticsKindMeta> kindMeta) {
        this.kindMetaMap = kindMeta;
        start();
    }

    /** 注册统计类别并初始化 statsTable 槽位。 */
    public void addStatisticsKindMeta(StatisticsKindMeta kindMeta) {
        kindMetaMap.put(kindMeta.getName(), kindMeta);
        statsTable.putIfAbsent(kindMeta.getName(), new ConcurrentHashMap<>(16));
    }

    /** 设置分位 brief 元数据，供新建项挂载拦截器。 */
    public void setBriefMeta(Pair<String, long[][]>[] briefMetas) {
        this.briefMetas = briefMetas;
    }

    /** 启动周期性空闲项清理任务。 */
    private void start() {
        int maxIdleTime = MAX_IDLE_TIME;
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<String, ConcurrentHashMap<String, StatisticsItem>>> iter
                    = statsTable.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, ConcurrentHashMap<String, StatisticsItem>> entry = iter.next();
                    String kind = entry.getKey();
                    ConcurrentHashMap<String, StatisticsItem> itemMap = entry.getValue();

                    if (itemMap == null || itemMap.isEmpty()) {
                        continue;
                    }

                    HashMap<String, StatisticsItem> tmpItemMap = new HashMap<>(itemMap);
                    for (StatisticsItem item : tmpItemMap.values()) {
                        // 超时且非 online 则移除
                        if (System.currentTimeMillis() - item.getLastTimeStamp().get() > MAX_IDLE_TIME
                            && (statisticsItemStateGetter == null || !statisticsItemStateGetter.online(item))) {
                            remove(item);
                        }
                    }
                }
            }
        }, maxIdleTime, maxIdleTime / 3, TimeUnit.MILLISECONDS);
    }

    /**
     * 递增指定 kind/key 的统计项；不存在则懒创建并调度打印。
     *
     * @param kind 统计类别
     * @param key 统计对象键
     * @param itemAccumulates 各子项增量
     */
    public boolean inc(String kind, String key, long... itemAccumulates) {
        ConcurrentHashMap<String, StatisticsItem> itemMap = statsTable.get(kind);
        if (itemMap != null) {
            StatisticsItem item = itemMap.get(key);

            // 不存在则创建并注册定时打印
            if (item == null) {
                item = new StatisticsItem(kind, key, kindMetaMap.get(kind).getItemNames());
                item.setInterceptor(new StatisticsBriefInterceptor(item, briefMetas));
                StatisticsItem oldItem = itemMap.putIfAbsent(key, item);
                if (oldItem != null) {
                    item = oldItem;
                } else {
                    scheduleStatisticsItem(item);
                }
            }

            // 执行累加
            item.incItems(itemAccumulates);

            return true;
        }

        return false;
    }

    /** 将新统计项交给对应 kind 的 ScheduledPrinter。 */
    private void scheduleStatisticsItem(StatisticsItem item) {
        kindMetaMap.get(item.getStatKind()).getScheduledPrinter().schedule(item);
    }

    /** 从 statsTable 移除并取消定时任务。 */
    public void remove(StatisticsItem item) {
        ConcurrentHashMap<String, StatisticsItem> itemMap = statsTable.get(item.getStatKind());
        if (itemMap != null) {
            itemMap.remove(item.getStatObject(), item);
        }

        StatisticsKindMeta kindMeta = kindMetaMap.get(item.getStatKind());
        if (kindMeta != null && kindMeta.getScheduledPrinter() != null) {
            kindMeta.getScheduledPrinter().remove(item);
        }
    }

    /** 返回在线状态判定器。 */
    public StatisticsItemStateGetter getStatisticsItemStateGetter() {
        return statisticsItemStateGetter;
    }

    /** 设置在线状态判定器。 */
    public void setStatisticsItemStateGetter(StatisticsItemStateGetter statisticsItemStateGetter) {
        this.statisticsItemStateGetter = statisticsItemStateGetter;
    }

    /** 关闭清理调度线程池。 */
    public void shutdown() {
        executor.shutdown();
    }
}
