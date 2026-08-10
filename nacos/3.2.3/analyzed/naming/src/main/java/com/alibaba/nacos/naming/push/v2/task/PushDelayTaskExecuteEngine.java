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

package com.alibaba.nacos.naming.push.v2.task;

import com.alibaba.nacos.common.task.NacosTask;
import com.alibaba.nacos.common.task.NacosTaskProcessor;
import com.alibaba.nacos.common.task.engine.NacosDelayTaskExecuteEngine;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManager;
import com.alibaba.nacos.naming.core.v2.index.ClientServiceIndexesManager;
import com.alibaba.nacos.naming.core.v2.index.ServiceStorage;
import com.alibaba.nacos.naming.core.v2.metadata.NamingMetadataManager;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.NamingExecuteTaskDispatcher;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.naming.push.v2.executor.PushExecutor;

/**
 * 命名推送延迟任务执行引擎。
 *
 * <p>持有 ClientManager、索引、ServiceStorage、元数据与 PushExecutor，到期 {@link PushDelayTask} 经 {@link NamingExecuteTaskDispatcher} 转为 {@link PushExecuteTask} 执行；受 {@link SwitchDomain} 推送开关控制。</p>
 *
 * @author xiweng.yy
 */
public class PushDelayTaskExecuteEngine extends NacosDelayTaskExecuteEngine {
    
    private final ClientManager clientManager;
    
    private final ClientServiceIndexesManager indexesManager;
    
    private final ServiceStorage serviceStorage;
    
    private final NamingMetadataManager metadataManager;
    
    private final PushExecutor pushExecutor;
    
    private final SwitchDomain switchDomain;
    
    public PushDelayTaskExecuteEngine(ClientManager clientManager,
        ClientServiceIndexesManager indexesManager,
        ServiceStorage serviceStorage, NamingMetadataManager metadataManager,
        PushExecutor pushExecutor, SwitchDomain switchDomain) {
        super(PushDelayTaskExecuteEngine.class.getSimpleName(), Loggers.PUSH);
        this.clientManager = clientManager;
        this.indexesManager = indexesManager;
        this.serviceStorage = serviceStorage;
        this.metadataManager = metadataManager;
        this.pushExecutor = pushExecutor;
        this.switchDomain = switchDomain;
        setDefaultTaskProcessor(new PushDelayTaskProcessor(this));
    }
    
    /** 获取客户端管理器。 */
    public ClientManager getClientManager() {
        return clientManager;
    }
    
    public ClientServiceIndexesManager getIndexesManager() {
        return indexesManager;
    }
    
    public ServiceStorage getServiceStorage() {
        return serviceStorage;
    }
    
    public NamingMetadataManager getMetadataManager() {
        return metadataManager;
    }
    
    /** 获取推送执行器。 */
    public PushExecutor getPushExecutor() {
        return pushExecutor;
    }
    
    /** 推送全局开关关闭时跳过延迟任务处理。 */
    @Override
    protected void processTasks() {
        if (!switchDomain.isPushEnabled()) {
            return;
        }
        super.processTasks();
    }
    
    private static class PushDelayTaskProcessor implements NacosTaskProcessor {
        
        private final PushDelayTaskExecuteEngine executeEngine;
        
        public PushDelayTaskProcessor(PushDelayTaskExecuteEngine executeEngine) {
            this.executeEngine = executeEngine;
        }
        
        /** 将 PushDelayTask 分派为 PushExecuteTask 异步执行。 */
        @Override
        public boolean process(NacosTask task) {
            PushDelayTask pushDelayTask = (PushDelayTask) task;
            Service service = pushDelayTask.getService();
            NamingExecuteTaskDispatcher.getInstance()
                .dispatchAndExecuteTask(service,
                    new PushExecuteTask(service, executeEngine, pushDelayTask));
            return true;
        }
    }
}
