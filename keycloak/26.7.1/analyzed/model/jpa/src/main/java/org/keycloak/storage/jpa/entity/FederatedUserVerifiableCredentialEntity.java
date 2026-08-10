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

package org.keycloak.storage.jpa.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;


/**
 * 联邦用户可验证凭证（VC）定义 JPA 实体，映射 FED_USER_VER_CREDENTIAL 表。
 * <p>
 * 存储外部用户存储中用户的 VC 定义记录；同一用户与客户端作用域组合唯一。
 * 命名查询支持按用户查询及按 realm、作用域、存储提供者批量删除。
 */
@Entity
@Table(name="FED_USER_VER_CREDENTIAL", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"USER_ID", "CLIENT_SCOPE_ID"})
})
@NamedQueries({
        @NamedQuery(name="federatedVerifiableCredentialsByUser", query="select vc from FederatedUserVerifiableCredentialEntity vc where vc.userId = :userId"),
        @NamedQuery(name="deleteFederatedVerifiableCredentialsByRealm", query="delete from FederatedUserVerifiableCredentialEntity vc where vc.realmId = :realmId"),
        @NamedQuery(name="deleteFederatedVerifiableCredentialsByClientScope", query="delete from FederatedUserVerifiableCredentialEntity vc where vc.clientScopeId = :scopeId"),
        @NamedQuery(name="deleteFederatedVerifiableCredentialsByUser", query="delete from FederatedUserVerifiableCredentialEntity vc where vc.userId = :userId and vc.realmId = :realmId"),
        @NamedQuery(name="deleteFederatedVerifiableCredentialsByStorageProvider", query="delete from FederatedUserVerifiableCredentialEntity vc where vc.storageProviderId = :storageProviderId"),
})
public class FederatedUserVerifiableCredentialEntity {

    /** VC 定义 UUID（主键）。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY)
    protected String id;

    /** 联邦用户 ID。 */
    @Column(name="USER_ID")
    protected String userId;

    /** 所属 realm ID。 */
    @Column(name="REALM_ID")
    protected String realmId;

    /** 用户存储提供者组件 ID。 */
    @Column(name="STORAGE_PROVIDER_ID", length = 36)
    protected String storageProviderId;

    /** 关联的客户端作用域 ID。 */
    @Column(name="CLIENT_SCOPE_ID")
    protected String clientScopeId;

    /** VC 定义 revision，用于乐观锁与并发控制。 */
    @Column(name="REVISION")
    protected String revision;

    /** 用户属性 JSON 序列化字符串。 */
    @Column(name="USER_ATTRIBUTES")
    protected String userAttributes;

    /** 创建时间（毫秒）。 */
    @Column(name="CREATED_DATE")
    private Long createdDate;

    /** 最后更新时间（毫秒）。 */
    @Column(name="UPDATED_DATE")
    private Long updatedDate;

    /** JPA 乐观锁版本号。 */
    @Version
    @Column(name="VERSION")
    private int version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getStorageProviderId() {
        return storageProviderId;
    }

    public void setStorageProviderId(String storageProviderId) {
        this.storageProviderId = storageProviderId;
    }

    public String getClientScopeId() {
        return clientScopeId;
    }

    public void setClientScopeId(String clientScopeId) {
        this.clientScopeId = clientScopeId;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getUserAttributes() {
        return userAttributes;
    }

    public void setUserAttributes(String userAttributes) {
        this.userAttributes = userAttributes;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    /** 仅按主键 id 判等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof FederatedUserVerifiableCredentialEntity that)) return false;

        if (!id.equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
