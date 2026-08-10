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

/**
 * 联邦用户凭证 JPA 实体，映射 FED_USER_CREDENTIAL 表。
 * <p>
 * 存储外部用户存储中的密码/WebAuthn 等凭证；{@link #priority} 决定同类型凭证顺序。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name="federatedUserCredentialByUser", query="select cred from FederatedUserCredentialEntity cred where cred.userId = :userId order by cred.priority"),
        @NamedQuery(name="federatedUserCredentialByUserAndType", query="select cred from FederatedUserCredentialEntity cred where cred.userId = :userId and cred.type = :type order by cred.priority"),
        @NamedQuery(name="federatedUserCredentialByNameAndType", query="select cred from FederatedUserCredentialEntity cred where cred.userId = :userId and cred.type = :type and cred.userLabel = :userLabel order by cred.priority"),
        @NamedQuery(name="deleteFederatedUserCredentialByUser", query="delete from FederatedUserCredentialEntity cred where cred.userId = :userId and cred.realmId = :realmId"),
        @NamedQuery(name="deleteFederatedUserCredentialByUserAndType", query="delete from FederatedUserCredentialEntity cred where cred.userId = :userId and cred.type = :type"),
        @NamedQuery(name="deleteFederatedUserCredentialByUserAndTypeAndUserLabel", query="delete from FederatedUserCredentialEntity cred where cred.userId = :userId and cred.type = :type and cred.userLabel = :userLabel"),
        @NamedQuery(name="deleteFederatedUserCredentialsByRealm", query="delete from FederatedUserCredentialEntity cred where cred.realmId=:realmId"),
        @NamedQuery(name="deleteFederatedUserCredentialsByStorageProvider", query="delete from FederatedUserCredentialEntity cred where cred.storageProviderId=:storageProviderId"),
        @NamedQuery(name="deleteFederatedUserCredentialsByRealmAndLink", query="delete from FederatedUserCredentialEntity cred where cred.userId IN (select u.id from UserEntity u where u.realmId=:realmId and u.federationLink=:link)")

})
@Table(name="FED_USER_CREDENTIAL")
@Entity
public class FederatedUserCredentialEntity {
    /** 凭证 UUID（主键）。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;

    /** 密钥数据（JSON，含哈希盐等）。 */
    @Column(name="SECRET_DATA")
    protected String secretData;

    /** 凭证元数据（JSON，类型相关配置）。 */
    @Column(name="CREDENTIAL_DATA")
    protected String credentialData;

    /** 凭证类型（如 password、otp）。 */
    @Column(name="TYPE")
    protected String type;

    /** 用户可见标签（如设备名）。 */
    @Column(name="USER_LABEL")
    protected String userLabel;

    /** 创建时间（毫秒）。 */
    @Column(name="CREATED_DATE")
    protected Long createdDate;

    /** 联邦用户 ID。 */
    @Column(name="USER_ID")
    protected String userId;

    /** 所属 realm ID。 */
    @Column(name = "REALM_ID")
    protected String realmId;

    /** 用户存储提供者组件 ID。 */
    @Column(name = "STORAGE_PROVIDER_ID")
    protected String storageProviderId;

    /** 同用户同类型凭证的排序优先级。 */
    @Column(name="PRIORITY")
    protected int priority;

    @Deprecated // Needed just for backwards compatibility when migrating old credentials
    /** 旧版 salt 列（迁移时按需转换为 secretData）。 */
    @Column(name="SALT")
    protected byte[] salt;


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getUserLabel() {
        return userLabel;
    }
    public void setUserLabel(String userLabel) {
        this.userLabel = userLabel;
    }

    public Long getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public String getSecretData() {
        return secretData;
    }
    public void setSecretData(String secretData) {
        this.secretData = secretData;
    }

    public String getCredentialData() {
        return credentialData;
    }
    public void setCredentialData(String credentialData) {
        this.credentialData = credentialData;
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Deprecated
    public byte[] getSalt() {
        return salt;
    }

    @Deprecated
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof FederatedUserCredentialEntity)) return false;

        FederatedUserCredentialEntity that = (FederatedUserCredentialEntity) o;

        if (!id.equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
