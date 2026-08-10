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
package org.keycloak.protocol.oid4vc.issuance;

import java.util.List;

import org.keycloak.jose.jwk.JWK;
import org.keycloak.protocol.oid4vc.issuance.credentialbuilder.CredentialBody;
import org.keycloak.protocol.oid4vc.model.CredentialRequest;
import org.keycloak.protocol.oid4vc.model.SupportedCredentialConfiguration;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;
import org.keycloak.services.managers.AuthenticationManager;

/**
 * 封装待签名的可验证凭证及签发上下文信息。
 * <p>将签发所需数据与 {@link VerifiableCredential} POJO 分离，避免序列化时意外泄露内部字段。</p>
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class VCIssuanceContext {

    /** 格式特定的凭证体（待签名内容）。 */
    private CredentialBody credentialBody;
    /** 已解析的凭证配置元数据。 */
    private SupportedCredentialConfiguration credentialConfig;
    /** 客户端提交的凭证请求（含 proof 等）。 */
    private CredentialRequest credentialRequest;
    /** 认证结果（用户会话与访问令牌）。 */
    private AuthenticationManager.AuthResult authResult;

    /** 证明验证后绑定的公钥列表。 */
    private List<JWK> attestedKeys;


    /** @return 格式特定的凭证体 */
    public CredentialBody getCredentialBody() {
        return credentialBody;
    }

    /** @param credentialBody 凭证体 @return 当前上下文（链式调用） */
    public VCIssuanceContext setCredentialBody(CredentialBody credentialBody) {
        this.credentialBody = credentialBody;
        return this;
    }

    /** @return 凭证配置 */
    public SupportedCredentialConfiguration getCredentialConfig() {
        return credentialConfig;
    }

    /** @param credentialConfig 凭证配置 @return 当前上下文 */
    public VCIssuanceContext setCredentialConfig(SupportedCredentialConfiguration credentialConfig) {
        this.credentialConfig = credentialConfig;
        return this;
    }

    /** @return 凭证请求对象 */
    public CredentialRequest getCredentialRequest() {
        return credentialRequest;
    }

    /** @param credentialRequest 凭证请求 @return 当前上下文 */
    public VCIssuanceContext setCredentialRequest(CredentialRequest credentialRequest) {
        this.credentialRequest = credentialRequest;
        return this;
    }

    /** @return 认证结果 */
    public AuthenticationManager.AuthResult getAuthResult() {
        return authResult;
    }

    /** @param authResult 认证结果 @return 当前上下文 */
    public VCIssuanceContext setAuthResult(AuthenticationManager.AuthResult authResult) {
        this.authResult = authResult;
        return this;
    }

    /** @param attestedKeys 证明绑定的公钥 @return 当前上下文 */
    public VCIssuanceContext setAttestedKeys(List<JWK> attestedKeys) {
        this.attestedKeys = attestedKeys;
        return this;
    }
}
