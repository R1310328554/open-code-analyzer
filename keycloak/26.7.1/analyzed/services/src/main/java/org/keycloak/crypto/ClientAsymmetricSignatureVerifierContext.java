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
import org.keycloak.keys.loader.PublicKeyStorageManager;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;

/**
 * 客户端 RSA JWS 签名验证上下文。
 * <p>从 {@link PublicKeyStorageManager} 加载客户端公钥，校验密钥类型与算法一致性。</p>
 */
public class ClientAsymmetricSignatureVerifierContext extends AsymmetricSignatureVerifierContext {

    /** @param session 当前会话 @param client 待验签客户端 @param input 待验证 JWS */
    public ClientAsymmetricSignatureVerifierContext(KeycloakSession session, ClientModel client, JWSInput input) throws VerificationException {
        super(getKey(session, client, input));
    }

    private static KeyWrapper getKey(KeycloakSession session, ClientModel client, JWSInput input) throws VerificationException {
        KeyWrapper key = PublicKeyStorageManager.getClientPublicKeyWrapper(session, client, input);
        if (key == null) {
            throw new VerificationException("Key not found");
        }
        if (!KeyType.RSA.equals(key.getType())) {
            throw new VerificationException("Key Type is not RSA: " + key.getType());
        }
        if (key.getAlgorithm() == null) {
            // 密钥未指定算法时，默认采用 JWS 头中的算法
            // 验签前应已完成算法白名单等策略校验
            key.setAlgorithm(input.getHeader().getRawAlgorithm());
        } else if (!key.getAlgorithm().equals(input.getHeader().getRawAlgorithm())) {
            throw new VerificationException("Key Algorithms are different, key-algorithm=" + key.getAlgorithm()
                    + " jwt-algorithm=" + input.getHeader().getRawAlgorithm());
        }
        return key;
    }
}
