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
package org.keycloak.protocol.oidc.utils;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/**
 * 服务端 JWKS 工具：从 realm 活跃密钥构建 JWKS，并将 {@link KeyWrapper} 转为 JWK。
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */public class JWKSServerUtils {
    /**
     * 收集 realm 中已启用且含公钥的密钥，构建 JWKS。
     * @param session Keycloak 会话
     * @param realm 领域模型
     * @return realm 的 JSON Web Key Set
     */
        JWK[] jwks = session.keys().getKeysStream(realm)
                .filter(k -> k.getStatus().isEnabled() && k.getPublicKey() != null)
                .map(JWKSServerUtils::toJwk)
                .filter(Objects::nonNull)
                .toArray(JWK[]::new);

        JSONWebKeySet keySet = new JSONWebKeySet();
        keySet.setKeys(jwks);
        return keySet;
    }


    /**
     * 将 {@link KeyWrapper} 转换为 JWK（支持 RSA、EC、OKP）。
     * @param key 密钥包装对象
     * @return 对应 JWK，不支持的类型返回 null
     */
        JWKBuilder b = JWKBuilder.create()
                .kid(key.getKid())
                .algorithm(key.getAlgorithmOrDefault());
        List<X509Certificate> certificates = Optional.ofNullable(key.getCertificateChain())
                .filter(certs -> !certs.isEmpty())
                .orElseGet(() -> Optional.ofNullable(key.getCertificate())
                        .map(Collections::singletonList)
                        .orElseGet(Collections::emptyList));
        if (key.getType().equals(KeyType.RSA)) {
            return b.rsa(key.getPublicKey(), certificates, key.getUse());
        } else if (key.getType().equals(KeyType.EC)) {
            return b.ec(key.getPublicKey(), certificates, key.getUse());
        } else if (key.getType().equals(KeyType.OKP)) {
            return b.okp(key.getPublicKey(), key.getUse());
        }
        return null;
    }
}
