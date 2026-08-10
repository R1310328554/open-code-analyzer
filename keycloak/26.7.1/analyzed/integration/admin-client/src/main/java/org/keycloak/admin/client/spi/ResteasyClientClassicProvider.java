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

import org.keycloak.admin.client.JacksonProvider;

import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;

/**
 * 基于 RESTEasy Classic 的 {@link ResteasyClientProvider} 实现。
 * <p>
 * 负责创建 RESTEasy 客户端实例并生成 JAX-RS 资源代理。
 */
public class ResteasyClientClassicProvider implements ResteasyClientProvider {

    @Override
    public Client newRestEasyClient(Object customJacksonProvider, SSLContext sslContext, boolean disableTrustManager) {
        ResteasyClientBuilderImpl builder = createClientBuilder().sslContext(sslContext);

        if (customJacksonProvider != null) {
            builder.register(customJacksonProvider, 100);
        } else {
            builder.register(JacksonProvider.class, 100);
        }

        if (disableTrustManager) {
            builder.disableTrustManager();
        }
        return builder.build();
    }

    /**
     * 创建带有通用默认配置和提供程序注册的客户端构建器实例。
     *
     * @return 新的 RESTEasy 客户端构建器
     */
    public static ResteasyClientBuilderImpl createClientBuilder() {
        return new ResteasyClientBuilderImpl().connectionPoolSize(10).register(StreamMessageBodyReader.class);
    }

    @Override
    public <R> R targetProxy(WebTarget client, Class<R> targetClass) {
        return ResteasyWebTarget.class.cast(client).proxy(targetClass);
    }
}
