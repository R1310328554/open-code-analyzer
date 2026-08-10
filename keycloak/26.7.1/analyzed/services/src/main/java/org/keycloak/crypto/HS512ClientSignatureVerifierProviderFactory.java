/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
 * HS512（HMAC-SHA）客户端 JWS 验签 SPI 工厂。
 * <p>注册 ID 为 {@link Algorithm#HS512}，创建 {@link MacSecretClientSignatureVerifierProvider}。</p>
 */
public class HS512ClientSignatureVerifierProviderFactory implements ClientSignatureVerifierProviderFactory {

    /** SPI 工厂标识：{@code HS512}。 */
    public static final String ID = Algorithm.HS512;

    @Override
    /** @return {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @param session 当前会话 @return HS512 客户端 HMAC 验签提供者 */
    public ClientSignatureVerifierProvider create(KeycloakSession session) {
        return new MacSecretClientSignatureVerifierProvider(session, Algorithm.HS512);
    }
}
