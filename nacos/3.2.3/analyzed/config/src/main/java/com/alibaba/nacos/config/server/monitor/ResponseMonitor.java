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

package com.alibaba.nacos.config.server.monitor;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 拉配置响应耗时分布监控：按毫秒区间累计请求占比，供 {@link PrintGetConfigResponeTask} 周期性输出。
 * 类名保留历史拼写 Monitory。
 * Response Monitory.
 *
 * @author Nacos
 */
public class ResponseMonitor {
    
    /** 各耗时区间（0-50ms … 3000ms+）的请求计数 */
    private static AtomicLong[] getConfigCountDetail = new AtomicLong[8];
    
    /** 拉配置请求总计数（用于计算百分比） */
    private static AtomicLong getConfigCount = new AtomicLong();
    
    /** 耗时区间上界：50ms */
    private static final int MS_50 = 50;
    
    /** 耗时区间上界：100ms */
    private static final int MS_100 = 100;
    
    /** 耗时区间上界：200ms */
    private static final int MS_200 = 200;
    
    /** 耗时区间上界：500ms */
    private static final int MS_500 = 500;
    
    /** 耗时区间上界：1000ms */
    private static final int MS_1000 = 1000;
    
    /** 耗时区间上界：2000ms */
    private static final int MS_2000 = 2000;
    
    /** 耗时区间上界：3000ms */
    private static final int MS_3000 = 3000;
    
    static {
        refresh();
    }
    
    /**
     * 初始化或重置各耗时区间计数器数组。
     */
    public static void refresh() {
        for (int i = 0; i < getConfigCountDetail.length; i++) {
            getConfigCountDetail[i] = new AtomicLong();
        }
    }
    
    /**
     * 记录一次拉配置耗时并累加到对应毫秒区间。
     *
     * @param time 本次请求耗时（毫秒）
     */
    public static void addConfigTime(long time) {
        getConfigCount.incrementAndGet();
        if (time < MS_50) {
            getConfigCountDetail[0].incrementAndGet();
        } else if (time < MS_100) {
            getConfigCountDetail[1].incrementAndGet();
        } else if (time < MS_200) {
            getConfigCountDetail[2].incrementAndGet();
        } else if (time < MS_500) {
            getConfigCountDetail[3].incrementAndGet();
        } else if (time < MS_1000) {
            getConfigCountDetail[4].incrementAndGet();
        } else if (time < MS_2000) {
            getConfigCountDetail[5].incrementAndGet();
        } else if (time < MS_3000) {
            getConfigCountDetail[6].incrementAndGet();
        } else {
            getConfigCountDetail[7].incrementAndGet();
        }
    }
    
    /** 生成各耗时区间占比字符串并清零区间计数（总计一并清零） */
    public static String getStringForPrint() {
        DecimalFormat df = new DecimalFormat("##.0");
        StringBuilder s = new StringBuilder("getConfig monitor:\r\n");
        s.append("0-50ms:"
            + df.format(getConfigCountDetail[0].getAndSet(0) * 100 / getConfigCount.get()))
            .append("%\r\n");
        s.append("100-200ms:"
            + df.format(getConfigCountDetail[2].getAndSet(0) * 100 / getConfigCount.get()))
            .append("%\r\n");
        s.append("200-500ms:"
            + df.format(getConfigCountDetail[3].getAndSet(0) * 100 / getConfigCount.get()))
            .append("%\r\n");
        s.append("500-1000ms:"
            + df.format(getConfigCountDetail[4].getAndSet(0) * 100 / getConfigCount.get()))
            .append("%\r\n");
        s.append("1000-2000ms:"
            + df.format(getConfigCountDetail[5].getAndSet(0) * 100 / getConfigCount.get()))
            .append("%\r\n");
        s.append("2000-3000ms:"
            + df.format(getConfigCountDetail[6].getAndSet(0) * 100 / getConfigCount.get()))
            .append("%\r\n");
        s.append("3000以上ms:"
            + df.format(getConfigCountDetail[7].getAndSet(0) * 100 / getConfigCount.getAndSet(0)))
            .append("%\r\n");
        return s.toString();
    }
}
