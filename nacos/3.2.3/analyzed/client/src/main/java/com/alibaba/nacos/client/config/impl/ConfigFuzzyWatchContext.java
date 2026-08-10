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

package com.alibaba.nacos.client.config.impl;

import com.alibaba.nacos.api.config.listener.ConfigFuzzyWatchChangeEvent;
import com.alibaba.nacos.api.config.listener.FuzzyWatchEventWatcher;
import com.alibaba.nacos.api.config.listener.FuzzyWatchLoadWatcher;
import com.alibaba.nacos.client.config.common.GroupKey;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.alibaba.nacos.common.utils.FuzzyGroupKeyPattern;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alibaba.nacos.api.common.Constants.ConfigChangedType.ADD_CONFIG;
import static com.alibaba.nacos.api.common.Constants.ConfigChangedType.DELETE_CONFIG;
import static com.alibaba.nacos.api.common.Constants.FUZZY_WATCH_DIFF_SYNC_NOTIFY;
import static com.alibaba.nacos.api.common.Constants.FUZZY_WATCH_INIT_NOTIFY;
import static com.alibaba.nacos.api.model.v2.ErrorCode.FUZZY_WATCH_PATTERN_MATCH_COUNT_OVER_LIMIT;
import static com.alibaba.nacos.api.model.v2.ErrorCode.FUZZY_WATCH_PATTERN_OVER_LIMIT;

/**
 * 单个 groupKey 模式的模糊监听上下文。
 *
 * <p>管理环境名、taskId、groupKey 模式、已接收的 groupKey 集合、监听器包装器及与服务端的一致性状态，支持初始化等待与超限抑制。</p>
 *
 * @author stone-98
 * @date 2024/3/4
 */
public class ConfigFuzzyWatchContext {
    
    /** 模糊监听上下文日志记录器。 */
    /** Logger for FuzzyListenContext. */
    /** FuzzyListenContext 日志。 */
    private static final Logger LOGGER = LogUtils.logger(ConfigFuzzyWatchContext.class);
    
    /** 环境/服务端名称。 */
    /** Environment name. */
    /** 环境名称。 */
    private String envName;
    
    /** 长轮询任务分片 ID。 */
    /** Task ID. */
    /** 任务 ID。 */
    private int taskId;
    
    private String groupKeyPattern;
    
    /** 该模式下已接收到的 groupKey 集合。 */
    /** Set of data IDs associated with the context. */
    /** 关联的 groupKey 集合。 */
    private Set<String> receivedGroupKeys = new ConcurrentHashSet<>();
    
    long syncVersion = 0;
    
    /** 模糊监听上下文是否已与服务端状态一致。 */
    /** Flag indicating whether the context is consistent with the server. */
    /** 是否与服务端一致。 */
    private final AtomicBoolean isConsistentWithServer = new AtomicBoolean();
    
    /** 初始化完成标志，供注册方等待首次同步。 */
    /** Condition object for waiting initialization completion. */
    /** 初始化完成条件。 */
    final AtomicBoolean initializationCompleted = new AtomicBoolean(false);
    
    /** 上下文是否已废弃，待从持有者移除。 */
    /** Flag indicating whether the context is discarded. */
    /** 是否已废弃。 */
    private volatile boolean isDiscard = false;
    
    long patternLimitTs = 0;
    
    private static final long SUPPRESSED_PERIOD = 60 * 1000L;
    
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
    
    /** 绑定到该模式的模糊监听器包装器集合。 */
    /** Set of listeners associated with the context. */
    /** 关联的监听器集合。 */
    private Set<ConfigFuzzyWatcherWrapper> configFuzzyWatcherWrappers = new HashSet<>();
    
    /**
     * Constructor with environment name, data ID pattern, and group.
     *
     * @param envName         Environment name
     * @param groupKeyPattern groupKeyPattern
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public ConfigFuzzyWatchContext(String envName, String groupKeyPattern) {
        this.envName = envName;
        this.groupKeyPattern = groupKeyPattern;
    }
    
    /**
     * Calculate the listeners to notify based on the given UUID.
     *
     * @param uuid UUID to filter listeners
     * @return Set of listeners to notify
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public Set<ConfigFuzzyWatcherWrapper> calculateListenersToNotify(String uuid) {
        Set<ConfigFuzzyWatcherWrapper> listenersToNotify = new HashSet<>();
        if (StringUtils.isEmpty(uuid)) {
            listenersToNotify = configFuzzyWatcherWrappers;
        } else {
            for (ConfigFuzzyWatcherWrapper listener : configFuzzyWatcherWrappers) {
                if (uuid.equals(listener.getUuid())) {
                    listenersToNotify.add(listener);
                }
            }
        }
        return listenersToNotify;
    }
    
    /**
     * Notify the listener with the specified data ID, type, and UUID.
     *
     * @param groupKey groupKey
     * @param uuid     UUID to filter listeners
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public void notifyWatcher(final String groupKey, final String changedType,
        final String syncType,
        final String uuid) {
        Set<ConfigFuzzyWatcherWrapper> listenersToNotify = calculateListenersToNotify(uuid);
        doNotifyWatchers(groupKey, changedType, syncType, listenersToNotify);
    }
    
    /**
     * Perform the notification for the specified data ID, type, and listeners.
     *
     * @param groupKey          groupKey
     * @param listenersToNotify Set of listeners to notify
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    private void doNotifyWatchers(final String groupKey, final String changedType,
        final String syncType,
        Set<ConfigFuzzyWatcherWrapper> listenersToNotify) {
        for (ConfigFuzzyWatcherWrapper watcher : listenersToNotify) {
            doNotifyWatcher(groupKey, changedType, syncType, watcher);
        }
    }
    
    /**
     * notify loader watcher.
     *
     * @param code over limit code,FUZZY_WATCH_PATTERN_MATCH_COUNT_OVER_LIMIT or FUZZY_WATCH_PATTERN_OVER_LIMIT.
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public void notifyLoaderWatcher(int code) {
        
        if (this.patternLimitSuppressed()) {
            return;
        }
        boolean notify = false;
        
        for (ConfigFuzzyWatcherWrapper configFuzzyWatcherWrapper : calculateListenersToNotify(
            null)) {
            if (configFuzzyWatcherWrapper.fuzzyWatchEventWatcher instanceof FuzzyWatchLoadWatcher) {
                
                if (FUZZY_WATCH_PATTERN_MATCH_COUNT_OVER_LIMIT.getCode().equals(code)) {
                    ((FuzzyWatchLoadWatcher) configFuzzyWatcherWrapper.fuzzyWatchEventWatcher)
                        .onConfigReachUpLimit();
                    notify = true;
                }
                if (FUZZY_WATCH_PATTERN_OVER_LIMIT.getCode().equals(code)) {
                    ((FuzzyWatchLoadWatcher) configFuzzyWatcherWrapper.fuzzyWatchEventWatcher)
                        .onPatternOverLimit();
                    notify = true;
                }
            }
        }
        if (notify) {
            this.refreshOverLimitTs();
        }
    }
    
    private void doNotifyWatcher(final String groupKey, final String changedType,
        final String syncType,
        ConfigFuzzyWatcherWrapper configFuzzyWatcher) {
        
        if (ADD_CONFIG.equals(changedType)
            && configFuzzyWatcher.getSyncGroupKeys().contains(groupKey)) {
            return;
        }
        
        if (DELETE_CONFIG.equals(changedType)
            && !configFuzzyWatcher.getSyncGroupKeys().contains(groupKey)) {
            return;
        }
        
        String[] parseKey = GroupKey.parseKey(groupKey);
        String dataId = parseKey[0];
        String group = parseKey[1];
        
        String tenant = parseKey[2];
        
        final String resetSyncType =
            initializationCompleted.get() ? syncType : FUZZY_WATCH_INIT_NOTIFY;
        AbstractFuzzyNotifyTask job = new AbstractFuzzyNotifyTask() {
            
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                ConfigFuzzyWatchChangeEvent event =
                    ConfigFuzzyWatchChangeEvent.build(tenant, group, dataId,
                        changedType, resetSyncType);
                if (configFuzzyWatcher != null) {
                    configFuzzyWatcher.fuzzyWatchEventWatcher.onEvent(event);
                }
                LOGGER.info(
                    "[{}] [notify-fuzzy-watcher-ok] dataId={}, group={}, tenant={}, watcher={}, job run cost={} millis.",
                    envName, dataId, group, tenant, configFuzzyWatcher,
                    (System.currentTimeMillis() - start));
                if (changedType.equals(DELETE_CONFIG)) {
                    configFuzzyWatcher.getSyncGroupKeys()
                        .remove(GroupKey.getKey(dataId, group, tenant));
                } else if (changedType.equals(ADD_CONFIG)) {
                    configFuzzyWatcher.getSyncGroupKeys()
                        .add(GroupKey.getKey(dataId, group, tenant));
                    
                }
            }
        };
        
        try {
            if (null != configFuzzyWatcher.fuzzyWatchEventWatcher.getExecutor()) {
                LOGGER.info(
                    "[{}] [notify-fuzzy-watcher] task submitted to user executor, dataId={}, group={}, tenant={}, listener={}.",
                    envName, dataId, group, tenant, configFuzzyWatcher);
                job.async = true;
                configFuzzyWatcher.fuzzyWatchEventWatcher.getExecutor().execute(job);
            } else {
                LOGGER.info(
                    "[{}] [notify-fuzzy-watcher] task execute in nacos thread, dataId={}, group={}, tenant={}, listener={}.",
                    envName, dataId, group, tenant, configFuzzyWatcher);
                job.run();
            }
        } catch (Throwable t) {
            LOGGER.error(
                "[{}] [notify-fuzzy-watcher-error] dataId={}, group={}, tenant={}, listener={}, throwable={}.",
                envName, dataId, group, tenant, configFuzzyWatcher, t.getCause());
        }
    }
    
    /**
     * Mark initialization as complete and notify waiting threads.
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public void markInitializationComplete() {
        initializationCompleted.set(true);
        synchronized (this) {
            this.notifyAll();
        }
    }
    
    /**
     * Remove a watcher from the context.
     *
     * @param watcher watcher to be removed
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public void removeWatcher(FuzzyWatchEventWatcher watcher) {
        
        Iterator<ConfigFuzzyWatcherWrapper> iterator = configFuzzyWatcherWrappers.iterator();
        while (iterator.hasNext()) {
            ConfigFuzzyWatcherWrapper next = iterator.next();
            if (next.fuzzyWatchEventWatcher.equals(watcher)) {
                iterator.remove();
                LOGGER.info(
                    "[{}] [remove-fuzzy-watcher-ok] groupKeyPattern={}, watcher={},uuid={} ",
                    getEnvName(),
                    this.groupKeyPattern, watcher, next.getUuid());
            }
        }
        
    }
    
    /**
     * Add a watcher to the context.
     *
     * @param configFuzzyWatcherWrapper watcher to be added
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public boolean addWatcher(ConfigFuzzyWatcherWrapper configFuzzyWatcherWrapper) {
        boolean added = configFuzzyWatcherWrappers.add(configFuzzyWatcherWrapper);
        if (added) {
            LOGGER.info("[{}] [add-fuzzy-watcher-ok] groupKeyPattern={}, watcher={},uuid={} ",
                getEnvName(),
                this.groupKeyPattern, configFuzzyWatcherWrapper.fuzzyWatchEventWatcher,
                configFuzzyWatcherWrapper.getUuid());
        }
        return added;
    }
    
    /**
     * Get the environment name.
     *
     * @return Environment name
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public String getEnvName() {
        return envName;
    }
    
    /**
     * Set the environment name.
     *
     * @param envName Environment name to be set
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public void setEnvName(String envName) {
        this.envName = envName;
    }
    
    /**
     * Get the task ID.
     *
     * @return Task ID
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public int getTaskId() {
        return taskId;
    }
    
    /**
     * Set the task ID.
     *
     * @param taskId Task ID to be set
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
    
    public String getGroupKeyPattern() {
        return groupKeyPattern;
    }
    
    /**
     * Get the flag indicating whether the context is consistent with the server.
     *
     * @return AtomicBoolean indicating whether the context is consistent with the server
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
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
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public boolean isDiscard() {
        return isDiscard;
    }
    
    /**
     * Set the flag indicating whether the context is discarded.
     *
     * @param discard True to mark the context as discarded, otherwise false
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public void setDiscard(boolean discard) {
        isDiscard = discard;
    }
    
    /**
     * Check if the context is initializing.
     *
     * @return True if the context is initializing, otherwise false
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public boolean isInitializing() {
        return !initializationCompleted.get();
    }
    
    public int getReceivedGroupKeysCount() {
        return receivedGroupKeys.size();
    }
    
    /**
     * Get the set of data IDs associated with the context. zw
     *
     * @return Set of data IDs
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public Set<String> getReceivedGroupKeys() {
        return Collections.unmodifiableSet(receivedGroupKeys);
    }
    
    public void refreshSyncVersion() {
        this.syncVersion = System.currentTimeMillis();
    }
    
    /**
     * add receive group key.
     * @param groupKey group key.
     * @return
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public boolean addReceivedGroupKey(String groupKey) {
        boolean added = receivedGroupKeys.add(groupKey);
        if (added) {
            refreshSyncVersion();
        }
        return added;
    }
    
    /**
     * remove receive group key.
     * @param groupKey group key.
     * @return
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public boolean removeReceivedGroupKey(String groupKey) {
        boolean removed = receivedGroupKeys.remove(groupKey);
        if (removed) {
            refreshSyncVersion();
        }
        return removed;
    }
    
    /**
     * Get the set of listeners associated with the context.
     *
     * @return Set of listeners
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public Set<ConfigFuzzyWatcherWrapper> getConfigFuzzyWatcherWrappers() {
        return configFuzzyWatcherWrappers;
    }
    
    /**
     * Abstract task for fuzzy notification.
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    abstract static class AbstractFuzzyNotifyTask implements Runnable {
        
        /**
         * Flag indicating whether the task is asynchronous.
          * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
         */
        boolean async = false;
        
        /**
         * Check if the task is asynchronous.
         *
         * @return True if the task is asynchronous, otherwise false
          * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
         */
        public boolean isAsync() {
            return async;
        }
    }
    
    void syncFuzzyWatchers() {
        for (ConfigFuzzyWatcherWrapper configFuzzyWatcher : configFuzzyWatcherWrappers) {
            
            if (configFuzzyWatcher.syncVersion == this.syncVersion) {
                continue;
            }
            
            Set<String> receivedGroupKeysContext = new HashSet<>(getReceivedGroupKeys());
            Set<String> syncGroupKeys = configFuzzyWatcher.getSyncGroupKeys();
            List<FuzzyGroupKeyPattern.GroupKeyState> groupKeyStates =
                FuzzyGroupKeyPattern.diffGroupKeys(
                    receivedGroupKeysContext, syncGroupKeys);
            if (CollectionUtils.isEmpty(groupKeyStates)) {
                configFuzzyWatcher.syncVersion = this.syncVersion;
            } else {
                for (FuzzyGroupKeyPattern.GroupKeyState groupKeyState : groupKeyStates) {
                    String changedType = groupKeyState.isExist() ? ADD_CONFIG : DELETE_CONFIG;
                    doNotifyWatcher(groupKeyState.getGroupKey(), changedType,
                        FUZZY_WATCH_DIFF_SYNC_NOTIFY,
                        configFuzzyWatcher);
                }
            }
            
        }
    }
    
    /**
     * creat a new future of this context.
     *
     * @return
      * <p>单模式模糊监听上下文；管理 groupKey 集合与监听器。</p>
     */
    public Future<Set<String>> createNewFuture() {
        Future<Set<String>> future = new Future<Set<String>>() {
            
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
                return ConfigFuzzyWatchContext.this.initializationCompleted.get();
            }
            
            @Override
            public Set<String> get() throws InterruptedException, ExecutionException {
                synchronized (ConfigFuzzyWatchContext.this) {
                    while (!ConfigFuzzyWatchContext.this.initializationCompleted.get()) {
                        ConfigFuzzyWatchContext.this.wait();
                    }
                }
                return new HashSet<>(ConfigFuzzyWatchContext.this.getReceivedGroupKeys());
            }
            
            public Set<String> get(long timeout, TimeUnit unit)
                throws InterruptedException, TimeoutException {
                if (!ConfigFuzzyWatchContext.this.initializationCompleted.get()) {
                    synchronized (ConfigFuzzyWatchContext.this) {
                        ConfigFuzzyWatchContext.this.wait(unit.toMillis(timeout));
                    }
                }
                
                if (!ConfigFuzzyWatchContext.this.initializationCompleted.get()) {
                    throw new TimeoutException(
                        "fuzzy watch result future timeout for " + unit.toMillis(timeout)
                            + " millis");
                }
                return new HashSet<>(ConfigFuzzyWatchContext.this.getReceivedGroupKeys());
            }
        };
        
        return future;
    }
}
