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
package org.keycloak.social.github;

import java.util.List;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * GitHub 社交身份提供者工厂。
 * <p>注册 provider id {@code github}，并提供 base URL、API URL、JSON 格式等配置项。</p>
 *
 * @author Pedro Igor
 */
public class GitHubIdentityProviderFactory extends AbstractIdentityProviderFactory<GitHubIdentityProvider> implements SocialIdentityProviderFactory<GitHubIdentityProvider> {

    /** GitHub IdP 的 provider id。 */
    public static final String PROVIDER_ID = "github";

    /** 管理控制台显示名称。 */
    @Override
    public String getName() {
        return "GitHub";
    }

    /** 创建 GitHub IdP 实例。 */
    @Override
    public GitHubIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new GitHubIdentityProvider(session, new OAuth2IdentityProviderConfig(model));
    }

    /** 创建默认 OAuth2 配置。 */
    @Override
    public OAuth2IdentityProviderConfig createConfig() {
        return new OAuth2IdentityProviderConfig();
    }

    /** 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 定义 GitHub 自建实例相关的 URL 与 JSON 格式配置属性。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create().property()
                .name(GitHubIdentityProvider.BASE_URL_KEY).label("Base URL").helpText("Override the default Base URL for this identity provider.")
                .type(ProviderConfigProperty.STRING_TYPE).add().property()
                .name(GitHubIdentityProvider.API_URL_KEY).label("API URL").helpText("Override the default API URL for this identity provider.")
                .type(ProviderConfigProperty.STRING_TYPE).add().property()
                .name(GitHubIdentityProvider.GITHUB_JSON_FORMAT_KEY).label("JSON Format").helpText("Enable to receive JSON format responses from GitHub. This is also required to automatically refresh access tokens retrieved from GitHub.")
                .defaultValue(false).type(ProviderConfigProperty.BOOLEAN_TYPE).add().build();
    }
}
