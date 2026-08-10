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

package org.keycloak.admin.client.token;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.representations.AccessTokenResponse;

/**
 * OpenID Connect 令牌端点的 JAX-RS 客户端接口。
 * <p>
 * 封装与 Keycloak 授权服务器交互所需的令牌授予、刷新及登出操作，
 * 供 {@link org.keycloak.admin.client.Keycloak} 等管理客户端在认证流程中调用。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public interface TokenService {

    /** 向指定领域请求访问令牌（grant_type 由表单参数决定）。 */
    @POST
    @Path("/realms/{realm}/protocol/openid-connect/token")
    AccessTokenResponse grantToken(@PathParam("realm") String realm, MultivaluedMap<String, String> map);

    /** 使用 refresh_token 刷新指定领域的访问令牌。 */
    @POST
    @Path("/realms/{realm}/protocol/openid-connect/token")
    AccessTokenResponse refreshToken(@PathParam("realm") String realm, MultivaluedMap<String, String> map);

    /** 注销指定领域的当前会话并吊销关联令牌。 */
    @POST
    @Path("/realms/{realm}/protocol/openid-connect/logout")
    void logout(@PathParam("realm") String realm, MultivaluedMap<String, String> map);

}
