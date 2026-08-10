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
package org.keycloak.social.twitter;

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
 * Twitter 社交身份提供者工厂。
 * <p>注册 provider id {@code twitter}；仅在 {@link Feature#TWITTER_BROKER} 特性启用时可用。</p>
 *
 * @author Pedro Igor
 */
public class TwitterIdentityProviderFactory extends AbstractIdentityProviderFactory<TwitterIdentityProvider> implements SocialIdentityProviderFactory<TwitterIdentityProvider>, EnvironmentDependentProviderFactory {

    /** Twitter IdP 在 Keycloak 中的 provider id。 */
    public static final String PROVIDER_ID = "twitter";

    /** 管理控制台显示的 IdP 名称。 */
    @Override
    public String getName() {
        return "Twitter";
    }

    /** 根据 realm 配置创建 Twitter IdP 实例。 */
    @Override
    public TwitterIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new TwitterIdentityProvider(session, new OAuth2IdentityProviderConfig(model));
    }

    /** 创建空的 OAuth2 IdP 配置对象。 */
    @Override
    public OAuth2IdentityProviderConfig createConfig() {
        return new OAuth2IdentityProviderConfig();
    }

    /** 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 检查 TWITTER_BROKER 特性是否已启用。 */
    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.TWITTER_BROKER);
    }
}
