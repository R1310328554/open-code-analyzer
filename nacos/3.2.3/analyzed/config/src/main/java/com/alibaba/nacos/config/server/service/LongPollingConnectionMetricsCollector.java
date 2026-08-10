/*
 *
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.config.server.service;

import com.alibaba.nacos.plugin.control.connection.ConnectionMetricsCollector;
import com.alibaba.nacos.sys.utils.ApplicationUtils;

import java.util.stream.Collectors;

/**
 * 长轮询连接指标采集器：实现 {@link ConnectionMetricsCollector}，
 * 统计 {@link LongPollingService#allSubs} 总数及按 IP 过滤的连接数。
 * long polling connection metrics.
 *
 * @author shiyiyue
 */
public class LongPollingConnectionMetricsCollector implements ConnectionMetricsCollector {
    
    /** 指标名称标识：long_polling。 */
    @Override
    public String getName() {
        return "long_polling";
    }
    
    /** 返回当前全部长轮询挂起连接数。 */
    @Override
    public int getTotalCount() {
        return ApplicationUtils.getBean(LongPollingService.class).allSubs.size();
    }
    
    /** 统计指定客户端 IP 的长轮询连接数（忽略大小写）。 */
    @Override
    public int getCountForIp(String ip) {
        return ApplicationUtils.getBean(LongPollingService.class).allSubs.stream()
            .filter(a -> a.ip.equalsIgnoreCase(ip)).collect(Collectors.toList()).size();
    }
}
