/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.distributed.distro.monitor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 单资源类型的 Distro 监控计数器：统计同步总数、成功/失败次数与校验失败次数。
 * Distro record for monitor.
 *
 * @author xiweng.yy
 */
public class DistroRecord {
    
    /** 资源/组件类型标识。 */
    private final String type;
    
    /** 同步尝试总次数。 */
    private final AtomicLong totalSyncCount;
    
    /** 同步成功次数。 */
    private final AtomicLong successfulSyncCount;
    
    /** 同步失败次数。 */
    private final AtomicLong failedSyncCount;
    
    /** 校验失败次数。 */
    private final AtomicInteger failedVerifyCount;
    
    /** 为指定类型初始化各原子计数器。 */
    public DistroRecord(String type) {
        this.type = type;
        this.totalSyncCount = new AtomicLong();
        this.successfulSyncCount = new AtomicLong();
        this.failedSyncCount = new AtomicLong();
        this.failedVerifyCount = new AtomicInteger();
    }
    
    /** 返回监控类型标识。 */
    public String getType() {
        return type;
    }
    
    /** 记录一次同步成功（同时递增总数）。 */
    public void syncSuccess() {
        successfulSyncCount.incrementAndGet();
        totalSyncCount.incrementAndGet();
    }
    
    /** 记录一次同步失败（同时递增总数）。 */
    public void syncFail() {
        failedSyncCount.incrementAndGet();
        totalSyncCount.incrementAndGet();
    }
    
    /** 记录一次校验失败。 */
    public void verifyFail() {
        failedVerifyCount.incrementAndGet();
    }
    
    /** 返回同步总次数。 */
    public long getTotalSyncCount() {
        return totalSyncCount.get();
    }
    
    /** 返回同步成功次数。 */
    public long getSuccessfulSyncCount() {
        return successfulSyncCount.get();
    }
    
    /** 返回同步失败次数。 */
    public long getFailedSyncCount() {
        return failedSyncCount.get();
    }
    
    /** 返回校验失败次数。 */
    public int getFailedVerifyCount() {
        return failedVerifyCount.get();
    }
}
