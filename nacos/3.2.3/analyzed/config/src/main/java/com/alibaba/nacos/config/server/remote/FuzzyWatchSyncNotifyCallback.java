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
import com.alibaba.nacos.api.remote.AbstractPushCallBack;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.plugin.control.ControlManagerCenter;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;

import static com.alibaba.nacos.api.common.Constants.FUZZY_WATCH_INIT_NOTIFY;

/**
 * 模糊监听同步推送结果回调：处理批次计数、初始化完成通知与失败重试。
 * 成功时若整批同步完成且为 INIT 类型，会追加推送 init-finish 请求。
 * Represents a callback for handling the result of an RPC push operation.
 *
 * @author stone-98
 */
class FuzzyWatchSyncNotifyCallback extends AbstractPushCallBack {
    
    /**
     * 关联的模糊监听同步推送任务实例。
     */
    FuzzyWatchSyncNotifyTask fuzzyWatchSyncNotifyTask;
    
    /**
     * Constructs a new RpcPushCallback with the specified parameters.
     *
     * @param fuzzyWatchSyncNotifyTask The RpcPushTask associated with the callback
      * <p>模糊监听同步推送回调；详见类级说明。</p>
     */
    public FuzzyWatchSyncNotifyCallback(FuzzyWatchSyncNotifyTask fuzzyWatchSyncNotifyTask) {
        super(3000L);
        this.fuzzyWatchSyncNotifyTask = fuzzyWatchSyncNotifyTask;
    }
    
    /**
     * Handles the successful completion of the RPC push operation.
      * <p>模糊监听同步推送回调；详见类级说明。</p>
     */
    @Override
    public void onSuccess() {
        // 记录 TPS 成功/失败计量点
        TpsCheckRequest tpsCheckRequest = new TpsCheckRequest();
        tpsCheckRequest
            .setPointName(FuzzyWatchSyncNotifyTask.CONFIG_FUZZY_WATCH_CONFIG_SYNC_SUCCESS);
        ControlManagerCenter.getInstance().getTpsControlManager().check(tpsCheckRequest);
        
        if (fuzzyWatchSyncNotifyTask.batchTaskCounter != null) {
            fuzzyWatchSyncNotifyTask.batchTaskCounter.batchSuccess(
                fuzzyWatchSyncNotifyTask.notifyRequest.getCurrentBatch());
            if (fuzzyWatchSyncNotifyTask.batchTaskCounter.batchCompleted()
                && fuzzyWatchSyncNotifyTask.notifyRequest.getSyncType()
                    .equals(FUZZY_WATCH_INIT_NOTIFY)) {
                ConfigFuzzyWatchSyncRequest request =
                    ConfigFuzzyWatchSyncRequest.buildInitFinishRequest(
                        fuzzyWatchSyncNotifyTask.notifyRequest.getGroupKeyPattern());
                
                // 构造 finish 推送任务并调度到客户端
                FuzzyWatchSyncNotifyTask fuzzyWatchSyncNotifyTaskFinish =
                    new FuzzyWatchSyncNotifyTask(
                        fuzzyWatchSyncNotifyTask.connectionManager,
                        fuzzyWatchSyncNotifyTask.rpcPushService, request,
                        null, fuzzyWatchSyncNotifyTask.maxRetryTimes,
                        fuzzyWatchSyncNotifyTask.connectionId);
                fuzzyWatchSyncNotifyTaskFinish.scheduleSelf();
            }
        }
    }
    
    /**
     * Handles the failure of the RPC push operation.
     *
     * @param e The exception thrown during the operation
      * <p>模糊监听同步推送回调；详见类级说明。</p>
     */
    @Override
    public void onFail(Throwable e) {
        // Check TPS limits
        TpsCheckRequest tpsCheckRequest = new TpsCheckRequest();
        tpsCheckRequest.setPointName(FuzzyWatchSyncNotifyTask.CONFIG_FUZZY_WATCH_CONFIG_SYNC_FAIL);
        ControlManagerCenter.getInstance().getTpsControlManager().check(tpsCheckRequest);
        
        // 记录失败日志并重新调度推送任务
        Loggers.REMOTE_PUSH.warn("Push fail, groupKeyPattern={}, clientId={}",
            fuzzyWatchSyncNotifyTask.notifyRequest.getGroupKeyPattern(),
            fuzzyWatchSyncNotifyTask.connectionId, e);
        fuzzyWatchSyncNotifyTask.scheduleSelf();
    }
}
