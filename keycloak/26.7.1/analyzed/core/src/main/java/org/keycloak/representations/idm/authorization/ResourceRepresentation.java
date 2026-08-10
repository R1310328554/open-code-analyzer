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

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.keycloak.json.StringListMapDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * <p>资源服务器所管理的一个或多个受保护资源的 REST 表示。
 *
 * <p>详见 <a href="https://docs.kantarainitiative.org/uma/draft-oauth-resource-reg.html#rfc.section.2.2">OAuth-resource-reg</a>。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ResourceRepresentation {

    /** 创建 {@link Builder} 实例。 */
    public static Builder create() {
        return new Builder();
    }

    /** 资源唯一标识。 */
    @JsonProperty("_id")
    private String id;

    /** 资源名称。 */
    private String name;

    /** 资源 URI 集合。 */
    @JsonProperty("uris")
    private Set<String> uris;
    /** 资源类型标识。 */
    private String type;
    /** 该资源可用的作用域集合。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("scopes")
    private Set<ScopeRepresentation> scopes;

    /** 资源图标 URI。 */
    @JsonProperty("icon_uri")
    private String iconUri;
    /** 资源所有者。 */
    private ResourceOwnerRepresentation owner;
    /** 是否启用所有者托管访问（UMA）。 */
    private Boolean ownerManagedAccess;

    /** 资源显示名称。 */
    private String displayName;

    /** 资源自定义属性（键 → 值列表）。 */
    @JsonDeserialize(using = StringListMapDeserializer.class)
    private Map<String, List<String>> attributes;

    /**
     * 创建新实例。
     *
     * @param name 描述资源集的可读名称
     * @param uris 提供资源集网络位置的 {@link URI} {@link List}
     * @param type 唯一标识资源集语义的字符串
     * @param scopes 该资源集可用的作用域
     * @param iconUri 表示资源集的图标 {@link URI}
     */
    public ResourceRepresentation(String name, Set<ScopeRepresentation> scopes, Set<String> uris, String type, String iconUri) {
        this.name = name;
        this.scopes = scopes;
        this.uris = uris;
        this.type = type;
        this.iconUri = iconUri;
    }

    /** 使用单个 URI 创建实例。 */
    public ResourceRepresentation(String name, Set<ScopeRepresentation> scopes, String uri, String type, String iconUri) {
        this(name, scopes, Collections.singleton(uri), type, iconUri);
    }

    /**
     * 创建新实例（无图标 URI）。
     *
     * @param name 描述资源集的可读名称
     * @param uris 提供资源集网络位置的 {@link URI} {@link List}
     * @param type 唯一标识资源集语义的字符串
     * @param scopes 该资源集可用的作用域
     */
    public ResourceRepresentation(String name, Set<ScopeRepresentation> scopes, Set<String> uris, String type) {
        this(name, scopes, uris, type, null);
    }

    /** 使用单个 URI 创建实例（无图标 URI）。 */
    public ResourceRepresentation(String name, Set<ScopeRepresentation> scopes, String uri, String type) {
        this(name, scopes, Collections.singleton(uri), type, null);
    }

    /**
     * 创建新实例（仅含名称与作用域）。
     *
     * @param name 描述资源集的可读名称
     * @param serverUri 标识该资源服务器的 {@link URI}
     * @param scopes 该资源集可用的作用域
     */
    public ResourceRepresentation(String name, Set<ScopeRepresentation> scopes) {
        this(name, scopes, (Set<String>) null, null, null);
    }

    /** 按名称与作用域名称列表创建实例。 */
    public ResourceRepresentation(String name, String... scopes) {
        this.name = name;
        this.scopes = new HashSet<>();
        for (String s : scopes) {
            ScopeRepresentation rep = new ScopeRepresentation(s);
            this.scopes.add(rep);
        }
    }

    /** 创建空实例。 */
    public ResourceRepresentation() {
        this(null, null, (Set<String>) null, null, null);
    }

    /** @param id 资源 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 资源 ID */
    public String getId() {
        return this.id;
    }

    /** @return 资源名称 */
    public String getName() {
        return this.name;
    }

    /** @return 资源显示名称 */
    public String getDisplayName() {
        return displayName;
    }

    /** @deprecated 请使用 {@link #getUris()} */
    @Deprecated
    @JsonIgnore
    public String getUri() {
        if (this.uris == null || this.uris.isEmpty()) {
            return null;
        }

        return this.uris.iterator().next();
    }

    /** @return 资源 URI 集合 */
    public Set<String> getUris() {
        return this.uris;
    }

    /** @return 资源类型 */
    public String getType() {
        return this.type;
    }

    /** @return 作用域集合（不可变视图） */
    public Set<ScopeRepresentation> getScopes() {
        if (this.scopes == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(this.scopes);
    }

    /** @return 图标 URI */
    public String getIconUri() {
        return this.iconUri;
    }

    /** @param name 资源名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @param displayName 显示名称 */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** @deprecated 请使用 {@link #setUris(Set)} */
    @Deprecated
    @JsonSetter("uri")
    public void setUri(String uri) {
        if (uri != null && !"".equalsIgnoreCase(uri.trim())) {
            this.uris = Collections.singleton(uri);
        }
    }

    /** @param uris 资源 URI 集合 */
    public void setUris(Set<String> uris) {
        if (uris != null) {
            Set<String> resultSet = new HashSet<>();
            for (String uri : uris) {
                if (uri != null && !"".equalsIgnoreCase(uri.trim())) {
                    resultSet.add(uri);
                }
            }

            this.uris = resultSet;
        }
    }

    /** @param type 资源类型 */
    public void setType(String type) {
        if (type != null && !"".equalsIgnoreCase(type.trim())) {
            this.type = type;
        }
    }

    /** @param scopes 作用域集合 */
    public void setScopes(Set<ScopeRepresentation> scopes) {
        this.scopes = scopes;
    }

    /**
     * UMA 资源表示反序列化的临时方案；Jackson 2.19+ 支持别名后可替换。
     *
     * @param scopes 作用域集合
     */
    @JsonSetter("resource_scopes")
    private void setScopesUma(Set<ScopeRepresentation> scopes) {
        this.scopes = scopes;
    }

    /** @param iconUri 图标 URI */
    public void setIconUri(String iconUri) {
        this.iconUri = iconUri;
    }

    /** @return 资源所有者 */
    public ResourceOwnerRepresentation getOwner() {
        return this.owner;
    }

    /** @param owner 资源所有者 */
    @JsonProperty
    public void setOwner(ResourceOwnerRepresentation owner) {
        this.owner = owner;
    }

    /** 按所有者 ID 设置资源所有者。 */
    @JsonIgnore
    public void setOwner(String ownerId) {
        if (ownerId == null) {
            owner = null;
            return;
        }

        if (owner == null) {
            owner = new ResourceOwnerRepresentation();
        }

        owner.setId(ownerId);
    }

    /** @return 是否启用所有者托管访问 */
    public Boolean getOwnerManagedAccess() {
        return ownerManagedAccess;
    }

    /** @param ownerManagedAccess 是否启用所有者托管访问 */
    public void setOwnerManagedAccess(Boolean ownerManagedAccess) {
        this.ownerManagedAccess = ownerManagedAccess;
    }

    /** 按名称添加一个或多个作用域。 */
    public void addScope(String... scopeNames) {
        if (scopes == null) {
            scopes = new HashSet<>();
        }
        for (String scopeName : scopeNames) {
            scopes.add(new ScopeRepresentation(scopeName));
        }
    }

    /** 添加作用域表示。 */
    public void addScope(ScopeRepresentation scope) {
        if (scopes == null) {
            scopes = new HashSet<>();
        }
        scopes.add(scope);
    }

    /** @return 资源属性映射 */
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    /** @param attributes 资源属性映射 */
    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    /** 按名称比较资源是否相等。 */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceRepresentation scope = (ResourceRepresentation) o;
        return Objects.equals(getName(), scope.getName());
    }

    public int hashCode() {
        return Objects.hash(getName());
    }

    /** 流式构建 {@link ResourceRepresentation} 的辅助类。 */
    public static final class Builder {

        private final ResourceRepresentation rep;

        private Builder() {
            rep = new ResourceRepresentation();
        }

        /** @param name 资源名称 */
        public Builder name(String name) {
            rep.setName(name);
            return this;
        }

        /** @return 构建完成的资源表示 */
        public ResourceRepresentation build() {
            return rep;
        }
    }
}
