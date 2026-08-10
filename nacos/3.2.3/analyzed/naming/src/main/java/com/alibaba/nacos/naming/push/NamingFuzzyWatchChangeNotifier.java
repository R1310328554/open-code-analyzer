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

package com.alibaba.nacos.naming.push;

import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.SmartSubscriber;
import com.alibaba.nacos.naming.core.v2.event.service.ServiceEvent;
import com.alibaba.nacos.naming.core.v2.index.NamingFuzzyWatchContextService;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.push.v2.PushConfig;
import com.alibaba.nacos.naming.push.v2.task.FuzzyWatchChangeNotifyTask;
import com.alibaba.nacos.naming.push.v2.task.FuzzyWatchPushDelayTaskEngine;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 模糊订阅（Fuzzy Watch）服务变更通知器。
 *
 * <p>监听 {@link ServiceEvent.ServiceChangedEvent}，当服务实例变化且命中模糊订阅上下文时，为各订阅客户端生成 {@link FuzzyWatchChangeNotifyTask} 并投递到延迟推送引擎。</p>
 *
 * @author shiyiyue
 */
@Service
public class NamingFuzzyWatchChangeNotifier extends SmartSubscriber {
    
    private NamingFuzzyWatchContextService namingFuzzyWatchContextService;
    
    private FuzzyWatchPushDelayTaskEngine fuzzyWatchPushDelayTaskEngine;
    
    /** 注册为 NotifyCenter 订阅者，注入模糊订阅上下文与延迟任务引擎。 */
    public NamingFuzzyWatchChangeNotifier(
        NamingFuzzyWatchContextService namingFuzzyWatchContextService,
        FuzzyWatchPushDelayTaskEngine fuzzyWatchPushDelayTaskEngine) {
        this.fuzzyWatchPushDelayTaskEngine = fuzzyWatchPushDelayTaskEngine;
        this.namingFuzzyWatchContextService = namingFuzzyWatchContextService;
        NotifyCenter.registerSubscriber(this);
    }
    
    /** 订阅服务变更事件类型。 */
    @Override
    public List<Class<? extends Event>> subscribeTypes() {
        List<Class<? extends Event>> result = new LinkedList<>();
        result.add(ServiceEvent.ServiceChangedEvent.class);
        return result;
    }
    
    /** 处理服务变更：同步上下文后为模糊订阅客户端生成推送任务。 */
    @Override
    public void onEvent(Event event) {
        if (event instanceof ServiceEvent.ServiceChangedEvent) {
            ServiceEvent.ServiceChangedEvent serviceChangedEvent =
                (ServiceEvent.ServiceChangedEvent) event;
            if (namingFuzzyWatchContextService.syncServiceContext(serviceChangedEvent.getService(),
                serviceChangedEvent.getChangedType())) {
                generateFuzzyWatchChangeNotifyTask(serviceChangedEvent.getService(),
                    serviceChangedEvent.getChangedType());
            }
        }
    }
    
    private void generateFuzzyWatchChangeNotifyTask(
        com.alibaba.nacos.naming.core.v2.pojo.Service service,
        String changedType) {
        
        String serviceKey = NamingUtils.getServiceKey(service.getNamespace(), service.getGroup(),
            service.getName());
        Set<String> fuzzyWatchedClients =
            namingFuzzyWatchContextService.getFuzzyWatchedClients(service);
        
        Loggers.SRV_LOG.info("FUZZY_WATCH:serviceKey {}   has {} clients  fuzzy watched",
            serviceKey,
            fuzzyWatchedClients == null ? 0 : fuzzyWatchedClients.size());
        // 按服务维度为每个模糊订阅客户端生成变更通知任务
        for (String clientId : fuzzyWatchedClients) {
            FuzzyWatchChangeNotifyTask fuzzyWatchChangeNotifyTask =
                new FuzzyWatchChangeNotifyTask(serviceKey,
                    changedType, clientId, PushConfig.getInstance().getPushTaskDelay());
            fuzzyWatchPushDelayTaskEngine.addTask(
                FuzzyWatchPushDelayTaskEngine.getTaskKey(fuzzyWatchChangeNotifyTask),
                fuzzyWatchChangeNotifyTask);
        }
    }
    
}
