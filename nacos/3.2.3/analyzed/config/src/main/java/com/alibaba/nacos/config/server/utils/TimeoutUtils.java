/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 客户端拉取配置数据的累计超时统计工具：从网络获取数据后累加 totalTime，拉取前检查是否已超过 totalTimeout。
 * A utility class that handles timeouts and is used by the client to retrieve the total timeout of the data. After
 * obtaining the data from the network,totalTime is accumulated. Before obtaining the data from the network, check
 * whether the totalTime is greater than totalTimeout. If yes, it indicates the totalTimeout
 *
 * @author leiwen.zh
 */
public class TimeoutUtils {
    
    /**
     * 累计拉取数据消耗的总时间，单位毫秒。
     * Total time to get the data of consumption, the unit of ms.
     */
    private final AtomicLong totalTime = new AtomicLong(0L);
    
    /** 上次重置 totalTime 的时间戳（毫秒） */
    private volatile long lastResetTime;
    
    /** 是否已完成 lastResetTime 的首次初始化 */
    private volatile boolean initialized = false;
    
    /**
     * 拉取数据的总超时上限，单位毫秒。
     * Total timeout to get data, the unit of ms.
     */
    private long totalTimeout;
    
    /**
     * 累计耗时过期阈值：超过该间隔后 resetTotalTime 才会清零 totalTime，单位毫秒。
     * The cumulative expiration time of the time consumed by fetching the data, the unit of ms.
     */
    private long invalidThreshold;
    
    /**
     * 构造超时统计器。
     *
     * @param totalTimeout     总超时上限（毫秒）
     * @param invalidThreshold 累计耗时过期阈值（毫秒）
     */
    public TimeoutUtils(long totalTimeout, long invalidThreshold) {
        this.totalTimeout = totalTimeout;
        this.invalidThreshold = invalidThreshold;
    }
    
    /**
     * 初始化上次重置时间（仅首次调用生效）。
     * Init last reset time.
     */
    public synchronized void initLastResetTime() {
        if (initialized) {
            return;
        }
        lastResetTime = System.currentTimeMillis();
        initialized = true;
    }
    
    /**
     * 累加本次拉取消耗的时间。
     * Cumulative total time.
     *
     * @param time 本次耗时（毫秒）
     */
    public void addTotalTime(long time) {
        totalTime.addAndGet(time);
    }
    
    /**
     * 判断累计耗时是否已超过总超时上限。
     * Is timeout.
     *
     * @return {@code true} 表示已超时
     */
    public boolean isTimeout() {
        return totalTime.get() > this.totalTimeout;
    }
    
    /**
     * 在累计耗时过期后清零 totalTime 并刷新 lastResetTime。
     * Clean the total time.
     */
    public void resetTotalTime() {
        if (isTotalTimeExpired()) {
            totalTime.set(0L);
            lastResetTime = System.currentTimeMillis();
        }
    }
    
    /** 返回累计耗时原子计数器，供外部读取当前值 */
    public AtomicLong getTotalTime() {
        return totalTime;
    }
    
    /** 判断自上次重置以来是否已超过 invalidThreshold */
    private boolean isTotalTimeExpired() {
        return System.currentTimeMillis() - lastResetTime > this.invalidThreshold;
    }
}
