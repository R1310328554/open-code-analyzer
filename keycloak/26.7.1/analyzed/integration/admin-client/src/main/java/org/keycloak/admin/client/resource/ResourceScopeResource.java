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

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

/**
 * 单个资源作用域（Scope）的管理 REST 资源。
 * <p>
 * 支持读取、更新、删除作用域，并查询引用该作用域的权限策略。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface ResourceScopeResource {

    /** 获取当前作用域的表示对象。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ScopeRepresentation toRepresentation();

    /** 更新当前作用域配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(ScopeRepresentation scope);

    /** 删除当前作用域。 */
    @DELETE
    void remove();

    /** 列出引用当前作用域的权限策略。 */
    @Path("/permissions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<PolicyRepresentation> permissions();
}
