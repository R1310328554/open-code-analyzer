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

import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import org.jboss.logging.Logger;

import static org.keycloak.provider.ProviderConfigProperty.LIST_TYPE;

/**
 * 自动生成 ECDSA 密钥的工厂（ID {@code ecdsa-generated}）：支持 ES256/ES384/ES512 签名。
 * <p>曲线 P-256/P-384/P-521 分别对应 ES256/ES384/ES512；密钥用途固定为签名（{@link KeyUse#SIG}）。</p>
 */
public class GeneratedEcdsaKeyProviderFactory extends AbstractGeneratedEcKeyProviderFactory<KeyProvider> {

    private static final Logger logger = Logger.getLogger(GeneratedEcdsaKeyProviderFactory.class);

    /** 组件配置中 ECDSA 私钥属性键。 */
    public static final String ECDSA_PRIVATE_KEY_KEY = "ecdsaPrivateKey";
    /** 组件配置中 ECDSA 公钥属性键。 */
    public static final String ECDSA_PUBLIC_KEY_KEY = "ecdsaPublicKey";
    /** 组件配置中 ECDSA 椭圆曲线属性键。 */
    public static final String ECDSA_ELLIPTIC_CURVE_KEY = "ecdsaEllipticCurveKey";

    // 支持 NIST P-256/P-384/P-521，分别对应 ES256/ES384/ES512
    protected static ProviderConfigProperty ECDSA_ELLIPTIC_CURVE_PROPERTY = new ProviderConfigProperty(ECDSA_ELLIPTIC_CURVE_KEY, "Elliptic Curve", "Elliptic Curve used in ECDSA", LIST_TYPE,
            String.valueOf(GeneratedEcdsaKeyProviderFactory.DEFAULT_ECDSA_ELLIPTIC_CURVE),
            "P-256", "P-384", "P-521");

    /** 工厂标识：{@code ecdsa-generated}。 */
    public static final String ID = "ecdsa-generated";

    /** 管理控制台帮助文本。 */
    private static final String HELP_TEXT = "Generates ECDSA keys";

     // secp256r1 / NIST P-256 / X9.62 prime256v1 / OID 1.2.840.10045.3.1.7
    /** 默认 ECDSA 曲线（NIST P-256）。 */
    public static final String DEFAULT_ECDSA_ELLIPTIC_CURVE = DEFAULT_EC_ELLIPTIC_CURVE;

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = AbstractGeneratedEcKeyProviderFactory.configurationBuilder()
            .property(ECDSA_ELLIPTIC_CURVE_PROPERTY)
            .build();

    @Override
    /** 创建 {@link GeneratedEcdsaKeyProvider} 实例。 */
    public KeyProvider create(KeycloakSession session, ComponentModel model) {
        return new GeneratedEcdsaKeyProvider(session.getContext().getRealm(), model);
    }

    @Override
    /** @return 工厂帮助说明 */
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    /** @return ECDSA 密钥组件配置属性列表 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    /** @return 工厂 ID {@code ecdsa-generated} */
    public String getId() {
        return ID;
    }

    @Override
    /** @return 工厂日志记录器 */
    protected Logger getLogger() {
        return logger;
    }

    @Override
    /** ECDSA 密钥仅用于签名用途。 */
    protected boolean isValidKeyUse(KeyUse keyUse) {
        return KeyUse.SIG.equals(keyUse);
    }

    @Override
    /** 判断是否支持 ES256/ES384/ES512 算法。 */
    protected boolean isSupportedEcAlgorithm(String algorithm) {
        return (algorithm.equals(Algorithm.ES256) || algorithm.equals(Algorithm.ES384)
                || algorithm.equals(Algorithm.ES512));
    }

    @Override
    /** 按 JWS 算法解析对应 NIST 曲线名。 */
    protected String getEcEllipticCurveKey(String algorithm) {
        return convertJWSAlgorithmToECDomainParmNistRep(algorithm);
    }

    @Override
    /** @return 椭圆曲线配置项描述 */
    protected ProviderConfigProperty getEcEllipticCurveProperty() {
        return ECDSA_ELLIPTIC_CURVE_PROPERTY;
    }

    @Override
    /** @return 椭圆曲线配置属性键 */
    protected String getEcEllipticCurveKey() {
        return ECDSA_ELLIPTIC_CURVE_KEY;
    }

    @Override
    /** @return ECDSA 私钥配置属性键 */
    protected String getEcPrivateKeyKey() {
        return ECDSA_PRIVATE_KEY_KEY;
    }

    @Override
    /** @return ECDSA 公钥配置属性键 */
    protected String getEcPublicKeyKey() {
        return ECDSA_PUBLIC_KEY_KEY;
    }

    @Override
    /** @return 默认 NIST 曲线名 */
    protected String getDefaultEcEllipticCurve() {
        return DEFAULT_ECDSA_ELLIPTIC_CURVE;
    }

    /** 将 NIST 曲线名映射为 JWS 签名算法（如 P-256 → ES256）。 */
    public static String convertECDomainParmNistRepToJWSAlgorithm(String ecInNistRep) {
        switch(ecInNistRep) {
            case "P-256" :
                return Algorithm.ES256;
            case "P-384" :
                return Algorithm.ES384;
            case "P-521" :
                return Algorithm.ES512;
            default :
                return null;
        }
    }

    /** 将 JWS 签名算法映射回 NIST 曲线名。 */
    public static String convertJWSAlgorithmToECDomainParmNistRep(String algorithm) {
        switch(algorithm) {
            case Algorithm.ES256 :
                return "P-256";
            case Algorithm.ES384 :
                return "P-384";
            case Algorithm.ES512 :
                return "P-521";
            default :
                return null;
        }
    }
}
