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

package org.keycloak.services.cors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * 默认 CORS Provider 工厂。
 * <p>合并 {@link Cors#DEFAULT_ALLOW_HEADERS} 与配置的额外允许头，创建 {@link DefaultCors} 实例。</p>
 * @author <a href="mailto:demetrio@carretti.pro">Dmitry Telegin</a>
 */
public class DefaultCorsFactory implements CorsFactory {

    /** Provider 唯一标识符 */
    private static final String PROVIDER_ID = "default";
    /** SPI 配置键：额外允许的 CORS 请求头 */
    private static final String ALLOWED_HEADERS = "allowedHeaders";
    /** 初始化后合并的允许请求头（逗号分隔） */
    private String allowedHeaders;

    /** {@inheritDoc} 创建 {@link DefaultCors} */
    @Override
    public Cors create(KeycloakSession session) {
        return new DefaultCors(session, allowedHeaders);
    }

    /** {@inheritDoc} 合并默认与自定义允许头 */
    @Override
    public void init(Config.Scope config) {
        Set<String> allowedHeaders = new HashSet<>(Cors.DEFAULT_ALLOW_HEADERS);

        String[] customAllowedHeaders = config.getArray(ALLOWED_HEADERS);
        if (customAllowedHeaders != null) {
            allowedHeaders.addAll(Arrays.asList(customAllowedHeaders));
        }

        this.allowedHeaders = String.join(", ", allowedHeaders);
    }

    /** {@inheritDoc} 无后置初始化 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** {@inheritDoc} 无资源需释放 */
    @Override
    public void close() {
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 返回 allowedHeaders 配置元数据 */
    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name(ALLOWED_HEADERS)
                .type("string")
                .helpText("A comma-separated list of additional allowed headers for CORS requests")
                .defaultValue("")
                .add()
                .build();
    }
}
