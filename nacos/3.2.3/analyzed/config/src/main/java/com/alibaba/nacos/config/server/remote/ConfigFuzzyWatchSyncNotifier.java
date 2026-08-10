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

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.remote.request.ConfigFuzzyWatchSyncRequest;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.SmartSubscriber;
import com.alibaba.nacos.common.task.BatchTaskCounter;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.FuzzyGroupKeyPattern;
import com.alibaba.nacos.config.server.configuration.ConfigCommonConfig;
import com.alibaba.nacos.config.server.model.event.ConfigFuzzyWatchEvent;
import com.alibaba.nacos.config.server.service.ConfigFuzzyWatchContextService;
import com.alibaba.nacos.core.remote.ConnectionManager;
import com.alibaba.nacos.core.remote.RpcPushService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.alibaba.nacos.api.common.Constants.FUZZY_WATCH_DIFF_SYNC_NOTIFY;
import static com.alibaba.nacos.api.common.Constants.FUZZY_WATCH_INIT_NOTIFY;

/**
 * 模糊监听同步通知器：消费 {@link ConfigFuzzyWatchEvent}，计算模式匹配 diff，
 * 分批 RPC 推送初始化或增量 {@link ConfigFuzzyWatchSyncRequest} 至客户端。
 * Handles batch fuzzy listen events and pushes corresponding notifications to clients.
 *
 * @author stone-98
 * @date 2024/3/18
 */
@Component(value = "configFuzzyWatchSyncNotifier")
public class ConfigFuzzyWatchSyncNotifier extends SmartSubscriber {
    
    private final ConnectionManager connectionManager;
    
    private final RpcPushService rpcPushService;
    
    private final ConfigFuzzyWatchContextService configFuzzyWatchContextService;
    
    public ConfigFuzzyWatchSyncNotifier(ConnectionManager connectionManager,
        RpcPushService rpcPushService,
        ConfigFuzzyWatchContextService configFuzzyWatchContextService) {
        this.connectionManager = connectionManager;
        this.rpcPushService = rpcPushService;
        this.configFuzzyWatchContextService = configFuzzyWatchContextService;
        NotifyCenter.registerSubscriber(this);
    }
    
    /**
     * 处理模糊监听事件：diff 匹配结果、分批推送并在初始化完成时发送 finish 请求。
     *
     * @param event The ConfigBatchFuzzyListenEvent to handle
     */
    public void handleFuzzyWatchEvent(ConfigFuzzyWatchEvent event) {
        
        // 按模式匹配服务端当前有效的 groupKey 集合
        Set<String> matchGroupKeys =
            configFuzzyWatchContextService.matchGroupKeys(event.getGroupKeyPattern());
        
        // 客户端侧已知的 groupKey（用于 diff）
        Set<String> clientExistingGroupKeys = event.getClientExistingGroupKeys();
        
        // 计算新增/删除 diff 列表
        List<FuzzyGroupKeyPattern.GroupKeyState> configStates =
            FuzzyGroupKeyPattern.diffGroupKeys(matchGroupKeys,
                clientExistingGroupKeys);
        
        if (CollectionUtils.isEmpty(configStates)) {
            int maxPushRetryTimes = ConfigCommonConfig.getInstance().getMaxPushRetryTimes();
            if (event.isInitializing()) {
                ConfigFuzzyWatchSyncRequest request =
                    ConfigFuzzyWatchSyncRequest.buildInitFinishRequest(
                        event.getGroupKeyPattern());
                // 调度 RPC 推送任务
                FuzzyWatchSyncNotifyTask fuzzyWatchSyncNotifyTask =
                    new FuzzyWatchSyncNotifyTask(connectionManager,
                        rpcPushService, request, null, maxPushRetryTimes, event.getConnectionId());
                fuzzyWatchSyncNotifyTask.scheduleSelf();
            }
            
        } else {
            
            // 超限时 matchGroupKeys 可能不完整，过滤掉删除类通知避免误删客户端缓存
            if (configFuzzyWatchContextService.reachToUpLimit(event.getGroupKeyPattern())) {
                configStates.removeIf(item -> !item.isExist());
            }
            
            String syncType =
                event.isInitializing() ? FUZZY_WATCH_INIT_NOTIFY : FUZZY_WATCH_DIFF_SYNC_NOTIFY;
            
            int batchSize = ConfigCommonConfig.getInstance().getBatchSize();
            // 按 batchSize 切分待推送状态
            List<List<FuzzyGroupKeyPattern.GroupKeyState>> divideConfigStatesIntoBatches =
                divideConfigStatesIntoBatches(
                    configStates, batchSize);
            
            // 记录总批次数，末批完成后触发 finish
            int totalBatch = divideConfigStatesIntoBatches.size();
            BatchTaskCounter batchTaskCounter =
                new BatchTaskCounter(divideConfigStatesIntoBatches.size());
            int currentBatch = 1;
            for (List<FuzzyGroupKeyPattern.GroupKeyState> configStateList : divideConfigStatesIntoBatches) {
                // 映射为同步请求的 Context（ADD/DELETE）
                Set<ConfigFuzzyWatchSyncRequest.Context> contexts =
                    configStateList.stream().map(state -> {
                        
                        String changeType = state.isExist() ? Constants.ConfigChangedType.ADD_CONFIG
                            : Constants.ConfigChangedType.DELETE_CONFIG;
                        return ConfigFuzzyWatchSyncRequest.Context.build(state.getGroupKey(),
                            changeType);
                    }).collect(Collectors.toSet());
                
                ConfigFuzzyWatchSyncRequest request =
                    ConfigFuzzyWatchSyncRequest.buildSyncRequest(syncType, contexts,
                        event.getGroupKeyPattern(), totalBatch, currentBatch);
                int maxPushRetryTimes = ConfigCommonConfig.getInstance().getMaxPushRetryTimes();
                // Create RPC push task and push the request to the client
                FuzzyWatchSyncNotifyTask fuzzyWatchSyncNotifyTask =
                    new FuzzyWatchSyncNotifyTask(connectionManager,
                        rpcPushService, request, batchTaskCounter, maxPushRetryTimes,
                        event.getConnectionId());
                fuzzyWatchSyncNotifyTask.scheduleSelf();
                currentBatch++;
            }
        }
        
    }
    
    @Override
    public List<Class<? extends Event>> subscribeTypes() {
        List<Class<? extends Event>> result = new LinkedList<>();
        result.add(ConfigFuzzyWatchEvent.class);
        return result;
    }
    
    @Override
    public void onEvent(Event event) {
        if (event instanceof ConfigFuzzyWatchEvent) {
            handleFuzzyWatchEvent((ConfigFuzzyWatchEvent) event);
        }
    }
    
    /**
     * 将配置状态列表按固定 batchSize 分组。
     *
     * @param configStates The collection of items to be divided into batches
     * @param batchSize    The size of each batch
     * @param <T>          The type of items in the collection
     * @return A list of batches, each containing a sublist of items
     */
    private <T> List<List<T>> divideConfigStatesIntoBatches(Collection<T> configStates,
        int batchSize) {
        // 用递增索引按 batchSize 分组
        AtomicInteger index = new AtomicInteger();
        
        // stream groupingBy 实现均匀分批
        return new ArrayList<>(
            configStates.stream()
                .collect(Collectors.groupingBy(e -> index.getAndIncrement() / batchSize))
                .values());
    }
    
}
