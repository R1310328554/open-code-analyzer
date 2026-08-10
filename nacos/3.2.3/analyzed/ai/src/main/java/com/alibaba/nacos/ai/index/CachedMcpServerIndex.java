/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.index;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;

import com.alibaba.nacos.ai.constant.Constants;
import com.alibaba.nacos.ai.model.mcp.McpServerIndexData;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.utils.StringUtils;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.service.ConfigDetailService;
import com.alibaba.nacos.config.server.service.query.ConfigQueryChainService;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainRequest;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse;
import com.alibaba.nacos.core.service.NamespaceOperationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced MCP cache index implementation combining memory cache and database queries.
 * <p>带缓存的 MCP 服务索引实现，优先读 {@link McpCacheIndex}，未命中时回源数据库并回填缓存；支持定时全量同步。</p>
 *
 * @author misselvexu
 */
public class CachedMcpServerIndex extends AbstractMcpServerIndex {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedMcpServerIndex.class);
    
    /** 内存缓存索引，维护名称/ID 双向映射。 */
    private final McpCacheIndex cacheIndex;
    
    /** 配置查询链服务，用于按 ID 精确查库。 */
    private final ConfigQueryChainService configQueryChainService;
    
    /** 定时任务调度器，驱动缓存同步。 */
    private final ScheduledExecutorService scheduledExecutor;
    
    /** 缓存同步定时任务的 Future，销毁时取消。 */
    private ScheduledFuture<?> syncTask;
    
    /** 是否启用缓存；关闭时所有查询直接走数据库。 */
    private final boolean cacheEnabled;
    
    /** 缓存同步间隔（秒）。 */
    private final long syncInterval;
    
    /**
     * Constructor.
     * <p>构造缓存索引；若 {@code cacheEnabled} 为 true 则立即启动定时同步任务。</p>
     */
    public CachedMcpServerIndex(ConfigDetailService configDetailService,
        NamespaceOperationService namespaceOperationService,
        ConfigQueryChainService configQueryChainService,
        McpCacheIndex cacheIndex, ScheduledExecutorService scheduledExecutor, boolean cacheEnabled,
        long syncInterval) {
        super(namespaceOperationService, configDetailService);
        this.configQueryChainService = configQueryChainService;
        this.cacheIndex = cacheIndex;
        this.scheduledExecutor = scheduledExecutor;
        this.cacheEnabled = cacheEnabled;
        this.syncInterval = syncInterval;
        if (cacheEnabled) {
            startSyncTask();
        }
        LOGGER.info("CachedMcpServerIndex initialized with cacheEnabled={}, syncInterval={}s",
            cacheEnabled,
            syncInterval);
    }
    
    /**
     * Get MCP server information by ID.
     * <p>按 MCP ID 查询：缓存命中直接返回，未命中则遍历命名空间查库并回填。</p>
     */
    @Override
    public McpServerIndexData getMcpServerById(String id) {
        if (!cacheEnabled) {
            LOGGER.debug("Cache disabled, querying directly from database for mcpId: {}", id);
            return getMcpServerByIdFromDatabase(id);
        }
        // 优先查询缓存
        McpServerIndexData cachedData = cacheIndex.getMcpServerById(id);
        if (cachedData != null) {
            LOGGER.debug("Cache hit for mcpId: {}", id);
            return cachedData;
        }
        // 缓存未命中，回源数据库
        LOGGER.debug("Cache miss for mcpId: {}, querying database", id);
        McpServerIndexData dbData = getMcpServerByIdFromDatabase(id);
        if (dbData != null) {
            cacheIndex.updateIndex(dbData.getNamespaceId(), dbData.getId(), dbData.getId());
            LOGGER.debug("Updated cache for mcpId: {}", id);
        }
        return dbData;
    }
    
    /**
     * Get MCP server information by name.
     * <p>按命名空间与服务名查询；命名空间为空时跨空间查找首个匹配项。</p>
     */
    @Override
    public McpServerIndexData getMcpServerByName(String namespaceId, String name) {
        if (StringUtils.isEmpty(namespaceId) && StringUtils.isEmpty(name)) {
            LOGGER.warn("Invalid parameters for getMcpServerByName: namespaceId={}, name={}",
                namespaceId, name);
            return null;
        }
        
        if (StringUtils.isEmpty(namespaceId)) {
            return getFirstMcpServerByName(name);
        }
        
        if (!cacheEnabled) {
            LOGGER.debug("Cache disabled, querying directly from database for name: {}:{}",
                namespaceId, name);
            return getMcpServerByNameFromDatabase(namespaceId, name);
        }
        // Priority query cache
        McpServerIndexData cachedData = cacheIndex.getMcpServerByName(namespaceId, name);
        if (cachedData != null) {
            LOGGER.debug("Cache hit for name: {}:{}", namespaceId, name);
            return cachedData;
        }
        // Cache miss, query database
        LOGGER.debug("Cache miss for name: {}:{}, querying database", namespaceId, name);
        McpServerIndexData dbData = getMcpServerByNameFromDatabase(namespaceId, name);
        if (dbData != null) {
            cacheIndex.updateIndex(namespaceId, name, dbData.getId());
            LOGGER.debug("Updated cache for name: {}:{}", namespaceId, name);
        }
        return dbData;
    }
    
    @Override
    protected void afterSearch(McpServerIndexData indexData, String name) {
        // 搜索完成后更新缓存映射
        if (cacheEnabled) {
            cacheIndex.updateIndex(indexData.getNamespaceId(), name, indexData.getId());
        }
    }
    
    /**
     * Get MCP server from database by ID.
     * <p>遍历全部命名空间，按版本 dataId 后缀查配置链，找到即返回索引数据。</p>
     */
    private McpServerIndexData getMcpServerByIdFromDatabase(String id) {
        ConfigQueryChainRequest request = new ConfigQueryChainRequest();
        request.setDataId(id + Constants.MCP_SERVER_VERSION_DATA_ID_SUFFIX);
        request.setGroup(Constants.MCP_SERVER_VERSIONS_GROUP);
        List<String> namespaceList = fetchOrderedNamespaceList();
        for (String namespaceId : namespaceList) {
            request.setTenant(namespaceId);
            ConfigQueryChainResponse response = configQueryChainService.handle(request);
            if (response
                .getStatus() == ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_FOUND_FORMAL) {
                McpServerIndexData result = new McpServerIndexData();
                result.setId(id);
                result.setNamespaceId(namespaceId);
                LOGGER.debug("Found MCP server in database: mcpId={}, namespaceId={}", id,
                    namespaceId);
                return result;
            }
        }
        LOGGER.debug("MCP server not found in database: mcpId={}", id);
        return null;
    }
    
    /**
     * Get MCP server from database by name.
     * <p>精确搜索单条配置并解析 MCP ID，避免经分页接口重复写缓存。</p>
     */
    private McpServerIndexData getMcpServerByNameFromDatabase(String namespaceId, String name) {
        // 直接查询数据库，避免调用searchMcpServerByName导致重复更新缓存
        Page<ConfigInfo> serverInfos =
            searchMcpServers(namespaceId, name, Constants.MCP_LIST_SEARCH_ACCURATE, 1, 1);
        if (CollectionUtils.isNotEmpty(serverInfos.getPageItems())) {
            ConfigInfo configInfo = serverInfos.getPageItems().get(0);
            McpServerIndexData result = new McpServerIndexData();
            result.setId(
                configInfo.getDataId().replace(Constants.MCP_SERVER_VERSION_DATA_ID_SUFFIX, ""));
            result.setNamespaceId(configInfo.getTenant());
            LOGGER.debug("Found MCP server in database: name={}:{}, mcpId={}", namespaceId, name,
                result.getId());
            return result;
        }
        LOGGER.debug("MCP server not found in database: name={}:{}", namespaceId, name);
        return null;
    }
    
    /**
     * Start scheduled sync task.
     * <p>以固定延迟调度 {@link #syncCacheFromDatabase}，异常仅记录日志不中断调度。</p>
     */
    private void startSyncTask() {
        syncTask = scheduledExecutor.scheduleWithFixedDelay(() -> {
            try {
                LOGGER.debug("Starting cache sync task");
                syncCacheFromDatabase();
                LOGGER.debug("Cache sync task completed");
            } catch (Exception e) {
                LOGGER.error("Error during cache sync task", e);
            }
        }, syncInterval, syncInterval, TimeUnit.SECONDS);
        LOGGER.info("Cache sync task started with interval: {}s", syncInterval);
    }
    
    /**
     * Shutdown the cache sync task and cleanup resources.
     * <p>Bean 销毁时取消同步任务并关闭调度线程池。</p>
     */
    @PreDestroy
    public void destroy() {
        try {
            if (syncTask != null) {
                syncTask.cancel(true);
            }
            scheduledExecutor.shutdown();
        } catch (Exception e) {
            LOGGER.warn("shutting down sync task schedule executor failed", e);
        }
    }
    
    /**
     * Sync cache from database.
     * <p>对每个命名空间执行模糊分页搜索，间接预热全部 MCP 名称→ID 缓存。</p>
     */
    private void syncCacheFromDatabase() {
        LOGGER.debug("Syncing cache from database");
        List<String> namespaceList = fetchOrderedNamespaceList();
        for (String namespaceId : namespaceList) {
            try {
                searchMcpServerByNameWithPage(namespaceId, null,
                    Constants.MCP_LIST_SEARCH_BLUR, 1, 1000);
            } catch (Exception e) {
                LOGGER.error("Error syncing cache for namespace: {}", namespaceId, e);
            }
        }
    }
    
    /**
     * Get cache statistics.
     * <p>返回命中率、未命中、驱逐次数与当前条目数等统计信息。</p>
     */
    public McpCacheIndex.CacheStats getCacheStats() {
        McpCacheIndex.CacheStats stats = cacheIndex.getStats();
        LOGGER.debug(
            "Cache stats: hitCount={}, missCount={}, evictionCount={}, size={}, hitRate=%.2f%%",
            stats.getHitCount(), stats.getMissCount(), stats.getEvictionCount(), stats.getSize(),
            stats.getHitRate() * 100);
        return stats;
    }
    
    /**
     * Clear cache.
     * <p>清空底层 {@link McpCacheIndex} 全部条目。</p>
     */
    public void clearCache() {
        cacheIndex.clear();
        LOGGER.info("Cache cleared");
    }
    
    /**
     * Manually trigger cache synchronization.
     * <p>手动触发一次全量缓存同步；缓存禁用时忽略。</p>
     */
    public void triggerCacheSync() {
        if (cacheEnabled) {
            LOGGER.info("Manual cache sync triggered");
            syncCacheFromDatabase();
        } else {
            LOGGER.warn("Cache is disabled, manual sync ignored");
        }
    }
    
    /**
     * Remove cache entry by namespace ID and MCP server name.
     * <p>按命名空间与服务名移除缓存条目；缓存禁用时为 no-op。</p>
     *
     * @param namespaceId namespace ID
     * @param mcpName     MCP server name
     */
    @Override
    public void removeMcpServerByName(String namespaceId, String mcpName) {
        if (cacheEnabled) {
            LOGGER.debug("Removing cache entry by name: namespaceId={}, mcpName={}", namespaceId,
                mcpName);
            cacheIndex.removeIndex(namespaceId, mcpName);
        } else {
            LOGGER.debug(
                "Cache is disabled, ignoring cache removal by name: namespaceId={}, mcpName={}",
                namespaceId,
                mcpName);
        }
    }
    
    /**
     * Remove cache entry by MCP server ID.
     * <p>按 MCP ID 移除缓存条目及关联的名称映射。</p>
     *
     * @param mcpId MCP server ID
     */
    @Override
    public void removeMcpServerById(String mcpId) {
        if (cacheEnabled) {
            LOGGER.debug("Removing cache entry by ID: mcpId={}", mcpId);
            cacheIndex.removeIndex(mcpId);
        } else {
            LOGGER.debug("Cache is disabled, ignoring cache removal by ID: mcpId={}", mcpId);
        }
    }
}
