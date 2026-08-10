/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.social.instagram;

import org.keycloak.Config.Scope;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * Instagram 社交身份提供者工厂。
 * <p>注册 provider id {@code instagram}，仅在启用 {@code INSTAGRAM_BROKER} 特性时可用。</p>
 *
 * @author Pedro Igor
 */
public class InstagramIdentityProviderFactory extends AbstractIdentityProviderFactory<InstagramIdentityProvider> implements SocialIdentityProviderFactory<InstagramIdentityProvider>, EnvironmentDependentProviderFactory {

    /** Instagram IdP 的 provider id。 */
    public static final String PROVIDER_ID = "instagram";

    /** 管理控制台显示名称。 */
    @Override
    public String getName() {
        return "Instagram";
    }

    /** 根据领域模型创建 {@link InstagramIdentityProvider} 实例。 */
    @Override
    public InstagramIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new InstagramIdentityProvider(session, new OAuth2IdentityProviderConfig(model));
    }

    /** 创建默认 OAuth2 配置对象。 */
    @Override
    public OAuth2IdentityProviderConfig createConfig() {
        return new OAuth2IdentityProviderConfig();
    }

    /** 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 检查当前环境是否启用了 Instagram Broker 特性。 */
    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.INSTAGRAM_BROKER);
    }
}
