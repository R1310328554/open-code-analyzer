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

package org.keycloak.representations.idm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 域部分导入请求的 REST 表示，支持用户、组、客户端、角色与 IdP 的增量导入。
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class PartialImportRepresentation {
    /** 资源已存在时的冲突处理策略。 */
    public enum Policy {
        /** 跳过已存在资源。 */
        SKIP,
        /** 覆盖已存在资源。 */
        OVERWRITE,
        /** 冲突时报错终止。 */
        FAIL
    };

    /** 默认冲突策略：失败。 */
    protected Policy policy = Policy.FAIL;
    /** 资源已存在时的策略字符串（与 {@link Policy} 对应）。 */
    protected String ifResourceExists;
    /** 待导入用户列表。 */
    protected List<UserRepresentation> users;
    /** 待导入组列表。 */
    protected List<GroupRepresentation> groups;
    /** 待导入客户端列表。 */
    protected List<ClientRepresentation> clients;
    /** 待导入身份提供者列表。 */
    protected List<IdentityProviderRepresentation> identityProviders;
    /** 待导入 IdP 映射器列表。 */
    protected List<IdentityProviderMapperRepresentation> identityProviderMappers;
    /** 待导入角色（域级与客户端级）。 */
    protected RolesRepresentation roles;

    /** @return 是否包含待导入用户 */
    public boolean hasUsers() {
        return (users != null) && !users.isEmpty();
    }

    /** @return 是否包含待导入组 */
    public boolean hasGroups() {
        return (groups != null) && !groups.isEmpty();
    }

    /** @return 是否包含待导入客户端 */
    public boolean hasClients() {
        return (clients != null) && !clients.isEmpty();
    }

    /** @return 是否包含待导入身份提供者 */
    public boolean hasIdps() {
        return (identityProviders != null) && !identityProviders.isEmpty();
    }

    /** @return 是否包含待导入域角色 */
    public boolean hasRealmRoles() {
        return (roles != null) && (roles.getRealm() != null) && (!roles.getRealm().isEmpty());
    }

    /** @return 是否包含待导入客户端角色 */
    public boolean hasClientRoles() {
        return (roles != null) && (roles.getClient() != null) && (!roles.getClient().isEmpty());
    }

    public String getIfResourceExists() {
        return ifResourceExists;
    }

    public void setIfResourceExists(String ifResourceExists) {
        this.ifResourceExists = ifResourceExists;
        this.policy = ifResourceExists != null ? Policy.valueOf(ifResourceExists) : null;
    }

    public Policy getPolicy() {
        return this.policy;
    }

    public List<UserRepresentation> getUsers() {
        return users;
    }

    public void setUsers(List<UserRepresentation> users) {
        this.users = users;
    }

    public List<ClientRepresentation> getClients() {
        return clients;
    }

    public List<GroupRepresentation> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupRepresentation> groups) {
        this.groups = groups;
    }

    public void setClients(List<ClientRepresentation> clients) {
        this.clients = clients;
    }

    public List<IdentityProviderRepresentation> getIdentityProviders() {
        return identityProviders;
    }

    public void setIdentityProviders(List<IdentityProviderRepresentation> identityProviders) {
        this.identityProviders = identityProviders;
    }

    public List<IdentityProviderMapperRepresentation> getIdentityProviderMappers() {
        return identityProviderMappers;
    }

    public void setIdentityProviderMappers(List<IdentityProviderMapperRepresentation> identityProviderMappers) {
        this.identityProviderMappers = identityProviderMappers;
    }

    public RolesRepresentation getRoles() {
        return roles;
    }

    public void setRoles(RolesRepresentation roles) {
        this.roles = roles;
    }
}
