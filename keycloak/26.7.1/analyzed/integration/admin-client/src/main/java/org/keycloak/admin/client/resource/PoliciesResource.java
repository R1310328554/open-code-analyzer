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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.authorization.PolicyEvaluationRequest;
import org.keycloak.representations.idm.authorization.PolicyEvaluationResponse;
import org.keycloak.representations.idm.authorization.PolicyProviderRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

/**
 * 授权策略（Policy）集合的管理 REST 资源。
 * <p>
 * 支持创建、查询、评估策略，并提供各类型策略子资源的访问入口。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface PoliciesResource {

    /** 创建新授权策略。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response create(PolicyRepresentation representation);

    /** 按 ID 获取单个策略管理资源。 */
    @Path("{id}")
    PolicyResource policy(@PathParam("id") String id);

    /** 按名称查找策略。 */
    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    PolicyRepresentation findByName(@QueryParam("name") String name);

    /** 列出全部策略。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<PolicyRepresentation> policies();

    /**
     * 按多种条件过滤并分页列出策略。
     *
     * @param id 策略 ID
     * @param name 策略名称
     * @param type 策略类型
     * @param resource 关联资源
     * @param scope 关联作用域
     * @param permission 是否为权限类型
     * @param owner 所有者
     * @param fields 返回字段选择
     * @param firstResult 分页起始偏移
     * @param maxResult 分页最大条数
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<PolicyRepresentation> policies(@QueryParam("policyId") String id,
            @QueryParam("name") String name,
            @QueryParam("type") String type,
            @QueryParam("resource") String resource,
            @QueryParam("scope") String scope,
            @QueryParam("permission") Boolean permission,
            @QueryParam("owner") String owner,
            @QueryParam("fields") String fields,
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResult);

    /** 列出所有可用的策略提供程序类型。 */
    @Path("providers")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<PolicyProviderRepresentation> policyProviders();

    /** 模拟评估授权策略决策结果。 */
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Path("evaluate")
    PolicyEvaluationResponse evaluate(PolicyEvaluationRequest evaluationRequest);

    /** 获取角色策略管理子资源。 */
    @Path("role")
    RolePoliciesResource role();

    /** 获取用户策略管理子资源。 */
    @Path("user")
    UserPoliciesResource user();

    /** 获取 JavaScript 策略管理子资源。 */
    @Path("js")
    JSPoliciesResource js();

    /** 获取时间条件策略管理子资源。 */
    @Path("time")
    TimePoliciesResource time();

    /** 获取聚合策略管理子资源。 */
    @Path("aggregate")
    AggregatePoliciesResource aggregate();

    /** 获取客户端策略管理子资源。 */
    @Path("client")
    ClientPoliciesResource client();

    /** 获取组策略管理子资源。 */
    @Path("group")
    GroupPoliciesResource group();

    /** 获取客户端作用域策略管理子资源。 */
    @Path("client-scope")
    ClientScopePoliciesResource clientScope();
    
    /** 获取正则表达式策略管理子资源。 */
    @Path("regex")
    RegexPoliciesResource regex();
}
