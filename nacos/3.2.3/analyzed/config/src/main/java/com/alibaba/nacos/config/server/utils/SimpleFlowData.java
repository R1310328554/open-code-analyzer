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
 * 滑动窗口流量统计：多槽位环形计数，定时轮转并计算各槽平均值，用于全局限流观测。
 * Simple Flow data.
 *
 * @author Nacos
 */
public class SimpleFlowData {
    
    /** 当前写入槽索引 */
    private int index = 0;
    
    /** 各时间槽计数器数组 */
    private AtomicInteger[] data;
    
    /** 上一轮轮转计算的全槽平均值 */
    private int average;
    
    /** 槽位总数 */
    private int slotCount;
    
    /** 定时轮转槽位的单线程调度器 */
    private ScheduledExecutorService timer = ExecutorFactory.Managed
        .newSingleScheduledExecutorService(ClassUtils.getCanonicalName(Config.class),
            new NameThreadFactory("com.alibaba.nacos.config.flow.control"));
    
    /** 初始化槽数组并按 interval 毫秒周期轮转 */
    public SimpleFlowData(int slotCount, int interval) {
        this.slotCount = slotCount;
        data = new AtomicInteger[slotCount];
        for (int i = 0; i < data.length; i++) {
            data[i] = new AtomicInteger(0);
        }
        timer.scheduleAtFixedRate(this::rotateSlot, interval, interval, TimeUnit.MILLISECONDS);
    }
    
    /** 当前槽原子累加 count 并返回新值 */
    public int addAndGet(int count) {
        return data[index].addAndGet(count);
    }
    
    /** 当前槽原子自增 1 并返回新值 */
    public int incrementAndGet() {
        return data[index].incrementAndGet();
    }
    
    /**
     * 汇总各槽计数求平均，切换至下一槽并清零。
     * Rotate the slot.
     */
    public void rotateSlot() {
        int total = 0;
        
        for (int i = 0; i < slotCount; i++) {
            total += data[i].get();
        }
        
        average = total / slotCount;
        
        index = (index + 1) % slotCount;
        data[index].set(0);
    }
    
    /** 当前槽实时计数 */
    public int getCurrentCount() {
        return data[index].get();
    }
    
    /** 最近一次轮转后的全槽平均计数 */
    public int getAverageCount() {
        return this.average;
    }
    
    /** 槽位数量 */
    public int getSlotCount() {
        return this.slotCount;
    }
    
    /** 按时间顺序拼接各槽计数，空格分隔，供调试输出 */
    public String getSlotInfo() {
        StringBuilder sb = new StringBuilder();
        
        int index = this.index + 1;
        
        for (int i = 0; i < slotCount; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(this.data[(i + index) % slotCount].get());
        }
        return sb.toString();
    }
    
    /** 获取 prevStep 个槽位之前的计数（环形回溯） */
    public int getCount(int prevStep) {
        prevStep = prevStep % this.slotCount;
        int index = (this.index + this.slotCount - prevStep) % this.slotCount;
        return this.data[index].intValue();
    }
    
}
