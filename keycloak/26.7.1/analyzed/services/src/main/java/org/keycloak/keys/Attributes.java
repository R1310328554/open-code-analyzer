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

import java.util.function.Supplier;

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;
import org.keycloak.jose.jwe.JWEConstants;
import org.keycloak.provider.ProviderConfigProperty;

import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.FILE_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.LIST_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

/**
 * 密钥组件（KeyProvider）通用配置属性常量：定义配置键与 {@link ProviderConfigProperty} 描述。
 * <p>涵盖优先级、启用/活跃状态、RSA/EC/对称密钥尺寸、算法选择与证书生成开关等。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface Attributes {

    /** 组件优先级配置键。 */
    String PRIORITY_KEY = "priority";
    /** 优先级配置项（数值越大越优先）。 */
    ProviderConfigProperty PRIORITY_PROPERTY = new ProviderConfigProperty(PRIORITY_KEY, "Priority", "Priority for the provider", STRING_TYPE, "0");

    /** 密钥是否启用配置键。 */
    String ENABLED_KEY = "enabled";
    /** 启用开关配置项。 */
    ProviderConfigProperty ENABLED_PROPERTY = new ProviderConfigProperty(ENABLED_KEY, "Enabled", "Set if the keys are enabled", BOOLEAN_TYPE, "true");

    /** 密钥是否活跃（可用于签名等）配置键。 */
    String ACTIVE_KEY = "active";
    /** 活跃开关配置项。 */
    ProviderConfigProperty ACTIVE_PROPERTY = new ProviderConfigProperty(ACTIVE_KEY, "Active", "Set if the keys can be used for signing", BOOLEAN_TYPE, "true");

    /** RSA 私钥 PEM 配置键。 */
    String PRIVATE_KEY_KEY = "privateKey";
    /** RSA 私钥 PEM 文件配置项。 */
    ProviderConfigProperty PRIVATE_KEY_PROPERTY = new ProviderConfigProperty(PRIVATE_KEY_KEY, "Private RSA Key", "Private RSA Key encoded in PEM format", FILE_TYPE, null, true);

    /** X509 证书 PEM 配置键。 */
    String CERTIFICATE_KEY = "certificate";
    /** X509 证书 PEM 文件配置项。 */
    ProviderConfigProperty CERTIFICATE_PROPERTY = new ProviderConfigProperty(CERTIFICATE_KEY, "X509 Certificate", "X509 Certificate encoded in PEM format", FILE_TYPE, null);

    /** RSA 密钥模数位长配置键。 */
    String KEY_SIZE_KEY = "keySize";
    /** RSA 密钥长度配置项（动态读取支持的位长列表）。 */
    Supplier<ProviderConfigProperty> KEY_SIZE_PROPERTY = () -> new ProviderConfigProperty(KEY_SIZE_KEY, "Key size", "Size for the generated keys", LIST_TYPE, "2048",
            CryptoIntegration.getProvider().getSupportedRsaKeySizes());

    /** 密钥用途（SIG/ENC）配置键。 */
    String KEY_USE = "keyUse";
    /** 密钥用途选择配置项（签名或加密）。 */
    ProviderConfigProperty KEY_USE_PROPERTY = new ProviderConfigProperty(KEY_USE, "Key use", "Whether the key should be used for signing or encryption.", LIST_TYPE,
            KeyUse.SIG.getSpecName(), KeyUse.SIG.getSpecName(), KeyUse.ENC.getSpecName());

    /** EC 密钥是否生成 X509 证书配置键。 */
    String EC_GENERATE_CERTIFICATE_KEY = "ecGenerateCertificate";
    /** EC 密钥创建时是否生成自签名证书的配置项。 */
    ProviderConfigProperty EC_GENERATE_CERTIFICATE_PROPERTY = new ProviderConfigProperty(
            EC_GENERATE_CERTIFICATE_KEY,
            "Generate Certificate",
            """
            If a certificate should be build on creation. If the certificate is build, it will be available in the \
            realm JWK for the key in the claim x5c and corresponding thumbprints may be available in the claims like \
            x5t or x5t#S256.""",
            BOOLEAN_TYPE,
            false);

    /** 密钥标识 kid 配置键。 */
    String KID_KEY = "kid";

    /** 对称密钥 secret 配置键。 */
    String SECRET_KEY = "secret";

    /** 对称密钥字节长度配置键。 */
    String SECRET_SIZE_KEY = "secretSize";
    /** 对称密钥 secret 字节长度配置项。 */
    ProviderConfigProperty SECRET_SIZE_PROPERTY = new ProviderConfigProperty(SECRET_SIZE_KEY, "Secret size", "Size in bytes for the generated secret", LIST_TYPE,
            String.valueOf(GeneratedHmacKeyProviderFactory.DEFAULT_HMAC_KEY_SIZE),
            "16", "24", "32", "64", "128", "256", "512");

    /** 密钥算法配置键。 */
    String ALGORITHM_KEY = "algorithm";

    /** RSA 签名算法选择配置项（RS256/PS256 等）。 */
    ProviderConfigProperty RS_ALGORITHM_PROPERTY = new ProviderConfigProperty(ALGORITHM_KEY, "Algorithm", "Intended algorithm for the key", LIST_TYPE,
            Algorithm.RS256,
            Algorithm.RS256, Algorithm.RS384, Algorithm.RS512, Algorithm.PS256, Algorithm.PS384, Algorithm.PS512);

    /** HMAC 签名算法选择配置项（HS256/HS512 等）。 */
    ProviderConfigProperty HS_ALGORITHM_PROPERTY = new ProviderConfigProperty(ALGORITHM_KEY, "Algorithm", "Intended algorithm for the key", LIST_TYPE,
            Algorithm.HS512,
            Algorithm.HS256, Algorithm.HS384, Algorithm.HS512);

    /** RSA 加密算法选择配置项（RSA-OAEP 等）。 */
    ProviderConfigProperty RS_ENC_ALGORITHM_PROPERTY = new ProviderConfigProperty(ALGORITHM_KEY, "Algorithm", "Intended algorithm for the key encryption", LIST_TYPE,
            JWEConstants.RSA_OAEP,
            JWEConstants.RSA1_5, JWEConstants.RSA_OAEP, JWEConstants.RSA_OAEP_256);

}
