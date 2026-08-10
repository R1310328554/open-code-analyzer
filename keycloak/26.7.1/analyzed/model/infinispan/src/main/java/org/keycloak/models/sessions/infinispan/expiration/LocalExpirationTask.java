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
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;


/**
 * 开发环境或单实例（无集群）场景下的 {@link ExpirationTask} 实现。
 * <p>
 * 不划分 realm 归属，每次调度对所有 realm 执行过期会话清理。
 */
class LocalExpirationTask extends BaseExpirationTask implements Predicate<RealmModel> {

    LocalExpirationTask(KeycloakSessionFactory factory, ScheduledExecutorService scheduledExecutorService, int intervalSeconds, Consumer<Duration> onTaskExecuted) {
        super(factory, scheduledExecutorService, intervalSeconds, onTaskExecuted);
    }

    @Override
    final Predicate<RealmModel> realmFilter() {
        return this;
    }

    /** 单实例模式下所有 realm 均参与本轮过期检查。 */
    @Override
    public final boolean test(RealmModel realmModel) {
        return true;
    }
}
