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

package com.alibaba.nacos.config.server.utils;

import com.alibaba.nacos.config.server.constant.PropertiesConstant;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.nacos.config.server.utils.LogUtil.FATAL_LOG;

/**
 * 配置模块运行时属性门面：Spring 启动时从 EnvUtil 加载通知超时、容量配额、dump 分页等可调参数。
 * Properties util.
 *
 * @author Nacos
 */
public class PropertyUtil implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private static final Logger LOGGER = LogUtil.DEFAULT_LOG;
    
    /** 配置变更通知连接超时（毫秒） */
    private static int notifyConnectTimeout = 100;
    
    /** 配置变更通知 Socket 读超时（毫秒） */
    private static int notifySocketTimeout = 200;
    
    /** 健康检查连续失败次数上限，超过则标记节点不可用 */
    private static int maxHealthCheckFailCount = 12;
    
    /** 是否启用配置客户端健康检查 */
    private static boolean isHealthCheck = true;
    
    /** 单条配置内容最大字节数（HTTP 发布上限） */
    private static int maxContent = 10 * 1024 * 1024;
    
    /**
     * 是否启用容量管理功能。
     * Whether to enable capacity management.
     */
    private static boolean isManageCapacity = true;
    
    /**
     * 灰度兼容模式：是否将 beta/tag 持久化到旧模型。
     * gray compatible model.
     */
    private static boolean grayCompatibleModel = true;
    
    /** 灰度迁移过程线程局部标记 */
    public static final ThreadLocal<Boolean> GRAY_MIGRATE_FLAG =
        ThreadLocal.withInitial(() -> false);
    
    // CONFIG_MIGRATE_FLAG has been replaced by {@link ConfigPersistContext}.
    
    /**
     * 是否启用容量上限校验（配置条数、内容大小等）。
     * Whether to enable the limit check function of capacity management, including the upper limit of configuration
     * number, configuration content size limit, etc.
     */
    private static boolean isCapacityLimitCheck = false;
    
    /**
     * 集群级默认配置条数配额。
     * The default cluster capacity limit.
     */
    private static int defaultClusterQuota = 100000;
    
    /**
     * 单 Group 默认配置条数配额。
     * the default capacity limit per Group.
     */
    private static int defaultGroupQuota = 200;
    
    /**
     * 单 Tenant 默认配置条数配额。
     * The default capacity limit per Tenant.
     */
    private static int defaultTenantQuota = 200;
    
    /**
     * 单条配置内容默认最大字节数。
     * The maximum size of the content in the configuration of a single, unit for bytes.
     */
    private static int defaultMaxSize = 100 * 1024;
    
    /**
     * 聚合配置默认最大子项数。
     * The default Maximum number of aggregated data.
     */
    private static int defaultMaxAggrCount = 10000;
    
    /**
     * 聚合配置单个子项内容默认最大字节数。
     * The maximum size of content in a single subconfiguration of aggregated data.
     */
    private static int defaultMaxAggrSize = 1024;
    
    /**
     * 容量达限时初始扩容百分比。
     * Initialize the expansion percentage of capacity has reached the limit.
     */
    private static int initialExpansionPercent = 100;
    
    /**
     * 容量 usage 表校正间隔（秒）。
     * Fixed capacity information table usage (usage) time interval, the unit is in seconds.
     */
    private static int correctUsageDelay = 10 * 60;
    
    /** 是否启用 dumpChange 增量同步 */
    private static boolean dumpChangeOn = true;
    
    /**
     * 配置历史保留天数，默认 30 天。
     * The number of days to retain the configuration history, the default is 30 days.
     */
    private static int configRententionDays = 30;
    
    /**
     * dumpChangeWorker 执行间隔，默认 30 秒。
     * dumpChangeWorkerInterval, default 30 seconds.
     */
    private static long dumpChangeWorkerInterval = 30 * 1000L;
    
    /** 是否开启 dumpChange 增量 dump */
    public static boolean isDumpChangeOn() {
        return dumpChangeOn;
    }
    
    public static void setDumpChangeOn(boolean dumpChangeOn) {
        PropertyUtil.dumpChangeOn = dumpChangeOn;
    }
    
    /** dumpChangeWorker 调度间隔（毫秒） */
    public static long getDumpChangeWorkerInterval() {
        return dumpChangeWorkerInterval;
    }
    
    public static void setDumpChangeWorkerInterval(long dumpChangeWorkerInterval) {
        PropertyUtil.dumpChangeWorkerInterval = dumpChangeWorkerInterval;
    }
    
    /** 通知连接超时（毫秒） */
    public static int getNotifyConnectTimeout() {
        return notifyConnectTimeout;
    }
    
    public static void setNotifyConnectTimeout(int notifyConnectTimeout) {
        PropertyUtil.notifyConnectTimeout = notifyConnectTimeout;
    }
    
    /** 通知 Socket 超时（毫秒） */
    public static int getNotifySocketTimeout() {
        return notifySocketTimeout;
    }
    
    public static void setNotifySocketTimeout(int notifySocketTimeout) {
        PropertyUtil.notifySocketTimeout = notifySocketTimeout;
    }
    
    /** 健康检查最大连续失败次数 */
    public static int getMaxHealthCheckFailCount() {
        return maxHealthCheckFailCount;
    }
    
    public static void setMaxHealthCheckFailCount(int maxHealthCheckFailCount) {
        PropertyUtil.maxHealthCheckFailCount = maxHealthCheckFailCount;
    }
    
    /** 是否启用健康检查 */
    public static boolean isHealthCheck() {
        return isHealthCheck;
    }
    
    public static void setHealthCheck(boolean isHealthCheck) {
        PropertyUtil.isHealthCheck = isHealthCheck;
    }
    
    /** HTTP 发布内容最大字节数 */
    public static int getMaxContent() {
        return maxContent;
    }
    
    public static void setMaxContent(int maxContent) {
        PropertyUtil.maxContent = maxContent;
    }
    
    /** 是否启用容量管理 */
    public static boolean isManageCapacity() {
        return isManageCapacity;
    }
    
    public static void setManageCapacity(boolean isManageCapacity) {
        PropertyUtil.isManageCapacity = isManageCapacity;
    }
    
    /** 集群默认配置条数配额 */
    public static int getDefaultClusterQuota() {
        return defaultClusterQuota;
    }
    
    public static void setDefaultClusterQuota(int defaultClusterQuota) {
        PropertyUtil.defaultClusterQuota = defaultClusterQuota;
    }
    
    /** 是否启用容量上限校验 */
    public static boolean isCapacityLimitCheck() {
        return isCapacityLimitCheck;
    }
    
    public static void setCapacityLimitCheck(boolean isCapacityLimitCheck) {
        PropertyUtil.isCapacityLimitCheck = isCapacityLimitCheck;
    }
    
    /** 单 Group 默认配额 */
    public static int getDefaultGroupQuota() {
        return defaultGroupQuota;
    }
    
    public static void setDefaultGroupQuota(int defaultGroupQuota) {
        PropertyUtil.defaultGroupQuota = defaultGroupQuota;
    }
    
    /** 单 Tenant 默认配额 */
    public static int getDefaultTenantQuota() {
        return defaultTenantQuota;
    }
    
    public static void setDefaultTenantQuota(int defaultTenantQuota) {
        PropertyUtil.defaultTenantQuota = defaultTenantQuota;
    }
    
    /** 容量达限初始扩容百分比 */
    public static int getInitialExpansionPercent() {
        return initialExpansionPercent;
    }
    
    public static void setInitialExpansionPercent(int initialExpansionPercent) {
        PropertyUtil.initialExpansionPercent = initialExpansionPercent;
    }
    
    /** 单条配置默认最大字节数 */
    public static int getDefaultMaxSize() {
        return defaultMaxSize;
    }
    
    public static void setDefaultMaxSize(int defaultMaxSize) {
        PropertyUtil.defaultMaxSize = defaultMaxSize;
    }
    
    /** 聚合配置默认最大子项数 */
    public static int getDefaultMaxAggrCount() {
        return defaultMaxAggrCount;
    }
    
    public static void setDefaultMaxAggrCount(int defaultMaxAggrCount) {
        PropertyUtil.defaultMaxAggrCount = defaultMaxAggrCount;
    }
    
    /**
     * 是否启用灰度兼容模式（beta/tag 写旧模型）。
     * control whether persist beta and tag to old model.
     *
     * @return
     */
    public static boolean isGrayCompatibleModel() {
        return grayCompatibleModel;
    }
    
    public static void setGrayCompatibleModel(boolean grayCompatibleModel) {
        PropertyUtil.grayCompatibleModel = grayCompatibleModel;
    }
    
    /** 聚合单子项默认最大字节数 */
    public static int getDefaultMaxAggrSize() {
        return defaultMaxAggrSize;
    }
    
    public static void setDefaultMaxAggrSize(int defaultMaxAggrSize) {
        PropertyUtil.defaultMaxAggrSize = defaultMaxAggrSize;
    }
    
    /** 容量 usage 校正间隔（秒） */
    public static int getCorrectUsageDelay() {
        return correctUsageDelay;
    }
    
    public static void setCorrectUsageDelay(int correctUsageDelay) {
        PropertyUtil.correctUsageDelay = correctUsageDelay;
    }
    
    /** 配置历史保留天数 */
    public static int getConfigRententionDays() {
        return configRententionDays;
    }
    
    /** 从配置读取并设置历史保留天数 */
    private void setConfigRententionDays() {
        String val = getProperty(PropertiesConstant.CONFIG_RENTENTION_DAYS);
        if (null != val) {
            int tmp = 0;
            try {
                tmp = Integer.parseInt(val);
                if (tmp > 0) {
                    PropertyUtil.configRententionDays = tmp;
                }
            } catch (NumberFormatException nfe) {
                FATAL_LOG.error("read nacos.config.retention.days wrong", nfe);
            }
        }
    }
    
    /** 是否为单机模式 */
    public static boolean isStandaloneMode() {
        return EnvUtil.getStandaloneMode();
    }
    
    /** 从 EnvUtil 批量加载全部可调属性 */
    private void loadSetting() {
        try {
            setNotifyConnectTimeout(
                Integer.parseInt(EnvUtil.getProperty(PropertiesConstant.NOTIFY_CONNECT_TIMEOUT,
                    String.valueOf(notifyConnectTimeout))));
            LOGGER.info("notifyConnectTimeout:{}", notifyConnectTimeout);
            setNotifySocketTimeout(
                Integer.parseInt(EnvUtil.getProperty(PropertiesConstant.NOTIFY_SOCKET_TIMEOUT,
                    String.valueOf(notifySocketTimeout))));
            LOGGER.info("notifySocketTimeout:{}", notifySocketTimeout);
            setHealthCheck(Boolean.parseBoolean(
                EnvUtil.getProperty(PropertiesConstant.IS_HEALTH_CHECK,
                    String.valueOf(isHealthCheck))));
            LOGGER.info("isHealthCheck:{}", isHealthCheck);
            setMaxHealthCheckFailCount(Integer.parseInt(
                EnvUtil.getProperty(PropertiesConstant.MAX_HEALTH_CHECK_FAIL_COUNT,
                    String.valueOf(maxHealthCheckFailCount))));
            LOGGER.info("maxHealthCheckFailCount:{}", maxHealthCheckFailCount);
            setMaxContent(
                Integer.parseInt(EnvUtil.getProperty(PropertiesConstant.MAX_CONTENT,
                    String.valueOf(maxContent))));
            LOGGER.info("maxContent:{}", maxContent);
            // capacity management
            setManageCapacity(getBoolean(PropertiesConstant.IS_MANAGE_CAPACITY, isManageCapacity));
            setCapacityLimitCheck(
                getBoolean(PropertiesConstant.IS_CAPACITY_LIMIT_CHECK, isCapacityLimitCheck));
            setDefaultClusterQuota(
                getInt(PropertiesConstant.DEFAULT_CLUSTER_QUOTA, defaultClusterQuota));
            setDefaultGroupQuota(getInt(PropertiesConstant.DEFAULT_GROUP_QUOTA, defaultGroupQuota));
            setDefaultTenantQuota(
                getInt(PropertiesConstant.DEFAULT_TENANT_QUOTA, defaultTenantQuota));
            setDefaultMaxSize(getInt(PropertiesConstant.DEFAULT_MAX_SIZE, defaultMaxSize));
            setDefaultMaxAggrCount(
                getInt(PropertiesConstant.DEFAULT_MAX_AGGR_COUNT, defaultMaxAggrCount));
            setDefaultMaxAggrSize(
                getInt(PropertiesConstant.DEFAULT_MAX_AGGR_SIZE, defaultMaxAggrSize));
            setCorrectUsageDelay(getInt(PropertiesConstant.CORRECT_USAGE_DELAY, correctUsageDelay));
            setInitialExpansionPercent(
                getInt(PropertiesConstant.INITIAL_EXPANSION_PERCENT, initialExpansionPercent));
            setConfigRententionDays();
            setDumpChangeOn(getBoolean(PropertiesConstant.DUMP_CHANGE_ON, dumpChangeOn));
            setDumpChangeWorkerInterval(
                getLong(PropertiesConstant.DUMP_CHANGE_WORKER_INTERVAL, dumpChangeWorkerInterval));
            setGrayCompatibleModel(
                getBoolean(PropertiesConstant.GRAY_CAPATIBEL_MODEL, grayCompatibleModel));
            
        } catch (Exception e) {
            LOGGER.error("read application.properties failed", e);
            throw e;
        }
    }
    
    /** 读取布尔型配置 */
    private boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getString(key, String.valueOf(defaultValue)));
    }
    
    /** 读取整型配置 */
    private int getInt(String key, int defaultValue) {
        return Integer.parseInt(getString(key, String.valueOf(defaultValue)));
    }
    
    /** 读取长整型配置 */
    private long getLong(String key, long defaultValue) {
        return Long.parseLong(getString(key, String.valueOf(defaultValue)));
    }
    
    /** 读取字符串配置，缺失时用默认值 */
    private String getString(String key, String defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        LOGGER.info("{}:{}", key, value);
        return value;
    }
    
    /** 从 EnvUtil 读取属性 */
    public String getProperty(String key) {
        return EnvUtil.getProperty(key);
    }
    
    /** 从 EnvUtil 读取属性，带默认值 */
    public String getProperty(String key, String defaultValue) {
        return EnvUtil.getProperty(key, defaultValue);
    }
    
    /** Spring 容器初始化回调，触发 loadSetting */
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        loadSetting();
    }
    
    /** 全量 dump 分页大小上限 */
    private static final int MAX_DUMP_PAGE = 1000;
    
    /** 全量 dump 分页大小下限 */
    private static final int MIN_DUMP_PAGE = 50;
    
    /** 每 512MB 内存对应的分页增量 */
    private static final int PAGE_MEMORY_DIVIDE_MB = 512;
    
    /** 懒加载的全量 dump 分页大小 */
    private static AtomicInteger allDumpPageSize;
    
    /**
     * 按容器/JVM 内存限制计算全量 dump 每页条数，512MB→50 条线性缩放，限制在 [50,1000]。
     */
    public static int getAllDumpPageSize() {
        if (allDumpPageSize == null) {
            allDumpPageSize = new AtomicInteger(initAllDumpPageSize());
        }
        return allDumpPageSize.get();
    }
    
    /** 根据内存上限初始化 dump 分页大小 */
    static int initAllDumpPageSize() {
        long memLimitMb = getMemLimitMb();
        
        //512MB->50 Page Size
        int pageSize = (int) ((float) memLimitMb / PAGE_MEMORY_DIVIDE_MB) * MIN_DUMP_PAGE;
        pageSize = Math.max(pageSize, MIN_DUMP_PAGE);
        pageSize = Math.min(pageSize, MAX_DUMP_PAGE);
        LOGGER.info("All dump page size is set to {} according to mem limit {} MB", pageSize,
            memLimitMb);
        return pageSize;
    }
    
    /** 获取内存上限（MB）：优先 cgroup 文件，否则 JVM maxHeap */
    public static long getMemLimitMb() {
        Optional<Long> memoryLimit = findMemoryLimitFromFile();
        if (memoryLimit.isPresent()) {
            return memoryLimit.get();
        }
        memoryLimit = findMemoryLimitFromSystem();
        return memoryLimit.get();
    }
    
    /** cgroup memory.limit_in_bytes 文件路径 */
    private static String limitMemoryFile;
    
    /** 从 cgroup 内存限制文件读取上限并转为 MB */
    private static Optional<Long> findMemoryLimitFromFile() {
        if (limitMemoryFile == null) {
            limitMemoryFile = EnvUtil.getProperty("memory_limit_file_path",
                "/sys/fs/cgroup/memory/memory.limit_in_bytes");
        }
        File file = new File(limitMemoryFile);
        try (BufferedReader reader =
            Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            long memoryLimit = Long.parseLong(reader.readLine().trim());
            return Optional.of(memoryLimit / 1024L / 1024L);
        } catch (IOException | NumberFormatException ignored) {
            return Optional.empty();
        }
    }
    
    /** 回退使用 JVM maxMemory 作为内存上限（MB） */
    private static Optional<Long> findMemoryLimitFromSystem() {
        long maxHeapSizeMb = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
        return Optional.of(maxHeapSizeMb);
    }
    
}
