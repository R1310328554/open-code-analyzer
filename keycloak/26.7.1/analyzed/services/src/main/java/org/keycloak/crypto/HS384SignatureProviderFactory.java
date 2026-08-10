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
 * HS384（HMAC-SHA384）服务端签名 SPI 工厂。
 * <p>创建 {@link MacSecretSignatureProvider}，并声明 oct JWK 私钥所需的 "k" claim。</p>
 */
public class HS384SignatureProviderFactory implements SignatureProviderFactory {

    /** SPI 工厂标识：{@code HS384}。 */
    public static final String ID = Algorithm.HS384;

    @Override
    /** @return {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @param session 当前会话 @return HS384 HMAC 签名提供者 */
    public SignatureProvider create(KeycloakSession session) {
        return new MacSecretSignatureProvider(session, Algorithm.HS384);
    }

    /**
     * 返回对称算法（oct JWK）私钥材料对应的 "k" claim。
     * @return oct 私钥 JWK 导出所需的 claim 集合
     */
    @Override
    public Set<String> getJwkPrivateKeyClaims() {
        return OCT_PRIVATE_JWK_CLAIMS;
    }

}
