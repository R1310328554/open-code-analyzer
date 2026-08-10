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
package org.keycloak.crypto;

import org.keycloak.common.VerificationException;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;

/**
 * 客户端 JWS 非对称签名验证提供者（RSA 等）。
 * <p>为指定算法创建 {@link ClientAsymmetricSignatureVerifierContext}，从客户端公钥校验令牌签名。</p>
 */
public class AsymmetricClientSignatureVerifierProvider implements ClientSignatureVerifierProvider {
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** JWS 签名算法标识（如 RS256）。 */
    private final String algorithm;

    /** @param session 当前会话 @param algorithm 签名算法名 */
    public AsymmetricClientSignatureVerifierProvider(KeycloakSession session, String algorithm) {
        this.session = session;
        this.algorithm = algorithm;
    }

    @Override
    /** 解析客户端 RSA 公钥并返回签名验证上下文。 */
    public SignatureVerifierContext verifier(ClientModel client, JWSInput input) throws VerificationException {
        return new ClientAsymmetricSignatureVerifierContext(session, client, input);
    }

    @Override
    /** @return 本提供者绑定的 JWS 算法名 */
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    /** @return 恒为 true，表示非对称签名算法 */
    public boolean isAsymmetricAlgorithm() {
        return true;
    }
}
