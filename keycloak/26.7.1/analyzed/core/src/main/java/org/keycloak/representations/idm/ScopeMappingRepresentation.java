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

import java.util.HashSet;
import java.util.Set;

/**
 * 客户端 Scope 与角色映射的 REST 表示，描述某客户端 Scope 关联的角色集合。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ScopeMappingRepresentation {
    /** 自身资源链接。 */
    protected String self; // link
    /** 客户端 ID。 */
    protected String client;

    /** @deprecated 客户端模板（已废弃，由 clientScope 替代）。 */
    @Deprecated // Replaced by clientScope
    protected String clientTemplate;
    /** 客户端 Scope 名称。 */
    protected String clientScope;
    /** 映射的角色名称集合。 */
    protected Set<String> roles;

    /** @return 自身资源链接 */
    public String getSelf() {
        return self;
    }

    /** @param self 自身资源链接 */
    public void setSelf(String self) {
        this.self = self;
    }

    /** @return 客户端 ID */
    public String getClient() {
        return client;
    }

    /** @param client 客户端 ID */
    public void setClient(String client) {
        this.client = client;
    }

    /** @return 客户端模板（已废弃） */
    @Deprecated
    public String getClientTemplate() {
        return clientTemplate;
    }

    /** @return 客户端 Scope 名称 */
    public String getClientScope() {
        return clientScope;
    }

    /** @param clientScope 客户端 Scope 名称 */
    public void setClientScope(String clientScope) {
        this.clientScope = clientScope;
    }

    /** @return 映射的角色名称集合 */
    public Set<String> getRoles() {
        return roles;
    }

    /** @param roles 映射的角色名称集合 */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /**
     * 便捷方法：添加单个角色到映射集合并返回自身以支持链式调用。
     *
     * @param role 角色名称
     * @return 当前 Scope 映射表示
     */
    public ScopeMappingRepresentation role(String role) {
        if (this.roles == null) this.roles = new HashSet<>();
        this.roles.add(role);
        return this;
    }

}
