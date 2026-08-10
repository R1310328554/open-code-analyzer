/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations.account;

import java.util.Set;

/**
 * 账户控制台中展示的组织摘要信息，含域名集合；相等性仅基于组织 ID。
 */
public class OrganizationRepresentation {

    /** 组织唯一标识。 */
    private String id;
    /** 组织名称。 */
    private String name;
    /** 组织别名（URL 友好标识）。 */
    private String alias;
    /** 组织是否启用，默认为 true。 */
    private boolean enabled = true;
    /** 组织描述。 */
    private String description;
    /** 组织关联的域名集合。 */
    private Set<String> domains;

    /** @return 组织 ID */
    public String getId() {
        return id;
    }

    /** @param id 组织 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @param name 组织名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 组织名称 */
    public String getName() {
        return name;
    }

    /** @return 组织别名 */
    public String getAlias() {
        return alias;
    }

    /** @param alias 组织别名 */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /** @return 是否启用 */
    public boolean isEnabled() {
        return this.enabled;
    }

    /** @param enabled 是否启用 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** @return 组织描述 */
    public String getDescription() {
        return this.description;
    }

    /** @param description 组织描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 关联域名集合 */
    public Set<String> getDomains() {
        return domains;
    }

    /** @param domains 关联域名集合 */
    public void setDomains(Set<String> domains) {
        this.domains = domains;
    }

    /** 基于 {@link #id} 判断相等；ID 为 null 时不相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof OrganizationRepresentation)) return false;

        OrganizationRepresentation that = (OrganizationRepresentation) o;

        return id != null && id.equals(that.getId());
    }

    /** 基于 {@link #id} 计算哈希；ID 为 null 时委托 {@link Object#hashCode()}。 */
    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        return id.hashCode();
    }
}
