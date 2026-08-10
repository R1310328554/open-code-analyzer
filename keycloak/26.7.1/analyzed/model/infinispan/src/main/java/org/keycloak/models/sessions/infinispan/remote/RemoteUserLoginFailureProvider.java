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
package org.keycloak.models.sessions.infinispan.remote;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserLoginFailureModel;
import org.keycloak.models.UserLoginFailureProvider;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureEntity;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureKey;
import org.keycloak.models.sessions.infinispan.query.LoginFailureQueries;
import org.keycloak.models.sessions.infinispan.query.QueryHelper;
import org.keycloak.models.sessions.infinispan.remote.transaction.LoginFailureChangeLogTransaction;
import org.keycloak.models.sessions.infinispan.stream.ValueIdentityBiFunction;
import org.keycloak.models.sessions.infinispan.util.SessionTimeouts;

import io.reactivex.rxjava3.schedulers.Schedulers;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.api.query.Query;
import org.infinispan.commons.util.concurrent.CompletionStages;
import org.infinispan.util.concurrent.WithinThreadExecutor;
import org.jboss.logging.Logger;

import static org.keycloak.common.util.StackUtil.getShortStackTrace;


/**
 * 基于远程 Infinispan 的 {@link UserLoginFailureProvider} 实现。
 * <p>
 * 登录失败记录存储在远程缓存中，CRUD 经 {@link LoginFailureChangeLogTransaction} 提交；
 * realm 暴力破解策略变更时批量调整现有条目的 TTL。
 */
public class RemoteUserLoginFailureProvider implements UserLoginFailureProvider {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());

    /** 登录失败变更日志事务。 */
    private final LoginFailureChangeLogTransaction transaction;

    public RemoteUserLoginFailureProvider(LoginFailureChangeLogTransaction transaction) {
        this.transaction = Objects.requireNonNull(transaction);
    }

    @Override
    public UserLoginFailureModel getUserLoginFailure(RealmModel realm, String userId) {
        if (log.isTraceEnabled()) {
            log.tracef("getUserLoginFailure(%s, %s)%s", realm, userId, getShortStackTrace());
        }
        return transaction.get(new LoginFailureKey(realm.getId(), userId));
    }

    @Override
    public UserLoginFailureModel addUserLoginFailure(RealmModel realm, String userId) {
        if (log.isTraceEnabled()) {
            log.tracef("addUserLoginFailure(%s, %s)%s", realm, userId, getShortStackTrace());
        }

        var key = new LoginFailureKey(realm.getId(), userId);
        var entity = new LoginFailureEntity(realm.getId(), userId);
        return transaction.create(key, entity);
    }

    @Override
    public void removeUserLoginFailure(RealmModel realm, String userId) {
        if (log.isTraceEnabled()) {
            log.tracef("removeUserLoginFailure(%s, %s)%s", realm, userId, getShortStackTrace());
        }
        transaction.remove(new LoginFailureKey(realm.getId(), userId));
    }

    @Override
    public void removeAllUserLoginFailures(RealmModel realm) {
        if (log.isTraceEnabled()) {
            log.tracef("removeAllUserLoginFailures(%s)%s", realm, getShortStackTrace());
        }

        transaction.removeByRealmId(realm.getId());
    }

    /**
     * 根据 realm 最新暴力破解设置更新或清除登录失败记录。
     * <p>
     * 若已关闭暴力破解保护则删除全部记录；否则按新策略重新计算每条缓存条目的 lifespan。
     */
    @Override
    public void updateWithLatestRealmSettings(RealmModel realm) {
        if (!realm.isBruteForceProtected()) {
            removeAllUserLoginFailures(realm);
            return;
        }
        RemoteCache<LoginFailureKey, LoginFailureEntity> cache = transaction.getCache();
        final long maxDeltaTimeMillis = realm.getMaxDeltaTimeSeconds() * 1000L;
        final boolean isPermanentLockout = realm.isPermanentLockout();
        final int maxTemporaryLockouts = realm.getMaxTemporaryLockouts();
        Query<LoginFailureEntity> query = LoginFailureQueries.searchByRealmId(cache, realm.getId());
        CompletionStages.performConcurrently(
                QueryHelper.streamAll(query, 20, Function.identity()),
                20,
                Schedulers.from(new WithinThreadExecutor()),
                entry -> updateLifetimeOfCacheEntry(entry, cache, isPermanentLockout, maxTemporaryLockouts, maxDeltaTimeMillis));

    }

    /** 按当前 realm 策略重算单条登录失败记录的缓存 TTL（值不变，仅更新过期时间）。 */
    private static CompletionStage<?> updateLifetimeOfCacheEntry(LoginFailureEntity entry, RemoteCache<LoginFailureKey, LoginFailureEntity> cache, boolean isPermanentLockout, int maxTemporaryLockouts, long maxDeltaTimeMillis) {
        long lifespan = SessionTimeouts.getLoginFailuresLifespanMs(isPermanentLockout, maxTemporaryLockouts, maxDeltaTimeMillis, entry);
        return cache.computeIfPresentAsync(new LoginFailureKey(entry.getRealmId(), entry.getUserId()),
                // 保持原值不变，仅更新 lifespan 与 idle 时间
                ValueIdentityBiFunction.getInstance(),
                lifespan, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {

    }
}
