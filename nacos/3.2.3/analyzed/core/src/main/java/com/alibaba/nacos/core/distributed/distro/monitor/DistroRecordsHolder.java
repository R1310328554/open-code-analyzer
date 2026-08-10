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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Distro 监控记录全局持有者：按类型维护 {@link DistroRecord} 并提供聚合统计。
 * Distro records holder.
 *
 * @author xiweng.yy
 */
public class DistroRecordsHolder {
    
    /** 单例实例。 */
    private static final DistroRecordsHolder INSTANCE = new DistroRecordsHolder();
    
    /** 资源类型 → 监控记录。 */
    private final ConcurrentMap<String, DistroRecord> distroRecords;
    
    /** 私有构造，初始化并发映射。 */
    private DistroRecordsHolder() {
        distroRecords = new ConcurrentHashMap<>();
    }
    
    /** 返回全局单例。 */
    public static DistroRecordsHolder getInstance() {
        return INSTANCE;
    }
    
    /** 若存在则返回指定类型的监控记录。 */
    public Optional<DistroRecord> getRecordIfExist(String type) {
        return Optional.ofNullable(distroRecords.get(type));
    }
    
    /** 获取或懒创建指定类型的监控记录。 */
    public DistroRecord getRecord(String type) {
        return distroRecords.computeIfAbsent(type, s -> new DistroRecord(type));
    }
    
    /** 汇总所有类型的同步总次数。 */
    public long getTotalSyncCount() {
        final AtomicLong result = new AtomicLong();
        distroRecords
            .forEach((s, distroRecord) -> result.addAndGet(distroRecord.getTotalSyncCount()));
        return result.get();
    }
    
    /** 汇总所有类型的同步成功次数。 */
    public long getSuccessfulSyncCount() {
        final AtomicLong result = new AtomicLong();
        distroRecords
            .forEach((s, distroRecord) -> result.addAndGet(distroRecord.getSuccessfulSyncCount()));
        return result.get();
    }
    
    /** 汇总所有类型的同步失败次数。 */
    public long getFailedSyncCount() {
        final AtomicLong result = new AtomicLong();
        distroRecords
            .forEach((s, distroRecord) -> result.addAndGet(distroRecord.getFailedSyncCount()));
        return result.get();
    }
    
    /** 汇总所有类型的校验失败次数。 */
    public int getFailedVerifyCount() {
        final AtomicInteger result = new AtomicInteger();
        distroRecords
            .forEach((s, distroRecord) -> result.addAndGet(distroRecord.getFailedVerifyCount()));
        return result.get();
    }
}
