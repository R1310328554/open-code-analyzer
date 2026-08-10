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

package org.keycloak.models.jpa.entities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.keycloak.models.GroupModel;
import org.keycloak.utils.StringUtil;

/**
 * 组织 JPA 实体，映射 {@code ORG} 表。
 * <p>Realm 内的 B2B 组织单元：含域名、成员组、邀请等；通过 {@link #groupId} 关联内部成员组。</p>
 */
@Table(name="ORG")
@Entity
@NamedQueries({
        @NamedQuery(name="getByOrgName", query="select distinct o from OrganizationEntity o where o.realmId = :realmId AND o.name = :name"),
        @NamedQuery(name="getByDomainName", query="select distinct o from OrganizationEntity o inner join OrganizationDomainEntity d ON o.id = d.organization.id" +
                " where o.realmId = :realmId and d.name in (:names)"),
        @NamedQuery(name="getCount", query="select count(o) from OrganizationEntity o where o.realmId = :realmId"),
        @NamedQuery(name="deleteOrganizationsByRealm", query="delete from OrganizationEntity o where o.realmId = :realmId"),
        @NamedQuery(name="existsByRealm", query="select o.id from OrganizationEntity o where o.realmId = :realmId"),
})
public class OrganizationEntity {

    /** 组织 UUID。 */
    @Id
    @Column(name = "ID", length = 36)
    @Access(AccessType.PROPERTY)
    private String id;

    /** 组织名称（realm 内唯一）。 */
    @Column(name = "NAME")
    private String name;

    /** URL 友好别名。 */
    @Column(name = "ALIAS")
    private String alias;

    /** 是否启用。 */
    @Column(name = "ENABLED")
    private boolean enabled;

    /** 组织描述。 */
    @Column(name = "DESCRIPTION")
    private String description;

    /** 组织门户重定向 URL。 */
    @Column(name = "REDIRECT_URL")
    private String redirectUrl;

    /** 所属 Realm ID。 */
    @Column(name = "REALM_ID")
    private String realmId;

    /**
     * 指向用于组织成员管理的内部 {@link GroupEntity}。
     */
    @Column(name = "GROUP_ID")
    private String groupId;

    /** 关联的验证域名集合；级联全量持久化。 */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy="organization")
    protected Set<OrganizationDomainEntity> domains = new HashSet<>();

    /** 组织类型用户组；删除组织时级联移除。 */
    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "organization", fetch = FetchType.LAZY)
    protected Set<GroupEntity> groups = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
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

    /** 空字符串归一化为 null，避免存储无意义重定向。 */
    public void setRedirectUrl(String redirectUrl) {
        if (StringUtil.isNullOrEmpty(redirectUrl)) {
            redirectUrl = null;
        }
        this.redirectUrl = redirectUrl;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realm) {
        this.realmId = realm;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public Collection<OrganizationDomainEntity> getDomains() {
        if (this.domains == null) {
            this.domains = new HashSet<>();
        }
        return this.domains;
    }

    public void addDomain(OrganizationDomainEntity domainEntity) {
        this.domains.add(domainEntity);
    }

    public void removeDomain(OrganizationDomainEntity domainEntity) {
        this.domains.remove(domainEntity);
    }

    public Set<GroupEntity> getGroups() {
        if (groups == null) groups = new HashSet<>();
        return groups;
    }

    public void setGroups(Set<GroupEntity> groups) {
        this.groups = groups;
    }

    /** 添加组织类型组并建立双向关联。 */
    public void addGroup(GroupEntity group) {
        getGroups().add(group);
        group.setOrganization(this);
        group.setType(GroupModel.Type.ORGANIZATION.intValue());
    }

    public void removeGroup(GroupEntity group) {
        getGroups().remove(group);
        group.setOrganization(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof OrganizationEntity)) return false;

        OrganizationEntity that = (OrganizationEntity) o;

        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        return id.hashCode();
    }
}
