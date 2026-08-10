/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization.jpa.entities;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * 授权资源 JPA 实体，映射 RESOURCE_SERVER_RESOURCE 表。
 * <p>表示资源服务器下的受保护资源，可绑定 URI、scope 与自定义属性。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@Entity
@Table(name = "RESOURCE_SERVER_RESOURCE", uniqueConstraints = {
        // 同一资源服务器、所有者下资源名唯一
        @UniqueConstraint(columnNames = {"NAME", "RESOURCE_SERVER_ID", "OWNER"})
})
@NamedQueries(
        {
                @NamedQuery(name="findResourceIdByOwner", query="select r from ResourceEntity r  where r.resourceServer = :serverId and r.owner = :owner"),
                @NamedQuery(name="findResourceIdByOwnerOrdered", query="select r from ResourceEntity r where r.resourceServer = :serverId and r.owner = :owner order by r.id"),
                @NamedQuery(name="findAnyResourceIdByOwner", query="select r from ResourceEntity r where r.owner = :owner"),
                @NamedQuery(name="findAnyResourceIdByOwnerOrdered", query="select r.id from ResourceEntity r where r.owner = :owner order by r.id"),
                @NamedQuery(name="findResourceIdByUri", query="select r.id from ResourceEntity r where  r.resourceServer = :serverId  and :uri in elements(r.uris)"),
                @NamedQuery(name="findResourceIdByName", query="select r from ResourceEntity r left join fetch r.scopes s where  r.resourceServer = :serverId  and r.owner = :ownerId and r.name = :name"),
                @NamedQuery(name="findResourceIdByType", query="select r from ResourceEntity r left join fetch r.scopes s where  r.resourceServer = :serverId  and r.owner = :ownerId and r.type = :type"),
                @NamedQuery(name="findResourceIdByTypeNoOwner", query="select r from ResourceEntity r left join fetch r.scopes s where  r.resourceServer = :serverId  and r.type = :type"),
                @NamedQuery(name="findResourceIdByTypeInstance", query="select r from ResourceEntity r left join fetch r.scopes s where  r.resourceServer = :serverId and r.type = :type and r.owner <> :serverId"),
                @NamedQuery(name="findResourceIdByServerId", query="select r.id from ResourceEntity r where  r.resourceServer = :serverId "),
                @NamedQuery(name="findResourceIdByScope", query="select r from ResourceEntity r inner join r.scopes s where r.resourceServer = :serverId and (s.resourceServer.id = :serverId and s.id in (:scopeIds))"),
                @NamedQuery(name="deleteResourceByResourceServer", query="delete from ResourceEntity r where r.resourceServer = :serverId")
        }
)
public class ResourceEntity {

    /** 资源 UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY)
    private String id;

    /** 资源名称。 */
    @Column(name = "NAME")
    private String name;

    /** 展示名称。 */
    @Column(name = "DISPLAY_NAME")
    private String displayName;

    /** 资源 URI 集合（RESOURCE_URIS 表）。 */
    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "VALUE")
    @CollectionTable(name = "RESOURCE_URIS", joinColumns = { @JoinColumn(name="RESOURCE_ID") })
    private Set<String> uris;

    /** 资源类型（如 urn:... 或自定义类型）。 */
    @Column(name = "TYPE")
    private String type;

    /** 图标 URI。 */
    @Column(name = "ICON_URI")
    private String iconUri;

    /** 资源所有者 ID（用户或客户端）。 */
    @Column(name = "OWNER")
    private String owner;

    /** 是否启用 UMA 所有者托管访问（Owner-Managed Access）。 */
    @Column(name = "OWNER_MANAGED_ACCESS")
    private boolean ownerManagedAccess;

    /** 所属资源服务器 ID（字符串外键，非 JPA 关联）。 */
    @Column(name = "RESOURCE_SERVER_ID")
    private String resourceServer;

    /** 资源关联的 scope 列表（RESOURCE_SCOPE 表）。 */
    @OneToMany(fetch = FetchType.LAZY, cascade = {})
    @JoinTable(name = "RESOURCE_SCOPE", joinColumns = @JoinColumn(name = "RESOURCE_ID"), inverseJoinColumns = @JoinColumn(name = "SCOPE_ID"))
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 20)
    private List<ScopeEntity> scopes;

    // 显式禁用 orphanRemoval：属性删除由 HQL 处理，但仍需手动从集合移除以避免 Hibernate 重复 DELETE
    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = false, mappedBy="resource", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 20)
    private Collection<ResourceAttributeEntity> attributes = new LinkedList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<String> getUris() {
        if (uris == null) {
            uris = new HashSet<>();
        }
        return uris;
    }

    public void setUris(Set<String> uris) {
        this.uris = uris;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ScopeEntity> getScopes() {
        if (scopes == null) {
            scopes = new LinkedList<>();
        }
        return this.scopes;
    }

    public String getIconUri() {
        return iconUri;
    }

    public void setIconUri(String iconUri) {
        this.iconUri = iconUri;
    }

    public String getResourceServer() {
        return resourceServer;
    }

    public void setResourceServer(String resourceServer) {
        this.resourceServer = resourceServer;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setOwnerManagedAccess(boolean ownerManagedAccess) {
        this.ownerManagedAccess = ownerManagedAccess;
    }

    public boolean isOwnerManagedAccess() {
        return ownerManagedAccess;
    }

    public Collection<ResourceAttributeEntity> getAttributes() {
        if (attributes == null) {
            attributes = new LinkedList<>();
        }
        return attributes;
    }

    public void setAttributes(Collection<ResourceAttributeEntity> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceEntity that = (ResourceEntity) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
