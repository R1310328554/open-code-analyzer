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
package org.keycloak.protocol.oidc.grants.ciba.resolvers;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 默认 CIBA 登录用户解析器工厂：注册 {@link DefaultCIBALoginUserResolver} 为 SPI 提供者。
 * <p>用于 CIBA 流程中根据 login_hint 等参数解析待认证用户。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class DefaultCIBALoginUserResolverFactory implements CIBALoginUserResolverFactory {

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "default-ciba-login-user-resolver";

    /** {@inheritDoc} 创建默认 CIBA 登录用户解析器实例 */
    @Override
    public CIBALoginUserResolver create(KeycloakSession session) {
        return new DefaultCIBALoginUserResolver(session);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
