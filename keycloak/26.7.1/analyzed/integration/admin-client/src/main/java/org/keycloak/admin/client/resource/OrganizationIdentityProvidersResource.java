/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.IdentityProviderRepresentation;

/**
 * 组织内身份提供程序集合的管理 REST 资源。
 * <p>
 * 支持将领域 IdP 关联到组织、列出已关联 IdP 及按 ID 访问单个 IdP 资源。
 */
public interface OrganizationIdentityProvidersResource {

    /**
     * 将指定 ID 的身份提供程序关联到当前组织。
     *
     * @param id 身份提供程序 ID 或别名
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response addIdentityProvider(String id);

    /** 列出当前组织关联的所有身份提供程序。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<IdentityProviderRepresentation> getIdentityProviders();

    /** 按 ID 获取单个组织身份提供程序资源。 */
    @Path("{id}")
    OrganizationIdentityProviderResource get(@PathParam("id") String id);
}
