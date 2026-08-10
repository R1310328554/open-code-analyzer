/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.crypto;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.common.VerificationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.util.Strings;

/**
 * 加密工具类：签名提供者查找与支持的非对称算法发现。
 * <p>聚合 {@link SignatureProvider} SPI 与 Realm 密钥流，供令牌/OIDC 能力协商使用。</p>
 *
 * @author <a href="https://github.com/forkimenjeckayang">Forkim Akwichek</a>
 */
public class CryptoUtils {

    /**
     * 按算法名查找已注册的 {@link SignatureProvider}，未注册则抛出 {@link VerificationException}。
     */
    public static SignatureProvider getSignatureProvider(KeycloakSession session, String algorithm) throws VerificationException {
        if (algorithm == null) {
            throw new VerificationException("Missing token algorithm");
        }
        SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, algorithm);
        if (signatureProvider == null) {
            throw new VerificationException("Unsupported token algorithm: " + algorithm);
        }
        return signatureProvider;
    }

    /**
     * 返回当前环境支持的非对称签名算法列表。
     * <p>遍历 {@link SignatureProvider} 工厂并过滤 {@link SignatureProvider#isAsymmetricAlgorithm()} 为 true 的项。</p>
     *
     * @param session The Keycloak session
     * @return List of asymmetric signature algorithm names
     */
    public static List<String> getSupportedAsymmetricSignatureAlgorithms(KeycloakSession session) {
        return session.getKeycloakSessionFactory().getProviderFactoriesStream(SignatureProvider.class)
                .map(ProviderFactory::getId)
                .map(algorithm -> new AbstractMap.SimpleEntry<>(algorithm, session.getProvider(SignatureProvider.class, algorithm)))
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue().isAsymmetricAlgorithm())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 返回 Realm 中可用于加密的非对称算法列表。
     * <p>从密钥流筛选 {@link KeyUse#ENC} 且公钥/私钥为 {@link PublicKey}/{@link PrivateKey} 的条目。</p>
     *
     * @param session The Keycloak session
     * @return List of asymmetric encryption algorithm names
     */
    public static List<String> getSupportedAsymmetricEncryptionAlgorithms(KeycloakSession session) {
        List<String> encAlgos = session.keys()
                .getKeysStream(session.getContext().getRealm())
                .filter(key -> KeyUse.ENC.equals(key.getUse()))
                .filter(key -> {
                    Key k = key.getPublicKey();
                    // 非对称密钥持有 PublicKey/PrivateKey，对称密钥为 SecretKey
                    return k instanceof PublicKey || key.getPrivateKey() instanceof PrivateKey;
                })
                .map(KeyWrapper::getAlgorithm)
                .filter(alg -> !Strings.isEmpty(alg))
                .distinct()
                .toList();
        return encAlgos;
    }
}
