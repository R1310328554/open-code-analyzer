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

package org.keycloak.authentication.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.keycloak.connections.jpa.AsynchronousCommitAllowed;

import org.hibernate.annotations.DynamicUpdate;

@NamedQueries({
        // 按 realm 批量删除根认证会话
        @NamedQuery(
                name = "deleteRootAuthSessionByRealm",
                query = "DELETE FROM RootAuthenticationSessionEntity sess" +
                        " WHERE sess.realmId = :realmId"),
        // 查找 realm 内已过期的根会话 ID
        @NamedQuery(
                name = "findExpiredRootAuthSessionIdsByRealm",
                query = "SELECT sess.id FROM RootAuthenticationSessionEntity sess" +
                        " WHERE sess.realmId = :realmId AND sess.timestamp < :timestamp"
        ),
        // 按 ID 列表删除已过期的根会话
        @NamedQuery(
                name = "deleteExpiredRootAuthSessionByIds",
                query = "DELETE FROM RootAuthenticationSessionEntity e WHERE e.id IN :ids AND e.timestamp < :timestamp"
        ),
        // 幂等插入：主键冲突时忽略（PostgreSQL on conflict）
        @NamedQuery(
                name = "insertRootAuthSessionIfAbsent",
                query = "insert into RootAuthenticationSessionEntity (id, realmId, timestamp, version) values (:id, :realmId, :timestamp, 0)" +
                        " on conflict do nothing"
        )
})
@Entity
@Table(name = "ROOT_AUTH_SESSION")
@DynamicUpdate
/** JPA 实体，映射 ROOT_AUTH_SESSION 表，表示 realm 下的根认证会话（可包含多个标签页子会话）。 */
public class RootAuthenticationSessionEntity implements AsynchronousCommitAllowed {

    /** 根会话 UUID。 */
    @Id
    @Column(name = "ID", length = 36)
    private String id;

    /** 所属 realm ID。 */
    @Column(name = "REALM_ID")
    private String realmId;

    /** 创建或最后活动时间戳（秒）。 */
    @Column(name = "TIMESTAMP")
    private long timestamp;

    /** 乐观锁版本号。 */
    @Version
    @Column(name = "VERSION")
    private int version;

    /** 按 tabId 索引的子认证会话集合，级联删除孤儿记录。 */
    @OneToMany(mappedBy = "rootAuthenticationSession", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "tabId")
    private Map<String, AuthenticationSessionEntity> authenticationSessions = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Map<String, AuthenticationSessionEntity> getAuthenticationSessions() {
        return authenticationSessions;
    }

    public void setAuthenticationSessions(Map<String, AuthenticationSessionEntity> authenticationSessions) {
        this.authenticationSessions = authenticationSessions;
    }

    /** 仅按 id 判等，忽略 realm 与子会话集合。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RootAuthenticationSessionEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("RootAuthenticationSessionEntity [ id=%s, realm=%s ]", id, realmId);
    }
}
