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

package org.keycloak.loginfailures.jpa;

import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.keycloak.models.UserLoginFailureModel;

/**
 * {@link UserLoginFailureModel} 的 JPA 适配器，封装 {@link LoginFailureEntity}。
 * <p>
 * 所有写操作均为幂等（赋值或递增），配合悲观锁 refresh 不会丢失失败计数。
 * 首次写前 {@link #ensureLocked()} 以 {@link LockModeType#PESSIMISTIC_WRITE} refresh，
 * 丢弃 persistence context 中可能过期的快照。
 */
public class UserLoginFailureAdapter implements UserLoginFailureModel {

    private final EntityManager em;
    private final LoginFailureEntity entity;
    /** 是否已对实体加悲观写锁并完成 refresh。 */
    private boolean locked;

    public UserLoginFailureAdapter(EntityManager em, LoginFailureEntity entity) {
        this.em = Objects.requireNonNull(em);
        this.entity = Objects.requireNonNull(entity);
    }

    /**
     * 写路径前置：refresh + 悲观写锁，保证基于最新行状态更新。
     * {@link JpaUserLoginFailureProvider} 保证每实体每会话仅一个 adapter 实例。
     */
    private void ensureLocked() {
        if (!locked) {
            // em.refresh 会丢弃未 flush 的本地修改；单 adapter 约束 + DefaultBruteForceProtector
            // 的「先判定再更新」模式避免基于陈旧读数的 lost update。
            em.refresh(entity, LockModeType.PESSIMISTIC_WRITE);
            locked = true;
        }
    }

    /** 复合 id：{@code realmId:userId}，与存储主键一致。 */
    @Override
    public String getId() {
        return entity.getRealmId() + ":" + entity.getUserId();
    }

    @Override
    public String getUserId() {
        return entity.getUserId();
    }

    @Override
    public int getFailedLoginNotBefore() {
        return Math.toIntExact(entity.getFailedLoginNotBefore());
    }

    @Override
    public void setFailedLoginNotBefore(int notBefore) {
        ensureLocked();
        entity.setFailedLoginNotBefore(notBefore);
    }

    @Override
    public int getNumFailures() {
        return entity.getNumFailures();
    }

    @Override
    public void incrementFailures() {
        ensureLocked();
        entity.setNumFailures(entity.getNumFailures() + 1);
    }

    @Override
    public int getNumTemporaryLockouts() {
        return entity.getNumTemporaryLockouts();
    }

    @Override
    public void incrementTemporaryLockouts() {
        ensureLocked();
        entity.setNumTemporaryLockouts(entity.getNumTemporaryLockouts() + 1);
    }

    /** 登录成功或管理员解锁：清零主认证失败计数与 IP/时间戳。 */
    @Override
    public void clearFailures() {
        ensureLocked();
        entity.setFailedLoginNotBefore(0);
        entity.setNumFailures(0);
        entity.setNumTemporaryLockouts(0);
        entity.setLastFailure(0);
        entity.setLastIPFailure(null);
    }

    @Override
    public long getLastFailure() {
        return entity.getLastFailure();
    }

    @Override
    public void setLastFailure(long lastFailure) {
        ensureLocked();
        entity.setLastFailure(lastFailure);
    }

    @Override
    public String getLastIPFailure() {
        return entity.getLastIPFailure();
    }

    @Override
    public void setLastIPFailure(String ip) {
        ensureLocked();
        entity.setLastIPFailure(ip);
    }

    @Override
    public int getNumSecondaryAuthFailures() {
        return entity.getNumSecondaryAuthFailures();
    }

    @Override
    public void incrementSecondaryAuthFailures() {
        ensureLocked();
        entity.setNumSecondaryAuthFailures(entity.getNumSecondaryAuthFailures() + 1);
    }

    /** 同时清除主认证与二次认证失败计数。 */
    @Override
    public void clearPrimaryAndSecondaryAuthFailures() {
        clearFailures();
        entity.setNumSecondaryAuthFailures(0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UserLoginFailureAdapter that = (UserLoginFailureAdapter) o;
        return entity.equals(that.entity);
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }
}
