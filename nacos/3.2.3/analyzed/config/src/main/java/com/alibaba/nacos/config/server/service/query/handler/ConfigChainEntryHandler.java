/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.service.query.handler;

import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.config.server.model.CacheItem;
import com.alibaba.nacos.config.server.service.ConfigCacheService;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainRequest;
import com.alibaba.nacos.config.server.service.query.model.ConfigQueryChainResponse;
import com.alibaba.nacos.config.server.utils.GroupKey2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 配置查询责任链入口处理器：规范化 tenant、加读锁加载 {@link CacheItem}，并将请求传递给后续处理器。
 * ConfigChainEntryHandler.
 * The entry point handler for the responsibility chain, responsible for initializing the chain and handling configuration query requests.
 *
 * @author Nacos
 */
public class ConfigChainEntryHandler extends AbstractConfigQueryHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigChainEntryHandler.class);
    
    private static final String CHAIN_ENTRY_HANDLER = "chainEntryHandler";
    
    /** 当前线程持有的缓存项，供链内后续 Handler 读取 */
    private static final ThreadLocal<CacheItem> CACHE_ITEM_THREAD_LOCAL = new ThreadLocal<>();
    
    @Override
    public String getName() {
        return CHAIN_ENTRY_HANDLER;
    }
    
    @Override
    public ConfigQueryChainResponse handle(ConfigQueryChainRequest request) throws IOException {
        
        // 规范化 namespace/tenant 参数
        request.setTenant(NamespaceUtil.processNamespaceParameter(request.getTenant()));
        String groupKey =
            GroupKey2.getKey(request.getDataId(), request.getGroup(), request.getTenant());
        int lockResult = ConfigCacheService.tryConfigReadLock(groupKey);
        CacheItem cacheItem = ConfigCacheService.getContentCache(groupKey);
        
        // 读锁成功且缓存存在：设置 ThreadLocal 并委托下一处理器
        if (lockResult > 0 && cacheItem != null) {
            try {
                CACHE_ITEM_THREAD_LOCAL.set(cacheItem);
                if (nextHandler != null) {
                    return nextHandler.handle(request);
                // 读锁冲突：返回 CONFIG_QUERY_CONFLICT
        } else {
                    LOGGER.warn("chainEntryHandler's next handler is null");
                    return new ConfigQueryChainResponse();
                }
            } finally {
                CACHE_ITEM_THREAD_LOCAL.remove();
                ConfigCacheService.releaseReadLock(groupKey);
            }
        // 未命中缓存或锁竞争失败：返回 CONFIG_NOT_FOUND
        } else if (lockResult == 0 || cacheItem == null) {
            ConfigQueryChainResponse response = new ConfigQueryChainResponse();
            response.setStatus(ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_NOT_FOUND);
            return response;
        } else {
            ConfigQueryChainResponse response = new ConfigQueryChainResponse();
            response.setStatus(ConfigQueryChainResponse.ConfigQueryStatus.CONFIG_QUERY_CONFLICT);
            return response;
        }
    }
    
    /** 供链内 Handler 获取入口阶段缓存的 {@link CacheItem} */
    public static CacheItem getThreadLocalCacheItem() {
        return CACHE_ITEM_THREAD_LOCAL.get();
    }
}
