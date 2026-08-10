/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.expiration;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;

import org.infinispan.client.hotrod.RemoteCache;

/**
 * 使用外部 Infinispan、但未启用嵌入式集群时的 {@link ExpirationTask} 实现。
 * <p>
 * 通过一致性哈希将 realm 分配到各 Keycloak 节点，避免重复清理。网络分区时可能出现多个实例
 * 同时负责同一 realm 的情况，此时依赖数据库锁保证数据一致。
 * <p>
 * 节点上下线信息无法实时同步，个别 realm 可能在某轮迭代中未被检查。
 */
class RemoteExpirationTask extends BaseExpirationTask {

    /** 基于远程工作缓存维护的一致性哈希环。 */
    private final ConsistentHash consistentHash;

    RemoteExpirationTask(KeycloakSessionFactory factory, ScheduledExecutorService scheduledExecutorService, int intervalSeconds, Consumer<Duration> onTaskExecuted, RemoteCache<String, String> workCache, String nodeName) {
        super(factory, scheduledExecutorService, intervalSeconds, onTaskExecuted);
        this.consistentHash = ConsistentHash.create(workCache, scheduledExecutorService, UUID.randomUUID().toString(), nodeName, intervalSeconds);
    }

    @Override
    public final void start() {
        consistentHash.start();
        super.start();
    }

    @Override
    public final void stop() {
        super.stop();
        consistentHash.stop();
    }

    /** 返回当前节点负责的 realm 过滤器快照。 */
    @Override
    final Predicate<RealmModel> realmFilter() {
        return consistentHash.consistentHashSnapshot();
    }

    /** 返回参与一致性哈希的活跃成员数量。 */
    int membersSize() {
        return consistentHash.size();
    }

}
