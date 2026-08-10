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

import java.util.Set;

import org.keycloak.models.KeycloakSession;

/**
 * EdDSA 服务端签名 SPI 工厂。
 * <p>创建 {@link EdDSASignatureProvider}，并声明 OKP 私钥 JWK 所需 claims。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class EdDSASignatureProviderFactory implements SignatureProviderFactory {

    /** SPI 工厂标识：{@code EdDSA}。 */
    public static final String ID = Algorithm.EdDSA;

    @Override
    /** @return {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @param session 当前会话 @return EdDSA 签名提供者 */
    public SignatureProvider create(KeycloakSession session) {
        return new EdDSASignatureProvider(session);
    }

    @Override
    /** @return OKP 私钥 JWK 导出所需的 claim 集合 */
    public Set<String> getJwkPrivateKeyClaims() {
        return OKP_PRIVATE_JWK_CLAIMS;
    }

}
