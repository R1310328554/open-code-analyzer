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

package com.alibaba.nacos.lock.monitor;

import com.alibaba.nacos.api.lock.remote.LockOperationEnum;
import com.alibaba.nacos.core.monitor.NacosMeterRegistryCenter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分布式锁 Micrometer 指标监控中心。
 *
 * <p>注册 gRPC 加锁/解锁总数、成功数及存活锁数量等 Gauge，
 * 并提供 {@link LockOperationEnum} 维度的计数器访问。</p>
 *
 * @author goumang.zh@alibaba-inc.com
 */
public class LockMetricsMonitor {
    
    /** 锁模块 Micrometer 注册表名称。 */
    private static final String METER_REGISTRY = NacosMeterRegistryCenter.LOCK_STABLE_REGISTRY;
    
    /** gRPC 加锁成功计数。 */
    private static AtomicInteger grpcLockSuccess = new AtomicInteger();
    
    /** gRPC 解锁成功计数。 */
    private static AtomicInteger grpcUnLockSuccess = new AtomicInteger();
    
    /** gRPC 加锁请求总数。 */
    private static AtomicInteger grpcLockTotal = new AtomicInteger();
    
    /** gRPC 解锁请求总数。 */
    private static AtomicInteger grpcUnLockTotal = new AtomicInteger();
    
    /** 当前存活锁数量。 */
    private static AtomicInteger aliveLockCount = new AtomicInteger();
    
    // 静态块：向 Nacos 指标中心注册各 Gauge
    static {
        ImmutableTag immutableTag = new ImmutableTag("module", "lock");
        List<Tag> tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "grpcLockTotal"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, grpcLockTotal);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "grpcLockSuccess"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, grpcLockSuccess);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "grpcUnLockTotal"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, grpcUnLockTotal);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "grpcUnLockSuccess"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, grpcUnLockSuccess);
        
        tags = new ArrayList<>();
        tags.add(immutableTag);
        tags.add(new ImmutableTag("name", "aliveLockCount"));
        NacosMeterRegistryCenter.gauge(METER_REGISTRY, "nacos_monitor", tags, aliveLockCount);
    }
    
    /** 获取 gRPC 加锁成功计数器。 */
    public static AtomicInteger getGrpcLockSuccess() {
        return grpcLockSuccess;
    }
    
    /** 获取 gRPC 解锁成功计数器。 */
    public static AtomicInteger getGrpcUnLockSuccess() {
        return grpcUnLockSuccess;
    }
    
    /** 获取 gRPC 加锁请求总数计数器。 */
    public static AtomicInteger getGrpcLockTotal() {
        return grpcLockTotal;
    }
    
    /** 获取 gRPC 解锁请求总数计数器。 */
    public static AtomicInteger getGrpcUnLockTotal() {
        return grpcUnLockTotal;
    }
    
    /** 获取锁请求处理耗时 Timer。 */
    public static Timer getLockHandlerTimer() {
        return NacosMeterRegistryCenter
            .timer(METER_REGISTRY, "nacos_timer", "module", "lock", "name", "lockHandlerRt");
    }
    
    /** 按操作类型返回成功计数器（加锁或解锁）。 */
    public static AtomicInteger getSuccessMeter(LockOperationEnum lockOperationEnum) {
        if (lockOperationEnum == LockOperationEnum.ACQUIRE) {
            return grpcLockSuccess;
        } else {
            return grpcUnLockSuccess;
        }
    }
    
    /** 按操作类型返回请求总数计数器（加锁或解锁）。 */
    public static AtomicInteger getTotalMeter(LockOperationEnum lockOperationEnum) {
        if (lockOperationEnum == LockOperationEnum.ACQUIRE) {
            return grpcLockTotal;
        } else {
            return grpcUnLockTotal;
        }
    }
}
