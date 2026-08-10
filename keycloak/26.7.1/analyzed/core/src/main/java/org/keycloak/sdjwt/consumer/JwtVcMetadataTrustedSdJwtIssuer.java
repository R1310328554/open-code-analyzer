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

package org.keycloak.sdjwt.consumer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.sdjwt.IssuerSignedJWT;
import org.keycloak.sdjwt.JwkParsingUtils;
import org.keycloak.sdjwt.SdJwtUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import static org.keycloak.OID4VCConstants.CLAIM_NAME_ISSUER;
import static org.keycloak.OID4VCConstants.JWT_VC_ISSUER_END_POINT;

/**
 * 用于 SD-JWT VP 验证的可信签发者实现。
 *
 * <p>
 * 面向在规范化 JWT VC 签发者元数据端点上公开验证密钥的签发者。
 * </p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-oauth-sd-jwt-vc-05#name-issuer-signed-jwt-verificat">
 * JWT VC Issuer Metadata
 * </a>
 */
public class JwtVcMetadataTrustedSdJwtIssuer implements TrustedSdJwtIssuer {

    /** 匹配可信签发者 URI 的正则模式。 */
    private final Pattern issuerUriPattern;
    /** HTTP 数据获取器。 */
    private final HttpDataFetcher httpDataFetcher;

    /**
     * @param issuerUri 可信签发者 URI
     */
    public JwtVcMetadataTrustedSdJwtIssuer(String issuerUri, HttpDataFetcher httpDataFetcher) {
        try {
            validateHttpsIssuerUri(issuerUri);
        } catch (VerificationException e) {
            throw new IllegalArgumentException(e);
        }

        // 构建仅匹配指定 URI 的正则模式
        this.issuerUriPattern = Pattern.compile(Pattern.quote(issuerUri));

        // 注入 HttpDataFetcher 实现
        this.httpDataFetcher = httpDataFetcher;
    }

    /**
     * @param issuerUriPattern 可信签发者 URI 的正则模式
     */
    public JwtVcMetadataTrustedSdJwtIssuer(Pattern issuerUriPattern, HttpDataFetcher httpDataFetcher) {
        this.issuerUriPattern = issuerUriPattern;
        this.httpDataFetcher = httpDataFetcher;
    }

    @Override
    public List<SignatureVerifierContext> resolveIssuerVerifyingKeys(IssuerSignedJWT issuerSignedJWT)
            throws VerificationException {
        // 读取 iss（声明）与 kid（头）
        String iss = Optional.ofNullable(issuerSignedJWT.getPayload().get(CLAIM_NAME_ISSUER))
                .map(JsonNode::asText)
                .orElse("");
        String kid = issuerSignedJWT.getJwsHeader().getKeyId();

        // 将 iss 声明与可信模式匹配
        Matcher matcher = issuerUriPattern.matcher(iss);
        if (!matcher.matches()) {
            throw new VerificationException(String.format(
                    "Unexpected Issuer URI claim. Expected=/%s/, Got=%s",
                    issuerUriPattern.pattern(), iss
            ));
        }

        // 按规范，仅支持 HTTPS URI
        validateHttpsIssuerUri(iss);

        // 获取公开的 JWK
        List<JWK> jwks = fetchIssuerMetadataJwks(iss);
        if (jwks.isEmpty()) {
            throw new VerificationException(
                    String.format("Issuer JWKs were unexpectedly resolved to an empty list. Issuer URI: %s", iss)
            );
        }

        // 若指定 kid，仅考虑匹配的（单个）密钥
        if (kid != null) {
            List<JWK> matchingJwks = jwks.stream()
                    .filter(jwk -> {
                        String jwkKid = jwk.getKeyId();
                        return jwkKid != null && jwkKid.equals(kid);
                    })
                    .collect(Collectors.toList());

            if (matchingJwks.isEmpty()) {
                throw new VerificationException(
                        String.format("No published JWK was found to match kid: %s", kid)
                );
            }

            if (matchingJwks.size() > 1) {
                throw new VerificationException(
                        String.format("Cannot choose between multiple exposed JWKs with same kid: %s", kid)
                );
            }

            jwks = Collections.singletonList(matchingJwks.get(0));
        }

        // 构建 SignatureVerifierContext 列表
        List<SignatureVerifierContext> verifiers = new ArrayList<>();
        for (JWK jwk : jwks) {
            try {
                verifiers.add(JwkParsingUtils.convertJwkToVerifierContext(jwk));
            } catch (Exception e) {
                throw new VerificationException("A potential JWK was retrieved but found invalid", e);
            }
        }

        return verifiers;
    }

    /** 校验签发者 URI 使用 HTTPS。 */
    private void validateHttpsIssuerUri(String issuerUri) throws VerificationException {
        if (!issuerUri.startsWith("https://")) {
            throw new VerificationException(
                    "HTTPS URI required to retrieve JWT VC Issuer Metadata"
            );
        }
    }

    /** 从 JWT VC 签发者元数据获取 JWK 列表。 */
    private List<JWK> fetchIssuerMetadataJwks(String issuerUri) throws VerificationException {
        // 按 draft-ietf-oauth-sd-jwt-vc-13 构建 JWT VC 元数据端点完整 URL
        String normalizedIssuerUri = normalizeUri(issuerUri);
        String jwtVcIssuerUri = buildJwtVcIssuerMetadataUri(normalizedIssuerUri);

        // 获取并解析元数据

        JwtVcMetadata issuerMetadata;
        JsonNode issuerMetadataNode = fetchData(jwtVcIssuerUri);

        try {
            issuerMetadata = SdJwtUtils.mapper.treeToValue(issuerMetadataNode, JwtVcMetadata.class);
        } catch (JsonProcessingException e) {
            throw new VerificationException("Failed to parse exposed JWT VC Metadata", e);
        }

        // 验证元数据

        String exposedIssuerUri = normalizeUri(issuerMetadata.getIssuer());

        if (!normalizedIssuerUri.equals(exposedIssuerUri)) {
            throw new VerificationException(String.format(
                    "Unexpected metadata's issuer. Expected=%s, Got=%s",
                    normalizedIssuerUri, exposedIssuerUri
            ));
        }

        // 提取公开的 JWKS（必要时解引用）

        String jwksUri = issuerMetadata.getJwksUri();
        JSONWebKeySet jwks = issuerMetadata.getJwks();

        if (jwks == null && jwksUri != null) {
            // 解引用 JWKS URI
            JsonNode jwksNode = fetchData(jwksUri);

            // 解析获取的 JWKS
            try {
                jwks = SdJwtUtils.mapper.treeToValue(jwksNode, JSONWebKeySet.class);
            } catch (JsonProcessingException e) {
                throw new VerificationException("Failed to parse exposed JWKS", e);
            }
        }

        if (jwks == null || jwks.getKeys() == null) {
            throw new VerificationException(
                    String.format("Could not resolve issuer JWKs with URI: %s", issuerUri));
        }

        return Arrays.asList(jwks.getKeys());
    }

    /** 构建 JWT VC 签发者元数据 URI。 */
    static String buildJwtVcIssuerMetadataUri(String issuerUri) throws VerificationException {
        try {
            URI parsedIssuer = URI.create(issuerUri);
            String issuerPath = Optional.ofNullable(parsedIssuer.getRawPath()).orElse("");
            String metadataPath = JWT_VC_ISSUER_END_POINT + issuerPath;

            URI metadata = new URI(
                    parsedIssuer.getScheme(),
                    parsedIssuer.getAuthority(),
                    metadataPath,
                    null,
                    null
            );

            return metadata.toString();
        } catch (IllegalArgumentException | URISyntaxException ex) {
            throw new VerificationException("Invalid issuer URI", ex);
        }
    }

    /** 从 URI 获取 JSON 数据。 */
    private JsonNode fetchData(String uri) throws VerificationException {
        try {
            return Objects.requireNonNull(httpDataFetcher.fetchJsonData(uri));
        } catch (Exception exception) {
            throw new VerificationException(
                    String.format("Could not fetch data from URI: %s", uri),
                    exception
            );
        }
    }

    /** 规范化 URI，移除末尾斜杠。 */
    private static String normalizeUri(String uri) {
        // 移除末尾斜杠
        return uri.replaceAll("/$", "");
    }
}
