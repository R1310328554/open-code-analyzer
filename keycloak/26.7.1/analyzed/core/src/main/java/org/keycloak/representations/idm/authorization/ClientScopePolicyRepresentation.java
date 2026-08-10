/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 * 客户端 Scope（client-scope）类型授权策略的 REST 表示，按令牌中的客户端 Scope 匹配请求。
 *
 * @author <a href="mailto:yoshiyuki.tabata.jy@hitachi.com">Yoshiyuki Tabata</a>
 */
public class ClientScopePolicyRepresentation extends AbstractPolicyRepresentation {

    /** 匹配的客户端 Scope 定义集合。 */
    private Set<ClientScopeDefinition> clientScopes;

    /** @return 固定策略类型 {@code client-scope} */
    @Override
    public String getType() {
        return "client-scope";
    }

    /** @return 客户端 Scope 定义集合 */
    public Set<ClientScopeDefinition> getClientScopes() {
        return clientScopes;
    }

    /** @param clientScopes 客户端 Scope 定义集合 */
    public void setClientScopes(Set<ClientScopeDefinition> clientScopes) {
        this.clientScopes = clientScopes;
    }

    /** 添加一个客户端 Scope（可指定是否必需）。 */
    public void addClientScope(String name, boolean required) {
        if (clientScopes == null) {
            clientScopes = new HashSet<>();
        }
        clientScopes.add(new ClientScopeDefinition(name, required));
    }

    /** 添加一个非必需的客户端 Scope。 */
    public void addClientScope(String name) {
        addClientScope(name, false);
    }

    /** 单个客户端 Scope 的定义。 */
    public static class ClientScopeDefinition {
        /** Scope ID 或名称。 */
        private String id;
        /** 是否为必需 Scope。 */
        private boolean required;

        public ClientScopeDefinition() {
            this(null, false);
        }

        public ClientScopeDefinition(String id, boolean required) {
            this.id = id;
            this.required = required;
        }

        /** @return Scope ID */
        public String getId() {
            return id;
        }

        /** @param id Scope ID */
        public void setId(String id) {
            this.id = id;
        }

        /** @return 是否必需 */
        public boolean isRequired() {
            return required;
        }

        /** @param required 是否必需 */
        public void setRequired(boolean required) {
            this.required = required;
        }

    }
}
