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

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.jose.jwe.JWEConstants;
import org.keycloak.jose.jwe.alg.JWEAlgorithmProvider;
import org.keycloak.models.KeycloakSession;

/**
 * JWE 密钥管理提供者：基于 ECDH-ES 的内容加密密钥协商。
 * <p>按 JWE 算法名（ECDH-ES 或 ECDH-ES+A*KW）委托底层 {@link JWEAlgorithmProvider} 执行密钥封装。</p>
 */
public class EcdhEsCekManagementProvider implements CekManagementProvider {

    /** 当前 Keycloak 会话（SPI 生命周期绑定）。 */
    private final KeycloakSession session;
    /** JWE 密钥管理算法标识（如 ECDH-ES+A256KW）。 */
    private final String jweAlgorithmName;

    /** @param session 当前会话 @param jweAlgorithmName JWA 密钥管理算法名 */
    public EcdhEsCekManagementProvider(KeycloakSession session, String jweAlgorithmName) {
        this.session = session;
        this.jweAlgorithmName = jweAlgorithmName;
    }

    @Override
    /** @return 对应 ECDH-ES 变体的 JWE 算法提供者；不支持的算法名返回 null */
    public JWEAlgorithmProvider jweAlgorithmProvider() {
        if (JWEConstants.ECDH_ES.equals(jweAlgorithmName) || JWEConstants.ECDH_ES_A128KW.equals(jweAlgorithmName)
                || JWEConstants.ECDH_ES_A192KW.equals(jweAlgorithmName)
                || JWEConstants.ECDH_ES_A256KW.equals(jweAlgorithmName)) {
            return CryptoIntegration.getProvider().getAlgorithmProvider(JWEAlgorithmProvider.class, jweAlgorithmName);
        } else {
            return null;
        }
    }

}
