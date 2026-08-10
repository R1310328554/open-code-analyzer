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
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;

import org.infinispan.Cache;
import org.infinispan.distribution.DistributionManager;

/**
 * 利用 Infinispan {@link DistributionManager} 将 realm 过期检查分散到各集群节点的 {@link ExpirationTask}。
 * <p>
 * 仅当本节点为 realm 键的 primary owner 时才执行 purge。
 * <p>
 * 一致性哈希视图在各成员间非同步更新，可能出现 realm 未被检查或由多节点重复检查；
 * 后者场景依赖数据库锁去重。
 */
class DistributionAwareExpirationTask extends BaseExpirationTask implements Predicate<RealmModel> {

    private final DistributionManager distributionManager;

    DistributionAwareExpirationTask(KeycloakSessionFactory factory, ScheduledExecutorService scheduledExecutorService, int intervalSeconds, Consumer<Duration> onTaskExecuted, DistributionManager distributionManager) {
        super(factory, scheduledExecutorService, intervalSeconds, onTaskExecuted);
        this.distributionManager = Objects.requireNonNull(distributionManager);
    }

    @Override
    final Predicate<RealmModel> realmFilter() {
        return this;
    }

    /** 本节点是否为该 realm 在缓存拓扑中的 primary owner。 */
    @Override
    public final boolean test(RealmModel realm) {
        return distributionManager.getCacheTopology().getDistribution(realm.getId()).isPrimary();
    }
}
