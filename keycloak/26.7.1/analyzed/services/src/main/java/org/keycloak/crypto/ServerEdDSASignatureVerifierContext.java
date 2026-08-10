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
 * 服务端 EdDSA（OKP）JWS 验签上下文。
 * <p>按 kid 解析 Realm OKP 公钥，或直接使用 {@link KeyWrapper} 校验签名。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ServerEdDSASignatureVerifierContext extends  AsymmetricSignatureVerifierContext {
    /** @param session 当前会话 @param kid 密钥 ID @param algorithm EdDSA 算法名 */
    public ServerEdDSASignatureVerifierContext(KeycloakSession session, String kid, String algorithm) throws VerificationException {
        super(ServerAsymmetricSignatureVerifierContext.getKey(session, kid, algorithm));
    }

    /** @param key 已解析的 OKP 验签公钥包装 */
    public ServerEdDSASignatureVerifierContext(KeyWrapper key) {
        super(key);
    }
}
