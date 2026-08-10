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

import org.keycloak.storage.jpa.JpaHashUtils;

import org.hibernate.annotations.Nationalized;

/**
 * 联邦用户属性 JPA 实体，映射 FED_USER_ATTRIBUTE 表。
 * <p>
 * 短值存 {@link #value}；超过 2024 字符的长值存 {@link #longValue}，并以
 * {@link JpaHashUtils} 生成精确/小写哈希供索引查询。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name="getFederatedAttributesByNameAndValue", query="select attr.userId from FederatedUserAttributeEntity attr where attr.name = :name and attr.value = :value and attr.realmId=:realmId"),
        @NamedQuery(name="getFederatedAttributesByNameAndLongValue", query="select attr.userId, attr.longValue from FederatedUserAttributeEntity attr where attr.name = :name and attr.longValueHash = :longValueHash and attr.realmId=:realmId"),
        @NamedQuery(name="getFederatedAttributesByUser", query="select attr from FederatedUserAttributeEntity attr where attr.userId = :userId and attr.realmId=:realmId"),
        @NamedQuery(name="deleteUserFederatedAttributesByUser", query="delete from  FederatedUserAttributeEntity attr where attr.userId = :userId and attr.realmId=:realmId"),
        @NamedQuery(name="deleteUserFederatedAttributesByUserAndName", query="delete from  FederatedUserAttributeEntity attr where attr.userId = :userId and attr.name=:name and attr.realmId=:realmId"),
        @NamedQuery(name="deleteUserFederatedAttributesByRealm", query="delete from  FederatedUserAttributeEntity attr where attr.realmId=:realmId"),
        @NamedQuery(name="deleteFederatedAttributesByStorageProvider", query="delete from FederatedUserAttributeEntity e where e.storageProviderId=:storageProviderId"),
        @NamedQuery(name="deleteUserFederatedAttributesByRealmAndLink", query="delete from  FederatedUserAttributeEntity attr where attr.userId IN (select u.id from UserEntity u where u.realmId=:realmId and u.federationLink=:link)")
})
@Table(name="FED_USER_ATTRIBUTE")
@Entity
public class FederatedUserAttributeEntity {

    /** 属性行 UUID（主键）。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;

    /** 联邦用户 ID。 */
    @Column(name = "USER_ID")
    protected String userId;

    /** 所属 realm ID。 */
    @Column(name = "REALM_ID")
    protected String realmId;

    /** 用户存储提供者组件 ID。 */
    @Column(name = "STORAGE_PROVIDER_ID")
    protected String storageProviderId;

    /** 属性名。 */
    @Column(name = "NAME")
    protected String name;
    /** 短属性值（≤2024 字符）。 */
    @Column(name = "VALUE")
    protected String value;

    /** 长属性值精确匹配哈希。 */
    @Column(name = "LONG_VALUE_HASH")
    private byte[] longValueHash;
    /** 长属性值小写匹配哈希。 */
    @Column(name = "LONG_VALUE_HASH_LOWER_CASE")
    private byte[] longValueHashLowerCase;
    /** 长属性值原文（Nationalized 列）。 */
    @Nationalized
    @Column(name = "LONG_VALUE")
    private String longValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        if (value != null && longValue != null) {
            throw new IllegalStateException(String.format("Federated user with id %s should not have set both `value` and `longValue` for attribute %s.", userId, name));
        }
        return value != null ? value : longValue;
    }

    public void setValue(String value) {
        if (value == null) {
            this.value = null;
            this.longValue = null;
            this.longValueHash = null;
            this.longValueHashLowerCase = null;
        } else if (value.length() > 2024) { // 长属性阈值见 jpa-changelog-2.1.0.xml
            this.value = null;
            this.longValue = value;
            this.longValueHash = JpaHashUtils.hashForAttributeValue(value);
            this.longValueHashLowerCase = JpaHashUtils.hashForAttributeValueLowerCase(value);
        } else {
            this.value = value;
            this.longValue = null;
            this.longValueHash = null;
            this.longValueHashLowerCase = null;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof FederatedUserAttributeEntity)) return false;

        FederatedUserAttributeEntity that = (FederatedUserAttributeEntity) o;

        if (!id.equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


}
