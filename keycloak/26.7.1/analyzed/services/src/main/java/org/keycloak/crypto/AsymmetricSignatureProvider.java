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
 * 服务端非对称签名提供者（RSA）。
 * <p>支持按会话密钥或显式 {@link KeyWrapper} 创建签名/验签上下文。</p>
 */
public class AsymmetricSignatureProvider implements SignatureProvider {

    /** 当前 Keycloak 会话，用于解析 kid 对应密钥。 */
    private final KeycloakSession session;
    /** 签名算法标识（如 RS256、RS384）。 */
    private final String algorithm;

    /** @param session 当前会话 @param algorithm 签名算法名 */
    public AsymmetricSignatureProvider(KeycloakSession session, String algorithm) {
        this.session = session;
        this.algorithm = algorithm;
    }

    @Override
    /** 使用 Realm 活动签名密钥创建 RSA 签名上下文。 */
    public SignatureSignerContext signer() throws SignatureException {
        return new ServerAsymmetricSignatureSignerContext(session, algorithm);
    }

    @Override
    /** 使用指定 RSA 密钥包装创建签名上下文。 */
    public SignatureSignerContext signer(KeyWrapper key) throws SignatureException {
        SignatureProvider.checkKeyForSignature(key, algorithm, KeyType.RSA);
        return new ServerAsymmetricSignatureSignerContext(key);
    }

    @Override
    /** 按 kid 解析公钥并创建验签上下文。 */
    public SignatureVerifierContext verifier(String kid) throws VerificationException {
        return new ServerAsymmetricSignatureVerifierContext(session, kid, algorithm);
    }

    @Override
    /** 使用指定公钥包装创建验签上下文。 */
    public SignatureVerifierContext verifier(KeyWrapper key) throws VerificationException {
        SignatureProvider.checkKeyForVerification(key, algorithm, KeyType.RSA);
        return new ServerAsymmetricSignatureVerifierContext(key);
    }

    @Override
    /** @return 恒为 true */
    public boolean isAsymmetricAlgorithm() {
        return true;
    }
}
