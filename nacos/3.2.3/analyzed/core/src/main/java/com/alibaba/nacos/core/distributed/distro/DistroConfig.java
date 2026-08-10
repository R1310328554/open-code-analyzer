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

package com.alibaba.nacos.core.distributed.distro;

import com.alibaba.nacos.core.config.AbstractDynamicConfig;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * Distro 协议动态配置：管理数据同步、校验与加载相关的延迟、超时与重试参数，支持从环境变量热加载。
 * Distro configuration.
 *
 * @author xiweng.yy
 */
public class DistroConfig extends AbstractDynamicConfig {
    
    /** 动态配置模块名。 */
    private static final String DISTRO = "Distro";
    
    /** 单例实例。 */
    private static final DistroConfig INSTANCE = new DistroConfig();
    
    /** 数据同步默认延迟（毫秒）。 */
    private long syncDelayMillis = DistroConstants.DEFAULT_DATA_SYNC_DELAY_MILLISECONDS;
    
    /** 单次同步超时（毫秒）。 */
    private long syncTimeoutMillis = DistroConstants.DEFAULT_DATA_SYNC_TIMEOUT_MILLISECONDS;
    
    /** 同步失败后的重试间隔（毫秒）。 */
    private long syncRetryDelayMillis = DistroConstants.DEFAULT_DATA_SYNC_RETRY_DELAY_MILLISECONDS;
    
    /** 数据校验定时任务间隔（毫秒）。 */
    private long verifyIntervalMillis = DistroConstants.DEFAULT_DATA_VERIFY_INTERVAL_MILLISECONDS;
    
    /** 单次校验超时（毫秒）。 */
    private long verifyTimeoutMillis = DistroConstants.DEFAULT_DATA_VERIFY_TIMEOUT_MILLISECONDS;
    
    /** 全量加载失败后的重试间隔（毫秒）。 */
    private long loadDataRetryDelayMillis =
        DistroConstants.DEFAULT_DATA_LOAD_RETRY_DELAY_MILLISECONDS;
    
    /** 全量加载超时（毫秒）。 */
    private long loadDataTimeoutMillis = DistroConstants.DEFAULT_DATA_LOAD_TIMEOUT_MILLISECONDS;
    
    /** 私有构造：注册 Distro 模块并加载默认配置。 */
    private DistroConfig() {
        super(DISTRO);
        resetConfig();
    }
    
    /** 从环境变量/配置中心刷新各 Distro 超时与间隔参数。 */
    @Override
    protected void getConfigFromEnv() {
        syncDelayMillis =
            EnvUtil.getProperty(DistroConstants.DATA_SYNC_DELAY_MILLISECONDS, Long.class,
                DistroConstants.DEFAULT_DATA_SYNC_DELAY_MILLISECONDS);
        syncTimeoutMillis =
            EnvUtil.getProperty(DistroConstants.DATA_SYNC_TIMEOUT_MILLISECONDS, Long.class,
                DistroConstants.DEFAULT_DATA_SYNC_TIMEOUT_MILLISECONDS);
        syncRetryDelayMillis =
            EnvUtil.getProperty(DistroConstants.DATA_SYNC_RETRY_DELAY_MILLISECONDS, Long.class,
                DistroConstants.DEFAULT_DATA_SYNC_RETRY_DELAY_MILLISECONDS);
        verifyIntervalMillis =
            EnvUtil.getProperty(DistroConstants.DATA_VERIFY_INTERVAL_MILLISECONDS, Long.class,
                DistroConstants.DEFAULT_DATA_VERIFY_INTERVAL_MILLISECONDS);
        verifyTimeoutMillis =
            EnvUtil.getProperty(DistroConstants.DATA_VERIFY_TIMEOUT_MILLISECONDS, Long.class,
                DistroConstants.DEFAULT_DATA_VERIFY_TIMEOUT_MILLISECONDS);
        loadDataRetryDelayMillis =
            EnvUtil.getProperty(DistroConstants.DATA_LOAD_RETRY_DELAY_MILLISECONDS, Long.class,
                DistroConstants.DEFAULT_DATA_LOAD_RETRY_DELAY_MILLISECONDS);
        loadDataTimeoutMillis =
            EnvUtil.getProperty(DistroConstants.DATA_LOAD_TIMEOUT_MILLISECONDS, Long.class,
                DistroConstants.DEFAULT_DATA_LOAD_TIMEOUT_MILLISECONDS);
    }
    
    /** 返回全局单例配置。 */
    public static DistroConfig getInstance() {
        return INSTANCE;
    }
    
    /** 获取同步延迟（毫秒）。 */
    public long getSyncDelayMillis() {
        return syncDelayMillis;
    }
    
    /** 设置同步延迟（毫秒）。 */
    public void setSyncDelayMillis(long syncDelayMillis) {
        this.syncDelayMillis = syncDelayMillis;
    }
    
    /** 获取同步超时（毫秒）。 */
    public long getSyncTimeoutMillis() {
        return syncTimeoutMillis;
    }
    
    /** 设置同步超时（毫秒）。 */
    public void setSyncTimeoutMillis(long syncTimeoutMillis) {
        this.syncTimeoutMillis = syncTimeoutMillis;
    }
    
    /** 获取同步重试间隔（毫秒）。 */
    public long getSyncRetryDelayMillis() {
        return syncRetryDelayMillis;
    }
    
    /** 设置同步重试间隔（毫秒）。 */
    public void setSyncRetryDelayMillis(long syncRetryDelayMillis) {
        this.syncRetryDelayMillis = syncRetryDelayMillis;
    }
    
    /** 获取校验任务间隔（毫秒）。 */
    public long getVerifyIntervalMillis() {
        return verifyIntervalMillis;
    }
    
    /** 设置校验任务间隔（毫秒）。 */
    public void setVerifyIntervalMillis(long verifyIntervalMillis) {
        this.verifyIntervalMillis = verifyIntervalMillis;
    }
    
    /** 获取校验超时（毫秒）。 */
    public long getVerifyTimeoutMillis() {
        return verifyTimeoutMillis;
    }
    
    /** 设置校验超时（毫秒）。 */
    public void setVerifyTimeoutMillis(long verifyTimeoutMillis) {
        this.verifyTimeoutMillis = verifyTimeoutMillis;
    }
    
    /** 获取全量加载重试间隔（毫秒）。 */
    public long getLoadDataRetryDelayMillis() {
        return loadDataRetryDelayMillis;
    }
    
    /** 设置全量加载重试间隔（毫秒）。 */
    public void setLoadDataRetryDelayMillis(long loadDataRetryDelayMillis) {
        this.loadDataRetryDelayMillis = loadDataRetryDelayMillis;
    }
    
    /** 获取全量加载超时（毫秒）。 */
    public long getLoadDataTimeoutMillis() {
        return loadDataTimeoutMillis;
    }
    
    /** 设置全量加载超时（毫秒）。 */
    public void setLoadDataTimeoutMillis(long loadDataTimeoutMillis) {
        this.loadDataTimeoutMillis = loadDataTimeoutMillis;
    }
    
    /** 输出当前 Distro 配置快照字符串。 */
    @Override
    protected String printConfig() {
        return "DistroConfig{" + "syncDelayMillis=" + syncDelayMillis + ", syncTimeoutMillis="
            + syncTimeoutMillis
            + ", syncRetryDelayMillis=" + syncRetryDelayMillis + ", verifyIntervalMillis="
            + verifyIntervalMillis
            + ", verifyTimeoutMillis=" + verifyTimeoutMillis + ", loadDataRetryDelayMillis="
            + loadDataRetryDelayMillis
            + ", loadDataTimeoutMillis=" + loadDataTimeoutMillis + '}';
    }
}
