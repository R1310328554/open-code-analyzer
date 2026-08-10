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

package org.keycloak.protocol.oid4vc.issuance.keybinding;

import org.keycloak.common.VerificationException;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.jose.jwk.OKPPublicJWK;
import org.keycloak.models.KeycloakSession;

/**
 * {@link ProofValidator} 抽象基类，封装 JWK 到签名验证器的通用转换逻辑。
 * <p>子类（JWT/Attestation proof）复用 {@link #getVerifier(JWK, String)} 完成密钥绑定校验。</p>
 */
public abstract class AbstractProofValidator implements ProofValidator {

    /** 当前 Keycloak 会话，用于获取 {@link SignatureProvider}。 */
    protected final KeycloakSession keycloakSession;

    /**
     * @param keycloakSession 当前请求会话
     */
    protected AbstractProofValidator(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    /**
     * 根据 JWK 与 JWS 算法构建签名验证上下文。
     *
     * @param jwk 证明或 attestation 中的公钥
     * @param jwsAlgorithm JWS 头中的 alg 值
     * @return 可用于校验签名的 {@link SignatureVerifierContext}
     */
    protected SignatureVerifierContext getVerifier(JWK jwk, String jwsAlgorithm) throws VerificationException {
        SignatureProvider signatureProvider = keycloakSession.getProvider(SignatureProvider.class, jwsAlgorithm);
        KeyWrapper keyWrapper = getKeyWrapper(jwk, jwsAlgorithm);
        keyWrapper.setUse(KeyUse.SIG);
        return signatureProvider.verifier(keyWrapper);
    }

    private KeyWrapper getKeyWrapper(JWK jwk, String algorithm) {
        KeyWrapper keyWrapper = new KeyWrapper();
        keyWrapper.setType(jwk.getKeyType());

        // 使用调用方传入的算法，而非 JWK 内嵌算法（若有），避免与 JWS 校验逻辑冲突
        keyWrapper.setAlgorithm(algorithm);

        // 若为 OKP 密钥则设置曲线参数
        if (jwk.getOtherClaim(OKPPublicJWK.CRV, String.class) != null) {
            keyWrapper.setCurve(jwk.getOtherClaim(OKPPublicJWK.CRV, String.class));
        }

        JWKParser parser = JWKParser.create(jwk);
        keyWrapper.setPublicKey(parser.toPublicKey());
        return keyWrapper;
    }
}
