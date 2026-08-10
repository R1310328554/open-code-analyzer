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

import org.keycloak.models.KeycloakSession;

/**
 * 服务端 EdDSA（OKP）JWS 签名上下文。
 * <p>复用 {@link ServerAsymmetricSignatureSignerContext#getKey} 解析 Realm 活动 OKP 密钥。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ServerEdDSASignatureSignerContext extends AsymmetricSignatureSignerContext {

    /** @param session 当前会话 @param algorithm EdDSA 算法名 */
    public ServerEdDSASignatureSignerContext(KeycloakSession session, String algorithm) throws SignatureException {
        super(ServerAsymmetricSignatureSignerContext.getKey(session, algorithm));
    }

    /** @param key 已解析的 OKP 签名私钥包装 */
    public ServerEdDSASignatureSignerContext(KeyWrapper key) {
        super(key);
    }
}
