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

package com.alibaba.nacos.core.monitor.topn;

import com.alibaba.nacos.core.config.AbstractDynamicConfig;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.util.concurrent.TimeUnit;

/**
 * TopN 监控动态配置：控制是否采集、TopN 条数与采集周期，由 {@link AbstractDynamicConfig} 热更新。
 * TopN configurations.
 *
 * @author xiweng.yy
 */
public class TopNConfig extends AbstractDynamicConfig {
    
    /** 动态配置分组名。 */
    private static final String TOP_N = "topN";
    
    /** 单例实例。 */
    private static final TopNConfig INSTANCE = new TopNConfig();
    
    /** 环境变量/配置项前缀。 */
    private static final String TOP_N_PREFIX = "nacos.core.monitor.topn.";
    
    /** 是否启用 TopN 采集的配置键。 */
    private static final String ENABLED_KEY = TOP_N_PREFIX + "enabled";
    
    /** TopN 保留条数配置键。 */
    private static final String COUNT_KEY = TOP_N_PREFIX + "count";
    
    /** 采集周期（毫秒）配置键。 */
    private static final String INTERNAL_MS_KEY = TOP_N_PREFIX + "internalMs";
    
    /** 默认启用 TopN 采集。 */
    private static final boolean DEFAULT_ENABLED = true;
    
    /** 默认保留前 10 名。 */
    private static final int DEFAULT_COUNT = 10;
    
    /** 默认每 30 秒刷新一次 TopN 快照。 */
    private static final long DEFAULT_INTERNAL_MS = TimeUnit.SECONDS.toMillis(30);
    
    /** 当前是否启用 TopN 采集。 */
    private boolean enabled;
    
    /** 当前 TopN 条数上限。 */
    private int countOfTopN;
    
    /** 当前采集周期（毫秒）。 */
    private long internalMs;
    
    /** 私有构造，注册动态配置并加载初始值。 */
    private TopNConfig() {
        super(TOP_N);
        resetConfig();
    }
    
    /** 从环境/配置中心读取 TopN 开关、条数与周期。 */
    @Override
    protected void getConfigFromEnv() {
        enabled = EnvUtil.getProperty(ENABLED_KEY, Boolean.class, DEFAULT_ENABLED);
        countOfTopN = EnvUtil.getProperty(COUNT_KEY, Integer.class, DEFAULT_COUNT);
        internalMs = EnvUtil.getProperty(INTERNAL_MS_KEY, Long.class, DEFAULT_INTERNAL_MS);
    }
    
    @Override
    protected String printConfig() {
        return toString();
    }
    
    @Override
    public String toString() {
        return "TopNConfig{" + "enabled=" + enabled + ", topNCount=" + countOfTopN + ", internalMs="
            + internalMs + '}';
    }
    
    /** 获取 TopN 配置单例。 */
    public static TopNConfig getInstance() {
        return INSTANCE;
    }
    
    /** 是否启用 TopN 采集。 */
    public boolean isEnabled() {
        return enabled;
    }
    
    /** 返回 TopN 条数上限。 */
    public int getCountOfTopN() {
        return countOfTopN;
    }
    
    /** 返回采集周期（毫秒）。 */
    public long getInternalMs() {
        return internalMs;
    }
}
