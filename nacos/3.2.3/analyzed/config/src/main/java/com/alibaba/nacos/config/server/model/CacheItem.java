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

package com.alibaba.nacos.config.server.model;

import com.alibaba.nacos.config.server.utils.SimpleReadWriteLock;
import com.alibaba.nacos.core.utils.StringPool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存配置缓存项：持有主配置 {@link ConfigCache}、灰度版本 Map 及读写锁。
 * groupKey 经 {@link StringPool} 驻留以节省堆内存。
 * Cache item.
 *
 * @author Nacos
 */
public class CacheItem {
    
    /** dataId+group+tenant 组合键（intern 字符串） */
    final String groupKey;
    
    /** 配置文件类型标识 */
    public String type;
    
    /** 主版本配置缓存体 */
    ConfigCache configCache = ConfigCacheFactoryDelegate.getInstance().createConfigCache();
    
    /** 灰度版本 ConfigCacheGray 映射（lazy init）。Use for gray. */
    
    private volatile Map<String, ConfigCacheGray> configCacheGray = null;
    
    /** 按 priority 与 grayName 排序后的灰度列表 */
    List<ConfigCacheGray> sortedConfigCacheGrayList = null;
    
    /** 保护缓存读写的简易读写锁 */
    private final SimpleReadWriteLock rwLock = new SimpleReadWriteLock();
    
    /**
     * 构造缓存项并设置加密 dataKey。
     *
     * @param groupKey         组合键
     * @param encryptedDataKey 加密密钥标识
     */
    public CacheItem(String groupKey, String encryptedDataKey) {
        this.groupKey = StringPool.get(groupKey);
        this.getConfigCache().setEncryptedDataKey(encryptedDataKey);
    }
    
    /** 构造无加密 dataKey 的缓存项 */
    public CacheItem(String groupKey) {
        this.groupKey = StringPool.get(groupKey);
    }
    
    /** 获取主配置缓存 */
    public ConfigCache getConfigCache() {
        return configCache;
    }
    
    /** 获取读写锁 */
    public SimpleReadWriteLock getRwLock() {
        return rwLock;
    }
    
    /** 获取文件类型 */
    public String getType() {
        return type;
    }
    
    /** 设置文件类型 */
    public void setType(String type) {
        this.type = type;
    }
    
    /** 获取 groupKey */
    public String getGroupKey() {
        return groupKey;
    }
    
    /**
     * init config gray if empty.
      * <p>内存配置缓存项；详见类级说明。</p>
     */
    /** 懒初始化灰度 Map（双重检查锁）。init config gray if empty. */
    public void initConfigGrayIfEmpty() {
        if (this.configCacheGray == null) {
            synchronized (this) {
                if (this.configCacheGray == null) {
                    this.configCacheGray = new ConcurrentHashMap<>(4);
                }
            }
        }
    }
    
    /**
     * 确保指定 grayName 的灰度缓存存在。
     *
     * @param grayName gray name.
     */
    public void initConfigGrayIfEmpty(String grayName) {
        initConfigGrayIfEmpty();
        this.configCacheGray.computeIfAbsent(grayName,
            k -> ConfigCacheFactoryDelegate.getInstance().createConfigCacheGray(k));
    }
    
    /** 返回已排序的灰度缓存列表 */
    public List<ConfigCacheGray> getSortConfigGrays() {
        return sortedConfigCacheGrayList;
    }
    
    /**
     * sort config gray.
      * <p>内存配置缓存项；详见类级说明。</p>
     */
    /** 按 priority 降序、grayName 升序重排灰度列表。sort config gray. */
    public void sortConfigGray() {
        if (configCacheGray == null || configCacheGray.isEmpty()) {
            sortedConfigCacheGrayList = null;
            return;
        }
        
        sortedConfigCacheGrayList = configCacheGray.values().stream().sorted((o1, o2) -> {
            if (o1.getPriority() != o2.getPriority()) {
                return Integer.compare(o1.getPriority(), o2.getPriority()) * -1;
            } else {
                return o1.getGrayName().compareTo(o2.getGrayName());
            }
            
        }).collect(Collectors.toList());
    }
    
    /** 获取灰度版本 Map */
    public Map<String, ConfigCacheGray> getConfigCacheGray() {
        return configCacheGray;
    }
    
    /** 清空灰度 Map 与排序列表 */
    public void clearConfigGrays() {
        this.configCacheGray = null;
        this.sortedConfigCacheGrayList = null;
    }
    
}
