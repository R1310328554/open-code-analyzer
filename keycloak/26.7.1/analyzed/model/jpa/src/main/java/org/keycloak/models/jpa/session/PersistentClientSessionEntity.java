/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.jpa.session;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.keycloak.connections.jpa.AsynchronousCommitAllowed;

import org.hibernate.annotations.DynamicUpdate;

/**
 * 持久化客户端会话 JPA 实体，映射 OFFLINE_CLIENT_SESSION 表。
 * <p>
 * 复合主键 (userSessionId, clientId, clientStorageProvider, externalClientId, offline)；
 * 支持本地客户端与外部存储客户端（{@link #LOCAL}/{@link #EXTERNAL} 标记）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@NamedQueries({
        @NamedQuery(name="deleteClientSessionsByRealm", query="delete from PersistentClientSessionEntity sess where sess.realmId = :realmId"),
        @NamedQuery(name="deleteClientSessionsByClient", query="delete from PersistentClientSessionEntity sess where sess.clientId = :clientId and sess.clientId != 'external'"),
        @NamedQuery(name="deleteClientSessionsByExternalClient", query="delete from PersistentClientSessionEntity sess where sess.clientStorageProvider = :clientStorageProvider and sess.externalClientId = :externalClientId and sess.clientStorageProvider != 'internal'"),
        @NamedQuery(name="deleteClientSessionsByClientStorageProvider", query="delete from PersistentClientSessionEntity sess where sess.clientStorageProvider = :clientStorageProvider"),
        @NamedQuery(name="deleteClientSessionsByUser", query="delete from PersistentClientSessionEntity sess where sess.userSessionId IN (select u.userSessionId from PersistentUserSessionEntity u where u.userId = :userId)"),
        @NamedQuery(name="deleteClientSessionsByUserSession", query="delete from PersistentClientSessionEntity sess where sess.userSessionId = :userSessionId and sess.offline = :offline"),
        @NamedQuery(name="deleteClientSessionsByUserSessions", query="delete from PersistentClientSessionEntity sess where sess.userSessionId in (:userSessionId) and sess.offline = :offline"),
        // 查询 "deleteExpiredClientSessions" 自 26.5 起已弃用，未来可能移除
        @NamedQuery(name="deleteExpiredClientSessions", query="delete from PersistentClientSessionEntity sess where sess.offline = :offline AND sess.userSessionId IN (select u.userSessionId from PersistentUserSessionEntity u where u.realmId = :realmId AND u.offline = :offline AND u.lastSessionRefresh < :lastSessionRefresh)"),
        @NamedQuery(name="deleteClientSessionsByRealmSessionType", query="delete from PersistentClientSessionEntity sess where sess.offline = :offline AND sess.realmId = :realmId"),
        @NamedQuery(name="findClientSessionsByUserSession", query="select sess from PersistentClientSessionEntity sess where sess.userSessionId=:userSessionId and sess.offline = :offline"),
        @NamedQuery(name="findClientSessionsOrderedByIdInterval", query="select sess from PersistentClientSessionEntity sess where sess.offline = :offline and sess.userSessionId >= :fromSessionId and sess.userSessionId <= :toSessionId order by sess.userSessionId"),
        @NamedQuery(name="findClientSessionsOrderedByIdExact", query="select sess from PersistentClientSessionEntity sess where sess.offline = :offline and sess.userSessionId IN (:userSessionIds)"),
        @NamedQuery(name="findClientSessionsCountByClient", query="select count(sess) from PersistentClientSessionEntity sess where sess.offline = :offline and sess.clientId = :clientId and sess.clientId != 'external'"),
        @NamedQuery(name="findClientSessionsCountByExternalClient", query="select count(sess) from PersistentClientSessionEntity sess where sess.offline = :offline and sess.clientStorageProvider = :clientStorageProvider and sess.externalClientId = :externalClientId and sess.clientStorageProvider != 'internal'"),
        // 查询 "findClientSessionsByUserSessionAndClient" 自 26.7 起已弃用，未来可能移除
        @NamedQuery(name="findClientSessionsByUserSessionAndClient", query="select sess from PersistentClientSessionEntity sess where sess.userSessionId=:userSessionId and sess.offline = :offline and sess.clientId=:clientId and sess.clientId != 'external'"),
        // 查询 "findClientSessionsByUserSessionAndExternalClient" 自 26.7 起已弃用，未来可能移除
        @NamedQuery(name="findClientSessionsByUserSessionAndExternalClient", query="select sess from PersistentClientSessionEntity sess where sess.userSessionId=:userSessionId and sess.offline = :offline and sess.clientStorageProvider = :clientStorageProvider and sess.externalClientId = :externalClientId and sess.clientStorageProvider != 'internal'"),
        @NamedQuery(name="findClientSessionsClientIds", query="SELECT sess.clientId, sess.externalClientId, sess.clientStorageProvider, count(sess)" +
                " FROM PersistentClientSessionEntity sess" +
                " WHERE sess.offline = :offline AND sess.realmId = :realmId" +
                " GROUP BY sess.clientId, sess.externalClientId, sess.clientStorageProvider"),
        @NamedQuery(name = "findClientSessionsOrderedByIdExactReadOnly",
                query = "SELECT new org.keycloak.models.jpa.session.ImmutablePersistentClientSessionEntity(sess.userSessionId, sess.clientId, sess.clientStorageProvider, sess.externalClientId, sess.offline, sess.data, sess.realmId, sess.timestamp)" +
                        " FROM PersistentClientSessionEntity sess" +
                        " WHERE sess.offline = :offline AND sess.userSessionId IN (:userSessionIds)"
        ),
})
@Table(name="OFFLINE_CLIENT_SESSION")
@Entity
@DynamicUpdate
@IdClass(PersistentClientSessionEntity.Key.class)
public class PersistentClientSessionEntity implements AsynchronousCommitAllowed {

    @Override
    public boolean isAsyncCommitAllowed(EntityOperationType operationType) {
        // 撤销 refresh token 触发的 DELETE 须同步提交，防止时序攻击
        return operationType != EntityOperationType.DELETE;
    }

    /** 本地客户端 storage provider 占位符。 */
    public static final String LOCAL = "local";
    /** 外部客户端 storage provider 占位符。 */
    public static final String EXTERNAL = "external";
    /** 所属用户会话 ID（复合主键之一）。 */
    @Id
    @Column(name = "USER_SESSION_ID", length = 36)
    protected String userSessionId;

    /** 客户端 ID 或 EXTERNAL 占位（复合主键之一）。 */
    @Id
    @Column(name="CLIENT_ID", length = 36)
    protected String clientId;

    /** 客户端存储 provider ID（复合主键之一）。 */
    @Id
    @Column(name="CLIENT_STORAGE_PROVIDER", length = 36)
    protected String clientStorageProvider;

    /** 外部客户端 ID（复合主键之一）。 */
    @Id
    @Column(name="EXTERNAL_CLIENT_ID", length = 255)
    protected String externalClientId;

    /** 会话最后活动时间戳（秒）。 */
    @Column(name="TIMESTAMP")
    protected int timestamp;

    /** 乐观锁版本号。 */
    @Version
    @Column(name="VERSION")
    private int version;

    /** 离线标志："1" 离线 / "0" 在线（复合主键之一）。 */
    @Id
    @Column(name = "OFFLINE_FLAG")
    protected String offline;

    /** 序列化的 client session 附加数据。 */
    @Column(name="DATA")
    protected String data;

    /** 所属 realm ID。 */
    @Column(name = "REALM_ID", length = 36)
    protected String realmId;

    public String getUserSessionId() {
        return userSessionId;
    }

    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientStorageProvider() {
        return clientStorageProvider;
    }

    public void setClientStorageProvider(String clientStorageProvider) {
        this.clientStorageProvider = clientStorageProvider;
    }

    public String getExternalClientId() {
        return externalClientId;
    }

    public void setExternalClientId(String externalClientId) {
        this.externalClientId = externalClientId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getOffline() {
        return offline;
    }

    public void setOffline(String offline) {
        this.offline = offline;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public int getVersion() {
        return version;
    }

    /** 复合主键类：userSessionId + clientId + clientStorageProvider + externalClientId + offline。 */
    public static class Key implements Serializable {

        protected String userSessionId;

        protected String clientId;
        protected String clientStorageProvider;
        protected String externalClientId;

        protected String offline;

        public Key() {
        }

        public Key(String userSessionId, String clientId, String clientStorageProvider, String externalClientId, String offline) {
            this.userSessionId = userSessionId;
            this.clientId = clientId;
            this.externalClientId = externalClientId;
            this.clientStorageProvider = clientStorageProvider;
            this.offline = offline;
        }

        public String getUserSessionId() {
            return userSessionId;
        }

        public String getClientId() {
            return clientId;
        }

        public String getOffline() {
            return offline;
        }

        public String getClientStorageProvider() {
            return clientStorageProvider;
        }

        public String getExternalClientId() {
            return externalClientId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return Objects.equals(this.userSessionId, key.userSessionId) &&
                    Objects.equals(this.clientId, key.clientId) &&
                    Objects.equals(this.externalClientId, key.externalClientId) &&
                    Objects.equals(this.clientStorageProvider, key.clientStorageProvider) &&
                    Objects.equals(this.offline, key.offline);
        }

        @Override
        public int hashCode() {
            int result = this.userSessionId != null ? this.userSessionId.hashCode() : 0;
            result = 37 * result + (this.clientId != null ? this.clientId.hashCode() : 0);
            result = 37 * result + (this.externalClientId != null ? this.externalClientId.hashCode() : 0);
            result = 37 * result + (this.clientStorageProvider != null ? this.clientStorageProvider.hashCode() : 0);
            result = 31 * result + (this.offline != null ? this.offline.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PersistentClientSessionEntity$Key[" +
                   "userSessionId='" + userSessionId + '\'' +
                   ", clientId='" + clientId + '\'' +
                   ", clientStorageProvider='" + clientStorageProvider + '\'' +
                   ", externalClientId='" + externalClientId + '\'' +
                   ", offline='" + offline + '\'' +
                   ']';
        }
    }
}
