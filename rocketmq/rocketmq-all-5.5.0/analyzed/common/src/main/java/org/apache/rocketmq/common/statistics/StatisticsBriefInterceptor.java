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
import org.apache.commons.lang3.tuple.Pair;

/**
 * {@link Interceptor} 实现：将 {@link StatisticsItem} 指定项写入 {@link StatisticsBrief} 分位摘要。
 */
public class StatisticsBriefInterceptor implements Interceptor {
    /** 各 brief 对应 StatisticsItem 项下标。 */
    private int[] indexOfItems;

    /** 与 briefMetas 一一对应的摘要实例。 */
    private StatisticsBrief[] statisticsBriefs;

    /** 绑定统计项与 brief 元数据，校验项名存在于 item。 */
    public StatisticsBriefInterceptor(StatisticsItem item, Pair<String, long[][]>[] briefMetas) {
        indexOfItems = new int[briefMetas.length];
        statisticsBriefs = new StatisticsBrief[briefMetas.length];
        for (int i = 0; i < briefMetas.length; i++) {
            String name = briefMetas[i].getKey();
            int index = ArrayUtils.indexOf(item.getItemNames(), name);
            if (index < 0) {
                throw new IllegalArgumentException("illegal briefItemName: " + name);
            }
            indexOfItems[i] = index;
            statisticsBriefs[i] = new StatisticsBrief(briefMetas[i].getValue());
        }
    }

    /** 按映射下标将增量采样到对应 {@link StatisticsBrief}。 */
    @Override
    public void inc(long... itemValues) {
        for (int i = 0; i < indexOfItems.length; i++) {
            int indexOfItem = indexOfItems[i];
            if (indexOfItem < itemValues.length) {
                statisticsBriefs[i].sample(itemValues[indexOfItem]);
            }
        }
    }

    /** 重置全部 brief 采样状态。 */
    @Override
    public void reset() {
        for (StatisticsBrief brief : statisticsBriefs) {
            brief.reset();
        }
    }

    /** 返回项下标映射。 */
    public int[] getIndexOfItems() {
        return indexOfItems;
    }

    /** 设置项下标映射。 */
    public void setIndexOfItems(int[] indexOfItems) {
        this.indexOfItems = indexOfItems;
    }

    /** 返回摘要数组。 */
    public StatisticsBrief[] getStatisticsBriefs() {
        return statisticsBriefs;
    }

    /** 设置摘要数组。 */
    public void setStatisticsBriefs(StatisticsBrief[] statisticsBriefs) {
        this.statisticsBriefs = statisticsBriefs;
    }
}
