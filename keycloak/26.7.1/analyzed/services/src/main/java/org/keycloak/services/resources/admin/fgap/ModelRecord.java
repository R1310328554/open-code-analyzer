/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.services.resources.admin.fgap;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

/**
 * 细粒度管理权限（FGAP）中领域模型的密封记录接口。
 * <p>将 {@link ClientModel}、{@link GroupModel}、{@link RoleModel}、{@link UserModel}、
 * {@link OrganizationModel} 统一封装为带资源类型与 ID 的可判别记录，供权限评估器使用。</p>
 */
sealed interface ModelRecord {

    /** 客户端模型记录，资源类型为 {@link AdminPermissionsSchema#CLIENTS_RESOURCE_TYPE}。 */
    record ClientModelRecord(ClientModel client) implements ModelRecord {
        @Override
        public String getResourceType() {
            return AdminPermissionsSchema.CLIENTS_RESOURCE_TYPE;
        }

        @Override
        public String getId() {
            return client == null ? null : client.getId();
        }
    }

    /** 组模型记录，资源类型为 {@link AdminPermissionsSchema#GROUPS_RESOURCE_TYPE}。 */
    record GroupModelRecord(GroupModel group) implements ModelRecord {
        @Override
        public String getResourceType() {
            return AdminPermissionsSchema.GROUPS_RESOURCE_TYPE;
        }

        @Override
        public String getId() {
            return group == null ? null : group.getId();
        }
    }

    /** 角色模型记录，资源类型为 {@link AdminPermissionsSchema#ROLES_RESOURCE_TYPE}。 */
    record RoleModelRecord(RoleModel role) implements ModelRecord {
        @Override
        public String getResourceType() {
            return AdminPermissionsSchema.ROLES_RESOURCE_TYPE;
        }

        @Override
        public String getId() {
            return role == null ? null : role.getId();
        }
    }

    /** 用户模型记录，资源类型为 {@link AdminPermissionsSchema#USERS_RESOURCE_TYPE}。 */
    record UserModelRecord(UserModel user) implements ModelRecord {
        @Override
        public String getResourceType() {
            return AdminPermissionsSchema.USERS_RESOURCE_TYPE;
        }

        @Override
        public String getId() {
            return user == null ? null : user.getId();
        }
    }

    /** 组织模型记录，资源类型为 {@link AdminPermissionsSchema#ORGANIZATIONS_RESOURCE_TYPE}。 */
    record OrganizationModelRecord(OrganizationModel organization) implements ModelRecord {
        @Override
        public String getResourceType() {
            return AdminPermissionsSchema.ORGANIZATIONS_RESOURCE_TYPE;
        }

        @Override
        public String getId() {
            return organization == null ? null : organization.getId();
        }
    }

    /** 返回关联模型的内部 ID，模型为 {@code null} 时表示类型级权限。 */
    String getId();
    /** 返回 FGAP 资源类型标识。 */
    String getResourceType();
}
