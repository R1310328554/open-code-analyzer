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
 * 服务端非对称（RSA/通用）JWS 验签上下文。
 * <p>按 kid 从 Realm 密钥库解析公钥，或直接使用 {@link KeyWrapper} 校验签名。</p>
 */
public class ServerAsymmetricSignatureVerifierContext extends AsymmetricSignatureVerifierContext {

    /** @param session 当前会话 @param kid 密钥 ID @param algorithm JWS 签名算法名 */
    public ServerAsymmetricSignatureVerifierContext(KeycloakSession session, String kid, String algorithm) throws VerificationException {
        super(getKey(session, kid, algorithm));
    }

    /** @param key 已解析的验签公钥包装 */
    public ServerAsymmetricSignatureVerifierContext(KeyWrapper key) throws VerificationException {
        super(key);
    }

    /** 按 kid 解析 Realm {@link KeyUse#SIG} 公钥；未找到则抛出 {@link VerificationException}。 */
    static KeyWrapper getKey(KeycloakSession session, String kid, String algorithm) throws VerificationException {
        KeyWrapper key = session.keys().getKey(session.getContext().getRealm(), kid, KeyUse.SIG, algorithm);
        if (key == null) {
            throw new VerificationException("Key not found");
        }
        return key;
    }

}
