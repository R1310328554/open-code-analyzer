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
package org.keycloak.keys;


import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ConfigurationValidationHelper;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * EC 密钥提供者工厂抽象基类：定义通用组件配置项与校验逻辑。
 * <p>配置包括优先级、启用/活跃状态及是否生成 X509 证书；并提供 NIST 曲线名到 SEC 命名的转换工具方法。</p>
 */
public abstract class AbstractEcKeyProviderFactory<T extends KeyProvider> implements KeyProviderFactory<T> {

    /** 默认 EC 曲线（NIST P-256）。 */
    public static final String DEFAULT_EC_ELLIPTIC_CURVE = "P-256";

    /** 构建 EC 密钥组件的标准配置属性列表。 */
    public final static ProviderConfigurationBuilder configurationBuilder() {
        return ProviderConfigurationBuilder.create()
                .property(Attributes.PRIORITY_PROPERTY)
                .property(Attributes.ENABLED_PROPERTY)
                .property(Attributes.ACTIVE_PROPERTY)
                .property(Attributes.EC_GENERATE_CERTIFICATE_PROPERTY);
    }

    @Override
    /** 校验 priority、enabled、active、generateCertificate 等配置项类型。 */
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model) throws ComponentValidationException {
        ConfigurationValidationHelper.check(model)
                .checkLong(Attributes.PRIORITY_PROPERTY, false)
                .checkBoolean(Attributes.ENABLED_PROPERTY, false)
                .checkBoolean(Attributes.ACTIVE_PROPERTY, false)
                .checkBoolean(Attributes.EC_GENERATE_CERTIFICATE_PROPERTY, false);
    }

    /** 将 NIST 曲线名（如 P-256）转换为 SEC 命名（如 secp256r1），供密钥生成使用。 */
    public static String convertECDomainParmNistRepToSecRep(String ecInNistRep) {
        // 将 NIST 椭圆曲线域参数名转换为 SEC 命名（用于 EC 密钥生成）
        String ecInSecRep = null;
        switch(ecInNistRep) {
            case "P-256" :
                ecInSecRep = "secp256r1";
                break;
            case "P-384" :
                ecInSecRep = "secp384r1";
                break;
            case "P-521" :
                ecInSecRep = "secp521r1";
                break;
            default :
                // 未知曲线名返回 null
        }
        return ecInSecRep;
    }
}
