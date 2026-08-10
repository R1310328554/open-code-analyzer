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
import org.keycloak.models.KeycloakSession;

/**
 * 服务端 HMAC 对称签名提供者。
 * <p>支持按会话密钥或显式 {@link KeyWrapper}（oct 类型）创建签名/验签上下文。</p>
 */
public class MacSecretSignatureProvider implements SignatureProvider {

    /** 当前 Keycloak 会话，用于解析 kid 对应密钥。 */
    private final KeycloakSession session;
    /** HMAC 签名算法标识（如 HS512）。 */
    private final String algorithm;

    /** @param session 当前会话 @param algorithm HMAC 算法名 */
    public MacSecretSignatureProvider(KeycloakSession session, String algorithm) {
        this.session = session;
        this.algorithm = algorithm;
    }

    @Override
    /** 使用 Realm 活动 HMAC 密钥创建签名上下文。 */
    public SignatureSignerContext signer() throws SignatureException {
        return new ServerMacSignatureSignerContext(session, algorithm);
    }

    @Override
    /** 使用指定 oct 密钥包装创建签名上下文。 */
    public SignatureSignerContext signer(KeyWrapper key) throws SignatureException {
        SignatureProvider.checkKeyForSignature(key, algorithm, KeyType.OCT);
        return new ServerMacSignatureSignerContext(key);
    }

    @Override
    /** 按 kid 解析共享密钥并创建验签上下文。 */
    public SignatureVerifierContext verifier(String kid) throws VerificationException {
        return new ServerMacSignatureVerifierContext(session, kid, algorithm);
    }

    @Override
    /** 使用指定 oct 密钥包装创建验签上下文。 */
    public SignatureVerifierContext verifier(KeyWrapper key) throws VerificationException {
        SignatureProvider.checkKeyForVerification(key, algorithm, KeyType.OCT);
        return new ServerMacSignatureVerifierContext(key);
    }

    @Override
    /** @return 恒为 false */
    public boolean isAsymmetricAlgorithm() {
        return false;
    }
}
