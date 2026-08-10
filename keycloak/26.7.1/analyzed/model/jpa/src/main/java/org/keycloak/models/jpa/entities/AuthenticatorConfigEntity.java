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
import jakarta.persistence.UniqueConstraint;

/**
 * Authenticator 配置 JPA 实体，映射 AUTHENTICATOR_CONFIG 表。
 * <p>
 * 键值对配置存 AUTHENTICATOR_CONFIG_ENTRY 集合表；同一 authenticator 下 name 唯一。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Table(name="AUTHENTICATOR_CONFIG")
@Entity
public class AuthenticatorConfigEntity {
    /** 配置 UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;

    /** Admin Console 中展示的配置别名。 */
    @Column(name="ALIAS")
    protected String alias;

    /** 所属 realm。 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REALM_ID")
    protected RealmEntity realm;

    /** authenticator 运行时参数（name → value）。 */
    @ElementCollection
    @MapKeyColumn(name="NAME")
    @Column(name="VALUE")
    @CollectionTable(
      name="AUTHENTICATOR_CONFIG_ENTRY",
      joinColumns={ @JoinColumn(name="AUTHENTICATOR_ID") },
      uniqueConstraints = { @UniqueConstraint(columnNames = {"AUTHENTICATOR_ID", "NAME"})}
    )
    private Map<String, String> config;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public RealmEntity getRealm() {
        return realm;
    }

    public void setRealm(RealmEntity realm) {
        this.realm = realm;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof AuthenticatorConfigEntity)) return false;

        AuthenticatorConfigEntity that = (AuthenticatorConfigEntity) o;

        if (!id.equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
