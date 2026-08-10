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

import java.util.Set;

import org.keycloak.models.KeycloakSession;

/**
 * ES256 服务端签名 SPI 工厂。
 * <p>创建 {@link ECDSASignatureProvider}，并声明 EC 私钥 JWK 所需 claims。</p>
 */
public class ES256SignatureProviderFactory implements SignatureProviderFactory {

    /** SPI 工厂标识：{@code ES256}。 */
    public static final String ID = Algorithm.ES256;

    @Override
    /** @return {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @param session 当前会话 @return ES256 签名提供者 */
    public SignatureProvider create(KeycloakSession session) {
        return new ECDSASignatureProvider(session, Algorithm.ES256);
    }

    @Override
    /** @return EC 私钥 JWK 导出所需的 claim 集合 */
    public Set<String> getJwkPrivateKeyClaims() {
        return EC_PRIVATE_JWK_CLAIMS;
    }

}
