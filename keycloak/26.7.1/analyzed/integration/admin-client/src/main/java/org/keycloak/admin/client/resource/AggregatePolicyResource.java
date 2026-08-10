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

import org.keycloak.representations.idm.authorization.AggregatePolicyRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;

/**
 * 单个聚合策略的管理 REST 资源。
 * <p>
 * 支持读取、更新、删除策略，并查询关联策略、依赖策略及受保护资源。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface AggregatePolicyResource {

    /** 获取当前聚合策略的 JSON 表示。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    AggregatePolicyRepresentation toRepresentation();

    /** 更新聚合策略配置。 */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void update(AggregatePolicyRepresentation representation);

    /** 删除当前聚合策略。 */
    @DELETE
    void remove();

    /** 列出本策略直接关联的子策略。 */
    @Path("/associatedPolicies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<PolicyRepresentation> associatedPolicies();

    /** 列出依赖本策略的其他策略。 */
    @Path("/dependentPolicies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<PolicyRepresentation> dependentPolicies();

    /** 列出本策略所保护的可授权资源。 */
    @Path("/resources")
    @GET
    @Produces("application/json")
    List<ResourceRepresentation> resources();

}
