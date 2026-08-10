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
package org.keycloak.models.sessions.infinispan.changes.remote.updater.loginfailures;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserLoginFailureModel;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.BaseUpdater;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.Expiration;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.Updater;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureEntity;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureKey;
import org.keycloak.models.sessions.infinispan.util.SessionTimeouts;
import org.keycloak.utils.KeycloakSessionUtil;

/**
 * {@link LoginFailureEntity} 的 {@link Updater}，同时实现 {@link UserLoginFailureModel}。
 * <p>
 * 跟踪登录失败实体的变更，提交时重放到 Infinispan 远程缓存。
 */
public class LoginFailuresUpdater extends BaseUpdater<LoginFailureKey, LoginFailureEntity> implements UserLoginFailureModel {

    // 待提交的重放变更列表
    private final List<Consumer<LoginFailureEntity>> changes;

    private LoginFailuresUpdater(LoginFailureKey key, LoginFailureEntity entity, long version, UpdaterState initialState) {
        super(key, entity, version, initialState);
        if (entity == null) {
            assert initialState == UpdaterState.DELETED;
            changes = List.of();
            return;
        }
        changes = new ArrayList<>(4);
    }

    /** 新建登录失败 Updater（本事务创建）。 */
    public static LoginFailuresUpdater create(LoginFailureKey key, LoginFailureEntity entity) {
        return new LoginFailuresUpdater(key, Objects.requireNonNull(entity), NO_VERSION, UpdaterState.CREATED);
    }

    /** 包装从缓存读取的登录失败实体。 */
    public static LoginFailuresUpdater wrap(LoginFailureKey key, LoginFailureEntity value, long version) {
        return new LoginFailuresUpdater(key, Objects.requireNonNull(value), version, UpdaterState.READ);
    }

    /** 创建表示删除操作的 Updater。 */
    public static LoginFailuresUpdater delete(LoginFailureKey key) {
        return new LoginFailuresUpdater(key, null, NO_VERSION, UpdaterState.DELETED);
    }

    @Override
    public Expiration computeExpiration() {
        RealmModel realm = KeycloakSessionUtil.getKeycloakSession().getContext().getRealm();
        return new Expiration(
                SessionTimeouts.getLoginFailuresMaxIdleMs(realm, null, getValue()),
                SessionTimeouts.getLoginFailuresLifespanMs(realm, null, getValue()));
    }

    @Override
    public LoginFailureEntity apply(LoginFailureKey ignored, LoginFailureEntity cachedEntity) {
        assert !isDeleted();
        assert !isReadOnly();
        if (cachedEntity == null) {
            // 缓存条目已被移除
            return null;
        }
        changes.forEach(c -> c.accept(cachedEntity));
        return cachedEntity;
    }


    @Override
    public int getFailedLoginNotBefore() {
        return getValue().getFailedLoginNotBefore();
    }

    @Override
    public long getLastFailure() {
        return getValue().getLastFailure();
    }

    @Override
    public String getLastIPFailure() {
        return getValue().getLastIPFailure();
    }

    @Override
    public int getNumFailures() {
        return getValue().getNumFailures();
    }

    @Override
    public int getNumTemporaryLockouts() {
        return getValue().getNumTemporaryLockouts();
    }

    @Override
    public String getUserId() {
        return getValue().getUserId();
    }

    @Override
    public String getId() {
        return getKey().toString();
    }

    @Override
    public void clearFailures() {
        changes.clear();
        addAndApplyChange(CLEAR);
    }

    @Override
    public void setFailedLoginNotBefore(int notBefore) {
        addAndApplyChange(e -> e.setFailedLoginNotBefore(notBefore));
    }

    @Override
    public void incrementFailures() {
        addAndApplyChange(INCREMENT_FAILURES);
    }

    @Override
    public void incrementTemporaryLockouts() {
        addAndApplyChange(INCREMENT_LOCK_OUTS);
    }

    @Override
    public void setLastFailure(long lastFailure) {
        addAndApplyChange(e -> e.setLastFailure(lastFailure));
    }

    @Override
    public void setLastIPFailure(String ip) {
        addAndApplyChange(e -> e.setLastIPFailure(ip));
    }

    @Override
    public int getNumSecondaryAuthFailures() {
        return getValue().getNumSecondaryAuthFailures();
    }

    @Override
    public void incrementSecondaryAuthFailures() {
        addAndApplyChange(INCREMENT_SECONDARY_AUTH_FAILURES);
    }

    @Override
    public void clearPrimaryAndSecondaryAuthFailures() {
        changes.clear();
        addAndApplyChange(CLEAR_PRIMARY_AND_SECONDARY_AUTH_FAILURES);
    }

    @Override
    protected boolean isUnchanged() {
        return changes.isEmpty();
    }

    /** 记录变更并立即应用到本地实体快照。 */
    private void addAndApplyChange(Consumer<LoginFailureEntity> change) {
        changes.add(change);
        change.accept(getValue());
    }

    // 预定义变更操作，避免重复 lambda 分配
    private static final Consumer<LoginFailureEntity> CLEAR = LoginFailureEntity::clearFailures;
    private static final Consumer<LoginFailureEntity> CLEAR_PRIMARY_AND_SECONDARY_AUTH_FAILURES = LoginFailureEntity::clearPrimaryAndSecondaryAuthFailures;
    private static final Consumer<LoginFailureEntity> INCREMENT_FAILURES = e -> e.setNumFailures(e.getNumFailures() + 1);
    private static final Consumer<LoginFailureEntity> INCREMENT_SECONDARY_AUTH_FAILURES = e -> e.setNumSecondaryAuthFailures(e.getNumSecondaryAuthFailures() + 1);
    private static final Consumer<LoginFailureEntity> INCREMENT_LOCK_OUTS = e -> e.setNumTemporaryLockouts(e.getNumTemporaryLockouts() + 1);
}
