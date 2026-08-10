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
 *
 */

package org.keycloak.testsuite.wellknown;


import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCWellKnownProviderFactory;
import org.keycloak.wellknown.WellKnownProvider;

/**
 * 自定义 OIDC Well-Known 提供者工厂：从 classpath JSON 加载配置覆盖项，
 * 优先级高于默认工厂，用于测试 Well-Known 端点的自定义行为。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CustomOIDCWellKnownProviderFactory extends OIDCWellKnownProviderFactory {

    /** 系统属性：是否在 Well-Known 响应中包含客户端作用域。 */
    public static final String INCLUDE_CLIENT_SCOPES = "oidc.wellknown.include.client.scopes";

    @Override
    public WellKnownProvider create(KeycloakSession session) {
        return new CustomOIDCWellKnownProvider(session, getOpenidConfigOverride(), includeClientScopes());
    }

    /** 从系统属性读取是否包含客户端作用域，未设置时默认为 true。 */
    private boolean includeClientScopes() {
        String includeClientScopesProp = System.getProperty("oidc.wellknown.include.client.scopes");
        return includeClientScopesProp == null || Boolean.parseBoolean(includeClientScopesProp);
    }

    /** 切换上下文类加载器后从 classpath JSON 初始化配置覆盖项。 */
    @Override
    public void init(Config.Scope config) {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(CustomOIDCWellKnownProviderFactory.class.getClassLoader());
            initConfigOverrideFromFile("classpath:wellknown/oidc-well-known-config-override.json");
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }

    @Override
    public String getId() {
        return "custom-testsuite-oidc-well-known-factory";
    }

    @Override
    public String getAlias() {
        return OIDCWellKnownProviderFactory.PROVIDER_ID;
    }

    // 优先级应高于默认工厂
    @Override
    public int getPriority() {
        return 1;
    }
}
