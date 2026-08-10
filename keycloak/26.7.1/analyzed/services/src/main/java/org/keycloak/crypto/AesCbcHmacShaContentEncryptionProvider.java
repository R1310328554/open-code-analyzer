/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.jose.jwe.enc.AesCbcHmacShaJWEEncryptionProvider;
import org.keycloak.jose.jwe.enc.JWEEncryptionProvider;
import org.keycloak.models.KeycloakSession;

/**
 * AES-CBC + HMAC-SHA 复合内容加密提供者。
 * <p>按 JWE 算法名（如 A128CBC-HS256）委托 {@link AesCbcHmacShaJWEEncryptionProvider} 执行加解密。</p>
 */
public class AesCbcHmacShaContentEncryptionProvider implements ContentEncryptionProvider {

    /** 当前 Keycloak 会话（SPI 生命周期绑定）。 */
    private final KeycloakSession session;
    /** JWE 内容加密算法标识，如 A256CBC-HS512。 */
    private final String jweAlgorithmName;

    /** @param session 当前会话 @param jweAlgorithmName JWA 内容加密算法名 */
    public AesCbcHmacShaContentEncryptionProvider(KeycloakSession session, String jweAlgorithmName) {
        this.session = session;
        this.jweAlgorithmName = jweAlgorithmName;
    }

    @Override
    /** @return 对应算法名的 AES-CBC+HMAC JWE 加密实现 */
    public JWEEncryptionProvider jweEncryptionProvider() {
        return new AesCbcHmacShaJWEEncryptionProvider(jweAlgorithmName);
    }

}
