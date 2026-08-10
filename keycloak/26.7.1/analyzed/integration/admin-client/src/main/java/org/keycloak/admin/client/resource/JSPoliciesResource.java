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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.authorization.JSPolicyRepresentation;

/**
 * JavaScript 策略（JS Policy）集合的管理 REST 资源。
 * <p>
 * JS 策略允许通过脚本逻辑自定义授权决策；提供创建、按 ID/名称查询的端点。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface JSPoliciesResource {

    /** 创建新的 JavaScript 策略。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response create(JSPolicyRepresentation representation);

    /** 按 ID 获取单个 JS 策略资源。 */
    @Path("{id}")
    JSPolicyResource findById(@PathParam("id") String id);

    /** 按名称搜索 JS 策略。 */
    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    JSPolicyRepresentation findByName(@QueryParam("name") String name);
}
