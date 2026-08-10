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

package org.keycloak.representations.idm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keycloak 组织（Organization）的 Admin REST API 表示，涵盖域名、成员、IdP 与组关联。
 */
public class OrganizationRepresentation {

    /** 组织内部 UUID。 */
    private String id;
    /** 组织显示名称。 */
    private String name;
    /** 组织别名（URL 友好标识）。 */
    private String alias;
    /** 组织是否启用。 */
    private boolean enabled = true;
    /** 组织描述。 */
    private String description;
    /** 组织 SSO 重定向 URL。 */
    private String redirectUrl;
    /** 自定义属性（键 → 多值列表）。 */
    private Map<String, List<String>> attributes;
    /** 组织关联的互联网域名集合。 */
    private Set<OrganizationDomainRepresentation> domains;
    /** 组织成员列表。 */
    private List<MemberRepresentation> members;
    /** 组织绑定的身份提供者列表。 */
    private List<IdentityProviderRepresentation> identityProviders;
    /** 组织关联的组列表。 */
    private List<GroupRepresentation> groups;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String>>  attributes) {
        this.attributes = attributes;
    }

    /** 便捷方法：设置单个属性值并返回自身以支持链式调用。 */
    public OrganizationRepresentation singleAttribute(String name, String value) {
        if (this.attributes == null) attributes = new HashMap<>();
        attributes.put(name, Collections.singletonList(value));
        return this;
    }

    public Set<OrganizationDomainRepresentation> getDomains() {
        return domains;
    }

    /** 按域名查找已配置的域表示。 */
    public OrganizationDomainRepresentation getDomain(String name) {
        if (domains == null) {
            return null;
        }
        return domains.stream()
                .filter(organizationDomainRepresentation -> name.equals(organizationDomainRepresentation.getName()))
                .findAny()
                .orElse(null);
    }

    /** 添加组织域名。 */
    public void addDomain(OrganizationDomainRepresentation domain) {
        if (domains == null) {
            domains = new HashSet<>();
        }
        domains.add(domain);
    }

    /** 移除组织域名。 */
    public void removeDomain(OrganizationDomainRepresentation domain) {
        if (domains == null) {
            return;
        }
        getDomains().remove(domain);
    }

    public List<MemberRepresentation> getMembers() {
        return members;
    }

    public void setMembers(List<MemberRepresentation> members) {
        this.members = members;
    }

    /** 添加组织成员。 */
    public void addMember(MemberRepresentation member) {
        if (members == null) {
            members = new ArrayList<>();
        }
        members.add(member);
    }

    public List<IdentityProviderRepresentation> getIdentityProviders() {
        return identityProviders;
    }

    public void setIdentityProviders(List<IdentityProviderRepresentation> identityProviders) {
        this.identityProviders = identityProviders;
    }

    /** 添加身份提供者绑定。 */
    public void addIdentityProvider(IdentityProviderRepresentation idp) {
        if (identityProviders == null) {
            identityProviders = new ArrayList<>();
        }
        identityProviders.add(idp);
    }

    public List<GroupRepresentation> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupRepresentation> groups) {
        this.groups = groups;
    }

    /** 添加关联组。 */
    public void addGroup(GroupRepresentation group) {
        if (groups == null) {
            groups = new ArrayList<>();
        }
        groups.add(group);
    }

    /** 基于组织 ID 比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof OrganizationRepresentation)) return false;

        OrganizationRepresentation that = (OrganizationRepresentation) o;

        return id != null && id.equals(that.getId());
    }

    /** 基于组织 ID 计算哈希。 */
    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        return id.hashCode();
    }
}
