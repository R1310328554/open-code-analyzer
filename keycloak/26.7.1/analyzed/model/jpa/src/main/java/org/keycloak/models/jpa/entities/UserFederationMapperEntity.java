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

import java.util.Map;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

/**
 * User Federation Mapper JPA 实体，映射 USER_FEDERATION_MAPPER 表。
 * <p>
 * 将外部用户存储（LDAP 等）的属性/角色映射到 Keycloak 内部模型；
 * {@link #federationMapperType} 标识 mapper SPI 类型，{@link #config} 存放运行时配置。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Entity
@Table(name="USER_FEDERATION_MAPPER")
public class UserFederationMapperEntity {

    /** Mapper UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // 关联常只取 id 不加载实体，PROPERTY 访问可避免额外 SQL
    protected String id;

    /** Admin Console 展示名称。 */
    @Column(name="NAME")
    protected String name;

    /** 所属 User Federation Provider。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FEDERATION_PROVIDER_ID")
    protected UserFederationProviderEntity federationProvider;

    /** Mapper SPI 类型（如 hardcoded-role、user-attribute-ldap-mapper）。 */
    @Column(name = "FEDERATION_MAPPER_TYPE")
    protected String federationMapperType;

    /** Mapper 运行时配置项（name/value 键值对）。 */
    @ElementCollection
    @MapKeyColumn(name="NAME")
    @Column(name="VALUE")
    @CollectionTable(name="USER_FEDERATION_MAPPER_CONFIG", joinColumns={ @JoinColumn(name="USER_FEDERATION_MAPPER_ID") })
    private Map<String, String> config;

    /** 所属 realm。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REALM_ID")
    private RealmEntity realm;

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

    public UserFederationProviderEntity getFederationProvider() {
        return federationProvider;
    }

    public void setFederationProvider(UserFederationProviderEntity federationProvider) {
        this.federationProvider = federationProvider;
    }

    public String getFederationMapperType() {
        return federationMapperType;
    }

    public void setFederationMapperType(String federationMapperType) {
        this.federationMapperType = federationMapperType;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
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
        if (o == null ) return false;
        if (!(o instanceof UserFederationMapperEntity)) return false;

        UserFederationMapperEntity that = (UserFederationMapperEntity) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
