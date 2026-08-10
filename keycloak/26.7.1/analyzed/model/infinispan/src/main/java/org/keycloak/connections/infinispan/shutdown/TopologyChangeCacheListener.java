/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.connections.infinispan.shutdown;

import java.util.Objects;

import org.infinispan.Cache;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.jboss.logging.Logger;

/**
 * Infinispan 缓存监听器：在缓存拓扑稳定（无 rehash 进行中）之前延迟服务器关闭。
 * <p>
 * 通过 {@link #waitForStableTopology(Cache)} 注册到指定缓存，返回应加入
 * {@link ShutdownManager} 的 {@link WaitConditionShutdownListener}。
 * <p>
 * <b>限制：</b>当两个及以上 Keycloak 实例同时收到关闭信号时，各实例可能在彼此
 * 真正离群之前就观察到稳定拓扑，从而并发关闭，增加数据丢失风险。
 */
@Listener
public class TopologyChangeCacheListener {

    private static final Logger logger = Logger.getLogger(TopologyChangeCacheListener.class);

    /** 与拓扑变更事件联动的关闭等待监听器。 */
    private final WaitConditionShutdownListener listener;

    /**
     * 在指定缓存上注册拓扑变更监听器，并返回在拓扑不稳定时阻塞关闭的
     * {@link WaitConditionShutdownListener}。
     *
     * @param cache 需监控拓扑变更的 Infinispan 缓存
     * @return 应注册到 {@link ShutdownManager} 的关闭等待监听器
     */
    public static WaitConditionShutdownListener waitForStableTopology(Cache<?, ?> cache) {
        var dm = cache.getAdvancedCache().getDistributionManager();
        var condition = new TopologyShutdownCondition(cache.getName(), dm);
        var listener = new WaitConditionShutdownListener(condition);
        cache.addListener(new TopologyChangeCacheListener(listener));
        return listener;
    }

    private TopologyChangeCacheListener(WaitConditionShutdownListener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    /** 拓扑变更事件到达时，通知关闭监听器重新检查稳定条件。 */
    @TopologyChanged
    public void onTopologyChange(TopologyChangedEvent<?, ?> event) {
        listener.check();
    }

    /**
     * 基于 DistributionManager 判断缓存拓扑是否仍在 rehash 或加入过程中的关闭条件。
     *
     * @param cacheName 缓存名称，用于日志
     * @param dm        分布管理器，提供 rehash/加入状态
     */
    private record TopologyShutdownCondition(String cacheName, DistributionManager dm) implements ShutdownCondition {

        /** rehash 未完成或节点加入未完成时视为仍在进行。 */
        @Override
        public boolean inProgress() {
            return dm.isRehashInProgress() || !dm.isJoinComplete();
        }

        /** 等待稳定拓扑超时时的警告日志。 */
        @Override
        public void onTimeout() {
            logger.warnf("Cache '%s': timed out waiting for stable topology during shutdown. Check for possible causes, or extend the shutdown timeout to allow for more time for the cache to rebalance.", cacheName);
        }

        /** 拓扑已稳定、可以继续关闭时的信息日志。 */
        @Override
        public void complete() {
            logger.infof("Cache '%s': topology stable, proceeding with shutdown", cacheName);
        }
    }
}
