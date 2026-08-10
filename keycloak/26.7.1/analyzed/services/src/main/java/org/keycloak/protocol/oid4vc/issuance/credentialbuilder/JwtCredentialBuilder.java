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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.keycloak.VCFormat;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.issuance.TimeClaimNormalizer;
import org.keycloak.protocol.oid4vc.issuance.TimeProvider;
import org.keycloak.protocol.oid4vc.model.CredentialBuildConfig;
import org.keycloak.protocol.oid4vc.model.CredentialDefinition;
import org.keycloak.protocol.oid4vc.model.CredentialSubject;
import org.keycloak.protocol.oid4vc.model.SupportedCredentialConfiguration;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;
import org.keycloak.representations.JsonWebToken;

import static org.keycloak.OID4VCConstants.CLAIM_NAME_SUBJECT_ID;

/**
 * JWT-VC（{@code jwt_vc_json}）格式凭证构建器。
 * <p>将 {@link VerifiableCredential} 嵌入 JWT 的 {@code vc} 声明，并设置 {@code iss}、{@code nbf}、{@code sub} 等标准字段。</p>
 */
public class JwtCredentialBuilder implements CredentialBuilder {

    /** JWT 载荷中嵌入 VC 的声明键名。 */
    private static final String VC_CLAIM_KEY = "vc";

    /** 时间源，用于默认签发时间。 */
    private final TimeProvider timeProvider;
    /** 签发时间归一化函数（Realm 时区等）。 */
    private final UnaryOperator<Instant> issuanceTimeNormalizer;

    /** 测试用构造：未提供 Session 时默认签发时间归一化将抛异常。 */
    public JwtCredentialBuilder(TimeProvider timeProvider) {
        this(timeProvider, instant -> {
            throw new IllegalStateException("KeycloakSession must not be null when defaulting issuance time");
        });
    }

    /**
     * 生产构造：使用 {@link TimeClaimNormalizer} 归一化默认签发时间。
     * @param timeProvider 时间源
     * @param session Keycloak 会话
     */
    public JwtCredentialBuilder(TimeProvider timeProvider, KeycloakSession session) {
        this(timeProvider, new TimeClaimNormalizer(
                Objects.requireNonNull(session, "KeycloakSession must not be null when defaulting issuance time")
        )::normalize);
    }

    /**
     * 可注入归一化逻辑的构造。
     * @param timeProvider 时间源
     * @param issuanceTimeNormalizer 签发时间归一化器
     */
    public JwtCredentialBuilder(TimeProvider timeProvider, UnaryOperator<Instant> issuanceTimeNormalizer) {
        this.timeProvider = Objects.requireNonNull(timeProvider, "TimeProvider must not be null");
        this.issuanceTimeNormalizer = Objects.requireNonNull(issuanceTimeNormalizer, "Issuance time normalizer must not be null");
    }

    /** {@inheritDoc} 返回 {@link VCFormat#JWT_VC}。 */
    @Override
    public String getSupportedFormat() {
        return VCFormat.JWT_VC;
    }

    @Override
    public JwtCredentialBody buildCredentialBody(
            VerifiableCredential verifiableCredential,
            CredentialBuildConfig credentialBuildConfig
    ) throws CredentialBuilderException {
        verifiableCredential.setType(getCredentialTypes(verifiableCredential.getType()));

        // 填充 VC 的 issuer 字段
        verifiableCredential.setIssuer(credentialBuildConfig.getCredentialIssuer());

        // 读取签发时间；nbf 为必填，缺失时使用当前时间并归一化（VC 自带 issuanceDate 则不再归一化）
        Instant issuanceInstant = Optional.ofNullable(verifiableCredential.getIssuanceDate())
                .orElseGet(() -> {
                    Instant defaultTime = Instant.ofEpochSecond(timeProvider.currentTimeSeconds());
                    return issuanceTimeNormalizer.apply(defaultTime);
                });

        long iat = issuanceInstant.getEpochSecond();

        // 设置必填 JWT 字段
        JsonWebToken jsonWebToken = new JsonWebToken()
                .issuer(verifiableCredential.getIssuer().toString())
                .nbf(iat)
                .id(CredentialBuilderUtils.createCredentialId(verifiableCredential));
        jsonWebToken.setOtherClaims(VC_CLAIM_KEY, verifiableCredential);

        // exp 为可选字段
        Optional.ofNullable(verifiableCredential.getExpirationDate())
                .ifPresent(d -> jsonWebToken.exp(d.getEpochSecond()));

        // 仅当 credential subject 含 id 时设置 sub
        CredentialSubject subject = verifiableCredential.getCredentialSubject();
        Optional.ofNullable(subject.getClaims().get(CLAIM_NAME_SUBJECT_ID))
                .map(Object::toString)
                .ifPresent(jsonWebToken::subject);

        JWSBuilder.EncodingBuilder jwsBuilder = new JWSBuilder()
                .type(credentialBuildConfig.getTokenJwsType())
                .jsonContent(jsonWebToken);

        return new JwtCredentialBody(jwsBuilder);
    }

    private static List<String> getCredentialTypes(List<String> credentialTypes) {
        List<String> types = new ArrayList<>(Optional.ofNullable(credentialTypes).orElseGet(List::of));
        if (!types.contains(CredentialDefinition.VERIFIABLE_CREDENTIAL_TYPE)) {
            types.add(0, CredentialDefinition.VERIFIABLE_CREDENTIAL_TYPE);
        }
        return types;
    }

    @Override
    public void contributeToMetadata(SupportedCredentialConfiguration credentialConfig, CredentialScopeModel credentialScope) {
        CredentialDefinition credentialDefinition = CredentialDefinition.parse(credentialScope);
        // OID4VCI 规范：jwt_vc_json 不得包含 @context（该字段仅适用于 ldp_vc）
        credentialDefinition.setContext(null);
        credentialConfig.setCredentialDefinition(credentialDefinition);
    }
}
