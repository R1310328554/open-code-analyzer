/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.cluster.infinispan.remote;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterListener;
import org.keycloak.cluster.ClusterProvider.DCNotify;
import org.keycloak.cluster.infinispan.TaskCallback;
import org.keycloak.cluster.infinispan.WrapperClusterEvent;
import org.keycloak.common.util.ConcurrentMultivaluedHashMap;
import org.keycloak.common.util.Retry;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.connections.infinispan.NodeInfo;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryRemoved;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCustomEvent;
import org.infinispan.client.hotrod.exceptions.HotRodClientException;
import org.infinispan.commons.io.UnsignedNumeric;
import org.infinispan.commons.marshall.Marshaller;
import org.jboss.logging.Logger;

import static org.keycloak.cluster.infinispan.InfinispanClusterProvider.TASK_KEY_PREFIX;

/**
 * 远程 Infinispan 集群事件通知管理器。
 * <p>
 * 通过 Hot Rod {@link ClientListener} 订阅 work 缓存的创建/修改/移除事件，
 * 反序列化 {@link WrapperClusterEvent} 后分发给已注册 listener，并在锁条目移除时通知 {@link TaskCallback}。
 */
@ClientListener(converterFactoryName = "___eager-key-value-version-converter", useRawData = true)
public class RemoteInfinispanNotificationManager {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    /** 异步任务的完成回调映射。 */
    private final ConcurrentMap<String, TaskCallback> taskCallbacks = new ConcurrentHashMap<>();
    /** 按 taskKey 注册的集群事件监听器。 */
    private final ConcurrentMultivaluedHashMap<String, ClusterListener> listeners = new ConcurrentMultivaluedHashMap<>();
    /** 事件处理线程池（避免阻塞 Hot Rod 回调线程）。 */
    private final Executor executor;
    /** 远端 work 缓存。 */
    private final RemoteCache<String, Object> workCache;
    /** 本节点与站点元数据。 */
    private final NodeInfo nodeInfo;
    /** Hot Rod 容器共享的序列化器。 */
    private final Marshaller marshaller;

    public RemoteInfinispanNotificationManager(Executor executor, RemoteCache<String, Object> workCache, NodeInfo nodeInfo) {
        this.executor = executor;
        this.workCache = workCache;
        this.nodeInfo = nodeInfo;
        this.marshaller = workCache.getRemoteCacheContainer().getMarshaller();
    }

    /** 注册 Hot Rod 客户端监听器。 */
    public void addClientListener() {
        workCache.addClientListener(this);
    }

    public void removeClientListener() {
        // 各 Provider 独立关闭，close() 调用顺序不确定，需检查容器是否仍存活
        if (workCache.getRemoteCacheContainer().isStarted()) {
            workCache.removeClientListener(this);
        }
    }

    public void registerListener(String taskKey, ClusterListener task) {
        listeners.add(taskKey, task);
    }

    public TaskCallback registerTaskCallback(String taskKey, TaskCallback callback) {
        var existing = taskCallbacks.putIfAbsent(taskKey, callback);
        return existing != null ? existing : callback;
    }

    /** 将包装事件写入远端 work 缓存，带退避重试。 */
    public void notify(String taskKey, Collection<? extends ClusterEvent> events, boolean ignoreSender, DCNotify dcNotify) {
        if (events == null || events.isEmpty()) {
            return;
        }
        var wrappedEvent = WrapperClusterEvent.wrap(taskKey, events, nodeInfo.nodeName(), nodeInfo.siteName(), dcNotify, ignoreSender);

        var eventKey = SecretGenerator.getInstance().generateSecureID();

        if (logger.isTraceEnabled()) {
            logger.tracef("Sending event with key %s: %s", eventKey, events);
        }

        Retry.executeWithBackoff((int iteration) -> {
            try {
                workCache.put(eventKey, wrappedEvent, 120, TimeUnit.SECONDS);
            } catch (HotRodClientException re) {
                if (logger.isDebugEnabled()) {
                    logger.debugf(re, "Failed sending notification to remote cache '%s'. Key: '%s', iteration '%s'. Will try to retry the task",
                            workCache.getName(), eventKey, iteration);
                }

                // 重新抛出，由 Retry 处理并重试
                throw re;
            }

        }, 10, 10);

    }

    /** 返回本节点名称，用于 LockEntry 与事件过滤。 */
    public String getMyNodeName() {
        return nodeInfo.nodeName();
    }

    /** 处理缓存条目创建/修改事件：反序列化后在 executor 中分发。 */
    @ClientCacheEntryCreated
    @ClientCacheEntryModified
    public void onEntryUpdated(ClientCacheEntryCustomEvent<byte[]> event) {
        try {
            byte[] data = event.getEventData();
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int length = UnsignedNumeric.readUnsignedInt(buffer);

            // 反序列化键
            String key = (String) marshaller.objectFromByteBuffer(data, buffer.position(), length);

            buffer.position(buffer.position() + length);
            length = UnsignedNumeric.readUnsignedInt(buffer);

            // 反序列化值
            Object value = marshaller.objectFromByteBuffer(data, buffer.position(), length);
            executor.execute(() -> eventReceived(key, value));
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Unexpected error handling an update/create event from Infinispan cluster", e);
        }
    }

    /** 处理缓存条目移除事件：通知 TaskCallback 任务完成。 */
    @ClientCacheEntryRemoved
    public void onEntryRemoved(ClientCacheEntryCustomEvent<byte[]> event) {
        try {
            byte[] data = event.getEventData();
            ByteBuffer buffer = ByteBuffer.wrap(data);
            int length = UnsignedNumeric.readUnsignedInt(buffer);

            // 反序列化被移除的键
            taskFinished((String) marshaller.objectFromByteBuffer(data, buffer.position(), length));
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Unexpected error handling a remove event from Infinispan cluster", e);
        }
    }

    /** 过滤并分发收到的集群事件给已注册 listener。 */
    private void eventReceived(String key, Object obj) {
        if (!(obj instanceof WrapperClusterEvent event)) {
            // TASK_KEY_PREFIX 条目在锁释放后很快消失，不必记录
            if (obj == null && !key.startsWith(TASK_KEY_PREFIX)) {
                logger.warnf("Event object wasn't available in remote cache after event was received. Event key: %s", key);
            }
            return;
        }

        if (event.rejectEvent(nodeInfo.nodeName(), nodeInfo.siteName())) {
            return;
        }

        String eventKey = event.getEventKey();

        if (logger.isTraceEnabled()) {
            logger.tracef("Received event: %s", event);
        }

        List<ClusterListener> myListeners = listeners.get(eventKey);
        if (myListeners != null) {
            for (var e : event.getDelegateEvents()) {
                myListeners.forEach(e);
            }
        }
    }

    /** 锁条目移除时通知 TaskCallback。 */
    private void taskFinished(String taskKey) {
        TaskCallback callback = taskCallbacks.remove(taskKey);
        if (callback == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debugf("Finished task '%s' with '%b'", taskKey, true);
        }
        callback.setSuccess(true);
        callback.getTaskCompletedLatch().countDown();
    }
}
