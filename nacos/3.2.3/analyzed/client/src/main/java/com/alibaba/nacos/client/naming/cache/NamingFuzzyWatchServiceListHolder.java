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

package com.alibaba.nacos.client.naming.cache;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.api.naming.listener.FuzzyWatchEventWatcher;
import com.alibaba.nacos.api.naming.remote.request.NamingFuzzyWatchRequest;
import com.alibaba.nacos.api.naming.remote.response.NamingFuzzyWatchResponse;
import com.alibaba.nacos.client.naming.event.NamingFuzzyWatchLoadEvent;
import com.alibaba.nacos.client.naming.event.NamingFuzzyWatchNotifyEvent;
import com.alibaba.nacos.client.naming.remote.gprc.NamingGrpcClientProxy;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.SmartSubscriber;
import com.alibaba.nacos.common.utils.CollectionUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.nacos.api.common.Constants.FUZZY_WATCH_INIT_NOTIFY;
import static com.alibaba.nacos.api.common.Constants.ServiceChangedType.ADD_SERVICE;
import static com.alibaba.nacos.api.common.Constants.WATCH_TYPE_CANCEL_WATCH;
import static com.alibaba.nacos.api.common.Constants.WATCH_TYPE_WATCH;
import static com.alibaba.nacos.api.model.v2.ErrorCode.FUZZY_WATCH_PATTERN_MATCH_COUNT_OVER_LIMIT;
import static com.alibaba.nacos.api.model.v2.ErrorCode.FUZZY_WATCH_PATTERN_OVER_LIMIT;

/**
 * 命名客户端模糊监听调度与上下文持有者。
 *
 * <p>维护 pattern -> {@link NamingFuzzyWatchContext} 映射，通过单线程 Worker 与服务端执行模糊订阅 RPC，并订阅 {@link NamingFuzzyWatchNotifyEvent} 推送。</p>
 *
 * @author tanyongquan
 */
public class NamingFuzzyWatchServiceListHolder extends SmartSubscriber implements Closeable {
    
    private static final Logger LOGGER = LogUtils.logger(NamingFuzzyWatchServiceListHolder.class);
    
    /** 本持有者的事件作用域，过滤 NotifyCenter 事件。 */
    private String notifierEventScope;
    
    /** gRPC 命名代理，执行模糊监听请求。 */
    private NamingGrpcClientProxy namingGrpcClientProxy;
    
    /** 触发模糊监听同步执行的单元素队列（铃铛）。 */
    /** fuzzyListenExecuteBell. */
    /** 模糊监听执行铃铛队列。 */
    private final BlockingQueue<Object> fuzzyWatchExecuteBell = new ArrayBlockingQueue<>(1);
    
    /** 放入铃铛队列的占位元素。 */
    private final Object bellItem = new Object();
    
    /** 上次全量对账同步的时间戳。 */
    private final AtomicLong fuzzyWatchLastAllSyncTime = new AtomicLong(System.currentTimeMillis());
    
    /** 全量对账间隔：3 分钟。 */
    private static final long FUZZY_LISTEN_ALL_SYNC_INTERNAL = 3 * 60 * 1000;
    
    /** 模糊监听 Worker 单线程调度器。 */
    ScheduledExecutorService executorService;
    
    /** 模糊模式到 {@link NamingFuzzyWatchContext} 的映射。 */
    /** The contents of {@code patternMatchMap} are Map{pattern -> Set[matched services]}. */
    /** patternMatchMap：pattern -> 匹配上下文。 */
    private Map<String, NamingFuzzyWatchContext> fuzzyMatchContextMap = new ConcurrentHashMap<>();
    
    public NamingFuzzyWatchServiceListHolder(String notifierEventScope) {
        this.notifierEventScope = notifierEventScope;
        NotifyCenter.registerSubscriber(this);
    }
    
    /**
     * shut down.
      * <p>模糊监听调度持有者；详见类级说明。</p>
     */
    @Override
    public void shutdown() {
        // 注销构造时注册的 NotifyCenter 订阅者
        NotifyCenter.deregisterSubscriber(this);
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * start.
      * <p>模糊监听调度持有者；详见类级说明。</p>
     */
    public void start() {
        
        executorService = Executors.newSingleThreadScheduledExecutor(
            new NameThreadFactory("com.alibaba.nacos.client.naming.fuzzy.watch.Worker"));
        executorService.submit(() -> {
            while (!executorService.isShutdown() && !executorService.isTerminated()) {
                try {
                    fuzzyWatchExecuteBell.poll(5L, TimeUnit.SECONDS);
                    if (executorService.isShutdown() || executorService.isTerminated()) {
                        continue;
                    }
                    executeNamingFuzzyWatch();
                } catch (Throwable e) {
                    LOGGER.error("[rpc-fuzzy-watch-execute] rpc fuzzy watch exception", e);
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException interruptedException) {
                        //ignore
                    }
                    notifyFuzzyWatchSync();
                }
            }
        });
    }
    
    public void registerNamingGrpcClientProxy(NamingGrpcClientProxy namingGrpcClientProxy) {
        this.namingGrpcClientProxy = namingGrpcClientProxy;
    }
    
    public NamingFuzzyWatchContext getFuzzyWatchContext(String groupKeyPattern) {
        return fuzzyMatchContextMap.get(groupKeyPattern);
    }
    
    /**
     * Add a watcher to the context.
     *
     * @param watcher watcher to be added
      * <p>模糊监听调度持有者；详见类级说明。</p>
     */
    public NamingFuzzyWatchContext registerFuzzyWatcher(String groupKeyPattern,
        FuzzyWatchEventWatcher watcher) {
        if (!namingGrpcClientProxy.isAbilitySupportedByServer(AbilityKey.SERVER_FUZZY_WATCH)) {
            throw new NacosRuntimeException(NacosException.SERVER_NOT_IMPLEMENTED,
                "Request Nacos server version is too low, not support fuzzy watch feature.");
        }
        NamingFuzzyWatchContext namingFuzzyWatchContext =
            initFuzzyWatchContextIfNeed(groupKeyPattern);
        namingFuzzyWatchContext.setDiscard(false);
        synchronized (namingFuzzyWatchContext) {
            FuzzyWatchEventWatcherWrapper fuzzyWatchEventWatcherWrapper =
                new FuzzyWatchEventWatcherWrapper(watcher);
            if (namingFuzzyWatchContext.getFuzzyWatchEventWatcherWrappers()
                .add(fuzzyWatchEventWatcherWrapper)) {
                LOGGER.info(" [add-watcher-ok] groupKeyPattern={}, watcher={},uuid={} ",
                    groupKeyPattern, watcher,
                    fuzzyWatchEventWatcherWrapper.getUuid());
                Set<String> receivedServiceKeys = namingFuzzyWatchContext.getReceivedServiceKeys();
                if (CollectionUtils.isNotEmpty(receivedServiceKeys)) {
                    for (String serviceKey : receivedServiceKeys) {
                        NamingFuzzyWatchNotifyEvent namingFuzzyWatchNotifyEvent =
                            NamingFuzzyWatchNotifyEvent.build(
                                notifierEventScope, groupKeyPattern, serviceKey,
                                ADD_SERVICE, FUZZY_WATCH_INIT_NOTIFY,
                                fuzzyWatchEventWatcherWrapper.getUuid());
                        NotifyCenter.publishEvent(namingFuzzyWatchNotifyEvent);
                    }
                }
            }
        }
        
        return namingFuzzyWatchContext;
    }
    
    /**
     * init fuzzy watch context.
     *
     * @param groupKeyPattern groupKeyPattern.
     * @return fuzzy context.
      * <p>模糊监听调度持有者；详见类级说明。</p>
     */
    public NamingFuzzyWatchContext initFuzzyWatchContextIfNeed(String groupKeyPattern) {
        if (!fuzzyMatchContextMap.containsKey(groupKeyPattern)) {
            synchronized (fuzzyMatchContextMap) {
                if (fuzzyMatchContextMap.containsKey(groupKeyPattern)) {
                    return fuzzyMatchContextMap.get(groupKeyPattern);
                }
                LOGGER.info("[fuzzy-watch] init fuzzy watch context for pattern {}",
                    groupKeyPattern);
                fuzzyMatchContextMap.putIfAbsent(groupKeyPattern,
                    new NamingFuzzyWatchContext(notifierEventScope, groupKeyPattern));
                notifyFuzzyWatchSync();
            }
        }
        return fuzzyMatchContextMap.get(groupKeyPattern);
    }
    
    /**
     * remove fuzzy watch context for pattern.
     *
     * @param groupKeyPattern group key pattern.
      * <p>模糊监听调度持有者；详见类级说明。</p>
     */
    public synchronized void removePatternMatchCache(String groupKeyPattern) {
        NamingFuzzyWatchContext namingFuzzyWatchContext = fuzzyMatchContextMap.get(groupKeyPattern);
        if (namingFuzzyWatchContext == null) {
            return;
        }
        if (namingFuzzyWatchContext.isDiscard()
            && namingFuzzyWatchContext.getFuzzyWatchEventWatcherWrappers()
                .isEmpty()) {
            LOGGER.info("[fuzzy-watch] remove fuzzy watch context for pattern {}", groupKeyPattern);
            fuzzyMatchContextMap.remove(groupKeyPattern);
        }
    }
    
    /**
     * notify sync fuzzy watch with server.
      * <p>模糊监听调度持有者；详见类级说明。</p>
     */
    void notifyFuzzyWatchSync() {
        fuzzyWatchExecuteBell.offer(bellItem);
    }
    
    /**
     * Execute fuzzy listen configuration changes.
     *
     * <p>This method iterates through all fuzzy listen contexts and determines whether they need to be added or
     * removed based on their consistency with the server and discard status. It then calls the appropriate method to
     * execute the fuzzy listen operation.
     *
     * @throws NacosException If an error occurs during the execution of fuzzy listen configuration changes.
      * <p>模糊监听调度持有者；详见类级说明。</p>
     */
    public void executeNamingFuzzyWatch() throws NacosException {
        
        // 获取当前时间戳
        long now = System.currentTimeMillis();
        
        // 判断是否需要进行全量对账
        boolean needAllSync =
            now - fuzzyWatchLastAllSyncTime.get() >= FUZZY_LISTEN_ALL_SYNC_INTERNAL;
        
        List<NamingFuzzyWatchContext> needSyncContexts = new ArrayList<>();
        // 遍历所有模糊监听上下文
        for (NamingFuzzyWatchContext context : fuzzyMatchContextMap.values()) {
            // 若已与服务端一致则先同步本地监听器
            if (context.isConsistentWithServer()) {
                context.syncFuzzyWatchers();
                // 未到全量间隔则跳过 RPC 同步
                if (!needAllSync) {
                    continue;
                }
            }
            
            needSyncContexts.add(context);
        }
        
        // 对需同步的上下文执行模糊订阅 RPC
        doExecuteNamingFuzzyWatch(needSyncContexts);
        
        // 全量对账完成后更新上次全量时间
        if (needAllSync) {
            fuzzyWatchLastAllSyncTime.set(now);
        }
    }
    
    public void resetConsistenceStatus() {
        fuzzyMatchContextMap.values()
            .forEach(fuzzyWatcherContext -> fuzzyWatcherContext.setConsistentWithServer(false));
    }
    
    /**
     * Execute fuzzy listen configuration changes for a specific map of contexts.
     *
     * <p>This method submits tasks to execute fuzzy listen operations asynchronously for the provided contexts. It
     * waits for all tasks to complete and logs any errors that occur.
     *
     * @param contextLists The map of contexts to execute fuzzy listen operations for.
     * @throws NacosException If an error occurs during the execution of fuzzy listen configuration changes.
      * <p>模糊监听调度持有者；详见类级说明。</p>
     */
    private void doExecuteNamingFuzzyWatch(List<NamingFuzzyWatchContext> contextLists)
        throws NacosException {
        // 无待同步上下文则直接返回
        if (CollectionUtils.isEmpty(contextLists)) {
            return;
        }
        
        // 逐个上下文构建请求并调用 gRPC
        for (NamingFuzzyWatchContext entry : contextLists) {
            // 提交模糊监听 RPC 任务
            NamingFuzzyWatchRequest configFuzzyWatchRequest = buildFuzzyWatchNamingRequest(entry);
            try {
                
            // 执行模糊监听 RPC
                NamingFuzzyWatchResponse listenResponse = namingGrpcClientProxy.fuzzyWatchRequest(
                    configFuzzyWatchRequest);
                if (listenResponse != null && listenResponse.isSuccess()) {
                    
                    if (configFuzzyWatchRequest.getWatchType().equals(WATCH_TYPE_CANCEL_WATCH)) {
                        removePatternMatchCache(entry.getGroupKeyPattern());
                    } else {
                        entry.setConsistentWithServer(true);
                    }
                    entry.clearOverLimitTs();
                }
                
            } catch (NacosException e) {
                // 记录错误并在短暂延迟后重试
                
                if (FUZZY_WATCH_PATTERN_OVER_LIMIT.getCode() == e.getErrCode()
                    || FUZZY_WATCH_PATTERN_MATCH_COUNT_OVER_LIMIT.getCode() == e.getErrCode()) {
                    LOGGER.error(
                        " fuzzy watch pattern over limit,pattern ->{} ,fuzzy watch will be suppressed,msg={}",
                        entry.getGroupKeyPattern(), e.getErrMsg());
                    NamingFuzzyWatchLoadEvent namingFuzzyWatchLoadEvent =
                        NamingFuzzyWatchLoadEvent.buildEvent(
                            e.getErrCode(), entry.getGroupKeyPattern(), notifierEventScope);
                    NotifyCenter.publishEvent(namingFuzzyWatchLoadEvent);
                    
                } else {
                    LOGGER.error(" fuzzy watch request fail.", e);
                    
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException interruptedException) {
                        // 忽略中断
                    }
                    // 重新触发同步铃铛
                    notifyFuzzyWatchSync();
                }
                
            }
        }
        
    }
    
    private NamingFuzzyWatchRequest buildFuzzyWatchNamingRequest(
        NamingFuzzyWatchContext namingFuzzyWatchContext) {
        NamingFuzzyWatchRequest namingFuzzyWatchRequest = new NamingFuzzyWatchRequest();
        namingFuzzyWatchRequest.setInitializing(namingFuzzyWatchContext.isInitializing());
        namingFuzzyWatchRequest.setNamespace(namingGrpcClientProxy.getNamespaceId());
        namingFuzzyWatchRequest
            .setReceivedGroupKeys(namingFuzzyWatchContext.getReceivedServiceKeys());
        namingFuzzyWatchRequest.setGroupKeyPattern(namingFuzzyWatchContext.getGroupKeyPattern());
        if (namingFuzzyWatchContext.isDiscard()
            && namingFuzzyWatchContext.getFuzzyWatchEventWatcherWrappers()
                .isEmpty()) {
            namingFuzzyWatchRequest.setWatchType(WATCH_TYPE_CANCEL_WATCH);
        } else {
            namingFuzzyWatchRequest.setWatchType(WATCH_TYPE_WATCH);
        }
        return namingFuzzyWatchRequest;
    }
    
    public Map<String, NamingFuzzyWatchContext> getFuzzyMatchContextMap() {
        return fuzzyMatchContextMap;
    }
    
    @Override
    public void onEvent(Event event) {
        
        if (event instanceof NamingFuzzyWatchNotifyEvent) {
            if (!event.scope().equals(notifierEventScope)) {
                return;
            }
            NamingFuzzyWatchNotifyEvent watchNotifyEvent = (NamingFuzzyWatchNotifyEvent) event;
            String changedType = watchNotifyEvent.getChangedType();
            String syncType = watchNotifyEvent.getSyncType();
            
            String serviceKey = watchNotifyEvent.getServiceKey();
            String pattern = watchNotifyEvent.getPattern();
            String watchUuid = watchNotifyEvent.getWatcherUuid();
            NamingFuzzyWatchContext namingFuzzyWatchContext = fuzzyMatchContextMap.get(pattern);
            if (namingFuzzyWatchContext == null) {
                return;
            }
            namingFuzzyWatchContext.notifyFuzzyWatchers(serviceKey, changedType, syncType,
                watchUuid);
        }
        if (event instanceof NamingFuzzyWatchLoadEvent) {
            if (!event.scope().equals(notifierEventScope)) {
                return;
            }
            
            NamingFuzzyWatchLoadEvent overLimitEvent = (NamingFuzzyWatchLoadEvent) event;
            NamingFuzzyWatchContext namingFuzzyWatchContext = fuzzyMatchContextMap.get(
                overLimitEvent.getGroupKeyPattern());
            if (namingFuzzyWatchContext == null) {
                return;
            }
            
            namingFuzzyWatchContext.notifyOverLimitWatchers(overLimitEvent.getCode());
        }
        
    }
    
    @Override
    public List<Class<? extends Event>> subscribeTypes() {
        List<Class<? extends Event>> result = new LinkedList<>();
        result.add(NamingFuzzyWatchNotifyEvent.class);
        result.add(NamingFuzzyWatchLoadEvent.class);
        return result;
    }
    
    public String getNotifierEventScope() {
        return notifierEventScope;
    }
}
