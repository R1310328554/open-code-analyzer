/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.tracing;

import java.util.Set;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.config.TracingOptions;
import org.keycloak.connections.httpclient.DefaultHttpClientFactory;
import org.keycloak.connections.httpclient.HttpClientBuilder;
import org.keycloak.connections.httpclient.HttpClientFactory;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.Provider;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.tracing.TracingProvider;

import io.opentelemetry.instrumentation.apachehttpclient.v4_3.ApacheHttpClientTelemetry;

/**
 * OpenTelemetry 插桩的 {@link HttpClientFactory}：为 Keycloak 出站 HTTP 调用创建可追踪的 Apache HttpClient。
 */
public class OTelHttpClientFactory extends DefaultHttpClientFactory implements EnvironmentDependentProviderFactory {
    public static final String PROVIDER_ID = "opentelemetry";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 使用 OTel 插桩的 HttpClientBuilder 包装默认构建逻辑。 */
    @Override
    protected HttpClientBuilder newHttpClientBuilder(KeycloakSession session) {
        var provider = (OTelTracingProvider) session.getProvider(TracingProvider.class);
        return new HttpClientBuilder(ApacheHttpClientTelemetry.builder(provider.getOpenTelemetry()).build().newHttpClientBuilder());
    }

    /** 复用 default 连接池配置，避免 OTel 工厂缺少 SPI 配置项。 */
    @Override
    public void init(Config.Scope config) {
        super.init(Config.scope("connectionsHttpClient", "default"));
    }

    @Override
    public int order() {
        return super.order() + 10;
    }

    @Override
    public Set<Class<? extends Provider>> dependsOn() {
        return Set.of(TracingProvider.class);
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.OPENTELEMETRY) && Configuration.isTrue(TracingOptions.TRACING_ENABLED);
    }
}
