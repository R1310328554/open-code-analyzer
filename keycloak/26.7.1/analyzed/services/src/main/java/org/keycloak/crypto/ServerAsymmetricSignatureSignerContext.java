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

import org.keycloak.models.KeycloakSession;

/**
 * 服务端非对称（RSA/通用）JWS 签名上下文。
 * <p>从 Realm 活动签名密钥或显式 {@link KeyWrapper} 初始化，用于签发令牌。</p>
 */
public class ServerAsymmetricSignatureSignerContext extends AsymmetricSignatureSignerContext {

    /** @param session 当前会话 @param algorithm JWS 签名算法名 */
    public ServerAsymmetricSignatureSignerContext(KeycloakSession session, String algorithm) throws SignatureException {
        super(getKey(session, algorithm));
    }

    /** @param key 已解析的签名私钥包装 */
    public ServerAsymmetricSignatureSignerContext(KeyWrapper key) throws SignatureException {
        super(key);
    }

    /** 解析 Realm 当前活动 {@link KeyUse#SIG} 密钥；未找到则抛出 {@link SignatureException}。 */
    static KeyWrapper getKey(KeycloakSession session, String algorithm) {
        KeyWrapper key = session.keys().getActiveKey(session.getContext().getRealm(), KeyUse.SIG, algorithm);
        if (key == null) {
            throw new SignatureException("Active key for " + algorithm + " not found");
        }
        return key;
    }

}
