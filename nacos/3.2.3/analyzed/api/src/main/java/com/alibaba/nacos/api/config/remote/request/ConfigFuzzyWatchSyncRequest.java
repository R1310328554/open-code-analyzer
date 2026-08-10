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

package com.alibaba.nacos.api.config.remote.request;

import com.alibaba.nacos.api.common.Constants;

import java.util.Set;

/**
 * 模糊监听差异同步请求，服务端向客户端推送与本地不一致的配置集合。
 *
 * <p>用于模糊订阅初始化及增量对齐，支持分批传输。</p>
 *
 * @author stone-98
 * @date 2024/3/6
 */
public class ConfigFuzzyWatchSyncRequest extends AbstractFuzzyWatchNotifyRequest {
    
    /** 模糊匹配的 groupKey 模式。 */
    private String groupKeyPattern;
    
    /** 差异配置上下文集合。 */
    private Set<Context> contexts;
    
    /** 同步类型，参见 FUZZY_WATCH_INIT_NOTIFY 等常量。 */
    private String syncType;
    
    /** 分批同步的总批次数。 */
    private int totalBatch;
    
    /** 当前批次序号（从 1 起）。 */
    private int currentBatch;
    
    /** 获取同步类型。 */
    public String getSyncType() {
        return syncType;
    }
    
    /** 设置同步类型。 */
    public void setSyncType(String syncType) {
        this.syncType = syncType;
    }
    
    /** 获取总批次数。 */
    public int getTotalBatch() {
        return totalBatch;
    }
    
    /** 设置总批次数。 */
    public void setTotalBatch(int totalBatch) {
        this.totalBatch = totalBatch;
    }
    
    /** 获取当前批次。 */
    public int getCurrentBatch() {
        return currentBatch;
    }
    
    /** 设置当前批次。 */
    public void setCurrentBatch(int currentBatch) {
        this.currentBatch = currentBatch;
    }
    
    /** 无参构造。 */
    public ConfigFuzzyWatchSyncRequest() {
    }
    
    /**
     * 私有构造，通过静态工厂方法创建实例。
     *
     * @param groupKeyPattern groupKey 匹配模式
     * @param contexts        差异配置上下文集合
     */
    private ConfigFuzzyWatchSyncRequest(String syncType, String groupKeyPattern,
        Set<Context> contexts, int totalBatch,
        int currentBatch) {
        this.groupKeyPattern = groupKeyPattern;
        this.contexts = contexts;
        this.syncType = syncType;
        this.currentBatch = currentBatch;
        this.totalBatch = totalBatch;
        
    }
    
    /**
     * 构建分批差异同步请求。
     *
     * @param contexts        差异配置上下文
     * @param groupKeyPattern groupKey 匹配模式
     * @return 同步请求实例
     */
    public static ConfigFuzzyWatchSyncRequest buildSyncRequest(String syncType,
        Set<Context> contexts,
        String groupKeyPattern, int totalBatch, int currentBatch) {
        return new ConfigFuzzyWatchSyncRequest(syncType, groupKeyPattern, contexts, totalBatch,
            currentBatch);
    }
    
    /**
     * 构建模糊监听初始化完成通知。
     *
     * @param groupKeyPattern groupKey 匹配模式
     * @return 初始化完成同步请求
     */
    public static ConfigFuzzyWatchSyncRequest buildInitFinishRequest(String groupKeyPattern) {
        return new ConfigFuzzyWatchSyncRequest(Constants.FINISH_FUZZY_WATCH_INIT_NOTIFY,
            groupKeyPattern, null, 0, 0);
    }
    
    /** 获取 groupKey 匹配模式。 */
    public String getGroupKeyPattern() {
        return groupKeyPattern;
    }
    
    /** 设置 groupKey 匹配模式。 */
    public void setGroupKeyPattern(String groupKeyPattern) {
        this.groupKeyPattern = groupKeyPattern;
    }
    
    /** 获取差异上下文集合。 */
    public Set<Context> getContexts() {
        return contexts;
    }
    
    /** 设置差异上下文集合。 */
    public void setContexts(Set<Context> contexts) {
        this.contexts = contexts;
    }
    
    /** 单条模糊监听差异的配置上下文。 */
    public static class Context {
        
        /** 配置的 groupKey。 */
        String groupKey;
        
        /** 变更类型，参见 {@link com.alibaba.nacos.api.common.Constants.ConfigChangedType}：ADD_CONFIG 表示客户端应新增，DELETE_CONFIG 表示应移除。 */
        private String changedType;
        
        /** 无参构造。 */
        public Context() {
        }
        
        /**
         * 构造差异上下文。
         *
         * @param groupKey    配置 groupKey
         * @param changedType 变更类型
         * @return 上下文实例
         */
        public static Context build(String groupKey, String changedType) {
            Context context = new Context();
            context.setGroupKey(groupKey);
            context.setChangedType(changedType);
            return context;
        }
        
        /** 获取 groupKey。 */
        public String getGroupKey() {
            return groupKey;
        }
        
        /** 设置 groupKey。 */
        public void setGroupKey(String groupKey) {
            this.groupKey = groupKey;
        }
        
        /** 获取变更类型。 */
        public String getChangedType() {
            return changedType;
        }
        
        /** 设置变更类型。 */
        public void setChangedType(String changedType) {
            this.changedType = changedType;
        }
    }
    
}
