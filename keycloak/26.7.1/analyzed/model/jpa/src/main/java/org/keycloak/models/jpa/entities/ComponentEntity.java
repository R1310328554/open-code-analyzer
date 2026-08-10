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

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * SPI 组件 JPA 实体，映射 COMPONENT 表。
 * <p>
 * 表示 realm 级可插拔组件实例：User Storage、LDAP Mapper、密钥提供者等。
 * {@link #providerType} + {@link #providerId} 标识 SPI 实现；{@link #parentId} 支持组件树。
 *
 * @author <a href="mailto:bburke@redhat.com">Bill Burke</a>
 */
@Entity
@Table(name="COMPONENT")
public class ComponentEntity {

    /** 组件 UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;

    /** 所属 realm。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REALM_ID")
    protected RealmEntity realm;

    /** Admin Console 展示名称。 */
    @Column(name="NAME")
    protected String name;

    /** SPI 接口类型（如 org.keycloak.storage.UserStorageProvider）。 */
    @Column(name="PROVIDER_TYPE")
    protected String providerType;

    /** 具体 provider 实现 ID。 */
    @Column(name="PROVIDER_ID")
    protected String providerId;

    /** 父组件 ID（子 mapper 等嵌套在 User Federation 下）。 */
    @Column(name="PARENT_ID")
    protected String parentId;

    /** 子类型区分（如 ldap-mapper 下的 group-mapper）。 */
    @Column(name="SUB_TYPE")
    protected String subType;

    /** 组件运行时配置项集合。 */
    @OneToMany(fetch = FetchType.LAZY, cascade ={ CascadeType.ALL}, orphanRemoval = true, mappedBy = "component")
    Set<ComponentConfigEntity> componentConfigs = new HashSet<>();

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

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public RealmEntity getRealm() {
        return realm;
    }

    public void setRealm(RealmEntity realm) {
        this.realm = realm;
    }

    public Set<ComponentConfigEntity> getComponentConfigs() {
        if (componentConfigs == null) {
            componentConfigs = new HashSet<>();
        }
        return componentConfigs;
    }

    public void setComponentConfigs(Set<ComponentConfigEntity> componentConfigs) {
        this.componentConfigs = componentConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof ComponentEntity)) return false;

        ComponentEntity that = (ComponentEntity) o;

        if (!id.equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
