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

import com.alibaba.nacos.api.naming.remote.request.NamingFuzzyWatchChangeNotifyRequest;
import com.alibaba.nacos.api.remote.PushCallBack;
import com.alibaba.nacos.common.task.AbstractExecuteTask;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.push.v2.NoRequiredRetryException;
import com.alibaba.nacos.naming.push.v2.PushConfig;

import static com.alibaba.nacos.naming.push.v2.task.FuzzyWatchPushDelayTaskEngine.getTaskKey;

/**
 * 模糊订阅服务变更通知执行任务。
 *
 * <p>由 {@link FuzzyWatchPushDelayTaskEngine} 调度，向指定客户端推送 {@link NamingFuzzyWatchChangeNotifyRequest}；失败且非 {@link NoRequiredRetryException} 时重新入队延迟重试。</p>
 *
 * @author tanyongquan
 */
public class FuzzyWatchChangeNotifyExecuteTask extends AbstractExecuteTask {
    
    private final String serviceKey;
    
    private final String changedType;
    
    private String clientId;
    
    private final FuzzyWatchPushDelayTaskEngine delayTaskEngine;
    
    public FuzzyWatchChangeNotifyExecuteTask(FuzzyWatchPushDelayTaskEngine delayTaskEngine,
        String serviceKey,
        String changedType, String targetClient) {
        this.serviceKey = serviceKey;
        this.changedType = changedType;
        this.clientId = targetClient;
        this.delayTaskEngine = delayTaskEngine;
    }
    
    /** 发起模糊订阅变更 RPC 推送并注册回调。 */
    @Override
    public void run() {
        
        delayTaskEngine.getPushExecutor().doFuzzyWatchNotifyPushWithCallBack(clientId,
            new NamingFuzzyWatchChangeNotifyRequest(serviceKey, changedType),
            new FuzzyWatchChangeNotifyCallback(clientId, serviceKey, changedType));
        
    }
    
    private class FuzzyWatchChangeNotifyCallback implements PushCallBack {
        
        private final String clientId;
        
        private String serviceKey;
        
        private String changedType;
        
        private FuzzyWatchChangeNotifyCallback(String clientId, String serviceKey,
            String changedType) {
            this.clientId = clientId;
            this.serviceKey = serviceKey;
            this.changedType = changedType;
        }
        
        @Override
        public long getTimeout() {
            return PushConfig.getInstance().getPushTaskTimeout();
        }
        
        /** 变更通知推送成功，记录日志。 */
        @Override
        public void onSuccess() {
            Loggers.PUSH.info(
                "[fuzzy watch] change notify success ,clientId {}, serviceKey {] ,changedType {} ",
                clientId, clientId, changedType);
            
        }
        
        /** 推送失败时按配置延迟重新加入任务引擎。 */
        @Override
        public void onFail(Throwable e) {
            
            Loggers.PUSH.warn(
                "[fuzzy watch] change notify fail ,clientId {}, serviceKey {] ,changedType {} ",
                clientId,
                clientId, changedType, e);
            
            if (!(e instanceof NoRequiredRetryException)) {
                FuzzyWatchChangeNotifyTask fuzzyWatchChangeNotifyTask =
                    new FuzzyWatchChangeNotifyTask(serviceKey,
                        changedType, clientId, PushConfig.getInstance().getPushTaskRetryDelay());
                delayTaskEngine.addTask(getTaskKey(fuzzyWatchChangeNotifyTask),
                    fuzzyWatchChangeNotifyTask);
            }
        }
    }
}
