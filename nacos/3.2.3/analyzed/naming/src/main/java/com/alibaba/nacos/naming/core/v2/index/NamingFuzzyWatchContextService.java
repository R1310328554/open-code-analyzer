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

package com.alibaba.nacos.naming.core.v2.index;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.SmartSubscriber;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.alibaba.nacos.common.utils.FuzzyGroupKeyPattern;
import com.alibaba.nacos.core.utils.GlobalExecutor;
import com.alibaba.nacos.naming.core.v2.ServiceManager;
import com.alibaba.nacos.naming.core.v2.event.client.ClientOperationEvent;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.GlobalConfig;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.alibaba.nacos.api.common.Constants.ServiceChangedType.ADD_SERVICE;
import static com.alibaba.nacos.api.common.Constants.ServiceChangedType.DELETE_SERVICE;
import static com.alibaba.nacos.api.model.v2.ErrorCode.FUZZY_WATCH_PATTERN_OVER_LIMIT;
import static com.alibaba.nacos.common.utils.FuzzyGroupKeyPattern.getNamespaceFromPattern;

/**
 * 命名模糊订阅（Fuzzy Watch）上下文服务。
 *
 * <p>维护模糊匹配模式与客户端、已匹配服务键之间的映射，并在服务增删时同步更新匹配索引。</p>
 *
 * @author shiyiyue
 */
@Component
public class NamingFuzzyWatchContextService extends SmartSubscriber {
    
    /**
     * 模糊模式 -> 监听该模式的客户端 ID 集合。
     */
    private final ConcurrentMap<String, Set<String>> watchedClientsMap = new ConcurrentHashMap<>();
    
    /**
     * 模糊模式 -> 已匹配的服务键集合。
     *
     * <p>客户端注册新模式时初始化；定时任务在无客户端监听时延迟清理。</p>
     */
    private final ConcurrentMap<String, Set<String>> matchedServiceKeysMap =
        new ConcurrentHashMap<>();
    
    public NamingFuzzyWatchContextService() {
    }
    
    @PostConstruct
    public void init() {
        GlobalExecutor.scheduleWithFixDelayByCommon(() -> trimFuzzyWatchContext(), 30000);
        NotifyCenter.registerSubscriber(this);
    }
    
    /**
     * 定时裁剪模糊订阅上下文。
     *
     * <p>1. 无监听客户端时移除 watchedClients；2. watchedClients 为空时移除 matchedServiceKeys，延迟删除以避免频繁重建匹配索引。</p>
     */
    void trimFuzzyWatchContext() {
        try {
            Iterator<Map.Entry<String, Set<String>>> iterator =
                matchedServiceKeysMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Set<String>> next = iterator.next();
                Set<String> watchedClients = this.watchedClientsMap.get(next.getKey());
                
                int serviceKeysCount = next.getValue().size();
                if (watchedClients == null) {
                    Loggers.SRV_LOG.info(
                        "[fuzzy-watch] no watchedClients context for pattern {},remove matchedGroupKeys context",
                        next.getKey());
                    iterator.remove();
                } else if (watchedClients.isEmpty()) {
                    Loggers.SRV_LOG.info(
                        "[fuzzy-watch] no client watched pattern {},remove watchedClients context",
                        next.getKey());
                    this.watchedClientsMap.remove(next.getKey());
                } else if (reachToUpLimit(serviceKeysCount)) {
                    Loggers.SRV_LOG.warn(
                        "[fuzzy-watch] pattern {} matched serviceKey count is reach to upper limit {}, fuzzy watch notify may be suppressed ",
                        next.getKey(), next.getValue().size());
                } else if (reachToUpLimit((int) (serviceKeysCount * 1.25))) {
                    Loggers.SRV_LOG.warn(
                        "[fuzzy-watch] pattern {} matched serviceKey count has reached to 80% of the upper limit {} ,"
                            + "it may has a risk of notify suppressed in the near further",
                        next.getKey(),
                        serviceKeysCount);
                }
            }
        } catch (Throwable throwable) {
            Loggers.SRV_LOG.error(
                "[fuzzy-watch] scheduled fuzzy-watch context trim failed",
                throwable);
        }
    }
    
    @Override
    public List<Class<? extends Event>> subscribeTypes() {
        List<Class<? extends Event>> result = new LinkedList<>();
        result.add(ClientOperationEvent.ClientReleaseEvent.class);
        return result;
    }
    
    @Override
    public void onEvent(Event event) {
        if (event instanceof ClientOperationEvent.ClientReleaseEvent) {
            removeFuzzyWatchContext(
                ((ClientOperationEvent.ClientReleaseEvent) event).getClientId());
        }
    }
    
    /**
     * 获取模糊订阅了指定服务的客户端 ID 集合。
     *
     * @param service 待匹配的服务
     * @return 匹配的客户端 ID 集合
     */
    public Set<String> getFuzzyWatchedClients(Service service) {
        Set<String> matchedClients = new HashSet<>();
        Iterator<Map.Entry<String, Set<String>>> iterator = watchedClientsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<String>> entry = iterator.next();
            if (FuzzyGroupKeyPattern.matchPattern(entry.getKey(), service.getName(),
                service.getGroup(),
                service.getNamespace())) {
                matchedClients.addAll(entry.getValue());
            }
        }
        return matchedClients;
    }
    
    /**
     * 将服务增删变更同步到模糊订阅匹配索引。
     *
     * @param changedService 变更的服务
     * @param changedType    变更类型（ADD_SERVICE / DELETE_SERVICE）
     * @return 是否需要向模糊订阅客户端推送通知
     */
    public boolean syncServiceContext(Service changedService, String changedType) {
        
        boolean needNotify = false;
        if (!changedType.equals(ADD_SERVICE) && !changedType.equals(DELETE_SERVICE)) {
            return false;
        }
        
        String serviceKey =
            NamingUtils.getServiceKey(changedService.getNamespace(), changedService.getGroup(),
                changedService.getName());
        Loggers.SRV_LOG.warn("[fuzzy-watch] service change matched,service key {},changed type {} ",
            serviceKey,
            changedType);
        
        Iterator<Map.Entry<String, Set<String>>> iterator =
            matchedServiceKeysMap.entrySet().iterator();
        boolean tryAdd = changedType.equals(ADD_SERVICE);
        boolean tryRemove = changedType.equals(DELETE_SERVICE);
        while (iterator.hasNext()) {
            Map.Entry<String, Set<String>> next = iterator.next();
            if (FuzzyGroupKeyPattern.matchPattern(next.getKey(), changedService.getName(),
                changedService.getGroup(),
                changedService.getNamespace())) {
                Set<String> matchedServiceKeys = next.getValue();
                boolean reachToUpLimit = reachToUpLimit(matchedServiceKeys.size());
                boolean containsAlready = matchedServiceKeys.contains(serviceKey);
                
                if (tryAdd && !containsAlready && reachToUpLimit) {
                    Loggers.SRV_LOG.warn(
                        "[fuzzy-watch] pattern matched service count is over limit , "
                            + "current service will be ignore for pattern {} ,current count is {}",
                        next.getKey(),
                        matchedServiceKeys.size());
                    continue;
                }
                
                if (tryAdd && !containsAlready && matchedServiceKeys.add(serviceKey)) {
                    Loggers.SRV_LOG.info(
                        "[fuzzy-watch] pattern {} matched service keys count changed to {}",
                        next.getKey(), matchedServiceKeys.size());
                    needNotify = true;
                    
                }
                if (tryRemove && containsAlready && matchedServiceKeys.remove(serviceKey)) {
                    Loggers.SRV_LOG.info(
                        "[fuzzy-watch]  pattern {} matched service keys count changed to {}",
                        next.getKey(), matchedServiceKeys.size());
                    needNotify = true;
                    if (reachToUpLimit) {
                        makeupMatchedGroupKeys(next.getKey());
                    }
                }
            }
        }
        return needNotify;
    }
    
    private boolean reachToUpLimit(int size) {
        return size >= GlobalConfig.getMaxMatchedServiceCount();
    }
    
    public boolean reachToUpLimit(String groupKeyPattern) {
        Set<String> strings = matchedServiceKeysMap.get(groupKeyPattern);
        return strings != null && (reachToUpLimit(strings.size()));
    }
    
    /**
     * 在负载保护模式下，删除服务后补全仍匹配该模式的服务键。
     *
     * @param groupKeyPattern 分组键模糊模式
     */
    public void makeupMatchedGroupKeys(String groupKeyPattern) {
        
        Set<String> matchedGroupKeys = matchedServiceKeysMap.get(groupKeyPattern);
        if (matchedGroupKeys == null || reachToUpLimit(matchedGroupKeys.size())) {
            return;
        }
        Set<Service> namespaceServices = ServiceManager.getInstance()
            .getSingletons(getNamespaceFromPattern(groupKeyPattern));
        for (Service service : namespaceServices) {
            String serviceKey =
                NamingUtils.getServiceKey(service.getNamespace(), service.getGroup(),
                    service.getName());
            if (FuzzyGroupKeyPattern.matchPattern(groupKeyPattern, service.getName(),
                service.getGroup(),
                service.getNamespace()) && !matchedGroupKeys.contains(serviceKey)) {
                if (matchedGroupKeys.add(serviceKey)) {
                    Loggers.SRV_LOG.info("[fuzzy-watch] pattern {} makeup service key {}",
                        groupKeyPattern, serviceKey);
                    if (reachToUpLimit(matchedGroupKeys.size())) {
                        Loggers.SRV_LOG.warn(
                            "[fuzzy-watch] pattern {} matched service count reach to up limit ,makeup group keys skip.",
                            groupKeyPattern);
                        return;
                    }
                }
                
            }
        }
    }
    
    /**
     * 注册客户端对指定模糊模式的订阅上下文。
     *
     * @param groupKeyPattern 含命名空间的完整模糊模式
     * @param clientId        客户端 ID
     */
    public void syncFuzzyWatcherContext(String groupKeyPattern, String clientId)
        throws NacosException {
        // 先占位 watchedClients，模式未超限时再添加 clientId
        watchedClientsMap.computeIfAbsent(groupKeyPattern, key -> new ConcurrentHashSet<>());
        initWatchMatchService(groupKeyPattern);
        watchedClientsMap.get(groupKeyPattern).add(clientId);
    }
    
    /**
     * 返回指定模式当前已匹配的服务键副本。
     *
     * @param groupKeyPattern 模糊模式
     * @return 已匹配服务键集合
     */
    public Set<String> matchServiceKeys(String groupKeyPattern) {
        Set<String> stringSet = matchedServiceKeysMap.get(groupKeyPattern);
        return stringSet == null ? new HashSet<>() : new HashSet<>(stringSet);
    }
    
    private void removeFuzzyWatchContext(String clientId) {
        Iterator<Map.Entry<String, Set<String>>> iterator = watchedClientsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<String>> next = iterator.next();
            next.getValue().remove(clientId);
        }
    }
    
    /**
     * 移除指定客户端对某模糊模式的订阅。
     *
     * @param groupKeyPattern 模糊模式
     * @param clientId        客户端 ID
     */
    public void removeFuzzyWatchContext(String groupKeyPattern, String clientId) {
        Set<String> clients = watchedClientsMap.get(groupKeyPattern);
        if (clients != null) {
            clients.remove(clientId);
        }
    }
    
    /**
     * 初始化或更新指定模糊模式的匹配索引。
     *
     * @param completedPattern 含命名空间 ID 的完整模式
     * @return 当前已匹配服务键的副本集合
     */
    public Set<String> initWatchMatchService(String completedPattern) throws NacosException {
        
        if (!matchedServiceKeysMap.containsKey(completedPattern)) {
            if (matchedServiceKeysMap.size() >= GlobalConfig.getMaxPatternCount()) {
                Loggers.SRV_LOG.warn(
                    "FUZZY_WATCH: fuzzy watch pattern count is over limit ,pattern {} init fail,current count is {}",
                    completedPattern, matchedServiceKeysMap.size());
                throw new NacosException(FUZZY_WATCH_PATTERN_OVER_LIMIT.getCode(),
                    FUZZY_WATCH_PATTERN_OVER_LIMIT.getMsg());
            }
            
            long matchBeginTime = System.currentTimeMillis();
            Set<Service> namespaceServices = ServiceManager.getInstance()
                .getSingletons(getNamespaceFromPattern(completedPattern));
            Set<String> matchedServices =
                matchedServiceKeysMap.computeIfAbsent(completedPattern, k -> new HashSet<>());
            boolean overMatchCount = false;
            for (Service service : namespaceServices) {
                if (FuzzyGroupKeyPattern.matchPattern(completedPattern, service.getName(),
                    service.getGroup(),
                    service.getNamespace())) {
                    if (matchedServices.size() >= GlobalConfig.getMaxMatchedServiceCount()) {
                        
                        Loggers.SRV_LOG.warn(
                            "[fuzzy-watch] pattern matched service count is over limit , "
                                + "other services will stop notify for pattern {} ,current count is {}",
                            completedPattern, matchedServices.size());
                        overMatchCount = true;
                        break;
                    }
                    matchedServices.add(
                        NamingUtils.getServiceKey(service.getNamespace(), service.getGroup(),
                            service.getName()));
                }
            }
            Loggers.SRV_LOG.info(
                "FUZZY_WATCH: pattern {} match {} services, overMatchCount={},cost {}ms",
                completedPattern, matchedServices.size(), overMatchCount,
                System.currentTimeMillis() - matchBeginTime);
            
        }
        
        return new HashSet(matchedServiceKeysMap.get(completedPattern));
    }
    
}
