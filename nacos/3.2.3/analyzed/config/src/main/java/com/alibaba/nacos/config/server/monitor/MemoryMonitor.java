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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 配置服务内存与响应监控入口：启动周期性打印内存、拉配置响应分布及异步通知队列任务。
 * 并每日清零部分 {@link MetricsMonitor} 计数器。
 * Memory monitor.
 *
 * @author Nacos
 */
@Service
public class MemoryMonitor {
    
    /**
     * 注入后注册三类定时监控任务（间隔 {@link #DELAY_SECONDS} 秒）。
     *
     * @param notifySingleService 异步通知服务，供队列监控使用
     */
    @Autowired
    public MemoryMonitor(AsyncNotifyService notifySingleService) {
        
        ConfigExecutor.scheduleConfigTask(new PrintMemoryTask(), DELAY_SECONDS, DELAY_SECONDS,
            TimeUnit.SECONDS);
        
        ConfigExecutor
            .scheduleConfigTask(new PrintGetConfigResponeTask(), DELAY_SECONDS, DELAY_SECONDS,
                TimeUnit.SECONDS);
        
        ConfigExecutor
            .scheduleConfigTask(new ThreadTaskQueueMonitorTask(notifySingleService), DELAY_SECONDS,
                DELAY_SECONDS,
                TimeUnit.SECONDS);
        
    }
    
    /** 监控任务初始延迟与执行间隔（秒） */
    private static final long DELAY_SECONDS = 10;
    
    /**
     * 每日零点重置拉配置、发布与模糊搜索等日级计数器。
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void clear() {
        MetricsMonitor.getConfigMonitor().set(0);
        MetricsMonitor.getPublishMonitor().set(0);
        MetricsMonitor.getFuzzySearchMonitor().set(0);
    }
}
