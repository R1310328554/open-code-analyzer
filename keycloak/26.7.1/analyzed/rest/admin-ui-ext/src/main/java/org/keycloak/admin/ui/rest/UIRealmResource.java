/*
 *
 *  * Copyright 2023  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.admin.ui.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;

import org.keycloak.admin.ui.rest.model.UIRealmInfo;
import org.keycloak.admin.ui.rest.model.UIRealmRepresentation;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.StorageProviderRealmModel;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.RealmAdminResource;
import org.keycloak.services.resources.admin.UserProfileResource;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.storage.UserStorageProviderModel;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * 管理控制台专用的领域 REST 装饰器：在标准 Admin Realm API 之上扩展 UI 特有行为。
 *
 * 仅供内置管理控制台使用。
 */
public class UIRealmResource {

    /** 委托给标准 {@link RealmAdminResource} 执行核心更新逻辑。 */
    private final RealmAdminResource delegate;
    private final KeycloakSession session;
    private final AdminPermissionEvaluator auth;
    private final AdminEventBuilder adminEvent;

    public UIRealmResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.auth = auth;
        this.adminEvent = adminEvent;
        this.delegate = new RealmAdminResource(session, auth, adminEvent);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation( hidden = true )
    /** 更新领域配置；成功后同步写入用户配置文件（UPConfig）。 */
    public Response updateRealm(UIRealmRepresentation rep) {
        Response response = delegate.updateRealm(rep);

        if (isSuccessful(response)) {
            updateUserProfileConfiguration(rep);
        }

        return response;
    }

    @GET
    @Path("info")
    @Operation(summary = "Gets information about the realm, viewable by all realm admins")
    @APIResponse(responseCode = "200", description = "", content = {
            @Content(schema = @Schema(implementation = UIRealmInfo.class, type = SchemaType.OBJECT))})
    @Produces(MediaType.APPLICATION_JSON)
    /** 返回领域摘要信息，所有领域管理员均可查看。 */
    public UIRealmInfo getInfo() {
        auth.requireAnyAdminRole();

        final var info = new UIRealmInfo();
        info.setUserProfileProvidersEnabled(isAtLeastOneUserStorageProviderEnabled());
        return info;
    }

    /** 判断当前领域是否至少启用了一个用户存储提供方。 */
    private boolean isAtLeastOneUserStorageProviderEnabled() {
        return ((StorageProviderRealmModel) session.getContext().getRealm()).getUserStorageProvidersStream()
                .anyMatch(UserStorageProviderModel::isEnabled);
    }

    /** 若请求体携带 UPConfig，则通过 {@link UserProfileResource} 持久化用户配置文件。 */
    private void updateUserProfileConfiguration(UIRealmRepresentation rep) {
        UserProfileResource userProfileResource = new UserProfileResource(session, auth, adminEvent);
        UPConfig config = rep.getUpConfig();
        if (config == null) {
            return;
        }
        userProfileResource.setAndGetConfiguration(config);
    }

    /** 判断 HTTP 响应是否属于 2xx 成功族。 */
    private boolean isSuccessful(Response response) {
        return Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily());
    }
}
