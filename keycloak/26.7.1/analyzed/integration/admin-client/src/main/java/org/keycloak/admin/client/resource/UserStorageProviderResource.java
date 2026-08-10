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
package org.keycloak.admin.client.resource;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.SynchronizationResultRepresentation;

/**
 * 用户存储提供程序的管理 REST 资源。
 * <p>
 * 支持触发用户同步、移除或解绑导入用户，以及 LDAP 映射器数据同步。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserStorageProviderResource {
    /**
     * 若提供程序支持同步，则触发用户同步。
     * <p>
     * action 可为 {@code triggerFullSync} 或 {@code triggerChangedUsersSync}。
     *
     * @param componentId 用户存储组件 ID
     * @param action 同步动作类型
     * @return 同步结果
     */
    @POST
    @Path("{componentId}/sync")
    @Produces(MediaType.APPLICATION_JSON)
    SynchronizationResultRepresentation syncUsers(@PathParam("componentId") String componentId, @QueryParam("action") String action);

    /**
     * 移除已导入的用户。
     *
     * @param componentId 用户存储组件 ID
     */
    @POST
    @Path("{componentId}/remove-imported-users")
    @Produces(MediaType.APPLICATION_JSON)
    void removeImportedUsers(@PathParam("componentId") String componentId);

    /**
     * 将已导入用户与用户存储提供程序解绑。
     *
     * @param componentId 用户存储组件 ID
     */
    @POST
    @Path("{componentId}/unlink-users")
    @Produces(MediaType.APPLICATION_JSON)
    void unlink(@PathParam("componentId") String componentId);

    /**
     * 触发 LDAP 映射器数据同步的 REST 调用。
     * <p>
     * direction 可为 {@code fedToKeycloak} 或 {@code keycloakToFed}。
     *
     * @param componentId 用户存储组件 ID
     * @param mapperId 映射器 ID
     * @param direction 同步方向
     * @return 同步结果
     */
    @POST
    @Path("{componentId}/mappers/{mapperId}/sync")
    @Produces(MediaType.APPLICATION_JSON)
    SynchronizationResultRepresentation syncMapperData(@PathParam("componentId") String componentId, @PathParam("mapperId") String mapperId, @QueryParam("direction") String direction);


}
