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

import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.config.server.Config;
import com.alibaba.nacos.core.utils.ClassUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 按 IP 哈希分槽的流量统计：单 IP 与全 IP 请求量滑动窗口计数，供 IP 级限流决策。
 * According to IP flow control, control the number of individual IP and IP total.
 *
 * @author leiwen.zh
 */
public class SimpleIpFlowData {
    
    /** IP 哈希映射后的各槽计数器 */
    private AtomicInteger[] data;
    
    /** 槽位数，≤0 时强制为 1 */
    private int slotCount;
    
    /** 各槽平均计数（轮转后更新） */
    private int averageCount;
    
    /** IP 流量轮转定时器 */
    private ScheduledExecutorService timer = ExecutorFactory.Managed
        .newSingleScheduledExecutorService(ClassUtils.getCanonicalName(Config.class),
            new NameThreadFactory("com.alibaba.nacos.config.flow.control.ip"));
    
    /** 初始化槽数组，按 interval 毫秒周期清零并更新平均值 */
    public SimpleIpFlowData(int slotCount, int interval) {
        if (slotCount <= 0) {
            this.slotCount = 1;
        } else {
            this.slotCount = slotCount;
        }
        data = new AtomicInteger[slotCount];
        for (int i = 0; i < data.length; i++) {
            data[i] = new AtomicInteger(0);
        }
        timer.scheduleAtFixedRate(this::rotateSlot, interval, interval, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 按 IP hashCode 取模定位槽位，原子自增并返回新值。
     * Atomically increments by one the current value.
     */
    public int incrementAndGet(String ip) {
        int index = 0;
        if (ip != null) {
            index = ip.hashCode() % slotCount;
        }
        if (index < 0) {
            index = -index;
        }
        return data[index].incrementAndGet();
    }
    
    /**
     * 汇总各槽计数求平均后清零所有槽。
     * Rotate the slot.
     */
    public void rotateSlot() {
        int totalCount = 0;
        for (int i = 0; i < slotCount; i++) {
            totalCount += data[i].get();
            data[i].set(0);
        }
        this.averageCount = totalCount / this.slotCount;
    }
    
    /** 查询指定 IP 对应槽的当前计数 */
    public int getCurrentCount(String ip) {
        int index = 0;
        if (ip != null) {
            index = ip.hashCode() % slotCount;
        }
        if (index < 0) {
            index = -index;
        }
        return data[index].get();
    }
    
    /** 最近一次轮转后的各槽平均计数 */
    public int getAverageCount() {
        return this.averageCount;
    }
}
