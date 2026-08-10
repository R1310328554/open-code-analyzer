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

import org.keycloak.jose.jwe.JWEConstants;
import org.keycloak.models.KeycloakSession;

/**
 * JWE 密钥管理算法 RSAES-PKCS1-v1_5 的 SPI 工厂。
 * <p>算法 ID 为 {@link #ID}（JWA {@code RSA1_5}），创建 {@link RsaCekManagementProvider} 实例。</p>
 */
public class RsaesPkcs1CekManagementProviderFactory implements CekManagementProviderFactory {

    /** JWE 密钥管理算法标识：RSA1_5。 */
    public static final String ID = JWEConstants.RSA1_5;

    @Override
    /** @return {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @param session 当前会话 @return RSA CEK 管理提供者 */
    public CekManagementProvider create(KeycloakSession session) {
        return new RsaCekManagementProvider(session, ID);
    }

}
