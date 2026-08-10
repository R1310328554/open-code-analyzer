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

package org.keycloak.protocol.oid4vc.issuance.credentialbuilder;

import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jws.JWSBuilder;

import org.jboss.logging.Logger;

/**
 * JWT-VC 格式的未完成凭证体，封装待签名的 {@link JWSBuilder.EncodingBuilder}。
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class JwtCredentialBody implements CredentialBody {

    private static final Logger LOGGER = Logger.getLogger(JwtCredentialBody.class);

    /** 待签名的 JWS 编码构建器。 */
    private final JWSBuilder.EncodingBuilder jwsEncodingBuilder;

    /** @param jwsEncodingBuilder 已填充载荷的 JWS 构建器 */
    public JwtCredentialBody(JWSBuilder.EncodingBuilder jwsEncodingBuilder) {
        this.jwsEncodingBuilder = jwsEncodingBuilder;
    }

    /** {@inheritDoc} JWT-VC 密钥绑定尚未实现，仅记录警告。 */
    public void addKeyBinding(JWK jwk) throws CredentialBuilderException {
        LOGGER.warnf("Key binding is not yet implemented for JWT credentials");
    }

    /**
     * 使用签发者签名上下文完成 JWS 签名。
     * @param signatureSignerContext 签名上下文
     * @return 紧凑序列化 JWT 字符串
     */
    public String sign(SignatureSignerContext signatureSignerContext) {
        return jwsEncodingBuilder.sign(signatureSignerContext);
    }
}
