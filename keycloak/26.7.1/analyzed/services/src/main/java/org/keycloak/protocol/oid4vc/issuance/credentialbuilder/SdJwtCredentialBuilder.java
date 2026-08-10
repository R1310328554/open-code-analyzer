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

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.keycloak.VCFormat;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.model.CredentialBuildConfig;
import org.keycloak.protocol.oid4vc.model.CredentialSubject;
import org.keycloak.protocol.oid4vc.model.SupportedCredentialConfiguration;
import org.keycloak.protocol.oid4vc.model.VerifiableCredential;
import org.keycloak.sdjwt.DisclosureSpec;
import org.keycloak.sdjwt.IssuerSignedJWT;
import org.keycloak.sdjwt.SdJwt;
import org.keycloak.sdjwt.SdJwtUtils;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.keycloak.OID4VCConstants.CLAIM_NAME_EXP;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_IAT;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_ISSUER;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_JTI;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_SUB;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_SUBJECT_ID;
import static org.keycloak.OID4VCConstants.CLAIM_NAME_VCT;

/**
 * SD-JWT-VC（{@code dc+sd-jwt}）格式凭证构建器。
 * <p>将 subject 声明映射为 JWT 载荷，按配置进行选择性披露、诱饵声明及 HAIP 兼容字段填充。</p>
 */
public class SdJwtCredentialBuilder implements CredentialBuilder {

    /** 默认构造。 */
    public SdJwtCredentialBuilder() {
    }

    /** {@inheritDoc} 返回 {@link VCFormat#SD_JWT_VC}。 */
    @Override
    public String getSupportedFormat() {
        return VCFormat.SD_JWT_VC;
    }

    @Override
    public SdJwtCredentialBody buildCredentialBody(
            VerifiableCredential verifiableCredential,
            CredentialBuildConfig credentialBuildConfig
    ) throws CredentialBuilderException {

        URI vcId = verifiableCredential.getId();
        Instant issuanceDate = verifiableCredential.getIssuanceDate();
        Instant expirationDate = verifiableCredential.getExpirationDate();

        // 读取 subject 声明
        CredentialSubject credentialSubject = verifiableCredential.getCredentialSubject();
        Map<String, Object> claims = new LinkedHashMap<>(credentialSubject.getClaims());

        // 将 subject id 映射为 JWT sub 声明
        Optional.ofNullable(claims.remove(CLAIM_NAME_SUBJECT_ID)).ifPresent(it ->
                claims.put(CLAIM_NAME_SUB, it)
        );

        // 始终写入 jti（凭证 ID）
        claims.put(CLAIM_NAME_JTI, vcId != null ? vcId : UUID.randomUUID().toString());

        Optional.ofNullable(issuanceDate).ifPresent(it ->
                claims.put(CLAIM_NAME_IAT, it.getEpochSecond())
        );

        // 除配置为可见的声明外，其余纳入选择性披露规范
        DisclosureSpec.Builder disclosureSpecBuilder = DisclosureSpec.builder();
        claims.entrySet()
                .stream()
                .filter(entry -> !credentialBuildConfig.getSdJwtVisibleClaims().contains(entry.getKey()))
                .forEach(entry -> {
                    if (entry instanceof List<?> listValue) {
                        // FIXME：不可达分支；原意可能是检查 entry.getValue()，直接修改会破坏多项测试，需进一步讨论期望行为

                        IntStream.range(0, listValue.size())
                                .forEach(i -> disclosureSpecBuilder
                                        .withUndisclosedArrayElt(entry.getKey(), i, SdJwtUtils.randomSalt())
                                );
                    } else {
                        disclosureSpecBuilder.withUndisclosedClaim(entry.getKey(), SdJwtUtils.randomSalt());
                    }
                });

        // 填充必可见的配置字段（issuer、vct）
        claims.put(CLAIM_NAME_ISSUER, credentialBuildConfig.getCredentialIssuer());
        claims.put(CLAIM_NAME_VCT, credentialBuildConfig.getCredentialType());

        // 从 VC 过期时间设置 exp（可选，但 HAIP 建议设置）；仅当协议映射器未预先设置时写入
        if (!claims.containsKey(CLAIM_NAME_EXP) && expirationDate != null) {
            claims.put(CLAIM_NAME_EXP, expirationDate.getEpochSecond());
        }

        // jti、nbf、iat 均为可选，需时由协议映射器设置

        // 添加配置的诱饵（decoy）声明数量
        if (credentialBuildConfig.getNumberOfDecoys() > 0) {
            IntStream.range(0, credentialBuildConfig.getNumberOfDecoys())
                    .forEach(i -> disclosureSpecBuilder.withDecoyClaim(SdJwtUtils.randomSalt()));
        }

        ObjectNode claimsNode = JsonSerialization.mapper.convertValue(claims, ObjectNode.class);
        IssuerSignedJWT issuerSignedJWT = IssuerSignedJWT.builder()
                                                         .withClaims(claimsNode,
                                                                     disclosureSpecBuilder.build())
                                                         .withHashAlg(credentialBuildConfig.getHashAlgorithm())
                                                         .withJwsType(credentialBuildConfig.getTokenJwsType())
                                                         .build();
        SdJwt.Builder sdJwtBuilder = SdJwt.builder();

        return new SdJwtCredentialBody(sdJwtBuilder, issuerSignedJWT);
    }

    /** {@inheritDoc} 设置 SD-JWT 格式的 {@code vct} 元数据。 */
    @Override
    public void contributeToMetadata(SupportedCredentialConfiguration credentialConfig, CredentialScopeModel credentialScope) {
        String vct = Optional.ofNullable(credentialScope.getVct()).orElse(credentialScope.getName());
        credentialConfig.setVct(vct);
    }
}
