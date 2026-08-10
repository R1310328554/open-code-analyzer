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

package org.keycloak.crypto;

import org.keycloak.common.VerificationException;
import org.keycloak.models.KeycloakSession;

/**
 * 服务端 EdDSA（OKP）签名提供者。
 * <p>支持按会话密钥或显式 {@link KeyWrapper} 创建 EdDSA 签名/验签上下文。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class EdDSASignatureProvider implements SignatureProvider {

    /** 当前 Keycloak 会话，用于解析 kid 对应密钥。 */
    private final KeycloakSession session;

    /** @param session 当前会话 */
    public EdDSASignatureProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    /** 使用 Realm 活动 OKP 密钥创建 EdDSA 签名上下文。 */
    public SignatureSignerContext signer() throws SignatureException {
        return new ServerEdDSASignatureSignerContext(session, Algorithm.EdDSA);
    }

    @Override
    /** 使用指定 OKP 密钥创建 EdDSA 签名上下文。 */
    public SignatureSignerContext signer(KeyWrapper key) throws SignatureException {
        SignatureProvider.checkKeyForSignature(key, Algorithm.EdDSA, KeyType.OKP);
        return new ServerEdDSASignatureSignerContext(key);
    }

    @Override
    /** 按 kid 解析 OKP 公钥并创建验签上下文。 */
    public SignatureVerifierContext verifier(String kid) throws VerificationException {
        return new ServerEdDSASignatureVerifierContext(session, kid, Algorithm.EdDSA);
    }

    @Override
    /** 使用指定 OKP 公钥创建验签上下文。 */
    public SignatureVerifierContext verifier(KeyWrapper key) throws VerificationException {
        SignatureProvider.checkKeyForVerification(key, Algorithm.EdDSA, KeyType.OKP);
        return new ServerEdDSASignatureVerifierContext(key);
    }

    @Override
    /** @return 恒为 true */
    public boolean isAsymmetricAlgorithm() {
        return true;
    }

}
