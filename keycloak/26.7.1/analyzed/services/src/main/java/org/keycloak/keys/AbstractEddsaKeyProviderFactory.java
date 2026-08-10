/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.keys;

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.crypto.Algorithm;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ConfigurationValidationHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import static org.keycloak.provider.ProviderConfigProperty.LIST_TYPE;

/**
 * EdDSA 密钥提供者工厂抽象基类：定义 Ed25519/Ed448 曲线选择与通用组件配置。
 * <p>配置包括优先级、启用/活跃状态及椭圆曲线选择；私钥/公钥组件属性键为 {@code eddsaPrivateKey}/{@code eddsaPublicKey}。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public abstract class AbstractEddsaKeyProviderFactory implements KeyProviderFactory {

    /** 组件配置中 EdDSA 私钥属性键。 */
    protected static final String EDDSA_PRIVATE_KEY_KEY = "eddsaPrivateKey";
    /** 组件配置中 EdDSA 公钥属性键。 */
    protected static final String EDDSA_PUBLIC_KEY_KEY = "eddsaPublicKey";
    /** 组件配置中 EdDSA 曲线选择属性键。 */
    protected static final String EDDSA_ELLIPTIC_CURVE_KEY = "eddsaEllipticCurveKey";
    /** 默认 EdDSA 曲线（Ed25519）。 */
    public static final String DEFAULT_EDDSA_ELLIPTIC_CURVE = Algorithm.Ed25519;

    protected static ProviderConfigProperty EDDSA_ELLIPTIC_CURVE_PROPERTY = new ProviderConfigProperty(EDDSA_ELLIPTIC_CURVE_KEY, 
            "Elliptic Curve", "Elliptic Curve used in EdDSA", LIST_TYPE,
            String.valueOf(DEFAULT_EDDSA_ELLIPTIC_CURVE), Algorithm.Ed25519, Algorithm.Ed448);
 
    /** 构建 EdDSA 密钥组件的标准配置属性列表。 */
    public final static ProviderConfigurationBuilder configurationBuilder() {
        return ProviderConfigurationBuilder.create()
                .property(Attributes.PRIORITY_PROPERTY)
                .property(Attributes.ENABLED_PROPERTY)
                .property(Attributes.ACTIVE_PROPERTY);
    }

    @Override
    /** 校验 priority、enabled、active 等配置项类型。 */
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        ConfigurationValidationHelper.check(model)
                .checkLong(Attributes.PRIORITY_PROPERTY, false)
                .checkBoolean(Attributes.ENABLED_PROPERTY, false)
                .checkBoolean(Attributes.ACTIVE_PROPERTY, false);
    }

}
