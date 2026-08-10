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

package com.alibaba.nacos.api.naming.remote.request;

import java.util.Set;

import static com.alibaba.nacos.api.common.Constants.Naming.NAMING_MODULE;

/**
 * 命名模糊监听全量/增量同步请求（服务端推送）。
 *
 * <p>服务端按 {@link #groupKeyPattern} 批量下发匹配服务的 {@link Context} 列表；大批量时分批传输，由 {@link #totalBatch} 与 {@link #currentBatch} 标识进度。</p>
 *
 * @author shiyiyue
 */
public class NamingFuzzyWatchSyncRequest extends AbstractFuzzyWatchNotifyRequest {
    
    /** 服务键匹配模式。 */
    private String groupKeyPattern;
    
    /** 本批次包含的服务变更上下文集合。 */
    private Set<Context> contexts;
    
    /** 同步总批次数。 */
    private int totalBatch;
    
    /** 当前批次序号（从 1 起）。 */
    private int currentBatch;
    
    /** 无参构造，供反序列化使用。 */
    public NamingFuzzyWatchSyncRequest() {
        
    }
    
    /**
     * 构造同步请求。
     *
     * @param pattern  服务键匹配模式
     * @param syncType 同步类型
     * @param contexts 服务变更上下文集合
     */
    public NamingFuzzyWatchSyncRequest(String pattern, String syncType, Set<Context> contexts) {
        super(syncType);
        this.groupKeyPattern = pattern;
        this.contexts = contexts;
    }
    
    /** 返回同步总批次数。 */
    public int getTotalBatch() {
        return totalBatch;
    }
    
    /** 设置同步总批次数。 */
    public void setTotalBatch(int totalBatch) {
        this.totalBatch = totalBatch;
    }
    
    /** 返回当前批次序号。 */
    public int getCurrentBatch() {
        return currentBatch;
    }
    
    /** 设置当前批次序号。 */
    public void setCurrentBatch(int currentBatch) {
        this.currentBatch = currentBatch;
    }
    
    /**
     * 构建带分批信息的同步通知请求。
     *
     * @param pattern      服务键匹配模式
     * @param syncType     同步类型
     * @param contexts     服务变更上下文集合
     * @param totalBatch   总批次数
     * @param currentBatch 当前批次序号
     * @return 新的 {@link NamingFuzzyWatchSyncRequest} 实例
     */
    public static NamingFuzzyWatchSyncRequest buildSyncNotifyRequest(String pattern,
        String syncType,
        Set<Context> contexts, int totalBatch, int currentBatch) {
        NamingFuzzyWatchSyncRequest namingFuzzyWatchSyncRequest =
            new NamingFuzzyWatchSyncRequest(pattern, syncType,
                contexts);
        namingFuzzyWatchSyncRequest.currentBatch = currentBatch;
        namingFuzzyWatchSyncRequest.totalBatch = totalBatch;
        return namingFuzzyWatchSyncRequest;
    }
    
    /** 返回服务键匹配模式。 */
    public String getGroupKeyPattern() {
        return groupKeyPattern;
    }
    
    /** 设置服务键匹配模式。 */
    public void setGroupKeyPattern(String groupKeyPattern) {
        this.groupKeyPattern = groupKeyPattern;
    }
    
    /** 返回本批次服务变更上下文。 */
    public Set<Context> getContexts() {
        return contexts;
    }
    
    /** 设置服务变更上下文集合。 */
    public void setContexts(Set<Context> contexts) {
        this.contexts = contexts;
    }
    
    /** 返回命名模块标识。 */
    @Override
    public String getModule() {
        return NAMING_MODULE;
    }
    
    /** 模糊监听同步上下文，描述单个服务的变更信息。 */
    public static class Context {
        
        /** 服务键（group@@service）。 */
        String serviceKey;
        
        /** 变更类型（ADD/DELETE/MODIFY）。 */
        private String changedType;
        
        /** 无参构造，供序列化使用。 */
        public Context() {
        }
        
        /**
         * 构建服务变更上下文。
         *
         * @param serviceKey  服务键
         * @param changedType 变更类型
         * @return 初始化完毕的 {@link Context} 实例
         */
        public static NamingFuzzyWatchSyncRequest.Context build(String serviceKey,
            String changedType) {
            NamingFuzzyWatchSyncRequest.Context context = new NamingFuzzyWatchSyncRequest.Context();
            context.setServiceKey(serviceKey);
            context.setChangedType(changedType);
            return context;
        }
        
        /** 返回服务键。 */
        public String getServiceKey() {
            return serviceKey;
        }
        
        /** 设置服务键。 */
        public void setServiceKey(String serviceKey) {
            this.serviceKey = serviceKey;
        }
        
        /** 返回变更类型。 */
        public String getChangedType() {
            return changedType;
        }
        
        /** 设置变更类型。 */
        public void setChangedType(String changedType) {
            this.changedType = changedType;
        }
    }
}
