/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authentication.authenticators.client;

import org.keycloak.authentication.ClientAuthenticationFlowContext;

/**
 * 客户端密钥 JWT 校验器：在 {@link JWTClientValidator} 基础上允许对称签名算法。
 */
public class JWTClientSecretValidator extends JWTClientValidator {

    /**
     * 构造客户端密钥 JWT 校验器。
     *
     * @param context 客户端认证流程上下文
     * @param signatureValidator 签名校验回调
     * @param clientAuthenticatorProviderId 认证器提供者 ID
     */
    public JWTClientSecretValidator(ClientAuthenticationFlowContext context, SignatureValidator signatureValidator, String clientAuthenticatorProviderId) throws Exception {
        super(context, signatureValidator, clientAuthenticatorProviderId);
    }

    /** @return 允许对称签名算法（HMAC） */
    @Override
    protected boolean isSymmetricAlgorithmAllowed() {
        return true;
    }
}
