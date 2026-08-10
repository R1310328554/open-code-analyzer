/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.protocol.oid4vc.issuance.keybinding;

import org.keycloak.models.KeycloakSession;

/**
 * 创建 {@link JwtCNonceHandler} 实例的工厂。
 *
 * @author Pascal Knüppel
 */
public class JwtCNonceHandlerFactory implements CNonceHandlerFactory {

    /** 提供方 ID，用于 SPI 配置选择实现。 */
    public static final String PROVIDER_ID = "oid4vci-jwt-c-nonce-builder";

    /** 为当前会话创建 JWT c_nonce 处理器。 */
    @Override
    public CNonceHandler create(KeycloakSession session) {
        return new JwtCNonceHandler(session);
    }

    /** @return 提供方标识 {@value #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
