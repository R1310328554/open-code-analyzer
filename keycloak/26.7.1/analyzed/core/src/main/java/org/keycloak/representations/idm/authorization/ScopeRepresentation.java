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
import java.util.List;
import java.util.Objects;

/**
 * <p>资源集上可执行的有界访问范围。在授权策略术语中，作用域是对资源集（对象）可应用的众多"动词"之一。
 *
 * <p>详见 <a href="https://docs.kantarainitiative.org/uma/draft-oauth-resource-reg.html#rfc.section.2.1">OAuth-resource-reg</a>。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ScopeRepresentation {

    /** 作用域 ID。 */
    private String id;
    /** 作用域名称。 */
    private String name;
    /** 作用域图标 URI。 */
    private String iconUri;
    /** 关联的策略列表。 */
    private List<PolicyRepresentation> policies;
    /** 关联的资源列表。 */
    private List<ResourceRepresentation> resources;
    /** 作用域显示名称。 */
    private String displayName;

    /**
     * 创建实例。
     *
     * @param name 描述访问范围的可读字符串
     * @param iconUri 表示该作用域的图标 {@link URI}
     */
    public ScopeRepresentation(String name, String iconUri) {
        this.name = name;
        this.iconUri = iconUri;
    }

    /**
     * 创建实例。
     *
     * @param name 描述访问范围的可读字符串
     */
    public ScopeRepresentation(String name) {
        this(name, null);
    }

    /** 创建空实例。 */
    public ScopeRepresentation() {
        this(null, null);
    }

    /** @return 作用域名称 */
    public String getName() {
        return this.name;
    }

    /** @return 显示名称 */
    public String getDisplayName() {
        return displayName;
    }

    /** @return 图标 URI */
    public String getIconUri() {
        return this.iconUri;
    }

    /** @return 作用域 ID */
    public String getId() {
        return this.id;
    }

    /** @param id 作用域 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @param name 作用域名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @param displayName 显示名称 */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** @param iconUri 图标 URI */
    public void setIconUri(String iconUri) {
        this.iconUri = iconUri;
    }

    /** @return 关联策略列表 */
    public List<PolicyRepresentation> getPolicies() {
        return this.policies;
    }

    /** @param policies 关联策略列表 */
    public void setPolicies(List<PolicyRepresentation> policies) {
        this.policies = policies;
    }

    /** @return 关联资源列表 */
    public List<ResourceRepresentation> getResources() {
        return this.resources;
    }

    /** @param resources 关联资源列表 */
    public void setResources(List<ResourceRepresentation> resources) {
        this.resources = resources;
    }

    /** 按名称比较作用域是否相等。 */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScopeRepresentation scope = (ScopeRepresentation) o;
        return Objects.equals(getName(), scope.getName());
    }

    public int hashCode() {
        return Objects.hash(getName());
    }
}
