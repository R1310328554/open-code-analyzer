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
package org.keycloak.social.paypal;

import java.util.List;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * PayPal 社交身份提供者工厂。
 * <p>注册 provider id {@code paypal} 并创建 {@link PayPalIdentityProvider} 实例。</p>
 *
 * @author Petter Lysne
 */
public class PayPalIdentityProviderFactory extends AbstractIdentityProviderFactory<PayPalIdentityProvider> implements SocialIdentityProviderFactory<PayPalIdentityProvider> {

    /** PayPal IdP 在 Keycloak 中的 provider id。 */
    public static final String PROVIDER_ID = "paypal";

    /** 管理控制台显示的 IdP 名称。 */
    @Override
    public String getName() {
        return "PayPal";
    }

    /** 根据 realm 配置创建 PayPal IdP 实例。 */
    @Override
    public PayPalIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new PayPalIdentityProvider(session, new PayPalIdentityProviderConfig(model));
    }

    /** 创建空的 PayPal IdP 配置对象。 */
    @Override
    public PayPalIdentityProviderConfig createConfig() {
        return new PayPalIdentityProviderConfig();
    }

    /** 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 返回沙箱开关等可配置属性列表。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property().name("sandbox")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .label("Target Sandbox")
                .helpText("Target PayPal's sandbox environment")
                .add().build();
    }
}
