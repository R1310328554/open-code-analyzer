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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import org.keycloak.connections.jpa.AsynchronousCommitAllowed;

/**
 * 登录失败持久化实体，映射表 {@code LOGIN_FAILURE}。
 * <p>
 * 复合主键 {@link LoginFailureKey}（realmId + userId）。无乐观锁版本列——
 * 仅通过 {@link UserLoginFailureAdapter} 以悲观锁 + 幂等更新访问，避免 lost update。
 * <p>
 * 实现 {@link AsynchronousCommitAllowed}，允许在异步提交路径下持久化。
 */
@NamedQueries({
        @NamedQuery(
                name = "insertLoginFailure",
                // PostgreSQL 风格 upsert：冲突时不抛异常，配合 Provider 幂等 add
                query = "insert into LoginFailureEntity (realmId, userId) values (:realmId, :userId)" +
                        " on conflict (realmId, userId) do nothing"
        ),
        @NamedQuery(
                name = "deleteLoginFailureByRealm",
                query = "delete from LoginFailureEntity e where e.realmId = :realmId"
        ),
        @NamedQuery(
                name = "findExpiredLoginFailureUserIdsByRealm",
                // lastFailure 早于 expire 阈值的用户 id，供分批删除
                query = "select e.userId from LoginFailureEntity e where e.realmId = :realmId and e.lastFailure < :expire"
        ),
        @NamedQuery(
                name = "deleteExpiredLoginFailureByRealmAndUserIds",
                // 删除时再次校验 lastFailure，避免与并发登录失败更新竞态
                query = "delete from LoginFailureEntity e where e.realmId = :realmId and e.userId in :userIds and e.lastFailure < :expire"
        ),
})
@Entity
@IdClass(LoginFailureKey.class)
@Table(name = "LOGIN_FAILURE")
public class LoginFailureEntity implements AsynchronousCommitAllowed {

    @Id
    @Column(name = "REALM_ID", length = 36)
    private String realmId;

    @Id
    @Column(name = "USER_ID")
    private String userId;

    /** 在此时间戳（秒）之前禁止再次尝试登录。 */
    @Column(name = "FAILED_LOGIN_NOT_BEFORE")
    private long failedLoginNotBefore;

    /** 当前窗口内连续失败次数。 */
    @Column(name = "NUM_FAILURES")
    private int numFailures;

    /** 累计临时锁定次数。 */
    @Column(name = "NUM_TEMPORARY_LOCKOUTS")
    private int numTemporaryLockouts;

    /** 最近一次失败时间（毫秒 epoch）。 */
    @Column(name = "LAST_FAILURE")
    private long lastFailure;

    /** 最近一次失败来源 IP。 */
    @Column(name = "LAST_IP_FAILURE")
    private String lastIPFailure;

    /** 二次认证（如 OTP）失败次数。 */
    @Column(name = "NUM_SECONDARY_AUTH_FAILURES")
    private int numSecondaryAuthFailures;

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getFailedLoginNotBefore() {
        return failedLoginNotBefore;
    }

    public void setFailedLoginNotBefore(long failedLoginNotBefore) {
        this.failedLoginNotBefore = failedLoginNotBefore;
    }

    public int getNumFailures() {
        return numFailures;
    }

    public void setNumFailures(int numFailures) {
        this.numFailures = numFailures;
    }

    public int getNumTemporaryLockouts() {
        return numTemporaryLockouts;
    }

    public void setNumTemporaryLockouts(int numTemporaryLockouts) {
        this.numTemporaryLockouts = numTemporaryLockouts;
    }

    public long getLastFailure() {
        return lastFailure;
    }

    public void setLastFailure(long lastFailure) {
        this.lastFailure = lastFailure;
    }

    public String getLastIPFailure() {
        return lastIPFailure;
    }

    public void setLastIPFailure(String lastIPFailure) {
        this.lastIPFailure = lastIPFailure;
    }

    public int getNumSecondaryAuthFailures() {
        return numSecondaryAuthFailures;
    }

    public void setNumSecondaryAuthFailures(int numSecondaryAuthFailures) {
        this.numSecondaryAuthFailures = numSecondaryAuthFailures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginFailureEntity that)) return false;
        return Objects.equals(realmId, that.realmId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(realmId, userId);
    }

    @Override
    public String toString() {
        return String.format("LoginFailureEntity [ userId=%s, realmId=%s, numFailures=%d, numSecondaryAuthFailures=%d ]",
                userId, realmId, numFailures, numSecondaryAuthFailures);
    }
}
