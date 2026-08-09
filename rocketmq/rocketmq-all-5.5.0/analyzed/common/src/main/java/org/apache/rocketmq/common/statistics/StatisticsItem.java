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

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 单条统计项：按 kind/object 维度累加多项计数，支持拦截器与快照差分。
 */
public class StatisticsItem {
    /** 统计类别（如 RPC、Topic）。 */
    private String statKind;
    /** 统计对象键（如方法名、Topic 名）。 */
    private String statObject;

    /** 各子项名称。 */
    private String[] itemNames;
    /** 各子项累加器。 */
    private AtomicLong[] itemAccumulates;
    /** 调用/更新次数。 */
    private AtomicLong invokeTimes;

    /** 可选拦截器（如分位摘要）。 */
    private Interceptor interceptor;

    /** 最近一次更新的时间戳（毫秒）。 */
    private AtomicLong lastTimeStamp;

    /** 构造统计项并初始化各子项累加器。 */
    public StatisticsItem(String statKind, String statObject, String... itemNames) {
        if (itemNames == null || itemNames.length <= 0) {
            throw new InvalidParameterException("StatisticsItem \"itemNames\" is empty");
        }

        this.statKind = statKind;
        this.statObject = statObject;
        this.itemNames = itemNames;

        AtomicLong[] accs = new AtomicLong[itemNames.length];
        for (int i = 0; i < itemNames.length; i++) {
            accs[i] = new AtomicLong(0);
        }

        this.itemAccumulates = accs;
        this.invokeTimes = new AtomicLong();
        this.lastTimeStamp = new AtomicLong(System.currentTimeMillis());
    }

    /** 递增各子项并刷新 invokeTimes、lastTimeStamp，通知拦截器。 */
    public void incItems(long... itemIncs) {
        int len = Math.min(itemIncs.length, itemAccumulates.length);
        for (int i = 0; i < len; i++) {
            itemAccumulates[i].addAndGet(itemIncs[i]);
        }

        invokeTimes.addAndGet(1);
        lastTimeStamp.set(System.currentTimeMillis());

        if (interceptor != null) {
            interceptor.inc(itemIncs);
        }
    }

    /** 是否从未调用或各子项均为 0。 */
    public boolean allZeros() {
        if (invokeTimes.get() == 0) {
            return true;
        }

        for (AtomicLong acc : itemAccumulates) {
            if (acc.get() != 0) {
                return false;
            }
        }
        return true;
    }

    /** 返回统计类别。 */
    public String getStatKind() {
        return statKind;
    }

    /** 返回统计对象键。 */
    public String getStatObject() {
        return statObject;
    }

    /** 返回子项名称数组。 */
    public String[] getItemNames() {
        return itemNames;
    }

    /** 返回各子项累加器。 */
    public AtomicLong[] getItemAccumulates() {
        return itemAccumulates;
    }

    /** 返回调用次数。 */
    public AtomicLong getInvokeTimes() {
        return invokeTimes;
    }

    /** 返回最后更新时间戳。 */
    public AtomicLong getLastTimeStamp() {
        return lastTimeStamp;
    }

    /** 按名称取子项累加器，未知项返回零值 AtomicLong。 */
    public AtomicLong getItemAccumulate(String itemName) {
        int index = ArrayUtils.indexOf(itemNames, itemName);
        if (index < 0) {
            return new AtomicLong(0);
        }
        return itemAccumulates[index];
    }

    /**
     * 获取当前快照（各累加器可能非原子一致）。
     *
     * @return 独立副本
     */
    public StatisticsItem snapshot() {
        StatisticsItem ret = new StatisticsItem(statKind, statObject, itemNames);

        ret.itemAccumulates = new AtomicLong[itemAccumulates.length];
        for (int i = 0; i < itemAccumulates.length; i++) {
            ret.itemAccumulates[i] = new AtomicLong(itemAccumulates[i].get());
        }

        ret.invokeTimes = new AtomicLong(invokeTimes.longValue());
        ret.lastTimeStamp = new AtomicLong(lastTimeStamp.longValue());

        return ret;
    }

    /**
     * 与另一统计项做差，得到区间增量。
     *
     * @param item 被减项（同 kind/object/itemNames）
     * @return 差分后的新 StatisticsItem
     */
    public StatisticsItem subtract(StatisticsItem item) {
        if (item == null) {
            return snapshot();
        }

        if (!statKind.equals(item.statKind) || !statObject.equals(item.statObject) || !Arrays.equals(itemNames,
            item.itemNames)) {
            throw new IllegalArgumentException("StatisticsItem's kind, key and itemNames must be exactly the same");
        }

        StatisticsItem ret = new StatisticsItem(statKind, statObject, itemNames);
        ret.invokeTimes = new AtomicLong(invokeTimes.get() - item.invokeTimes.get());
        ret.itemAccumulates = new AtomicLong[itemAccumulates.length];
        for (int i = 0; i < itemAccumulates.length; i++) {
            ret.itemAccumulates[i] = new AtomicLong(itemAccumulates[i].get() - item.itemAccumulates[i].get());
        }
        return ret;
    }

    /** 返回拦截器。 */
    public Interceptor getInterceptor() {
        return interceptor;
    }

    /** 设置拦截器。 */
    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

}
