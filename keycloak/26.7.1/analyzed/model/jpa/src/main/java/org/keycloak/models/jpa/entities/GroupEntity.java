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

package org.keycloak.models.jpa.entities;

import java.util.Collection;
import java.util.LinkedList;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.Nationalized;

/**
 * 用户组 JPA 实体，映射 {@code KEYCLOAK_GROUP} 表。
 * <p>支持层级结构（{@code parentId}）及组织类型组（{@link org.keycloak.models.GroupModel.Type#ORGANIZATION}）。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name="getGroupIdsByParent", query="select u.id from GroupEntity u where u.realm = :realm and u.type = 0 and u.parentId = :parent order by u.name ASC"),
        @NamedQuery(name="deleteGroupsByRealm", query="delete from GroupEntity g where g.realm = :realm")
})
@Entity
@Table(name="KEYCLOAK_GROUP",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"REALM_ID", "PARENT_GROUP", "NAME"})}
)
public class GroupEntity {

    /**
     * 写入 PARENT 列时表示顶级组的占位 ID。
     */
    public static String TOP_PARENT_ID = " ";

    /** 组 UUID；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;

    /** 组名称（同一 realm + 父组下唯一）。 */
    @Nationalized
    @Column(name = "NAME")
    protected String name;

    /** 组描述。 */
    @Nationalized
    @Column(name = "DESCRIPTION")
    protected String description;

    /** 父组 ID；顶级组为 {@link #TOP_PARENT_ID}。 */
    @Column(name = "PARENT_GROUP")
    private String parentId;

    /** 所属 Realm ID。 */
    @Column(name = "REALM_ID")
    private String realm;

    /** 组类型，对应 {@link org.keycloak.models.GroupModel.Type} 整型值。 */
    @Column(name = "TYPE")
    private int type;

    /** 创建时间戳（毫秒）。 */
    @Column(name = "CREATED_TIMESTAMP")
    private Long createdTimestamp;

    /** 最后修改时间戳（毫秒）。 */
    @Column(name = "LAST_MODIFIED_TIMESTAMP")
    private Long lastModifiedTimestamp;

    /**
     * 当类型为 {@link org.keycloak.models.GroupModel.Type#ORGANIZATION} 时，
     * 指向拥有该组的 {@link OrganizationEntity}。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORG_ID")
    private OrganizationEntity organization;

    /** 组自定义属性；级联删除。 */
    @OneToMany(
            cascade = CascadeType.REMOVE,
            orphanRemoval = true, mappedBy="group")
    protected Collection<GroupAttributeEntity> attributes = new LinkedList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Collection<GroupAttributeEntity> getAttributes() {
        if (attributes == null) {
            attributes = new LinkedList<>();
        }
        return attributes;
    }

    public void setAttributes(Collection<GroupAttributeEntity> attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof GroupEntity)) return false;

        GroupEntity that = (GroupEntity) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
