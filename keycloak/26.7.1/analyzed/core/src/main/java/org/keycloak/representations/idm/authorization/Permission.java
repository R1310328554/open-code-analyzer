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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 授权权限条目，描述对某资源（及可选作用域、声明）的访问许可。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Permission {

    /** 资源标识符（JSON 字段 {@code rsid}）。 */
    @JsonProperty("rsid")
    private String resourceId;

    /** 资源名称（JSON 字段 {@code rsname}）。 */
    @JsonProperty("rsname")
    private String resourceName;

    /** 已授权的作用域集合。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<String> scopes;

    /** 与权限关联的声明（键 → 值集合）。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Set<String>> claims;

    /** 创建空权限条目。 */
    public Permission() {
        this(null, null, null, null);
    }

    /**
     * @param resourceId 资源 ID
     * @param scopes 作用域集合
     */
    public Permission(final String resourceId, final Set<String> scopes) {
        this(resourceId, null, scopes, null);
    }

    /**
     * @param resourceId 资源 ID
     * @param resourceName 资源名称
     * @param scopes 作用域集合
     * @param claims 声明映射
     */
    public Permission(final String resourceId, String resourceName, final Set<String> scopes, Map<String, Set<String>> claims) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.scopes = scopes;
        this.claims = claims;
    }

    /** @param resourceId 资源 ID */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /** @return 资源 ID，空白字符串视为 {@code null} */
    public String getResourceId() {
        if (resourceId == null || "".equals(resourceId.trim())) {
            return null;
        }
        return this.resourceId;
    }

    /** @param resourceName 资源名称 */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /** @return 资源名称 */
    public String getResourceName() {
        return this.resourceName;
    }

    /** @return 作用域集合，懒初始化空集合 */
    public Set<String> getScopes() {
        if (this.scopes == null) {
            this.scopes = new HashSet<>();
        }

        return this.scopes;
    }

    /** @return 声明映射 */
    public Map<String, Set<String>> getClaims() {
        return claims;
    }

    /** 基于资源 ID/名称及作用域交集判断权限是否匹配。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !getClass().isAssignableFrom(o.getClass())) return false;

        Permission that = (Permission) o;

        if (getResourceId() != null || getResourceName() != null) {
            if (!getResourceId().equals(that.resourceId)) {
                return false;
            }

            if (getScopes().isEmpty() && that.getScopes().isEmpty()) {
                return true;
            }
        } else if (that.resourceId != null) {
            return false;
        }

        for (String scope : that.getScopes()) {
            if (getScopes().contains(scope)) {
                return true;
            }
        }

        return false;
    }

    /** @return 基于资源 ID 的哈希值 */
    @Override
    public int hashCode() {
        return Objects.hash(resourceId);
    }

    /** @return 权限条目的可读字符串 */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Permission {").append("id=").append(resourceId).append(", name=").append(resourceName)
                .append(", scopes=").append(scopes).append("}");

        return builder.toString();
    }

    /** @param scopes 作用域集合 */
    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }
}
