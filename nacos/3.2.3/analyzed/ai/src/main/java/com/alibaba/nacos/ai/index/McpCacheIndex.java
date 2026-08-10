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

import com.alibaba.nacos.ai.model.mcp.McpServerIndexData;

/**
 * MCP cache index interface providing fast mapping between MCP Name and MCP ID.
 * <p>MCP 缓存索引接口，提供命名空间+服务名与 MCP ID 之间的高速双向映射，并暴露缓存统计与清理能力。</p>
 *
 * @author misselvexu
 */
public interface McpCacheIndex {
    
    /**
     * Get MCP ID by namespace ID and MCP name.
     * <p>根据命名空间与服务名解析 MCP ID，未命中返回 null。</p>
     *
     * @param namespaceId namespace ID
     * @param mcpName     MCP name
     * @return MCP ID, returns null if not found
     */
    String getMcpId(String namespaceId, String mcpName);
    
    /**
     * Get MCP server information by namespace ID and MCP name.
     * <p>按命名空间与服务名返回完整索引数据。</p>
     *
     * @param namespaceId namespace ID
     * @param mcpName     MCP name
     * @return MCP server information, returns null if not found
     */
    McpServerIndexData getMcpServerByName(String namespaceId, String mcpName);
    
    /**
     * Get MCP server information by MCP ID.
     * <p>按 MCP ID 返回索引数据。</p>
     *
     * @param mcpId MCP ID
     * @return MCP server information, returns null if not found
     */
    McpServerIndexData getMcpServerById(String mcpId);
    
    /**
     * Update index.
     * <p>写入或刷新名称→ID 映射及 ID→索引数据条目。</p>
     *
     * @param namespaceId namespace ID
     * @param mcpName     MCP name
     * @param mcpId       MCP ID
     */
    void updateIndex(String namespaceId, String mcpName, String mcpId);
    
    /**
     * Remove index by name.
     * <p>按命名空间与服务名删除缓存条目。</p>
     *
     * @param namespaceId namespace ID
     * @param mcpName     MCP name
     */
    void removeIndex(String namespaceId, String mcpName);
    
    /**
     * Remove index by ID.
     * <p>按 MCP ID 删除缓存条目及关联名称映射。</p>
     *
     * @param mcpId MCP ID
     */
    void removeIndex(String mcpId);
    
    /**
     * Clear cache.
     * <p>清空全部缓存条目并重置统计（实现类决定细节）。</p>
     */
    void clear();
    
    /**
     * Get cache size.
     * <p>返回当前缓存条目数量。</p>
     *
     * @return number of cache entries
     */
    int getSize();
    
    /**
     * Get cache statistics.
     * <p>返回命中、未命中、驱逐与容量等统计数据。</p>
     *
     * @return cache statistics
     */
    CacheStats getStats();
    
    /**
     * Cache statistics.
     * <p>缓存运行统计快照，含命中率计算。</p>
     */
    class CacheStats {
        
        /** 缓存命中次数。 */
        private final long hitCount;
        
        /** 缓存未命中次数。 */
        private final long missCount;
        
        /** LRU/过期驱逐次数。 */
        private final long evictionCount;
        
        /** 当前缓存条目数。 */
        private final long size;
        
        public CacheStats(long hitCount, long missCount, long evictionCount, long size) {
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.evictionCount = evictionCount;
            this.size = size;
        }
        
        public long getHitCount() {
            return hitCount;
        }
        
        public long getMissCount() {
            return missCount;
        }
        
        public long getEvictionCount() {
            return evictionCount;
        }
        
        public long getSize() {
            return size;
        }
        
        /** 计算命中率：命中 / (命中 + 未命中)，无请求时返回 0。 */
        public double getHitRate() {
            long total = hitCount + missCount;
            return total == 0 ? 0.0 : (double) hitCount / total;
        }
    }
}
