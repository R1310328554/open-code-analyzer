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

import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import org.jboss.logging.Logger;

import static org.keycloak.provider.ProviderConfigProperty.LIST_TYPE;

/**
 * 自动生成 ECDH 密钥的工厂（ID {@code ecdh-generated}）：支持 JWE 密钥协商算法。
 * <p>曲线 P-256/P-384/P-521 分别对应 ECDH-ES+A128KW/A192KW/A256KW；密钥用途固定为加密（{@link KeyUse#ENC}）。</p>
 */
public class GeneratedEcdhKeyProviderFactory extends AbstractGeneratedEcKeyProviderFactory<KeyProvider> {

    // secp256r1 / NIST P-256 / X9.62 prime256v1 / OID 1.2.840.10045.3.1.7
    /** 默认 ECDH 曲线（NIST P-256）。 */
    public static final String DEFAULT_ECDH_ELLIPTIC_CURVE = DEFAULT_EC_ELLIPTIC_CURVE;

    /** 组件配置中 ECDH 算法属性键。 */
    public static final String ECDH_ALGORITHM_KEY = "ecdhAlgorithm";

    /** 组件配置中 ECDH 椭圆曲线属性键。 */
    public static final String ECDH_ELLIPTIC_CURVE_KEY = "ecdhEllipticCurveKey";
    /** 组件配置中 ECDH 私钥属性键。 */
    public static final String ECDH_PRIVATE_KEY_KEY = "ecdhPrivateKey";
    /** 组件配置中 ECDH 公钥属性键。 */
    public static final String ECDH_PUBLIC_KEY_KEY = "ecdhPublicKey";

    // 支持 NIST P-256/P-384/P-521 曲线，分别对应不同 JWE 密钥长度
    protected static ProviderConfigProperty ECDH_ELLIPTIC_CURVE_PROPERTY = new ProviderConfigProperty(ECDH_ELLIPTIC_CURVE_KEY, "Elliptic Curve", "Elliptic Curve used in ECDH", LIST_TYPE,
            String.valueOf(GeneratedEcdhKeyProviderFactory.DEFAULT_ECDH_ELLIPTIC_CURVE),
            "P-256", "P-384", "P-521");

    protected static ProviderConfigProperty ECDH_ALGORITHM_PROPERTY = new ProviderConfigProperty(ECDH_ALGORITHM_KEY,
            "Algorithm", "Algorithm for processing the Content Encryption Key", LIST_TYPE, Algorithm.ECDH_ES,
            Algorithm.ECDH_ES, Algorithm.ECDH_ES_A128KW, Algorithm.ECDH_ES_A192KW, Algorithm.ECDH_ES_A256KW);

    /** 管理控制台帮助文本。 */
    private static final String HELP_TEXT = "Generates ECDH keys";

    /** 工厂标识：{@code ecdh-generated}。 */
    public static final String ID = "ecdh-generated";

    private static final Logger logger = Logger.getLogger(GeneratedEcdhKeyProviderFactory.class);

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = AbstractGeneratedEcKeyProviderFactory.configurationBuilder()
            .property(ECDH_ELLIPTIC_CURVE_PROPERTY)
            .property(ECDH_ALGORITHM_PROPERTY)
            .build();

    /** 将 NIST 曲线名映射为对应 JWE 密钥管理算法（如 P-256 → ECDH-ES+A128KW）。 */
    public static String convertECDomainParmNistRepToJWEAlgorithm(String ecInNistRep) {
        switch(ecInNistRep) {
            case "P-256" :
                return Algorithm.ECDH_ES_A128KW;
            case "P-384" :
                return Algorithm.ECDH_ES_A192KW;
            case "P-521" :
                return Algorithm.ECDH_ES_A256KW;
            default :
                return null;
        }
    }

    /** 将 JWE 密钥管理算法映射回 NIST 曲线名。 */
    public static String convertJWEAlgorithmToECDomainParmNistRep(String algorithm) {
        switch(algorithm) {
            case Algorithm.ECDH_ES_A128KW :
                return "P-256";
            case Algorithm.ECDH_ES_A192KW :
                return "P-384";
            case Algorithm.ECDH_ES_A256KW :
                return "P-521";
            default :
                return null;
        }
    }

    @Override
    /** 创建 {@link GeneratedEcdhKeyProvider} 实例。 */
    public KeyProvider create(KeycloakSession session, ComponentModel model) {
        return new GeneratedEcdhKeyProvider(session.getContext().getRealm(), model);
    }

    @Override
    /** @return ECDH 密钥组件配置属性列表 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    /** @return 默认 NIST 曲线名 */
    protected String getDefaultEcEllipticCurve() {
        return DEFAULT_ECDH_ELLIPTIC_CURVE;
    }

    @Override
    /** @return 椭圆曲线配置属性键 */
    protected String getEcEllipticCurveKey() {
        return ECDH_ELLIPTIC_CURVE_KEY;
    }

    @Override
    /** 按 JWE 算法解析对应 NIST 曲线名。 */
    protected String getEcEllipticCurveKey(String algorithm) {
        if (Algorithm.ECDH_ES.equals(algorithm)) {
            return DEFAULT_ECDH_ELLIPTIC_CURVE;
        }
        return convertJWEAlgorithmToECDomainParmNistRep(algorithm);
    }

    @Override
    /** @return 椭圆曲线配置项描述 */
    protected ProviderConfigProperty getEcEllipticCurveProperty() {
        return ECDH_ELLIPTIC_CURVE_PROPERTY;
    }

    @Override
    /** @return ECDH 私钥配置属性键 */
    protected String getEcPrivateKeyKey() {
        return ECDH_PRIVATE_KEY_KEY;
    }

    @Override
    /** @return ECDH 公钥配置属性键 */
    protected String getEcPublicKeyKey() {
        return ECDH_PUBLIC_KEY_KEY;
    }

    @Override
    /** @return 工厂帮助说明 */
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    /** @return 工厂 ID {@code ecdh-generated} */
    public String getId() {
        return ID;
    }

    @Override
    /** @return 工厂日志记录器 */
    protected Logger getLogger() {
        return logger;
    }

    @Override
    /** 判断是否支持 ECDH-ES 及 ECDH-ES+A*KW 算法。 */
    protected boolean isSupportedEcAlgorithm(String algorithm) {
        return (algorithm.equals(Algorithm.ECDH_ES) || algorithm.equals(Algorithm.ECDH_ES_A128KW)
                || algorithm.equals(Algorithm.ECDH_ES_A192KW) || algorithm.equals(Algorithm.ECDH_ES_A256KW));
    }

    @Override
    /** ECDH 密钥仅用于加密用途。 */
    protected boolean isValidKeyUse(KeyUse keyUse) {
        return KeyUse.ENC.equals(keyUse);
    }
}
