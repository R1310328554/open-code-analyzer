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

import java.util.concurrent.TimeUnit;

/**
 * 速率计数器抽象基类，按时间窗口累加与查询请求计数。
 *
 * <p>子类实现具体的窗口存储策略；本类提供秒/分/时粒度的时间对齐工具方法。</p>
 *
 * @author zunfei.lzf
 */
public abstract class RateCounter {
    
    /** 计数器名称。 */
    private String name;
    
    /** 统计周期（秒/分/时）。 */
    private TimeUnit period;
    
    public RateCounter(String name, TimeUnit period) {
        this.name = name;
        this.period = period;
    }
    
    /**
     * 获取统计周期。
     *
     * @return 时间单位
     */
    public TimeUnit getPeriod() {
        return period;
    }
    
    /**
     * 累加指定时间窗口的请求计数。
     *
     * @param timestamp 时间戳（毫秒）
     * @param count     累加数量
     * @return 累加后的总计数
     */
    public abstract long add(long timestamp, long count);
    
    /**
     * 尝试累加计数，超过上限时记录拦截并返回 {@code false}。
     *
     * @param timestamp  时间戳（毫秒）
     * @param countDelta 累加增量
     * @param upperLimit 上限阈值
     * @return 是否在限制内
     */
    public abstract boolean tryAdd(long timestamp, long countDelta, long upperLimit);
    
    /**
     * 获取指定时间窗口的当前计数。
     *
     * @param timestamp 时间戳（毫秒）
     * @return 当前计数
     */
    public abstract long getCount(long timestamp);
    
    /**
     * 获取计数器名称。
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 将毫秒时间戳对齐到分钟起始（截断秒以下精度）。
     *
     * @param timeStamp 毫秒时间戳
     * @return 对齐后的毫秒时间戳
     */
    public static long getTrimMillsOfMinute(long timeStamp) {
        String millString = String.valueOf(timeStamp);
        String substring = millString.substring(0, millString.length() - 3);
        return Long.parseLong(Long.parseLong(substring) / 60 * 60 + "000");
    }
    
    /**
     * 将毫秒时间戳对齐到秒起始（截断毫秒以下精度）。
     *
     * @param timeStamp 毫秒时间戳
     * @return 对齐后的毫秒时间戳
     */
    public static long getTrimMillsOfSecond(long timeStamp) {
        String millString = String.valueOf(timeStamp);
        String substring = millString.substring(0, millString.length() - 3);
        return Long.parseLong(substring + "000");
    }
    
    /**
     * 将毫秒时间戳对齐到小时起始。
     *
     * @param timeStamp 毫秒时间戳
     * @return 对齐后的毫秒时间戳
     */
    public static long getTrimMillsOfHour(long timeStamp) {
        String millString = String.valueOf(timeStamp);
        String substring = millString.substring(0, millString.length() - 3);
        return Long.parseLong(Long.parseLong(substring) / (60 * 60) * (60 * 60) + "000");
    }
}
