/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.credential;

import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.common.util.CollectionUtil;

import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.server.ServerProperty;

/**
 * WebAuthn 凭证输入模型：封装注册/认证流程中的 attestation 与 authentication 数据。
 * <p>实现 {@link CredentialInput}，供 {@link WebAuthnCredentialProvider} 转换与校验。</p>
 */
public class WebAuthnCredentialModelInput implements CredentialInput {

    /** 认证器 attestation 中的凭证数据（AAGUID、凭证 ID、COSE 公钥）。 */
    private AttestedCredentialData attestedCredentialData;
    private AttestationStatement attestationStatement;
    /** 认证阶段服务器属性与用户验证要求；不持久化，仅用于单次认证。 */
    private KeycloakWebAuthnAuthenticationParameters authenticationParameters; // not persisted because it can only be used on authentication operation.
    /** WebAuthn4J 认证请求；不持久化，仅用于单次认证校验。 */
    private AuthenticationRequest authenticationRequest; // not persisted because it can only be used on authentication operation.
    /** 认证器签名计数器（防克隆检测）。 */
    private long count;
    /** 持久化存储中的凭证主键 ID。 */
    private String credentialDBId;
    private final String credentialType;
    private String attestationStatementFormat;
    private Set<AuthenticatorTransport> transports;

    /** @param credentialType WebAuthn 凭证类型（双因素或无密码） */
    public WebAuthnCredentialModelInput(String credentialType) {
        this.credentialType = credentialType;
    }

    @Override
    public String getCredentialId() {
        return credentialDBId;
    }

    @Override
    /** WebAuthn 不使用 challenge-response 字符串，调用将抛出异常。 */
    public String getChallengeResponse() {
        throw new UnsupportedOperationException("WebAuthn credential doesn't support getChallengeResponse");
    }

    @Override
    public String getType() {
        return credentialType;
    }


    public AttestedCredentialData getAttestedCredentialData() {
        return attestedCredentialData;
    }

    public AttestationStatement getAttestationStatement() {
        return attestationStatement;
    }

    public long getCount() {
        return count;
    }

    public KeycloakWebAuthnAuthenticationParameters getAuthenticationParameters() {
        return authenticationParameters;
    }

    public void setAuthenticationParameters(KeycloakWebAuthnAuthenticationParameters authenticationParameters) {
        this.authenticationParameters = authenticationParameters;
    }

    public AuthenticationRequest getAuthenticationRequest() {
        return authenticationRequest;
    }

    public void setAuthenticationRequest(AuthenticationRequest authenticationRequest) {
        this.authenticationRequest = authenticationRequest;
    }

    public void setAttestedCredentialData(AttestedCredentialData attestedCredentialData) {
        this.attestedCredentialData = attestedCredentialData;
    }

    public void setAttestationStatement(AttestationStatement attestationStatement) {
        this.attestationStatement = attestationStatement;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getCredentialDBId() {
        return credentialDBId;
    }

    public void setCredentialDBId(String credentialDBId) {
        this.credentialDBId = credentialDBId;
    }

    public String getCredentialType() {
        return credentialType;
    }

    public String getAttestationStatementFormat() {
        return attestationStatementFormat;
    }

    public void setAttestationStatementFormat(String attestationStatementFormat) {
        this.attestationStatementFormat = attestationStatementFormat;
    }

    public Set<AuthenticatorTransport> getTransports() {
        return transports != null ? transports : Collections.emptySet();
    }

    public void setTransports(Set<AuthenticatorTransport> transports) {
        this.transports = transports;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Credential Type = " + credentialType + ",");
        if (credentialDBId != null)
            sb.append("Credential DB Id = ")
              .append(credentialDBId)
              .append(",");
        if (attestationStatement != null) {
            sb.append("Attestation Statement Format = ")
              .append(attestationStatement.getFormat())
              .append(",");
        } else if (attestationStatementFormat != null) {
            sb.append("Attestation Statement Format = ")
              .append(attestationStatementFormat)
              .append(",");
        }
        if (attestedCredentialData != null) {
            sb.append("AAGUID = ")
              .append(attestedCredentialData.getAaguid().toString())
              .append(",");
            sb.append("CREDENTIAL_ID = ")
              .append(Base64.getEncoder().encodeToString(attestedCredentialData.getCredentialId()))
              .append(",");
            COSEKey credPubKey = attestedCredentialData.getCOSEKey();
            byte[] keyId = credPubKey.getKeyId();
            if (keyId != null)
                sb.append("CREDENTIAL_PUBLIC_KEY.key_id = ")
                  .append(Base64.getEncoder().encodeToString(keyId))
                  .append(",");
            sb.append("CREDENTIAL_PUBLIC_KEY.algorithm = ")
              .append(String.valueOf(credPubKey.getAlgorithm().getValue()))
              .append(",");
            sb.append("CREDENTIAL_PUBLIC_KEY.key_type = ")
              .append(credPubKey.getKeyType().name())
              .append(",");
        }
        if (authenticationRequest != null) {
            // 仅在认证流程中设置
            sb.append("Credential Id = ")
              .append(Base64.getEncoder().encodeToString(authenticationRequest.getCredentialId()))
              .append(",");
        }
        if (CollectionUtil.isNotEmpty(getTransports())) {
            final String transportsString = getTransports().stream()
                    .map(AuthenticatorTransport::getValue)
                    .collect(Collectors.joining(","));

            sb.append("Transports = [")
              .append(transportsString)
              .append("],");
        }
        if (sb.length() > 0)
            sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    /** Keycloak 侧 WebAuthn 认证参数：服务器属性与用户验证策略。 */
    public static class KeycloakWebAuthnAuthenticationParameters{

        private final ServerProperty serverProperty;
        private final boolean userVerificationRequired;

        /** @param serverProperty RP 侧 challenge/origin 等服务器属性
         *  @param userVerificationRequired 是否要求用户验证（UV） */
        public KeycloakWebAuthnAuthenticationParameters(ServerProperty serverProperty, boolean userVerificationRequired) {
            this.serverProperty = serverProperty;
            this.userVerificationRequired = userVerificationRequired;
        }

        public ServerProperty getServerProperty() {
            return serverProperty;
        }

        public boolean isUserVerificationRequired() {
            return userVerificationRequired;
        }
    }
}
