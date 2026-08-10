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

package com.alibaba.nacos.naming.push.v2.task;

import com.alibaba.nacos.api.naming.remote.request.NamingFuzzyWatchSyncRequest;
import com.alibaba.nacos.common.task.AbstractDelayTask;
import com.alibaba.nacos.common.task.BatchTaskCounter;
import com.alibaba.nacos.naming.misc.Loggers;

import java.util.HashSet;
import java.util.Set;

/**
 * 模糊订阅同步/初始化延迟任务。
 *
 * <p>携带 pattern、syncType、分批 serviceKey 集合及 {@link BatchTaskCounter}，支持 merge 合并同 pattern 的待同步服务；到期后由引擎调度 {@link FuzzyWatchSyncNotifyExecuteTask}。</p>
 *
 * @author tanyongquan
 */
public class FuzzyWatchSyncNotifyTask extends AbstractDelayTask {
    
    private final String clientId;
    
    private final String pattern;
    
    private final Set<NamingFuzzyWatchSyncRequest.Context> syncServiceKeys;
    
    private final String syncType;
    
    private int totalBatch = 1;
    
    private int currentBatch = 1;
    
    private BatchTaskCounter batchTaskCounter;
    
    private long executeStartTime = System.currentTimeMillis();
    
    public FuzzyWatchSyncNotifyTask(String clientId, String pattern, String syncType,
        Set<NamingFuzzyWatchSyncRequest.Context> syncServiceKeys, long delay) {
        this.clientId = clientId;
        this.pattern = pattern;
        this.syncType = syncType;
        if (syncServiceKeys != null) {
            this.syncServiceKeys = syncServiceKeys;
        } else {
            this.syncServiceKeys = new HashSet<>();
        }
        setTaskInterval(delay);
        setLastProcessTime(System.currentTimeMillis());
    }
    
    public int getTotalBatch() {
        return totalBatch;
    }
    
    public void setTotalBatch(int totalBatch) {
        this.totalBatch = totalBatch;
    }
    
    public int getCurrentBatch() {
        return currentBatch;
    }
    
    public void setCurrentBatch(int currentBatch) {
        this.currentBatch = currentBatch;
    }
    
    /** 合并同 pattern 的同步任务，合并 serviceKey 并取最早处理时间。 */
    @Override
    public void merge(AbstractDelayTask task) {
        if (!(task instanceof FuzzyWatchSyncNotifyTask)) {
            return;
        }
        FuzzyWatchSyncNotifyTask oldTask = (FuzzyWatchSyncNotifyTask) task;
        
        if (oldTask.getSyncServiceKeys() != null) {
            syncServiceKeys.addAll(oldTask.getSyncServiceKeys());
        }
        setLastProcessTime(Math.min(getLastProcessTime(), task.getLastProcessTime()));
        Loggers.PUSH.info("[FUZZY-WATCH-INIT-PUSH] Task merge for pattern {}", pattern);
    }
    
    /** 获取模糊订阅匹配 pattern。 */
    public String getPattern() {
        return pattern;
    }
    
    /** 获取本批次待同步的服务上下文集合。 */
    public Set<NamingFuzzyWatchSyncRequest.Context> getSyncServiceKeys() {
        return syncServiceKeys;
    }
    
    /** 获取同步类型（INIT 或 FINISH 等）。 */
    public String getSyncType() {
        return syncType;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public BatchTaskCounter getBatchTaskCounter() {
        return batchTaskCounter;
    }
    
    public void setBatchTaskCounter(BatchTaskCounter batchTaskCounter) {
        this.batchTaskCounter = batchTaskCounter;
    }
    
    public long getExecuteStartTime() {
        return executeStartTime;
    }
}
