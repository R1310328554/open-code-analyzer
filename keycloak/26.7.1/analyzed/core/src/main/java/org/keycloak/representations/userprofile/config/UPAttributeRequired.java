/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.representations.userprofile.config;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 属性必填规则配置，按角色或 OAuth 范围限定在何种上下文中该属性为必填。
 *
 * @author Vlastimil Elias <velias@redhat.com>
 *
 */
public class UPAttributeRequired implements Cloneable {

    /** 触发必填的角色集合；为空表示不限定角色。 */
    private Set<String> roles;
    /** 触发必填的 OAuth 范围集合；为空表示不限定范围。 */
    private Set<String> scopes;

    /** 默认构造函数，供 JSON 反序列化与反射使用。 */
    public UPAttributeRequired() {
        // 供反射使用
    }

    /**
     * 以角色与范围集合构造必填规则。
     *
     * @param roles  角色集合
     * @param scopes 范围集合
     */
    public UPAttributeRequired(Set<String> roles, Set<String> scopes) {
        this.roles = roles;
        this.scopes = scopes;
    }

    /**
     * 判断该配置是否表示属性始终必填（角色与范围均未限定）。
     *
     * @return 若属性始终必填则返回 {@code true}
     */
    @JsonIgnore
    public boolean isAlways() {
        return (roles == null || roles.isEmpty()) && (scopes == null || scopes.isEmpty());
    }

    /** @return 触发必填的角色集合 */
    public Set<String> getRoles() {
        return roles;
    }

    /** @param roles 触发必填的角色集合 */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    /** @return 触发必填的范围集合 */
    public Set<String> getScopes() {
        return scopes;
    }

    /** @param scopes 触发必填的范围集合 */
    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }


    @Override
    public String toString() {
        return "UPAttributeRequired [isAlways=" + isAlways() + ", roles=" + roles + ", scopes=" + scopes + "]";
    }

    /**
     * 深拷贝当前必填规则（集合内容独立复制）。
     *
     * @return 独立的必填规则副本
     */
    @Override
    protected UPAttributeRequired clone() {
        Set<String> scopes = this.scopes == null ? null : new HashSet<>(this.scopes);
        Set<String> roles = this.roles == null ? null : new HashSet<>(this.roles);
        return new UPAttributeRequired(roles, scopes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roles, scopes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final UPAttributeRequired other = (UPAttributeRequired) obj;
        return Objects.equals(this.roles, other.roles)
                && Objects.equals(this.scopes, other.scopes);
    }
}
