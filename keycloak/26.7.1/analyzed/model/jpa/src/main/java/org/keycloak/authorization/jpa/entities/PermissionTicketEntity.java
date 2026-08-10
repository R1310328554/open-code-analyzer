/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authorization.jpa.entities;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * UMA 权限票据 JPA 实体，映射 RESOURCE_SERVER_PERM_TICKET 表。
 * <p>记录资源所有者与请求者之间的权限申请/授予状态，可关联策略与 scope。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@Entity
@Table(name = "RESOURCE_SERVER_PERM_TICKET", uniqueConstraints = {
        // 同一 owner/requester/资源/scope 组合唯一
        @UniqueConstraint(columnNames = {"OWNER", "REQUESTER", "RESOURCE_SERVER_ID", "RESOURCE_ID", "SCOPE_ID"})
})
@NamedQueries(
    {
        @NamedQuery(name="findPermissionIdByResource", query="select p.id from PermissionTicketEntity p inner join p.resource r where p.resourceServer.id = :serverId and (r.resourceServer = :serverId and r.id = :resourceId)"),
        @NamedQuery(name="findPermissionIdByScope", query="select p.id from PermissionTicketEntity p inner join p.scope s where p.resourceServer.id = :serverId and (s.resourceServer.id = :serverId and s.id = :scopeId)"),
        @NamedQuery(name="findPermissionTicketIdByServerId", query="select p.id from PermissionTicketEntity p where  p.resourceServer.id = :serverId "),
        @NamedQuery(name="findGrantedResources", query="select distinct(p.resource.id) from PermissionTicketEntity p where p.requester = :requester and p.grantedTimestamp is not null order by p.resource.id"),
        @NamedQuery(name="findGrantedResourcesByName", query="select distinct(r.id) from ResourceEntity r inner join PermissionTicketEntity p on r.id = p.resource.id where p.grantedTimestamp is not null and p.requester = :requester and lower(r.name) like :resourceName order by r.id"),
        @NamedQuery(name="findGrantedOwnerResources", query="select distinct(p.resource.id) from PermissionTicketEntity p where p.owner = :owner and p.grantedTimestamp is not null order by p.resource.id")
    }
)
public class PermissionTicketEntity {

    /** 票据 UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name = "ID", length = 36)
    @Access(AccessType.PROPERTY)
    private String id;

    /** 资源所有者用户 ID。 */
    @Column(name = "OWNER")
    private String owner;

    /** 权限请求者用户 ID。 */
    @Column(name = "REQUESTER")
    private String requester;

    /** 票据创建时间（毫秒）。 */
    @Column(name = "CREATED_TIMESTAMP")
    private Long createdTimestamp;

    /** 授予时间（毫秒），null 表示尚未授予。 */
    @Column(name = "GRANTED_TIMESTAMP")
    private Long grantedTimestamp;

    /** 关联的受保护资源。 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "RESOURCE_ID")
    private ResourceEntity resource;

    /** 可选 scope；null 表示资源级权限。 */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "SCOPE_ID")
    private ScopeEntity scope;

    /** 所属资源服务器。 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "RESOURCE_SERVER_ID")
    private ResourceServerEntity resourceServer;

    /** 授予后关联的用户托管策略，可为 null。 */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "POLICY_ID")
    private PolicyEntity policy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ResourceEntity getResource() {
        return resource;
    }

    public void setResource(ResourceEntity resource) {
        this.resource = resource;
    }

    public ScopeEntity getScope() {
        return scope;
    }

    public void setScope(ScopeEntity scope) {
        this.scope = scope;
    }

    public ResourceServerEntity getResourceServer() {
        return resourceServer;
    }

    public void setResourceServer(ResourceServerEntity resourceServer) {
        this.resourceServer = resourceServer;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getRequester() {
        return requester;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Long getGrantedTimestamp() {
        return grantedTimestamp;
    }

    public void setGrantedTimestamp(long grantedTimestamp) {
        this.grantedTimestamp = grantedTimestamp;
    }

    /** 是否已授予（grantedTimestamp 非空）。 */
    public boolean isGranted() {
        return grantedTimestamp != null;
    }

    public PolicyEntity getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyEntity policy) {
        this.policy = policy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionTicketEntity that = (PermissionTicketEntity) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
