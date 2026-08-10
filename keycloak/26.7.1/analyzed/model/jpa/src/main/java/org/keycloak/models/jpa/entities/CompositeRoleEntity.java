/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * 复合角色关联 JPA 实体，映射 {@code COMPOSITE_ROLE} 表。
 * <p>表示父角色包含子角色的组合关系；曾用 {@code @ManyToMany} 或原生 SQL，现改为独立关联表，
 * 删除角色时可直接 JPA DML，避免遍历大量父级条目。</p>
 */
@Entity
@Table(name="COMPOSITE_ROLE")
@NamedQueries({
        // 删除角色时一并移除其作为父或子角色的所有复合关系
        @NamedQuery(name="deleteRoleFromComposites", query="delete CompositeRoleEntity c where c.parentRole = :role or c.childRole = :role"),
        @NamedQuery(name="deleteSingleCompositeFromRole", query="delete CompositeRoleEntity c where c.parentRole = :parentRole and c.childRole = :childRole"),
})
@IdClass(CompositeRoleEntity.Key.class)
public class CompositeRoleEntity {
    /** 复合角色中的父角色（包含方）。 */
    @Id
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="COMPOSITE")
    private RoleEntity parentRole;

    /** 被包含的子角色。 */
    @Id
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CHILD_ROLE")
    private RoleEntity childRole;

    public CompositeRoleEntity() {
    }

    public CompositeRoleEntity(RoleEntity parentRole, RoleEntity childRole) {
        // 字段不可为 null，否则 Hibernate 自动依赖检测失效
        this.parentRole = parentRole;
        this.childRole = childRole;
    }

    public RoleEntity getParentRole() {
        return parentRole;
    }

    public void setParentRole(RoleEntity parentRole) {
        this.parentRole = parentRole;
    }

    public RoleEntity getChildRole() {
        return childRole;
    }

    public void setChildRole(RoleEntity childRole) {
        this.childRole = childRole;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof CompositeRoleEntity that)) return false;

        return parentRole.equals(that.parentRole) && childRole.equals(that.childRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childRole, parentRole);
    }

    /** 复合主键：(parentRole, childRole)。 */
    public static class Key implements Serializable {
        private RoleEntity childRole;
        private RoleEntity parentRole;

        public Key() {
        }

        public Key(RoleEntity parentRole, RoleEntity childRole) {
            this.childRole = childRole;
            this.parentRole = parentRole;
        }

        public RoleEntity getChildRole() {
            return childRole;
        }

        public void setChildRole(RoleEntity childRole) {
            this.childRole = childRole;
        }

        public RoleEntity getParentRole() {
            return parentRole;
        }

        public void setParentRole(RoleEntity parentRole) {
            this.parentRole = parentRole;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key key)) return false;
            return Objects.equals(childRole, key.childRole) && Objects.equals(parentRole, key.parentRole);
        }

        @Override
        public int hashCode() {
            return Objects.hash(childRole, parentRole);
        }
    }
}
