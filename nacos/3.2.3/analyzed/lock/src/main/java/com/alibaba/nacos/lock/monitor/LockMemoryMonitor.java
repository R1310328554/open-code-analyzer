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

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 锁模块内存/指标定时清理服务。
 *
 * <p>每日零点重置 {@link LockMetricsMonitor} 中 gRPC 加解锁计数器，防止长期累积。</p>
 *
 * @author goumang.zh@alibaba-inc.com
 */
@Service
public class LockMemoryMonitor {
    
    /** 每日零点自动清零 gRPC 锁操作计数指标。 */
    @Scheduled(cron = "0 0 0 * * ?")
    public void clear() {
        LockMetricsMonitor.getGrpcLockTotal().set(0);
        LockMetricsMonitor.getGrpcLockSuccess().set(0);
        LockMetricsMonitor.getGrpcUnLockTotal().set(0);
        LockMetricsMonitor.getGrpcUnLockSuccess().set(0);
    }
}
