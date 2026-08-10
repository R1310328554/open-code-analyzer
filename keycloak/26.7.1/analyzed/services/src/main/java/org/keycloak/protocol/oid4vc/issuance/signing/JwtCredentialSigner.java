/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.issuance.signing;

import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oid4vc.issuance.credentialbuilder.CredentialBody;
import org.keycloak.protocol.oid4vc.issuance.credentialbuilder.JwtCredentialBody;
import org.keycloak.protocol.oid4vc.model.CredentialBuildConfig;

import org.jboss.logging.Logger;

/**
 * 实现 JWT VC（{@code jwt_vc}）格式的 {@link CredentialSigner}。
 * <p>返回已签名的 JWT 凭证字符串，供凭证端点交付。</p>
 * {@see https://identity.foundation/jwt-vc-presentation-profile/}
 */
public class JwtCredentialSigner extends AbstractCredentialSigner<String> {

    private static final Logger LOGGER = Logger.getLogger(JwtCredentialSigner.class);

    /** @param keycloakSession 当前 Keycloak 会话 */
    public JwtCredentialSigner(KeycloakSession keycloakSession) {
        super(keycloakSession);
    }

    /**
     * 对 {@link JwtCredentialBody} 执行 JWS 签名。
     *
     * @param credentialBody        JWT 凭证体
     * @param credentialBuildConfig 签名配置
     * @return 紧凑序列化的已签名 JWT 字符串
     */
            throws CredentialSignerException {
        if (!(credentialBody instanceof JwtCredentialBody jwtCredentialBody)) {
            throw new CredentialSignerException("Credential body unexpectedly not of type JwtCredentialBody");
        }

        LOGGER.debugf("Sign credentials to jwt-vc format.");
        return jwtCredentialBody.sign(getSigner(credentialBuildConfig));
    }
}
