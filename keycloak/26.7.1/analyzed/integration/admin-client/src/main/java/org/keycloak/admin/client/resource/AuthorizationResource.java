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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;

/**
 * 客户端授权服务（Authorization Services）的管理 REST 资源。
 * <p>
 * 提供资源服务器配置的读写、导入/导出，以及资源、范围、策略、权限子资源的导航入口。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface AuthorizationResource {

    /** 更新资源服务器授权配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void update(ResourceServerRepresentation server);

    /** 获取当前资源服务器的授权设置。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ResourceServerRepresentation getSettings();

    /** 导入资源服务器授权配置。 */
    @Path("/import")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void importSettings(ResourceServerRepresentation server);

    /** 导出资源服务器授权配置。 */
    @Path("/settings")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ResourceServerRepresentation exportSettings();

    /** @return 可授权资源子资源 */
    @Path("/resource")
    ResourcesResource resources();

    /** @return 授权范围子资源 */
    @Path("/scope")
    ResourceScopesResource scopes();

    /** @return 授权策略子资源 */
    @Path("/policy")
    PoliciesResource policies();

    /** @return 权限子资源 */
    @Path("/permission")
    PermissionsResource permissions();
}
