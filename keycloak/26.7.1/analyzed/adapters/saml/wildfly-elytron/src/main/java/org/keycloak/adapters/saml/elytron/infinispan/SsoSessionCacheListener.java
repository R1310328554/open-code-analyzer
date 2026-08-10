/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.adapters.saml.elytron.infinispan;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.keycloak.adapters.spi.SessionIdMapper;

import org.infinispan.Cache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryRemoved;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryRemovedEvent;
import org.infinispan.context.Flag;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.annotation.TransactionCompleted;
import org.infinispan.notifications.cachelistener.annotation.TransactionRegistered;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.Event;
import org.infinispan.notifications.cachelistener.event.TransactionCompletedEvent;
import org.infinispan.notifications.cachelistener.event.TransactionRegisteredEvent;
import org.infinispan.notifications.cachelistener.event.TransactionalEvent;
import org.infinispan.notifications.cachemanagerlistener.annotation.CacheStarted;
import org.infinispan.notifications.cachemanagerlistener.annotation.CacheStopped;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStartedEvent;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStoppedEvent;
import org.jboss.logging.Logger;

/**
 * Infinispan SSO 会话缓存监听器：同步集群内 HTTP 会话与 SSO 映射的创建与删除。
 *
 * <p>本地事件由 HTTP 会话监听器处理；远程集群事件与跨 DC RemoteStore 事件
 * 在单线程执行器中异步更新 {@link SessionIdMapper}。</p>
 *
 * @author hmlnarik
 */
@Listener(sync = false)
@ClientListener()
public class SsoSessionCacheListener {

    /** 本类日志记录器。 */
    private static final Logger LOG = Logger.getLogger(SsoSessionCacheListener.class);

    /** 全局事务 ID 到待处理缓存事件的映射。 */
    private final ConcurrentMap<String, Queue<Event>> map = new ConcurrentHashMap<>();

    /** 会话 ID 映射器。 */
    private final SessionIdMapper idMapper;

    /** SSO 会话缓存。 */
    private final Cache<String, String[]> ssoCache;

    /** 异步处理远程/集群事件的单线程执行器。 */
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 创建 SSO 缓存监听器。
     *
     * @param ssoCache SSO 缓存实例
     * @param idMapper 会话 ID 映射器
     */
    public SsoSessionCacheListener(Cache<String, String[]> ssoCache, SessionIdMapper idMapper) {
        this.ssoCache = ssoCache;
        this.idMapper = idMapper;
    }

    /** 全局事务开始时为该事务创建事件队列。 */
    @TransactionRegistered
    public void startTransaction(TransactionRegisteredEvent event) {
        if (event.getGlobalTransaction() == null) {
            return;
        }

        map.put(event.getGlobalTransaction().globalId(), new ConcurrentLinkedQueue<>());
    }

    /** 缓存启动时重建单线程执行器。 */
    @CacheStarted
    public void cacheStarted(CacheStartedEvent event) {
        this.executor = Executors.newSingleThreadExecutor();
    }

    /** 缓存停止时关闭执行器。 */
    @CacheStopped
    public void cacheStopped(CacheStoppedEvent event) {
        this.executor.shutdownNow();
    }

    /**
     * 收集集群缓存条目创建/删除事件；本地事件与 pre 事件忽略。
     * 有全局事务时先入队，否则立即处理。
     */
    @CacheEntryCreated
    @CacheEntryRemoved
    public void addEvent(TransactionalEvent event) {
        if (event.isOriginLocal()) {
            // 本地事件由本地 HTTP 会话监听器处理
            return;
        }

        if (event.isPre()) {    // 仅处理 post 事件
            return;
        }

        if (event.getGlobalTransaction() != null) {
            map.get(event.getGlobalTransaction().globalId()).add(event);
        } else {
            processEvent(event);
        }
    }

    /** 全局事务成功提交后批量处理已入队的缓存事件。 */
    @TransactionCompleted
    public void endTransaction(TransactionCompletedEvent event) {
        if (event.getGlobalTransaction() == null) {
            return;
        }

        Queue<Event> events = map.remove(event.getGlobalTransaction().globalId());

        if (events == null || ! event.isTransactionSuccessful()) {
            return;
        }

        for (final Event e : events) {
            processEvent(e);
        }
    }

    /** 将缓存事件提交到单线程执行器异步处理。 */
    private void processEvent(final Event e) {
        switch (e.getType()) {
            case CACHE_ENTRY_CREATED:
                this.executor.submit(new Runnable() {
                    @Override public void run() {
                        cacheEntryCreated((CacheEntryCreatedEvent) e);
                    }
                });
                break;

            case CACHE_ENTRY_REMOVED:
                this.executor.submit(new Runnable() {
                    @Override public void run() {
                        cacheEntryRemoved((CacheEntryRemovedEvent) e);
                    }
                });
                break;
        }
    }

    /** 处理集群内缓存条目创建：建立 SSO 会话映射。 */
    private void cacheEntryCreated(CacheEntryCreatedEvent event) {
        if (! (event.getKey() instanceof String) || ! (event.getValue() instanceof String[])) {
            return;
        }
        String httpSessionId = (String) event.getKey();
        String[] value = (String[]) event.getValue();
        String ssoId = value[0];
        String principal = value[1];

        LOG.tracev("cacheEntryCreated {0}:{1}", httpSessionId, ssoId);

        this.idMapper.map(ssoId, principal, httpSessionId);
    }

    /** 处理集群内缓存条目删除：移除 HTTP 会话映射。 */
    private void cacheEntryRemoved(CacheEntryRemovedEvent event) {
        if (! (event.getKey() instanceof String)) {
            return;
        }

        LOG.tracev("cacheEntryRemoved {0}", event.getKey());

        this.idMapper.removeSession((String) event.getKey());
    }

    /** 处理跨 DC RemoteStore 上的缓存条目创建事件。 */
    @ClientCacheEntryCreated
    public void remoteCacheEntryCreated(ClientCacheEntryCreatedEvent event) {
        if (! (event.getKey() instanceof String)) {
            return;
        }

        String httpSessionId = (String) event.getKey();

        if (idMapper.hasSession(httpSessionId)) {
            // 忽略 RemoteStore 触发的本地重复事件
            LOG.tracev("IGNORING remoteCacheEntryCreated {0}", httpSessionId);
            return;
        }

        this.executor.submit(new Runnable() {

            @Override
            public void run() {
                String[] value;
                try {
                    value = ssoCache.get((String) httpSessionId);

                    if (value != null) {
                        String ssoId = value[0];
                        String principal = value[1];

                        LOG.tracev("remoteCacheEntryCreated {0}:{1}", httpSessionId, ssoId);

                        idMapper.map(ssoId, principal, httpSessionId);
                    } else {
                        LOG.tracev("remoteCacheEntryCreated {0}", event.getKey());

                    }
                } catch (Exception ex) {
                    LOG.debugf(ex, "Cannot get remote cache entry %s", httpSessionId);
                }
            }
        });
    }

    /** 处理跨 DC RemoteStore 上的缓存条目删除事件。 */
    @ClientCacheEntryRemoved
    public void remoteCacheEntryRemoved(ClientCacheEntryRemovedEvent event) {
        LOG.tracev("remoteCacheEntryRemoved {0}", event.getKey());

        this.executor.submit(new Runnable() {

            @Override
            public void run() {
                idMapper.removeSession((String) event.getKey());
                ssoCache.getAdvancedCache().withFlags(Flag.SKIP_CACHE_STORE).remove((String) event.getKey());
            }
        });
    }
}
