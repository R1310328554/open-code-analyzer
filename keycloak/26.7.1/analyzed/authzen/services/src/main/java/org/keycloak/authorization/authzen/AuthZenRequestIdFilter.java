/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authorization.authzen;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.common.Profile;

/**
 * 在 AuthZen 端点响应中回显 {@code X-Request-ID} 请求头，符合 OpenID AuthZen Authorization API 1.0 规范（第 10.1.3 节）。
 * <p>
 * 本过滤器在异常映射器之后运行，确保 4xx/5xx 错误响应也携带该头。
 */
@Provider
public class AuthZenRequestIdFilter implements ContainerResponseFilter {

    /** 请求/响应关联 ID 的 HTTP 头名称。 */
    public static final String X_REQUEST_ID = "X-Request-ID";
    /** AuthZen 访问 API 路径片段，用于限定过滤器作用范围。 */
    private static final String AUTHZEN_PATH_SEGMENT = AuthZenRealmResourceProviderFactory.PROVIDER_ID + "/" + AuthZen.AUTHZEN_ACCESS_PATH;

    /**
     * 对 AuthZen 路径下的响应，将请求中的 {@code X-Request-ID} 写回响应头。
     *
     * @param requestContext  入站请求上下文
     * @param responseContext 出站响应上下文
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (!Profile.isFeatureEnabled(Profile.Feature.AUTHZEN)) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();
        if (path == null || !path.contains(AUTHZEN_PATH_SEGMENT)) {
            return;
        }

        String requestId = requestContext.getHeaderString(X_REQUEST_ID);
        if (requestId != null) {
            responseContext.getHeaders().putSingle(X_REQUEST_ID, requestId);
        }
    }
}
