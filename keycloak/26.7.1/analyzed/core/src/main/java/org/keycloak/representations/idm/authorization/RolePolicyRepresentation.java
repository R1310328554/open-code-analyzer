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
package org.keycloak.representations.idm.authorization;

import java.util.HashSet;
import java.util.Set;

/**
 * 角色（role）类型授权策略的 REST 表示，按用户持有的角色匹配请求。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class RolePolicyRepresentation extends AbstractPolicyRepresentation {

    /** 匹配的角色定义集合。 */
    private Set<RoleDefinition> roles;
    /** 是否从令牌中获取角色（而非仅依赖策略定义）。 */
    private Boolean fetchRoles;

    /** @return 固定策略类型 {@code role} */
    @Override
    public String getType() {
        return "role";
    }

    /** @return 角色定义集合 */
    public Set<RoleDefinition> getRoles() {
        return roles;
    }

    /** @param roles 角色定义集合 */
    public void setRoles(Set<RoleDefinition> roles) {
        this.roles = roles;
    }

    /** 按角色名称添加角色，可指定是否为必需角色。 */
    public void addRole(String name, Boolean required) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(new RoleDefinition(name, required));
    }

    /** 按角色名称添加角色（默认非必需）。 */
    public void addRole(String name) {
        addRole(name, false);
    }

    /** 添加客户端角色（默认非必需）。 */
    public void addClientRole(String clientId, String name) {
        addRole(clientId + "/" +name, false);
    }

    /** 添加客户端角色，可指定是否为必需角色。 */
    public void addClientRole(String clientId, String name, boolean required) {
        addRole(clientId + "/" + name, required);
    }

    /** @return 是否从令牌获取角色 */
    public Boolean isFetchRoles() {
        return fetchRoles;
    }

    /** @param fetchRoles 是否从令牌获取角色 */
    public void setFetchRoles(Boolean fetchRoles) {
        this.fetchRoles = fetchRoles;
    }

    /** 角色匹配定义，标识角色 ID 及是否为必需角色。 */
    public static class RoleDefinition implements Comparable<RoleDefinition> {

        /** 角色 ID 或名称。 */
        private String id;
        /** 是否为必需角色（AND 语义）。 */
        private Boolean required;

        public RoleDefinition() {
            this(null, false);
        }

        public RoleDefinition(String id, Boolean required) {
            this.id = id;
            this.required = required;
        }

        /** @return 角色 ID */
        public String getId() {
            return id;
        }

        /** @param id 角色 ID */
        public void setId(String id) {
            this.id = id;
        }

        /** @return 是否为必需角色 */
        public Boolean isRequired() {
            return required;
        }

        /** @param required 是否为必需角色 */
        public void setRequired(Boolean required) {
            this.required = required;
        }

        @Override
        public int compareTo(RoleDefinition o) {
            if (id == null || o.id == null) {
                return 1;
            }
            return id.compareTo(o.id);
        }
    }
}
