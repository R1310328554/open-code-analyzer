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

package com.alibaba.nacos.config.server.monitor;

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
 * 配置模块动态 Micrometer 指标刷新服务：定时刷新配置变更 TopN 并周期性清零计数。
 * 与 {@link MetricsMonitor} 及 {@link NacosMeterRegistryCenter} 配合暴露运维指标。
 * dynamic meter refresh service.
 *
 * @author <a href="mailto:liuyixiao0821@gmail.com">liuyixiao</a>
 */
@Service
public class ConfigDynamicMeterRefreshService {
    
    /** TopN 配置变更计数注册表名称 */
    private static final String TOPN_CONFIG_CHANGE_REGISTRY =
        NacosMeterRegistryCenter.TOPN_CONFIG_CHANGE_REGISTRY;
    
    /** 保留的配置变更 TopN 条数 */
    private static final int CONFIG_CHANGE_N = 10;
    
    /**
     * 每 30 秒刷新配置变更次数 TopN 到 Micrometer gauge。
     */
    @Scheduled(cron = "0/30 * * * * *")
    public void refreshTopnConfigChangeCount() {
        NacosMeterRegistryCenter.clear(TOPN_CONFIG_CHANGE_REGISTRY);
        List<Pair<String, AtomicInteger>> topnConfigChangeCount =
            MetricsMonitor.getConfigChangeCount()
                .getCounterOfTopN(CONFIG_CHANGE_N);
        for (Pair<String, AtomicInteger> configChangeCount : topnConfigChangeCount) {
            List<Tag> tags = new ArrayList<>();
            tags.add(new ImmutableTag("config", configChangeCount.getFirst()));
            NacosMeterRegistryCenter.gauge(TOPN_CONFIG_CHANGE_REGISTRY, "config_change_count", tags,
                configChangeCount.getSecond());
        }
    }
    
    /**
     * 每周一零点重置配置变更累计计数，避免长期膨胀。
     */
    @Scheduled(cron = "0 0 0 ? * 1")
    public void resetTopnConfigChangeCount() {
        MetricsMonitor.getConfigChangeCount().reset();
    }
}
