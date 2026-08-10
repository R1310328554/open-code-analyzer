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
 * Client Scope 与 Realm 角色的映射 JPA 实体，映射 CLIENT_SCOPE_ROLE_MAPPING 表。
 * <p>
 * 复合主键 (clientScope, role)；授予该 scope 的用户/客户端自动获得对应角色。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name="deleteClientScopeRoleMappingByRole", query="delete from ClientScopeRoleMappingEntity where role = :role"),
        @NamedQuery(name="deleteClientScopeRoleMappingByClientScope", query="delete from ClientScopeRoleMappingEntity where clientScope = :clientScope")
})
@Table(name="CLIENT_SCOPE_ROLE_MAPPING")
@Entity
@IdClass(ClientScopeRoleMappingEntity.Key.class)
public class ClientScopeRoleMappingEntity {

    /** 所属 client scope（复合主键之一）。 */
    @Id
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "SCOPE_ID")
    protected ClientScopeEntity clientScope;

    /** 映射的 realm 角色（复合主键之一）。 */
    @Id
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="ROLE_ID")
    protected RoleEntity role;

    public ClientScopeEntity getClientScope() {
        return clientScope;
    }

    public void setClientScope(ClientScopeEntity clientScope) {
        this.clientScope = clientScope;
    }

    public RoleEntity getRole() {
        return role;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

    /** 复合主键类：clientScope + role。 */
    public static class Key implements Serializable {

        protected ClientScopeEntity clientScope;

        protected RoleEntity role;

        public Key() {
        }

        public Key(ClientScopeEntity clientScope, RoleEntity role) {
            this.clientScope = clientScope;
            this.role = role;
        }

        public ClientScopeEntity getClientScope() {
            return clientScope;
        }

        public RoleEntity getRole() {
            return role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (clientScope != null ? !clientScope.getId().equals(key.clientScope != null ? key.clientScope.getId() : null) : key.clientScope != null) return false;
            if (role != null ? !role.getId().equals(key.role != null ? key.role.getId() : null) : key.role != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = clientScope != null ? clientScope.getId().hashCode() : 0;
            result = 31 * result + (role != null ? role.getId().hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!(o instanceof ClientScopeRoleMappingEntity)) return false;

        ClientScopeRoleMappingEntity key = (ClientScopeRoleMappingEntity) o;

        if (clientScope != null ? !clientScope.getId().equals(key.clientScope != null ? key.clientScope.getId() : null) : key.clientScope != null) return false;
        if (role != null ? !role.getId().equals(key.role != null ? key.role.getId() : null) : key.role != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clientScope != null ? clientScope.getId().hashCode() : 0;
        result = 31 * result + (role != null ? role.getId().hashCode() : 0);
        return result;
    }


}
