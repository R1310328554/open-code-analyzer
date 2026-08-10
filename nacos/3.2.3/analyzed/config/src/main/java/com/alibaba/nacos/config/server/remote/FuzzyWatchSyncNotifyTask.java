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

package com.alibaba.nacos.config.server.remote;

import com.alibaba.nacos.api.config.remote.request.ConfigFuzzyWatchSyncRequest;
import com.alibaba.nacos.common.task.BatchTaskCounter;
import com.alibaba.nacos.config.server.utils.ConfigExecutor;
import com.alibaba.nacos.core.remote.ConnectionManager;
import com.alibaba.nacos.core.remote.RpcPushService;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.plugin.control.ControlManagerCenter;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;

import java.util.concurrent.TimeUnit;

/**
 * 模糊监听同步推送任务：向客户端分批推送 {@link ConfigFuzzyWatchSyncRequest}。
 * 配合 {@link FuzzyWatchSyncNotifyCallback} 完成批次计数与 init-finish 收尾。
 * Represents a task for pushing FuzzyListenNotifyDiffRequest to clients.
 *
 * @author stone-98
 */
class FuzzyWatchSyncNotifyTask implements Runnable {
    
    static final String CONFIG_FUZZY_WATCH_CONFIG_SYNC = "FUZZY_WATCH_CONFIG_SYNC_PUSH";
    
    static final String CONFIG_FUZZY_WATCH_CONFIG_SYNC_SUCCESS =
        "CONFIG_FUZZY_WATCH_CONFIG_SYNC_SUCCESS";
    
    static final String CONFIG_FUZZY_WATCH_CONFIG_SYNC_FAIL = "CONFIG_FUZZY_WATCH_CONFIG_SYNC_FAIL";
    
    final ConnectionManager connectionManager;
    
    final RpcPushService rpcPushService;
    
    /**
     * 待推送的模糊监听同步请求。
     */
    ConfigFuzzyWatchSyncRequest notifyRequest;
    
    /**
     * 推送失败时的最大重试次数。
     */
    int maxRetryTimes;
    
    /**
     * 当前已执行的推送尝试次数。
     */
    int tryTimes = 0;
    
    /**
     * 目标客户端 RPC 连接 ID。
     */
    String connectionId;
    
    BatchTaskCounter batchTaskCounter;
    
    /**
     * Constructs a new RpcPushTask with the specified parameters.
     *
     * @param notifyRequest    The FuzzyListenNotifyDiffRequest to be pushed
     * @param batchTaskCounter The batchTaskCounter counter for tracking the number of finished push batches
     * @param maxRetryTimes    The maximum number of times to retry pushing the request
     * @param connectionId     The ID of the connection associated with the client
      * <p>模糊监听同步 RPC 推送任务；详见类级说明。</p>
     */
    public FuzzyWatchSyncNotifyTask(ConnectionManager connectionManager,
        RpcPushService rpcPushService,
        ConfigFuzzyWatchSyncRequest notifyRequest, BatchTaskCounter batchTaskCounter,
        int maxRetryTimes,
        String connectionId) {
        this.connectionManager = connectionManager;
        this.rpcPushService = rpcPushService;
        this.notifyRequest = notifyRequest;
        this.batchTaskCounter = batchTaskCounter;
        this.maxRetryTimes = maxRetryTimes;
        this.connectionId = connectionId;
    }
    
    /**
     * Checks if the maximum number of retry times has been reached.
     *
     * @return true if the maximum number of retry times has been reached, otherwise false
      * <p>模糊监听同步 RPC 推送任务；详见类级说明。</p>
     */
    public boolean isOverTimes() {
        return maxRetryTimes > 0 && this.tryTimes >= maxRetryTimes;
    }
    
    /**
     * Executes the task, attempting to push the request to the client.
      * <p>模糊监听同步 RPC 推送任务；详见类级说明。</p>
     */
    @Override
    public void run() {
        
        if (isOverTimes()) {
            // 超过最大重试次数：记录告警并注销客户端连接
            Loggers.REMOTE_PUSH.warn(
                "Push callback retry failed over times. groupKeyPattern={}, clientId={}, will unregister client.",
                notifyRequest.getGroupKeyPattern(), connectionId);
            connectionManager.unregister(connectionId);
        } else if (connectionManager.getConnection(connectionId) != null) {
            tryTimes++;
            TpsCheckRequest tpsCheckRequest = new TpsCheckRequest();
            
            tpsCheckRequest.setPointName(CONFIG_FUZZY_WATCH_CONFIG_SYNC);
            if (!ControlManagerCenter.getInstance().getTpsControlManager().check(tpsCheckRequest)
                .isSuccess()) {
                scheduleSelf();
            } else {
                rpcPushService.pushWithCallback(connectionId, notifyRequest,
                    new FuzzyWatchSyncNotifyCallback(this),
                    ConfigExecutor.getClientConfigNotifierServiceExecutor());
            }
        }
        
    }
    
    void scheduleSelf() {
        ConfigExecutor.scheduleClientConfigNotifier(this, tryTimes * 2L, TimeUnit.SECONDS);
    }
}
