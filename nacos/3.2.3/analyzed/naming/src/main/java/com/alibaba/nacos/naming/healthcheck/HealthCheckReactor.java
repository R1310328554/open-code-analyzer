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

package com.alibaba.nacos.naming.healthcheck;

import com.alibaba.nacos.naming.healthcheck.heartbeat.BeatCheckTask;
import com.alibaba.nacos.naming.healthcheck.interceptor.HealthCheckTaskInterceptWrapper;
import com.alibaba.nacos.naming.healthcheck.v2.HealthCheckTaskV2;
import com.alibaba.nacos.naming.misc.GlobalExecutor;
import com.alibaba.nacos.naming.misc.Loggers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 健康检查任务调度中心，统一管理 V1 心跳与 V2 健康探测。
 *
 * <p>通过 {@link GlobalExecutor} 延迟/周期执行检测任务，支持取消与立即调度。</p>
 *
 * @author nacos
 */
public class HealthCheckReactor {
    
    /** taskKey → 周期调度 Future，用于心跳任务取消。 */
    private static Map<String, ScheduledFuture> futureMap = new ConcurrentHashMap<>();
    
    /**
     * 调度 V2 健康检查任务，按归一化 RT 延迟执行。
     *
     * @param task health check task
     */
    /** 包装拦截器后提交 V2 检测任务。 */
    public static void scheduleCheck(HealthCheckTaskV2 task) {
        task.setStartTime(System.currentTimeMillis());
        Runnable wrapperTask = new HealthCheckTaskInterceptWrapper(task);
        GlobalExecutor.scheduleNamingHealth(wrapperTask, task.getCheckRtNormalized(),
            TimeUnit.MILLISECONDS);
    }
    
    /**
     * 以 5 秒初始延迟与 5 秒周期调度客户端心跳检测（V1）。
     *
     * @param task client beat check task
     */
    public static void scheduleCheck(BeatCheckTask task) {
        Runnable wrapperTask =
            task instanceof NacosHealthCheckTask
                ? new HealthCheckTaskInterceptWrapper((NacosHealthCheckTask) task)
                : task;
        futureMap.computeIfAbsent(task.taskKey(),
            k -> GlobalExecutor.scheduleNamingHealth(wrapperTask, 5000, 5000,
                TimeUnit.MILLISECONDS));
    }
    
    /**
     * 取消已调度的心跳检测任务。
     *
     * @param task client beat check task
     */
    public static void cancelCheck(BeatCheckTask task) {
        ScheduledFuture scheduledFuture = futureMap.get(task.taskKey());
        if (scheduledFuture == null) {
            return;
        }
        try {
            scheduledFuture.cancel(true);
            futureMap.remove(task.taskKey());
        } catch (Exception e) {
            Loggers.EVT_LOG.error("[CANCEL-CHECK] cancel failed!", e);
        }
    }
    
    /**
     * 立即调度一次性健康检查任务。
     *
     * @param task health check task
     * @return scheduled future
     */
    public static ScheduledFuture<?> scheduleNow(Runnable task) {
        return GlobalExecutor.scheduleNamingHealth(task, 0, TimeUnit.MILLISECONDS);
    }
}
