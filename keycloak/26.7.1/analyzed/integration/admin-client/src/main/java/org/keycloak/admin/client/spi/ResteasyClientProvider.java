/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.admin.client.spi;

import javax.net.ssl.SSLContext;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;

/**
 * 用于屏蔽底层 JAX-RS 实现差异的客户端 SPI。
 * <p>
 * 使 admin-client 可在不同 RESTEasy 版本或实现上统一创建客户端与资源代理。
 */
public interface ResteasyClientProvider {

    /**
     * 创建新的 {@link Client} 实例。
     *
     * @param messageHandler {@link jakarta.ws.rs.ext.MessageBodyReader} 和/或 {@link jakarta.ws.rs.ext.MessageBodyWriter} 实例
     * @param sslContext 可选的 {@link SSLContext}
     * @param disableTrustManager 使用 TLS 时是否跳过服务端证书校验
     * @return 配置完成的 JAX-RS 客户端
     */
    Client newRestEasyClient(Object messageHandler, SSLContext sslContext, boolean disableTrustManager);

    /**
     * 为指定 {@code targetClass} 创建实现相关的资源代理。
     *
     * @param target {@link WebTarget} 实例
     * @param targetClass JAX-RS 客户端资源接口类
     * @return {@code targetClass} 的代理实例
     */
    <R> R targetProxy(WebTarget target, Class<R> targetClass);
}
