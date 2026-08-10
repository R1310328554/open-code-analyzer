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

import com.alibaba.nacos.config.server.service.notify.AsyncNotifyService;
import com.alibaba.nacos.config.server.utils.ConfigExecutor;

import static com.alibaba.nacos.config.server.utils.LogUtil.MEMORY_LOG;

/**
 * 线程任务队列监控任务：周期性采样异步通知队列与客户端推送队列深度，
 * 写入内存日志并更新 {@link MetricsMonitor} 指标，便于观测配置变更推送积压。
 * NotifyTaskQueueMonitorTask.
 *
 * @author zongtanghu
 */
public class ThreadTaskQueueMonitorTask implements Runnable {
    
    private final AsyncNotifyService notifySingleService;
    
    /**
     * 构造监控任务（保留 {@link AsyncNotifyService} 引用供后续扩展）。
     *
     * @param notifySingleService 单条异步通知服务
     */
    ThreadTaskQueueMonitorTask(AsyncNotifyService notifySingleService) {
        this.notifySingleService = notifySingleService;
    }
    
    /** 采样队列长度并刷新监控指标。 */
    @Override
    public void run() {
        int size = ConfigExecutor.asyncNotifyQueueSize();
        int notifierClientSize = ConfigExecutor.asyncConfigChangeClientNotifyQueueSize();
        MEMORY_LOG.info("toNotifyTaskSize = {}", size);
        MEMORY_LOG.info("toClientNotifyTaskSize = {}", notifierClientSize);
        MetricsMonitor.getNotifyTaskMonitor().set(size);
        MetricsMonitor.getNotifyClientTaskMonitor().set(notifierClientSize);
    }
}
