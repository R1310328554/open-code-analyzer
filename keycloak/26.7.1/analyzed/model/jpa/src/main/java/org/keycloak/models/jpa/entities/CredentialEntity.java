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
import jakarta.persistence.Version;

/**
 * 用户凭据 JPA 实体，映射 {@code CREDENTIAL} 表。
 * <p>存储密码、OTP、WebAuthn 等认证凭据；{@code secretData} / {@code credentialData}
 * 为 JSON 序列化字段，由 {@link org.keycloak.models.credential} 层解析。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name="credentialByUser", query="select cred from CredentialEntity cred where cred.user = :user order by cred.priority"),
        @NamedQuery(name="deleteCredentialsByRealm", query="delete from CredentialEntity cred where cred.user IN (select u from UserEntity u where u.realmId=:realmId)"),
        @NamedQuery(name="deleteCredentialsByRealmAndLink", query="delete from CredentialEntity cred where cred.user IN (select u from UserEntity u where u.realmId=:realmId and u.federationLink=:link)")

})
@Table(name="CREDENTIAL")
@Entity
public class CredentialEntity {
    /** 凭据 UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;

    /** 凭据类型（如 password、otp、webauthn）。 */
    @Column(name="TYPE")
    protected String type;

    /** 用户自定义标签，便于在账户控制台区分多个同类凭据。 */
    @Column(name="USER_LABEL")
    protected String userLabel;

    /** 创建时间戳（毫秒）。 */
    @Column(name="CREATED_DATE")
    protected Long createdDate;
    
    /** 所属用户。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="USER_ID")
    protected UserEntity user;

    /** 敏感数据 JSON（哈希、私钥等）。 */
    @Column(name="SECRET_DATA")
    protected String secretData;

    /** 非敏感元数据 JSON（算法参数、计数器等）。 */
    @Column(name="CREDENTIAL_DATA")
    protected String credentialData;

    /** 同类型凭据间的优先级，数值越小越优先。 */
    @Column(name="PRIORITY")
    protected int priority;

    @Deprecated // Needed just for backwards compatibility when migrating old credentials
    /** 旧版密码盐值，迁移完成后不再使用。 */
    @Column(name="SALT")
    protected byte[] salt;

    /** 乐观锁版本，并发更新凭据时防丢失。 */
    @Version
    @Column(name="VERSION")
    private int version;

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

    public UserEntity getUser() {
        return user;
    }
    public void setUser(UserEntity user) {
        this.user = user;
    }

    @Deprecated
    public byte[] getSalt() {
        return salt;
    }

    @Deprecated
    public void setSalt(byte[] salt) {
        this.salt = salt;
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof CredentialEntity)) return false;

        CredentialEntity that = (CredentialEntity) o;

        if (!id.equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
