/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.common.VerificationException;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;

/**
 * 客户端 HMAC JWS 签名验证提供者。
 * <p>为 HS256/384/512 等对称算法创建 {@link ClientMacSignatureVerifierContext}。</p>
 */
public class MacSecretClientSignatureVerifierProvider implements ClientSignatureVerifierProvider {
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** HMAC 签名算法标识（如 HS512）。 */
    private final String algorithm;

    /** @param session 当前会话 @param algorithm HMAC 算法名 */
    public MacSecretClientSignatureVerifierProvider(KeycloakSession session, String algorithm) {
        this.session = session;
        this.algorithm = algorithm;
    }

    @Override
    /** 从客户端共享密钥创建 HMAC 验签上下文。 */
    public SignatureVerifierContext verifier(ClientModel client, JWSInput input) throws VerificationException {
        return new ClientMacSignatureVerifierContext(session, client, algorithm);
    }

    @Override
    /** @return 本提供者绑定的 HMAC 算法名 */
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    /** @return 恒为 false，表示对称 HMAC 算法 */
    public boolean isAsymmetricAlgorithm() {
        return false;
    }
}
