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

import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.task.NacosTask;
import com.alibaba.nacos.common.task.NacosTaskProcessor;
import com.alibaba.nacos.common.task.engine.NacosDelayTaskExecuteEngine;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.NamingExecuteTaskDispatcher;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.push.v2.executor.PushExecutor;
import com.alibaba.nacos.naming.push.v2.executor.PushExecutorDelegate;
import org.springframework.stereotype.Component;

/**
 * 模糊订阅推送延迟任务执行引擎。
 *
 * <p>Spring {@link Component}，继承 {@link NacosDelayTaskExecuteEngine}，处理 {@link FuzzyWatchChangeNotifyTask} 与 {@link FuzzyWatchSyncNotifyTask}；推送开关关闭时跳过调度。</p>
 *
 * @author tanyongquan
 */
@Component
public class FuzzyWatchPushDelayTaskEngine extends NacosDelayTaskExecuteEngine {
    
    private final PushExecutorDelegate pushExecutor;
    
    private final SwitchDomain switchDomain;
    
    public FuzzyWatchPushDelayTaskEngine(PushExecutorDelegate pushExecutor,
        SwitchDomain switchDomain) {
        super(FuzzyWatchPushDelayTaskEngine.class.getSimpleName(), Loggers.PUSH);
        this.pushExecutor = pushExecutor;
        this.switchDomain = switchDomain;
        setDefaultTaskProcessor(new WatchPushDelayTaskProcessor(this));
    }
    
    /** 获取委托推送执行器。 */
    public PushExecutor getPushExecutor() {
        return pushExecutor;
    }
    
    /** 全局推送开关开启时才处理到期延迟任务。 */
    @Override
    protected void processTasks() {
        if (!switchDomain.isPushEnabled()) {
            return;
        }
        super.processTasks();
    }
    
    private static class WatchPushDelayTaskProcessor implements NacosTaskProcessor {
        
        private final FuzzyWatchPushDelayTaskEngine fuzzyWatchPushExecuteEngine;
        
        public WatchPushDelayTaskProcessor(
            FuzzyWatchPushDelayTaskEngine fuzzyWatchPushExecuteEngine) {
            this.fuzzyWatchPushExecuteEngine = fuzzyWatchPushExecuteEngine;
        }
        
        @Override
        public boolean process(NacosTask task) {
            
            if (task instanceof FuzzyWatchChangeNotifyTask) {
                // 服务变更时处理模糊订阅变更通知任务
                FuzzyWatchChangeNotifyTask fuzzyWatchChangeNotifyTask =
                    (FuzzyWatchChangeNotifyTask) task;
                NamingExecuteTaskDispatcher.getInstance().dispatchAndExecuteTask(getTaskKey(task),
                    new FuzzyWatchChangeNotifyExecuteTask(fuzzyWatchPushExecuteEngine,
                        fuzzyWatchChangeNotifyTask.getServiceKey(),
                        fuzzyWatchChangeNotifyTask.getChangedType(),
                        fuzzyWatchChangeNotifyTask.getClientId()));
            } else if (task instanceof FuzzyWatchSyncNotifyTask) {
                // 客户端新建模糊订阅时处理同步通知任务
                FuzzyWatchSyncNotifyTask fuzzyWatchSyncNotifyTask = (FuzzyWatchSyncNotifyTask) task;
                String pattern = fuzzyWatchSyncNotifyTask.getPattern();
                String clientId = fuzzyWatchSyncNotifyTask.getClientId();
                NamingExecuteTaskDispatcher.getInstance().dispatchAndExecuteTask(getTaskKey(task),
                    new FuzzyWatchSyncNotifyExecuteTask(clientId, pattern,
                        fuzzyWatchPushExecuteEngine,
                        fuzzyWatchSyncNotifyTask));
            }
            return true;
        }
        
    }
    
    /** 根据任务类型生成引擎内唯一 taskKey。 */
    public static String getTaskKey(NacosTask task) {
        if (task instanceof FuzzyWatchChangeNotifyTask) {
            return "fwcnT-" + ((FuzzyWatchChangeNotifyTask) task).getClientId()
                + ((FuzzyWatchChangeNotifyTask) task).getServiceKey();
        } else if (task instanceof FuzzyWatchSyncNotifyTask) {
            return "fwsnT-" + ((FuzzyWatchSyncNotifyTask) task).getSyncType() + "-"
                + ((FuzzyWatchSyncNotifyTask) task).getClientId()
                + ((FuzzyWatchSyncNotifyTask) task).getPattern()
                + "-" + ((FuzzyWatchSyncNotifyTask) task).getCurrentBatch();
        } else {
            throw new NacosRuntimeException(500, "unknown fuzzy task type");
        }
    }
}
