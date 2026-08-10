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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 领域（Realm）集合的顶层管理 REST 资源。
 * <p>
 * 映射至 {@code /admin/realms}，支持创建领域、列出全部领域及按名称访问单个领域。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
@Path("/admin/realms")
@Consumes(MediaType.APPLICATION_JSON)
public interface RealmsResource {

    /** 按领域名称获取单个领域的管理资源。 */
    @Path("/{realm}")
    RealmResource realm(@PathParam("realm") String realm);

    /** 创建新领域。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void create(RealmRepresentation realmRepresentation);

    /** 列出服务器上所有可访问的领域。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RealmRepresentation> findAll();

}
