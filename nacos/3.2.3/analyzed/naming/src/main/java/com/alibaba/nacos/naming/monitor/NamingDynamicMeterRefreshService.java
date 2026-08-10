/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.monitor;

import com.alibaba.nacos.common.utils.Pair;
import com.alibaba.nacos.core.monitor.NacosMeterRegistryCenter;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Tag;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Naming 动态 Micrometer 指标刷新服务。
 *
 * <p>定时将 {@link ServiceTopNCounter} 中变更最频繁的服务 TopN 写入独立注册表，并周期性清零计数器，供 Grafana 等监控面板展示热点服务。</p>
 *
 * @author <a href="mailto:liuyixiao0821@gmail.com">liuyixiao</a>
 */
@Service
public class NamingDynamicMeterRefreshService {
    
    /** 服务变更 TopN 专用 Micrometer 注册表。 */
    private static final String TOPN_SERVICE_CHANGE_REGISTRY =
        NacosMeterRegistryCenter.TOPN_SERVICE_CHANGE_REGISTRY;
    
    /** TopN 榜单保留的服务数量。 */
    private static final int SERVICE_CHANGE_N = 10;
    
    /**
     * 每 30 秒刷新服务变更 TopN 指标到 Micrometer。
     */
    @Scheduled(cron = "0/30 * * * * *")
    public void refreshTopnServiceChangeCount() {
        NacosMeterRegistryCenter.clear(TOPN_SERVICE_CHANGE_REGISTRY);
        List<Pair<String, AtomicInteger>> topnServiceChangeCount =
            MetricsMonitor.getServiceChangeCount()
                .getCounterOfTopN(SERVICE_CHANGE_N);
        for (Pair<String, AtomicInteger> serviceChangeCount : topnServiceChangeCount) {
            List<Tag> tags = new ArrayList<>();
            tags.add(new ImmutableTag("service", serviceChangeCount.getFirst()));
            NacosMeterRegistryCenter
                .gauge(TOPN_SERVICE_CHANGE_REGISTRY, "service_change_count", tags,
                    serviceChangeCount.getSecond());
        }
    }
    
    /**
     * 每周一零点清零服务变更计数器，避免长期累积失真。
     */
    @Scheduled(cron = "0 0 0 ? * 1")
    public void resetTopnServiceChangeCount() {
        MetricsMonitor.getServiceChangeCount().reset();
    }
}
