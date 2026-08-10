/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;

/**
 * 客户端作用域评估 REST 资源。
 * <p>
 * 模拟指定用户与作用域组合下将生成的访问令牌、ID 令牌及 UserInfo 响应，
 * 便于管理员在配置阶段预览令牌内容。
 *
 * @author <a href="mailto:ggrazian@redhat.com">Giuseppe Graziano</a>
 */
public interface ClientScopeEvaluateResource {

    /**
     * 生成示例访问令牌（Access Token）。
     * <p>
     * 模拟以指定 {@code scope} 参数发起授权请求时将产生的令牌。
     *
     * @param scopeParam scope 参数值，可为 null
     * @param userId 用户 ID
     * @param audience audience 参数值；自 Keycloak 26.2 起支持，部分授权类型不支持时应传 null
     * @return 生成的访问令牌
     */
    @GET
    @Path("generate-example-access-token")
    AccessToken generateAccessToken(@QueryParam("scope") String scopeParam, @QueryParam("userId") String userId, @QueryParam("audience") String audience);

    /**
     * 生成示例 ID 令牌（ID Token）。
     * <p>
     * 模拟以指定 {@code scope} 参数发起 OpenID Connect 授权时将产生的 ID 令牌。
     *
     * @param scopeParam scope 参数值，可为 null
     * @param userId 用户 ID
     * @param audience audience 参数值；自 Keycloak 26.2 起支持，部分授权类型不支持时应传 null
     * @return 生成的 ID 令牌
     */
    @GET
    @Path("generate-example-id-token")
    IDToken generateExampleIdToken(@QueryParam("scope") String scopeParam, @QueryParam("userId") String userId, @QueryParam("audience") String audience);

    /**
     * 生成示例 UserInfo 响应。
     * <p>
     * 模拟以指定 {@code scope} 生成访问令牌后调用 UserInfo 端点将返回的内容。
     *
     * @param scopeParam scope 参数值，可为 null
     * @param userId 用户 ID
     * @return 生成的 UserInfo 键值对
     */
    @GET
    @Path("generate-example-userinfo")
    Map<String, Object> generateExampleUserinfo(@QueryParam("scope") String scopeParam, @QueryParam("userId") String userId);

}
