/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

import java.util.Set;

import org.keycloak.models.KeycloakSession;

/**
 * PS256（RSA-PSS SHA-256）服务端签名 SPI 工厂。
 * <p>创建 {@link AsymmetricSignatureProvider}，并声明 RSA 私钥 JWK 所需 claims。</p>
 */
public class PS256SignatureProviderFactory implements SignatureProviderFactory {

    /** SPI 工厂标识：{@code PS256}。 */
    public static final String ID = Algorithm.PS256;

    @Override
    /** @return {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @param session 当前会话 @return PS256 RSA-PSS 签名提供者 */
    public SignatureProvider create(KeycloakSession session) {
        return new AsymmetricSignatureProvider(session, Algorithm.PS256);
    }

    @Override
    /** @return RSA 私钥 JWK 导出所需的 claim 集合 */
    public Set<String> getJwkPrivateKeyClaims() {
        return RSA_PRIVATE_JWK_CLAIMS;
    }

}
