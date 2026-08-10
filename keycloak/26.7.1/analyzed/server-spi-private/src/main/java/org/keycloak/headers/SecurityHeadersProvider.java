/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.headers;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;

import org.keycloak.provider.Provider;

/**
 * HTTP 安全响应头提供者 SPI。
 * <p>在 JAX-RS 响应写出前注入 X-Frame-Options、Content-Security-Policy、 X-Content-Type-Options 等头，降低点击劫持与 MIME 嗅探风险。</p>
 */
public interface SecurityHeadersProvider extends Provider {

    /** 获取当前请求/响应的可变头配置构建器。 */
    SecurityHeadersOptions options();

    /** 根据配置向响应上下文写入安全 HTTP 头。 */
    void addHeaders(ContainerRequestContext requestContext, ContainerResponseContext responseContext);

    /** 默认无资源需释放。 */
    @Override
    default void close() {
    }

}
