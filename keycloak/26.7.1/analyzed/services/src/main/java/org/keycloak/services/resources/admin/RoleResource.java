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

package org.keycloak.services.resources.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import static org.keycloak.utils.StringUtil.isBlank;

/**
 * 角色管理抽象基类。
 * <p>封装角色 CRUD、复合角色及属性同步的共用逻辑，供 {@link RoleContainerResource} 与 {@link RoleByIdResource} 继承。</p>
 *
 * @resource Roles
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class RoleResource {
    /** 当前领域 */
    protected RealmModel realm;

    /** @param realm 当前领域 */
    public RoleResource(RealmModel realm) {
        this.realm = realm;
    }

    /** 将 {@link RoleModel} 转为完整 {@link RoleRepresentation}。 */
    protected RoleRepresentation getRole(RoleModel roleModel) {
        return ModelToRepresentation.toRepresentation(roleModel);
    }

    /** 从角色容器中删除角色。 */
    protected void deleteRole(RoleModel role) {
        if (!role.getContainer().removeRole(role)) {
            throw new NotFoundException("Role not found");
        }
    }

    /** 根据表示更新角色名称、描述与属性，并发布重命名事件。 */
    protected void updateRole(RoleRepresentation rep, RoleModel role, RealmModel realm,
            KeycloakSession session) {
        String newName = rep.getName();
        if (isBlank(newName)) {
            throw new BadRequestException("role has no name");
        }
        String previousName = role.getName();
        if (!Objects.equals(previousName, newName)) {
            role.setName(newName);

            session.getKeycloakSessionFactory().publish(new RoleModel.RoleNameChangeEvent() {
                @Override
                public RealmModel getRealm() {
                    return realm;
                }

                @Override
                public String getNewName() {
                    return newName;
                }

                @Override
                public String getPreviousName() {
                    return previousName;
                }

                @Override
                public String getClientId() {
                    if (!role.isClientRole()) {
                        return null;
                    }

                    return ((ClientModel) role.getContainer()).getClientId();
                }

                @Override
                public KeycloakSession getKeycloakSession() {
                    return session;
                }
            });
        }

        role.setDescription(rep.getDescription());

        if (rep.getAttributes() != null) {
            Set<String> attrsToRemove = new HashSet<>(role.getAttributes().keySet());
            attrsToRemove.removeAll(rep.getAttributes().keySet());

            for (Map.Entry<String, List<String>> attr : rep.getAttributes().entrySet()) {
                role.setAttribute(attr.getKey(), attr.getValue());
            }

            for (String attr : attrsToRemove) {
                role.removeAttribute(attr);
            }
        }
    }

    /** 为角色添加复合子角色并记录管理事件。 */
    protected void addComposites(AdminPermissionEvaluator auth, AdminEventBuilder adminEvent, UriInfo uriInfo, List<RoleRepresentation> roles, RoleModel role) {
        for (RoleRepresentation rep : roles) {
            if (rep.getId() == null) throw new NotFoundException("Could not find composite role");
            RoleModel composite = realm.getRoleById(rep.getId());
            if (composite == null) {
                throw new NotFoundException("Could not find composite role");
            }
            auth.roles().requireMapComposite(composite);
            role.addCompositeRole(composite);
        }

        if (role.isClientRole()) {
            adminEvent.resource(ResourceType.CLIENT_ROLE);
        } else {
            adminEvent.resource(ResourceType.REALM_ROLE);
        }

        adminEvent.operation(OperationType.CREATE).resourcePath(uriInfo).representation(roles).success();
    }

    /** 返回角色复合中的领域级子角色。 */
    protected Stream<RoleRepresentation> getRealmRoleComposites(RoleModel role) {
        return role.getCompositesStream()
                .filter(composite -> composite.getContainer() instanceof RealmModel)
                .map(ModelToRepresentation::toBriefRepresentation);
    }

    /** 返回角色复合中属于指定客户端的子角色。 */
    protected Stream<RoleRepresentation> getClientRoleComposites(ClientModel app, RoleModel role) {
        return role.getCompositesStream()
                .filter(composite -> Objects.equals(composite.getContainer(), app))
                .map(ModelToRepresentation::toBriefRepresentation);
    }

    /** 从角色复合中移除子角色并记录管理事件。 */
    protected void deleteComposites(AdminEventBuilder adminEvent, UriInfo uriInfo, List<RoleRepresentation> roles, RoleModel role) {
        for (RoleRepresentation rep : roles) {
            RoleModel composite = realm.getRoleById(rep.getId());
            if (composite == null) {
                throw new NotFoundException("Could not find composite role");
            }
            role.removeCompositeRole(composite);
        }

        if (role.isClientRole()) {
            adminEvent.resource(ResourceType.CLIENT_ROLE);
        } else {
            adminEvent.resource(ResourceType.REALM_ROLE);
        }

        adminEvent.operation(OperationType.DELETE).resourcePath(uriInfo).representation(roles).success();
    }
}
