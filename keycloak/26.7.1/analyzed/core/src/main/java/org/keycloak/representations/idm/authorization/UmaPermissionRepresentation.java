/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.representations.idm.authorization;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * UMA 权限策略的 REST 表示，定义角色、组、客户端、用户及附加条件。
 *
 * @author <a href="mailto:federico@martel-innovate.com">Federico M. Facca</a>
 */
public class UmaPermissionRepresentation extends AbstractPolicyRepresentation {
    
    /** 匹配的角色集合。 */
    private Set<String> roles;
    /** 匹配的组集合。 */
    private Set<String> groups;
    /** 匹配的客户端集合。 */
    private Set<String> clients;
    /** 匹配的用户集合。 */
    private Set<String> users;
    /** 附加条件表达式。 */
    private String condition;

    /** @return 固定策略类型 {@code uma} */
    @Override
    public String getType() {
        return "uma";
    }

    /** @param roles 角色集合 */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /** 添加一个或多个角色。 */
    public void addRole(String... role) {
        if (roles == null) {
            roles = new HashSet<>();
        }

        roles.addAll(Arrays.asList(role));
    }

    /** 添加客户端角色。 */
    public void addClientRole(String clientId, String roleName) {
        addRole(clientId + "/" + roleName);
    }

    /** 移除指定角色。 */
    public void removeRole(String role) {
        if (roles != null) {
            roles.remove(role);
        }
    }

    /** @return 角色集合 */
    public Set<String> getRoles() {
        return roles;
    }

    /** @param groups 组集合 */
    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    /** 添加一个或多个组。 */
    public void addGroup(String... group) {
        if (groups == null) {
            groups = new HashSet<>();
        }

        groups.addAll(Arrays.asList(group));
    }

    /** 移除指定组。 */
    public void removeGroup(String group) {
        if (groups != null) {
            groups.remove(group);
        }
    }

    /** @return 组集合 */
    public Set<String> getGroups() {
        return groups;
    }

    /** @param clients 客户端集合 */
    public void setClients(Set<String> clients) {
        this.clients = clients;
    }

    /** 添加一个或多个客户端。 */
    public void addClient(String... client) {
        if (clients == null) {
            clients = new HashSet<>();
        }

        clients.addAll(Arrays.asList(client));
    }

    /** 移除指定客户端。 */
    public void removeClient(String client) {
        if (clients != null) {
            clients.remove(client);
        }
    }

    /** @return 客户端集合 */
    public Set<String> getClients() {
        return clients;
    }

    /** @param users 用户集合 */
    public void setUsers(Set<String> users) {
        this.users = users;
    }

    /** 添加一个或多个用户。 */
    public void addUser(String... user) {
        if (this.users == null) {
            this.users = new HashSet<>();
        }
        this.users.addAll(Arrays.asList(user));
    }

    /** 移除指定用户。 */
    public void removeUser(String user) {
        if (this.users != null) {
            this.users.remove(user);
        }
    }

    /** @return 用户集合 */
    public Set<String> getUsers() {
        return this.users;
    }

    /** @param condition 附加条件表达式 */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /** @return 附加条件表达式 */
    public String getCondition() {
        return condition;
    }
}
