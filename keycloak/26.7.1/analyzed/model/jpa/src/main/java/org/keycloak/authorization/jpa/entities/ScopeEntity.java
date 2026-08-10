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
 * 授权 scope JPA 实体，映射 RESOURCE_SERVER_SCOPE 表。
 * <p>表示资源服务器下可授予的操作范围（如 view、edit）。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@Entity
@Table(name = "RESOURCE_SERVER_SCOPE", uniqueConstraints = {
        // 同一资源服务器内 scope 名唯一
        @UniqueConstraint(columnNames = {"NAME", "RESOURCE_SERVER_ID"})
})
@NamedQueries(
        {
                @NamedQuery(name="findScopeIdByName", query="select s.id from ScopeEntity s where s.resourceServer.id = :serverId and s.name = :name"),
                @NamedQuery(name="findScopeIdByResourceServer", query="select s.id from ScopeEntity s where s.resourceServer.id = :serverId"),
                @NamedQuery(name="deleteScopeByResourceServer", query="delete from ScopeEntity s where s.resourceServer.id = :serverId")
        }
)
public class ScopeEntity {

    /** scope UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY)
    private String id;

    /** scope 名称。 */
    @Column(name = "NAME")
    private String name;

    /** 展示名称。 */
    @Column(name = "DISPLAY_NAME")
    private String displayName;

    /** 图标 URI。 */
    @Column(name = "ICON_URI")
    private String iconUri;

    /** 所属资源服务器。 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "RESOURCE_SERVER_ID")
    private ResourceServerEntity resourceServer;

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

    /** 空或纯空白展示名规范化为 null。 */
    public void setDisplayName(String displayName) {
        if (displayName != null && !"".equals(displayName.trim())) {
            this.displayName = displayName;
        } else {
            this.displayName = null;
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconUri() {
        return iconUri;
    }

    public void setIconUri(String iconUri) {
        this.iconUri = iconUri;
    }

    public ResourceServerEntity getResourceServer() {
        return resourceServer;
    }

    public void setResourceServer(final ResourceServerEntity resourceServer) {
        this.resourceServer = resourceServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScopeEntity that = (ScopeEntity) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
