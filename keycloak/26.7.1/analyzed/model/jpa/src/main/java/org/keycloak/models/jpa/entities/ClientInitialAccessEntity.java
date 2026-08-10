/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.jpa.entities;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * 客户端初始访问令牌 JPA 实体，映射 CLIENT_INITIAL_ACCESS 表。
 * <p>
 * 用于动态客户端注册：管理员签发带过期时间与剩余使用次数的注册链接。
 * {@link #removeExpiredClientInitialAccess} 清理过期或次数耗尽的记录。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Entity
@Table(name="CLIENT_INITIAL_ACCESS")
@NamedQueries({
        @NamedQuery(name="findClientInitialAccessByRealm", query="select ia from ClientInitialAccessEntity ia where ia.realm = :realm order by timestamp"),
        @NamedQuery(name="removeClientInitialAccessByRealm", query="delete from ClientInitialAccessEntity ia where ia.realm = :realm"),
        @NamedQuery(name="removeExpiredClientInitialAccess", query="delete from ClientInitialAccessEntity ia where (ia.expiration > 0 and (ia.timestamp + ia.expiration) < :currentTime) or ia.remainingCount = 0"),
        @NamedQuery(name="decreaseClientInitialAccessRemainingCount", query="update ClientInitialAccessEntity ia set ia.remainingCount = ia.remainingCount - 1 where ia.id = :id")
})
public class ClientInitialAccessEntity {

    /** 记录 UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;

    /** 创建时间（epoch 秒）。 */
    @Column(name="TIMESTAMP")
    private int timestamp;

    /** 有效期（秒）；0 表示永不过期。 */
    @Column(name="EXPIRATION")
    private int expiration;

    /** 初始允许注册次数。 */
    @Column(name="COUNT")
    private int count;

    /** 剩余可用次数；每次成功注册减 1。 */
    @Column(name="REMAINING_COUNT")
    private int remainingCount;

    /** 所属 realm。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REALM_ID")
    protected RealmEntity realm;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getRemainingCount() {
        return remainingCount;
    }

    public void setRemainingCount(int remainingCount) {
        this.remainingCount = remainingCount;
    }

    public RealmEntity getRealm() {
        return realm;
    }

    public void setRealm(RealmEntity realm) {
        this.realm = realm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof ClientInitialAccessEntity)) return false;

        ClientInitialAccessEntity that = (ClientInitialAccessEntity) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
