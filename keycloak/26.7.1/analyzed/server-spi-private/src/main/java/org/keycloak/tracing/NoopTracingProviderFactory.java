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

package org.keycloak.tracing;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 空操作追踪提供者工厂：提供单例 {@link NoopTracingProvider}。
 * <p>当 OpenTelemetry 禁用时作为默认提供者；启用时优先级最低。</p>
 */
public class NoopTracingProviderFactory implements TracingProviderFactory {
    /** 提供者标识符 {@code noop} */
    public static final String PROVIDER_ID = "noop";
    private static TracingProvider SINGLETON;

    /** @return 共享的单例 {@link NoopTracingProvider} */
    @Override
    public TracingProvider create(KeycloakSession session) {
        if (SINGLETON == null) {
            SINGLETON = new NoopTracingProvider();
        }
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
        SINGLETON = null;
    }

    /** @return {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 根据 OpenTelemetry 特性开关决定工厂排序优先级 */
    @Override
    public int order() {
        // OpenTelemetry 禁用时作为默认提供者；启用时优先级最低
        return !Profile.isFeatureEnabled(Profile.Feature.OPENTELEMETRY) ? 1000 : -1000;
    }

    /** @return 始终为 {@code true}，空操作提供者始终可用 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return true;
    }
}
