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

import com.alibaba.nacos.config.server.service.ClientTrackService;
import com.alibaba.nacos.config.server.service.ConfigCacheService;

import static com.alibaba.nacos.config.server.utils.LogUtil.MEMORY_LOG;

/**
 * 定时打印配置缓存与订阅统计：group 数、订阅客户端数、订阅条目数，并更新 configCount 指标。
 * Print memory task.
 *
 * @author zongtanghu
 */
public class PrintMemoryTask implements Runnable {
    
    /** 采集缓存 group 数与订阅规模，写入 MEMORY_LOG 并刷新 MetricsMonitor */
    @Override
    public void run() {
        int groupCount = ConfigCacheService.groupCount();
        int subClientCount = ClientTrackService.subscribeClientCount();
        long subCount = ClientTrackService.subscriberCount();
        MEMORY_LOG.info("groupCount = {}, subscriberClientCount = {}, subscriberCount = {}",
            groupCount, subClientCount,
            subCount);
        MetricsMonitor.getConfigCountMonitor().set(groupCount);
    }
}
