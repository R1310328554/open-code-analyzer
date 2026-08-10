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
 * 服务端 HMAC 签名上下文。
 * <p>从 Realm 当前活动 SIG 密钥或显式 {@link KeyWrapper} 初始化，委托 {@link MacSignatureSignerContext} 执行 JWS 签名。</p>
 */
public class ServerMacSignatureSignerContext extends MacSignatureSignerContext {

    /** @param session 当前会话 @param algorithm HMAC 算法标识（如 HS512） */
    public ServerMacSignatureSignerContext(KeycloakSession session, String algorithm) throws SignatureException {
        super(getKey(session, algorithm));
    }

    /** @param key 已解析的 oct 对称密钥包装 */
    public ServerMacSignatureSignerContext(KeyWrapper key) throws SignatureException {
        super(key);
    }

    /** 解析 Realm 中指定算法的活动签名密钥，未找到时抛出 {@link SignatureException}。 */
    private static KeyWrapper getKey(KeycloakSession session, String algorithm) {
        KeyWrapper key = session.keys().getActiveKey(session.getContext().getRealm(), KeyUse.SIG, algorithm);
        if (key == null) {
            throw new SignatureException("Active key for " + algorithm + " not found");
        }
        return key;
    }

}
