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
package org.keycloak.social.facebook;

import java.util.List;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * Facebook 社交身份提供者工厂。
 * <p>注册 provider id {@code facebook}，并提供额外 profile 字段配置项。</p>
 *
 * @author Pedro Igor
 */
public class FacebookIdentityProviderFactory extends AbstractIdentityProviderFactory<FacebookIdentityProvider> implements SocialIdentityProviderFactory<FacebookIdentityProvider> {

    /** Facebook IdP 的 provider id。 */
    public static final String PROVIDER_ID = "facebook";

    /** 管理控制台显示名称。 */
    @Override
    public String getName() {
        return "Facebook";
    }

    /** 创建 Facebook IdP 实例。 */
    @Override
    public FacebookIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new FacebookIdentityProvider(session, new FacebookIdentityProviderConfig(model));
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

    /** 定义管理端可配置的额外 profile 字段属性。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property().name("fetchedFields")
                .label("Additional user's profile fields")
                .helpText("Provide additional fields which would be fetched using the profile request. This will be appended to the default set of 'id,name,email,first_name,last_name'.")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add().build();
    }
}
