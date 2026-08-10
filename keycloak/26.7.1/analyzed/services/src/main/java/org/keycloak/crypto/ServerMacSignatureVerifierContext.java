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
 * 服务端 HMAC 验签上下文。
 * <p>按 kid 从 Realm 密钥库解析 SIG 密钥，或使用显式 {@link KeyWrapper} 初始化。</p>
 */
public class ServerMacSignatureVerifierContext extends MacSignatureVerifierContext {

    /** @param session 当前会话 @param kid JWK kid @param algorithm HMAC 算法标识 */
    public ServerMacSignatureVerifierContext(KeycloakSession session, String kid, String algorithm) throws VerificationException {
        super(getKey(session, kid, algorithm));
    }

    /** @param key 已解析的 oct 对称密钥包装 */
    public ServerMacSignatureVerifierContext(KeyWrapper key) throws VerificationException {
        super(key);
    }

    /** 按 kid 与算法查找 Realm 签名密钥，未找到时抛出 {@link VerificationException}。 */
    private static KeyWrapper getKey(KeycloakSession session, String kid, String algorithm) throws VerificationException {
        KeyWrapper key = session.keys().getKey(session.getContext().getRealm(), kid, KeyUse.SIG, algorithm);
        if (key == null) {
            throw new VerificationException("Key not found");
        }
        return key;
    }

}
