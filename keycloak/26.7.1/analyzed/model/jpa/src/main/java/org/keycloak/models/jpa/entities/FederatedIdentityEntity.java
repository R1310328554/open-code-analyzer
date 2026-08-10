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

package org.keycloak.models.jpa.entities;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * 联邦身份（社交/IdP 登录）关联 JPA 实体，映射 {@code FEDERATED_IDENTITY} 表。
 * <p>将本地 {@link UserEntity} 与外部 IdP 的用户标识绑定；主键为 (user, identityProvider)。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name= "findFederatedIdentityByUser", query="select link from FederatedIdentityEntity link where link.user = :user"),
        @NamedQuery(name= "findFederatedIdentityByUserAndProvider", query="select link from FederatedIdentityEntity link where link.user = :user and link.identityProvider = :identityProvider"),
        @NamedQuery(name= "findUserByFederatedIdentityAndRealm", query="select link.user from FederatedIdentityEntity link where link.realmId = :realmId and link.identityProvider = :identityProvider and link.userId = :userId"),
        @NamedQuery(name= "deleteFederatedIdentityByRealm", query="delete from FederatedIdentityEntity social where social.user IN (select u from UserEntity u where realmId=:realmId)"),
        @NamedQuery(name= "deleteFederatedIdentityByProvider", query="delete from FederatedIdentityEntity fdi where fdi.realmId = :realmId and fdi.identityProvider = :providerAlias "),
        @NamedQuery(name= "deleteFederatedIdentityByRealmAndLink", query="delete from FederatedIdentityEntity social where social.user IN (select u from UserEntity u where realmId=:realmId and u.federationLink=:link)"),
        @NamedQuery(name= "deleteFederatedIdentityByUser", query="delete from FederatedIdentityEntity social where social.user = :user")
})
@Table(name="FEDERATED_IDENTITY")
@Entity
@IdClass(FederatedIdentityEntity.Key.class)
public class FederatedIdentityEntity {

    /** 本地用户；复合主键之一。 */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private UserEntity user;

    /** 所属 Realm ID，便于按 realm 批量删除。 */
    @Column(name = "REALM_ID")
    protected String realmId;

    /** IdP 别名（如 google、github）；复合主键之一。 */
    @Id
    @Column(name = "IDENTITY_PROVIDER")
    protected String identityProvider;
    /** 外部 IdP 侧的用户唯一标识。 */
    @Column(name = "FEDERATED_USER_ID")
    protected String userId;
    /** 外部 IdP 侧的用户名/显示名。 */
    @Column(name = "FEDERATED_USERNAME")
    protected String userName;

    /** 可选的 IdP access token，仅在 storeToken 启用时持久化。 */
    @Column(name = "TOKEN")
    protected String token;

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getIdentityProvider() {
        return identityProvider;
    }

    public void setIdentityProvider(String identityProvider) {
        this.identityProvider = identityProvider;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    /** 复合主键：(user, identityProvider)。 */
    public static class Key implements Serializable {

        protected UserEntity user;

        protected String identityProvider;

        public Key() {
        }

        public Key(UserEntity user, String identityProvider) {
            this.user = user;
            this.identityProvider = identityProvider;
        }

        public UserEntity getUser() {
            return user;
        }

        public String getIdentityProvider() {
            return identityProvider;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (identityProvider != null ? !identityProvider.equals(key.identityProvider) : key.identityProvider != null)
                return false;
            if (user != null ? !user.getId().equals(key.user != null ? key.user.getId() : null) : key.user != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = user != null ? user.getId().hashCode() : 0;
            result = 31 * result + (identityProvider != null ? identityProvider.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "FederatedIdentityEntity.Key [user=" + (user != null ? user.getId() : null) + ", identityProvider=" + identityProvider + "]";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof FederatedIdentityEntity)) return false;

        FederatedIdentityEntity key = (FederatedIdentityEntity) o;

        if (identityProvider != null ? !identityProvider.equals(key.identityProvider) : key.identityProvider != null)
            return false;
        if (user != null ? !user.getId().equals(key.user != null ? key.user.getId() : null) : key.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.getId().hashCode() : 0;
        result = 31 * result + (identityProvider != null ? identityProvider.hashCode() : 0);
        return result;
    }


}
