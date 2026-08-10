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

package org.keycloak.testsuite.broker.oidc;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.crypto.KeyType;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/**
 * 返回 JWKS 但故意省略 {@code use} 字段的 REST 资源，用于验证 broker 对不完整 JWK 的处理。
 */
public class MissingUseJwksRestResource {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    /**
     * @param session Keycloak 会话
     */
    public MissingUseJwksRestResource(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 返回 realm 启用密钥的 JWKS，RSA/EC 密钥的 {@code use} 字段被置为 null。
     *
     * @return JSON 格式的 {@link JSONWebKeySet} 响应
     */
    @GET
    @Path("jwks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response jwks() {
        RealmModel realm = session.getContext().getRealm();
        JWK[] jwks = session.keys().getKeysStream(realm)
                .filter(k -> k.getStatus().isEnabled() && k.getPublicKey() != null)
                .map(k -> {
                    JWKBuilder b = JWKBuilder.create().kid(k.getKid()).algorithm(k.getAlgorithmOrDefault());
                    List<X509Certificate> certificates = Optional.ofNullable(k.getCertificateChain())
                        .filter(certs -> !certs.isEmpty())
                        .orElseGet(() -> Collections.singletonList(k.getCertificate()));
                    if (k.getType().equals(KeyType.RSA)) {
                        JWK rsaKey = b.rsa(k.getPublicKey(), certificates, k.getUse());
                        // 故意清除 use 字段以模拟不规范 JWKS
                        rsaKey.setPublicKeyUse(null);
                        return rsaKey;
                    } else if (k.getType().equals(KeyType.EC)) {
                        JWK ecKey = b.ec(k.getPublicKey(), k.getUse());
                        ecKey.setPublicKeyUse(null);
                        return ecKey;
                    } else if (k.getType().equals(KeyType.OKP)) {
                        return b.okp(k.getPublicKey(), k.getUse());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toArray(JWK[]::new);

        JSONWebKeySet keySet = new JSONWebKeySet();
        keySet.setKeys(jwks);

        return Response.ok(keySet).build();
    }
}
