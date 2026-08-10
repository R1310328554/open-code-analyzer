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

package com.alibaba.nacos.config.server.configuration;

import com.alibaba.nacos.core.config.AbstractDynamicConfig;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * Nacos Config 模块通用动态配置：推送重试/超时、批量大小、Derby 运维开关及模糊监听配额等，
 * 继承 {@link com.alibaba.nacos.core.config.AbstractDynamicConfig} 从环境变量热加载。
 * Nacos config common configs.
 *
 * @author blake.qiu
 */

public class ConfigCommonConfig extends AbstractDynamicConfig {
    
    /** 动态配置分组名，用于 {@link com.alibaba.nacos.core.config.AbstractDynamicConfig} 注册 */
    private static final String CONFIG_COMMON = "ConfigCommon";
    
    /** 单例实例，供全模块读取推送与模糊监听相关参数 */
    private static final ConfigCommonConfig INSTANCE = new ConfigCommonConfig();
    
    /** 配置变更推送到客户端的最大重试次数，默认 50 */
    private int maxPushRetryTimes = 50;
    
    /** 单次推送超时时间（毫秒），默认 3000 */
    private long pushTimeout = 3000L;
    
    /** 批量推送时每批客户端数量，默认 20 */
    private int batchSize = 20;
    
    /** 是否启用嵌入式 Derby 运维 SQL 接口，默认关闭 */
    private boolean derbyOpsEnabled = false;
    
    private int maxPatternCount = 20;
    
    private int maxMatchedConfigCount = 500;
    
    private ConfigCommonConfig() {
        super(CONFIG_COMMON);
        resetConfig();
    }
    
    /** 获取 Config 通用配置单例 */
    public static ConfigCommonConfig getInstance() {
        return INSTANCE;
    }
    
    public int getMaxPushRetryTimes() {
        return maxPushRetryTimes;
    }
    
    public void setMaxPushRetryTimes(int maxPushRetryTimes) {
        this.maxPushRetryTimes = maxPushRetryTimes;
    }
    
    public long getPushTimeout() {
        return pushTimeout;
    }
    
    public int getBatchSize() {
        return batchSize;
    }
    
    public boolean isDerbyOpsEnabled() {
        return derbyOpsEnabled;
    }
    
    public void setDerbyOpsEnabled(boolean derbyOpsEnabled) {
        this.derbyOpsEnabled = derbyOpsEnabled;
    }
    
    public int getMaxPatternCount() {
        return maxPatternCount;
    }
    
    public int getMaxMatchedConfigCount() {
        return maxMatchedConfigCount;
    }
    
    /** 从 {@link com.alibaba.nacos.sys.env.EnvUtil} 读取 nacos.config.* 相关环境配置 */
    @Override
    protected void getConfigFromEnv() {
        maxPushRetryTimes =
            EnvUtil.getProperty("nacos.config.push.maxRetryTime", Integer.class, 50);
        pushTimeout = EnvUtil.getProperty("nacos.config.push.timeout", Long.class, 3000L);
        batchSize = EnvUtil.getProperty("nacos.config.push.batchSize", Integer.class, 20);
        derbyOpsEnabled =
            EnvUtil.getProperty("nacos.config.derby.ops.enabled", Boolean.class, false);
        
        maxPatternCount =
            EnvUtil.getProperty("nacos.config.fuzzy.watch.max.pattern.count", Integer.class, 20);
        maxMatchedConfigCount =
            EnvUtil.getProperty("nacos.config.fuzzy.watch.max.pattern.match.config.count",
                Integer.class, 500);
    }
    
    @Override
    protected String printConfig() {
        return toString();
    }
    
    @Override
    public String toString() {
        return "ConfigCommonConfig{" + "maxPushRetryTimes=" + maxPushRetryTimes
            + ", derbyOpsEnabled=" + derbyOpsEnabled
            + '}';
    }
}
