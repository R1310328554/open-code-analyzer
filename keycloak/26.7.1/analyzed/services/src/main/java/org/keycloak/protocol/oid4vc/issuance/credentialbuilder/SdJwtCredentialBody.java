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
import org.keycloak.sdjwt.IssuerSignedJWT;
import org.keycloak.sdjwt.SdJwt;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.keycloak.OID4VCConstants.CLAIM_NAME_CNF;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_JWK;

/**
 * SD-JWT-VC（{@code dc+sd-jwt}）格式的未完成凭证体。
 * <p>持有 {@link SdJwt.Builder} 与 {@link IssuerSignedJWT}，支持密钥绑定与最终签名。</p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class SdJwtCredentialBody implements CredentialBody {

    /** SD-JWT 顶层构建器。 */
    private final SdJwt.Builder sdJwtBuilder;
    /** 签发者已签名 JWT 部分（含选择性披露规范）。 */
    private final IssuerSignedJWT issuerSignedJWT;

    /**
     * @param sdJwtBuilder SD-JWT 构建器
     * @param issuerSignedJWT 签发者 JWT 部分
     */
    public SdJwtCredentialBody(SdJwt.Builder sdJwtBuilder, IssuerSignedJWT issuerSignedJWT) {
        this.sdJwtBuilder = sdJwtBuilder;
        this.issuerSignedJWT = issuerSignedJWT;
    }

    /** {@inheritDoc} 在 {@code cnf.jwk} 中写入持有者公钥实现密钥绑定。 */
    public void addKeyBinding(JWK jwk) throws CredentialBuilderException {
        ObjectNode jwkNode = JsonSerialization.mapper.convertValue(jwk, ObjectNode.class);
        ObjectNode keyBindingNode = JsonSerialization.mapper.createObjectNode();
        keyBindingNode.set(CLAIM_NAME_JWK, jwkNode);
        issuerSignedJWT.getPayload().set(CLAIM_NAME_CNF, keyBindingNode);
    }

    /** @return 签发者 JWT 部分 */
    public IssuerSignedJWT getIssuerSignedJWT() {
        return issuerSignedJWT;
    }

    /**
     * 完成 SD-JWT 签发者签名并序列化为 SD-JWT 字符串。
     * @param signatureSignerContext 签名上下文
     * @return 紧凑 SD-JWT 表示
     */
    public String sign(SignatureSignerContext signatureSignerContext) {
        SdJwt sdJwt = sdJwtBuilder.withIssuerSignedJwt(issuerSignedJWT)
                .withIssuerSigningContext(signatureSignerContext)
                .build();

        return sdJwt.toSdJwtString();
    }
}
