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

import com.alibaba.nacos.api.naming.listener.FuzzyWatchChangeEvent;
import com.alibaba.nacos.api.naming.listener.FuzzyWatchEventWatcher;
import com.alibaba.nacos.api.naming.listener.FuzzyWatchLoadWatcher;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.alibaba.nacos.common.utils.FuzzyGroupKeyPattern;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.alibaba.nacos.api.common.Constants.FUZZY_WATCH_DIFF_SYNC_NOTIFY;
import static com.alibaba.nacos.api.common.Constants.FUZZY_WATCH_INIT_NOTIFY;
import static com.alibaba.nacos.api.common.Constants.ServiceChangedType.ADD_SERVICE;
import static com.alibaba.nacos.api.common.Constants.ServiceChangedType.DELETE_SERVICE;
import static com.alibaba.nacos.api.model.v2.ErrorCode.FUZZY_WATCH_PATTERN_MATCH_COUNT_OVER_LIMIT;
import static com.alibaba.nacos.api.model.v2.ErrorCode.FUZZY_WATCH_PATTERN_OVER_LIMIT;

/**
 * 单个 groupKey 模式的命名模糊监听上下文。
 *
 * <p>管理环境名、模式、已接收 serviceKey 集合、监听器包装器及与服务端的一致性状态；负责向 {@link FuzzyWatchEventWatcher} 分发 {@link FuzzyWatchChangeEvent}。</p>
 *
 * @author stone-98
 * @date 2024/3/4
 */
public class NamingFuzzyWatchContext {
    
    /** 模糊监听上下文日志记录器。 */
    /** Logger for FuzzyListenContext. */
    /** FuzzyListenContext 日志器。 */
    private static final Logger LOGGER = LogUtils.logger(NamingFuzzyWatchContext.class);
    
    /** 环境/作用域名称，用于日志前缀。 */
    /** Environment name. */
    /** 环境名称。 */
    private String envName;
    
    /** 模糊匹配的 groupKey 模式字符串。 */
    private String groupKeyPattern;
    
    /** 服务端已推送、与本模式匹配的 serviceKey 集合。 */
    /** Set of service keys associated with the context. */
    /** 上下文关联的 serviceKey 集合。 */
    private Set<String> receivedServiceKeys = new ConcurrentHashSet<>();
    
    /** serviceKey 集合变更时的同步版本戳。 */
    private long syncVersion = 0;
    
    /** 是否已与服务端模糊监听状态一致。 */
    /** Flag indicating whether the context is consistent with the server. */
    /** 与服务端一致性标志。 */
    private final AtomicBoolean isConsistentWithServer = new AtomicBoolean();
    
    /** 初始化是否完成的标志，供 Future 等待。 */
    /** Condition object for waiting initialization completion. */
    /** 等待初始化完成的条件对象。 */
    final AtomicBoolean initializationCompleted = new AtomicBoolean(false);
    
    /** 上下文是否已标记废弃（待取消订阅）。 */
    /** Flag indicating whether the context is discarded. */
    /** 上下文废弃标志。 */
    private volatile boolean isDiscard = false;
    
    /** 已注册的模糊监听包装器集合。 */
    /** Set of listeners associated with the context. */
    /** 关联的监听器集合。 */
    private final Set<FuzzyWatchEventWatcherWrapper> fuzzyWatchEventWatcherWrappers =
        new HashSet<>();
    
    /** 上次触发模式/匹配数超限通知的时间戳，用于抑制重复告警。 */
    long patternLimitTs = 0;
    
    /** 超限通知抑制窗口（毫秒）。 */
    private static final long SUPPRESSED_PERIOD = 60 * 1000L;
    
    /** 当前是否处于超限通知抑制期内。 */
    boolean patternLimitSuppressed() {
        return patternLimitTs > 0
            && System.currentTimeMillis() - patternLimitTs < SUPPRESSED_PERIOD;
    }
    
    public void clearOverLimitTs() {
        this.patternLimitTs = 0;
    }
    
    public void refreshOverLimitTs() {
        this.patternLimitTs = System.currentTimeMillis();
    }
    
    public void refreshSyncVersion() {
        this.syncVersion = System.currentTimeMillis();
    }
    
    /**
     * Constructor with environment name, data ID pattern, and group.
     *
     * @param envName         Environment name
     * @param groupKeyPattern groupKeyPattern
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public NamingFuzzyWatchContext(String envName, String groupKeyPattern) {
        this.envName = envName;
        this.groupKeyPattern = groupKeyPattern;
    }
    
    /** 构造 {@link FuzzyWatchChangeEvent} 并异步/同步回调指定监听器。 */
    private void doNotifyWatcher(final String serviceKey, final String changedType,
        final String syncType,
        FuzzyWatchEventWatcherWrapper fuzzyWatchEventWatcherWrapper) {
        
        if (ADD_SERVICE.equals(changedType) && fuzzyWatchEventWatcherWrapper.getSyncServiceKeys()
            .contains(serviceKey)) {
            return;
        }
        
        if (DELETE_SERVICE.equals(changedType)
            && !fuzzyWatchEventWatcherWrapper.getSyncServiceKeys()
                .contains(serviceKey)) {
            return;
        }
        
        String[] serviceKeyItems = NamingUtils.parseServiceKey(serviceKey);
        String namespace = serviceKeyItems[0];
        String groupName = serviceKeyItems[1];
        String serviceName = serviceKeyItems[2];
        
        final String resetSyncType =
            !initializationCompleted.get() ? FUZZY_WATCH_INIT_NOTIFY : syncType;
        
        Runnable job = () -> {
            long start = System.currentTimeMillis();
            FuzzyWatchChangeEvent event =
                new FuzzyWatchChangeEvent(serviceName, groupName, namespace, changedType,
                    resetSyncType);
            if (fuzzyWatchEventWatcherWrapper != null) {
                fuzzyWatchEventWatcherWrapper.fuzzyWatchEventWatcher.onEvent(event);
            }
            LOGGER.info(
                "[{}] [notify-watcher-ok] serviceName={}, groupName={}, namespace={}, watcher={},changedType={}, job run cost={} millis.",
                envName, serviceName, groupName, namespace,
                fuzzyWatchEventWatcherWrapper.fuzzyWatchEventWatcher,
                changedType, (System.currentTimeMillis() - start));
            if (changedType.equals(DELETE_SERVICE)) {
                fuzzyWatchEventWatcherWrapper.getSyncServiceKeys()
                    .remove(NamingUtils.getServiceKey(namespace, groupName, serviceName));
            } else if (changedType.equals(ADD_SERVICE)) {
                fuzzyWatchEventWatcherWrapper.getSyncServiceKeys()
                    .add(NamingUtils.getServiceKey(namespace, groupName, serviceName));
            }
        };
        
        try {
            if (null != fuzzyWatchEventWatcherWrapper.fuzzyWatchEventWatcher.getExecutor()) {
                LOGGER.info(
                    "[{}] [notify-watcher] task submitted to user executor, serviceName={}, groupName={}, namespace={}, listener={}.",
                    envName, serviceName, groupName, namespace, fuzzyWatchEventWatcherWrapper);
                fuzzyWatchEventWatcherWrapper.fuzzyWatchEventWatcher.getExecutor().execute(job);
            } else {
                LOGGER.info(
                    "[{}] [notify-watcher] task execute in nacos thread, serviceName={}, groupName={}, namespace={}, listener={}.",
                    envName, serviceName, groupName, namespace, fuzzyWatchEventWatcherWrapper);
                job.run();
            }
        } catch (Throwable t) {
            LOGGER.error(
                "[{}] [notify-watcher-error] serviceName={}, groupName={}, namespace={}, listener={}, throwable={}.",
                envName, serviceName, groupName, namespace, fuzzyWatchEventWatcherWrapper,
                t.getCause());
        }
    }
    
    /**
     * Mark initialization as complete and notify waiting threads.
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public void markInitializationComplete() {
        LOGGER.info(
            "[{}] [fuzzy-watch] pattern init notify finish pattern={},match service count {}",
            envName,
            groupKeyPattern, receivedServiceKeys.size());
        initializationCompleted.set(true);
        synchronized (this) {
            notifyAll();
        }
    }
    
    /**
     * Remove a watcher from the context.
     *
     * @param watcher watcher to be removed
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public synchronized void removeWatcher(FuzzyWatchEventWatcher watcher) {
        Iterator<FuzzyWatchEventWatcherWrapper> iterator =
            fuzzyWatchEventWatcherWrappers.iterator();
        while (iterator.hasNext()) {
            FuzzyWatchEventWatcherWrapper next = iterator.next();
            if (next.fuzzyWatchEventWatcher.equals(watcher)) {
                iterator.remove();
                LOGGER.info("[{}] [remove-watcher-ok] groupKeyPattern={}, watcher={},uuid={} ",
                    getEnvName(),
                    this.groupKeyPattern, watcher, next.getUuid());
            }
        }
        if (fuzzyWatchEventWatcherWrappers.isEmpty()) {
            this.setConsistentWithServer(false);
            this.setDiscard(true);
        }
    }
    
    /**
     * Get the environment name.
     *
     * @return Environment name
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public String getEnvName() {
        return envName;
    }
    
    /**
     * Set the environment name.
     *
     * @param envName Environment name to be set
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public void setEnvName(String envName) {
        this.envName = envName;
    }
    
    public String getGroupKeyPattern() {
        return groupKeyPattern;
    }
    
    /**
     * Get the flag indicating whether the context is consistent with the server.
     *
     * @return AtomicBoolean indicating whether the context is consistent with the server
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public boolean isConsistentWithServer() {
        return isConsistentWithServer.get();
    }
    
    public void setConsistentWithServer(boolean isConsistentWithServer) {
        this.isConsistentWithServer.set(isConsistentWithServer);
    }
    
    /**
     * Check if the context is discarded.
     *
     * @return True if the context is discarded, otherwise false
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public boolean isDiscard() {
        return isDiscard;
    }
    
    /**
     * Set the flag indicating whether the context is discarded.
     *
     * @param discard True to mark the context as discarded, otherwise false
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public void setDiscard(boolean discard) {
        isDiscard = discard;
    }
    
    /**
     * Check if the context is initializing.
     *
     * @return True if the context is initializing, otherwise false
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public boolean isInitializing() {
        return !initializationCompleted.get();
    }
    
    /**
     * Get the set of data IDs associated with the context.
     *
     * @return Set of data IDs
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public Set<String> getReceivedServiceKeys() {
        return Collections.unmodifiableSet(receivedServiceKeys);
    }
    
    /**
     * add received service key.
     *
     * @param serviceKey service key.
     * @return
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public boolean addReceivedServiceKey(String serviceKey) {
        boolean added = receivedServiceKeys.add(serviceKey);
        if (added) {
            refreshSyncVersion();
        }
        return added;
    }
    
    /**
     * remove received service key.
     *
     * @param serviceKey service key.
     * @return
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public boolean removeReceivedServiceKey(String serviceKey) {
        
        boolean removed = receivedServiceKeys.remove(serviceKey);
        if (removed) {
            refreshSyncVersion();
        }
        return removed;
    }
    
    /**
     * Get the set of listeners associated with the context.
     *
     * @return Set of listeners
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public Set<FuzzyWatchEventWatcherWrapper> getFuzzyWatchEventWatcherWrappers() {
        return fuzzyWatchEventWatcherWrappers;
    }
    
    /** 对账上下文与监听器已同步 serviceKey，补发差异变更事件。 */
    void syncFuzzyWatchers() {
        for (FuzzyWatchEventWatcherWrapper namingFuzzyWatcher : fuzzyWatchEventWatcherWrappers) {
            
            if (namingFuzzyWatcher.syncVersion == this.syncVersion) {
                continue;
            }
            
            Set<String> receivedServiceKeysContext = new HashSet<>(this.getReceivedServiceKeys());
            Set<String> syncGroupKeys = namingFuzzyWatcher.getSyncServiceKeys();
            List<FuzzyGroupKeyPattern.GroupKeyState> groupKeyStates =
                FuzzyGroupKeyPattern.diffGroupKeys(
                    receivedServiceKeysContext, syncGroupKeys);
            if (CollectionUtils.isEmpty(groupKeyStates)) {
                namingFuzzyWatcher.syncVersion = this.syncVersion;
            } else {
                for (FuzzyGroupKeyPattern.GroupKeyState groupKeyState : groupKeyStates) {
                    String changedType = groupKeyState.isExist() ? ADD_SERVICE : DELETE_SERVICE;
                    doNotifyWatcher(groupKeyState.getGroupKey(), changedType,
                        FUZZY_WATCH_DIFF_SYNC_NOTIFY,
                        namingFuzzyWatcher);
                }
            }
        }
    }
    
    /** 向指定或全部监听器推送单条 serviceKey 变更。 */
    void notifyFuzzyWatchers(String serviceKey, String changedType, String syncType,
        String watcherUuid) {
        for (FuzzyWatchEventWatcherWrapper namingFuzzyWatcher : filterWatchers(watcherUuid)) {
            doNotifyWatcher(serviceKey, changedType, syncType, namingFuzzyWatcher);
        }
    }
    
    /** 向 {@link FuzzyWatchLoadWatcher} 通知模式或匹配数超限。 */
    void notifyOverLimitWatchers(int code) {
        
        if (this.patternLimitSuppressed()) {
            return;
        }
        boolean notify = false;
        
        for (FuzzyWatchEventWatcherWrapper namingFuzzyWatcherWrapper : filterWatchers(null)) {
            if (namingFuzzyWatcherWrapper.fuzzyWatchEventWatcher instanceof FuzzyWatchLoadWatcher) {
                
                if (FUZZY_WATCH_PATTERN_MATCH_COUNT_OVER_LIMIT.getCode().equals(code)) {
                    ((FuzzyWatchLoadWatcher) namingFuzzyWatcherWrapper.fuzzyWatchEventWatcher)
                        .onServiceReachUpLimit();
                    notify = true;
                }
                if (FUZZY_WATCH_PATTERN_OVER_LIMIT.getCode().equals(code)) {
                    ((FuzzyWatchLoadWatcher) namingFuzzyWatcherWrapper.fuzzyWatchEventWatcher)
                        .onPatternOverLimit();
                    notify = true;
                }
            }
        }
        if (notify) {
            this.refreshOverLimitTs();
        }
    }
    
    private Set<FuzzyWatchEventWatcherWrapper> filterWatchers(String uuid) {
        if (StringUtils.isBlank(uuid)
            || CollectionUtils.isEmpty(getFuzzyWatchEventWatcherWrappers())) {
            return getFuzzyWatchEventWatcherWrappers();
        } else {
            return getFuzzyWatchEventWatcherWrappers().stream()
                .filter(a -> a.getUuid().equals(uuid))
                .collect(Collectors.toSet());
        }
    }
    
    /**
     * create a new future of this context.
     *
     * @return
      * <p>单模式模糊监听上下文；详见类级说明。</p>
     */
    public Future<ListView<String>> createNewFuture() {
        Future<ListView<String>> completableFuture = new Future<ListView<String>>() {
            
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                throw new UnsupportedOperationException("not support to cancel fuzzy watch");
            }
            
            @Override
            public boolean isCancelled() {
                return false;
            }
            
            @Override
            public boolean isDone() {
                return NamingFuzzyWatchContext.this.initializationCompleted.get();
            }
            
            @Override
            public ListView<String> get() throws InterruptedException {
                synchronized (NamingFuzzyWatchContext.this) {
                    while (!NamingFuzzyWatchContext.this.initializationCompleted.get()) {
                        NamingFuzzyWatchContext.this.wait();
                    }
                }
                
                ListView<String> result = new ListView<>();
                result.setData(Arrays.asList(
                    NamingFuzzyWatchContext.this.receivedServiceKeys.toArray(new String[0])));
                result.setCount(result.getData().size());
                return result;
            }
            
            @Override
            public ListView<String> get(long timeout, TimeUnit unit)
                throws InterruptedException, TimeoutException {
                
                if (!NamingFuzzyWatchContext.this.initializationCompleted.get()) {
                    synchronized (NamingFuzzyWatchContext.this) {
                        NamingFuzzyWatchContext.this.wait(unit.toMillis(timeout));
                    }
                }
                
                if (!NamingFuzzyWatchContext.this.initializationCompleted.get()) {
                    throw new TimeoutException(
                        "fuzzy watch result future timeout for " + unit.toMillis(timeout)
                            + " millis");
                }
                
                ListView<String> result = new ListView<>();
                result.setData(Arrays.asList(
                    NamingFuzzyWatchContext.this.receivedServiceKeys.toArray(new String[0])));
                result.setCount(result.getData().size());
                return result;
            }
        };
        return completableFuture;
    }
}
