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

import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.util.Time;
import org.keycloak.constants.OID4VCIConstants;
import org.keycloak.crypto.CryptoUtils;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.http.HttpResponse;
import org.keycloak.jose.jwe.JWEConstants;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.KeyManager;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.OID4VCLoginProtocolFactory;
import org.keycloak.protocol.oid4vc.issuance.credentialbuilder.CredentialBuilder;
import org.keycloak.protocol.oid4vc.issuance.credentialbuilder.CredentialBuilderFactory;
import org.keycloak.protocol.oid4vc.model.CredentialIssuer;
import org.keycloak.protocol.oid4vc.model.CredentialRequestEncryptionMetadata;
import org.keycloak.protocol.oid4vc.model.CredentialResponseEncryptionMetadata;
import org.keycloak.protocol.oid4vc.model.SupportedCredentialConfiguration;
import org.keycloak.protocol.oidc.utils.JWKSServerUtils;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.Urls;
import org.keycloak.services.resources.ServerMetadataResource;
import org.keycloak.urls.UrlType;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.MediaType;
import org.keycloak.wellknown.WellKnownProvider;

import org.apache.http.HttpHeaders;
import org.jboss.logging.Logger;

import static org.keycloak.OID4VCConstants.SIGNED_METADATA_JWT_TYPE;
import static org.keycloak.OID4VCConstants.WELL_KNOWN_OPENID_CREDENTIAL_ISSUER;
import static org.keycloak.constants.OID4VCIConstants.BATCH_CREDENTIAL_ISSUANCE_BATCH_SIZE;
import static org.keycloak.jose.jwk.RSAPublicJWK.RS256;

/**
 * OID4VCI 凭证签发者元数据的 {@link WellKnownProvider} 实现。
 * <p>端点 {@code /.well-known/openid-credential-issuer}，返回 Credential Issuer Metadata，含支持的凭证配置、加密参数、批量签发能力等。</p>
 * <p>{@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-11.2.2}</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public class OID4VCIssuerWellKnownProvider implements WellKnownProvider {

    private static final Logger LOGGER = Logger.getLogger(OID4VCIssuerWellKnownProvider.class);

    /** Well-Known 提供方 ID。 */
    public static final String PROVIDER_ID = "openid-credential-issuer";

    // Realm 属性：签名元数据 JWT 的生命周期与算法
    public static final String SIGNED_METADATA_LIFESPAN_ATTR = "oid4vci.signed_metadata.lifespan";
    public static final String SIGNED_METADATA_ALG_ATTR = "oid4vci.signed_metadata.alg";

    public static final String VC_KEY = "vc";
    public static final String ATTR_RESPONSE_ENCRYPTION_REQUIRED = "oid4vci.response.encryption.required";
    public static final String ATTR_REQUEST_ENCRYPTION_REQUIRED = "oid4vci.request.encryption.required";

    public static final String DEFLATE_COMPRESSION = "DEF";
    public static final String ATTR_REQUEST_ZIP_ALGS = "oid4vci.request.zip.algorithms";

    protected final KeycloakSession keycloakSession;

    public OID4VCIssuerWellKnownProvider(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public Object getConfig() {
        RealmModel realm = keycloakSession.getContext().getRealm();
        if (!realm.isVerifiableCredentialsEnabled()) {
            LOGGER.debugf("OID4VCI functionality is disabled for realm '%s'. Verifiable Credentials switch is off.", realm.getName());
            throw new NotFoundException("OID4VCI functionality is disabled for this realm");
        }
        CredentialIssuer issuer = getIssuerMetadata();
        // 显式设置 Date 响应头，满足 RFC7231 与一致性测试要求
        keycloakSession.getContext().getHttpResponse().setHeader(HttpHeaders.DATE, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
        return getMetadataResponse(issuer, keycloakSession);
    }

    public CredentialIssuer getIssuerMetadata() {
        KeycloakContext context = keycloakSession.getContext();

        // 构建请求/响应加密元数据（二者在 OID4VCI 中相互独立）
        // OID4VCI 中请求加密与响应加密相互独立
        CredentialResponseEncryptionMetadata responseEnc = getCredentialResponseEncryption(keycloakSession);
        CredentialRequestEncryptionMetadata requestEnc = getCredentialRequestEncryption(keycloakSession);

        // Request and response encryption are independent.
        if (responseEnc != null && requestEnc == null) {
            LOGGER.debug("credential_response_encryption is advertised but credential_request_encryption metadata is not available. " +
                    "This is allowed because request and response encryption are independent.");
        }

        // 若使用旧版 Realm 作用域路由，附加弃用头与日志
        addDeprecationHeadersIfOldRoute(keycloakSession);

        return new CredentialIssuer()
                .setCredentialIssuer(getIssuer(context))
                .setCredentialEndpoint(getCredentialsEndpoint(context))
                .setNonceEndpoint(getNonceEndpoint(context))
                .setCredentialsSupported(getSupportedCredentials(keycloakSession))
                .setAuthorizationServers(List.of(getIssuer(context)))
                .setCredentialResponseEncryption(responseEnc)
                .setCredentialRequestEncryption(requestEnc)
                .setBatchCredentialIssuance(getBatchCredentialIssuance(keycloakSession));
    }

    public Object getMetadataResponse(CredentialIssuer issuer, KeycloakSession session) {
        String acceptHeader = session.getContext().getRequestHeaders().getHeaderString(HttpHeaders.ACCEPT);
        boolean preferJwt = acceptHeader != null
                && acceptHeader.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JWT);

        if (preferJwt) {
            Optional<String> signedJwt = generateSignedMetadata(issuer, session);
            if (signedJwt.isPresent()) {
                session.getContext().getHttpResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JWT);
                return signedJwt.get();
            } else {
                RealmModel realm = session.getContext().getRealm();
                LOGGER.debugf("Falling back to JSON response due to signed metadata failure for realm: %s", realm.getName());
            }
        }

        // JSON 元数据为必选项，始终支持
        session.getContext().getHttpResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        return issuer;
    }

    private CredentialIssuer.BatchCredentialIssuance getBatchCredentialIssuance(KeycloakSession session) {
        return getBatchCredentialIssuance(session.getContext().getRealm());
    }

    /**
     * 返回指定 Realm 的批量凭证签发配置。
     * <p>公开静态方法，便于单元测试无需完整会话状态。</p>
     * @param realm Realm 模型
     * @return 批量签发配置；未配置或无效时返回 null
     */
    public static CredentialIssuer.BatchCredentialIssuance getBatchCredentialIssuance(RealmModel realm) {
        String batchSize = realm.getAttribute(BATCH_CREDENTIAL_ISSUANCE_BATCH_SIZE);
        if (batchSize != null) {
            try {
                int parsedBatchSize = Integer.parseInt(batchSize);
                if (parsedBatchSize < 2) {
                    LOGGER.warnf("%s must be 2 or greater, but was %d. Skipping batch_credential_issuance.", BATCH_CREDENTIAL_ISSUANCE_BATCH_SIZE, parsedBatchSize);
                    return null;
                }
                return new CredentialIssuer.BatchCredentialIssuance()
                        .setBatchSize(parsedBatchSize);
            } catch (Exception e) {
                LOGGER.warnf(e, "Failed to parse %s from realm attributes.", BATCH_CREDENTIAL_ISSUANCE_BATCH_SIZE);
            }
        }
        return null;
    }

    /**
     * 使用 JsonWebToken 基础设施将元数据签名为 JWS。
     * @param metadata 待签名的 CredentialIssuer 元数据
     * @param session Keycloak 会话
     * @return 成功时为紧凑 JWS 字符串；失败时为空（回退到未签名 JSON）
     */
    public Optional<String> generateSignedMetadata(CredentialIssuer metadata, KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();
        KeyManager keyManager = session.keys();

        // Select asymmetric signing algorithm
        String alg;
        try {
            alg = getSigningAlgorithm(realm, session);
        } catch (IllegalStateException e) {
            LOGGER.warnf("Failed to get signing algorithm: %s. Falling back to unsigned metadata.", e.getMessage());
            return Optional.empty(); // Return empty to indicate fallback to JSON
        }

        // Retrieve active key
        KeyWrapper keyWrapper = keyManager.getActiveKey(realm, KeyUse.SIG, alg);
        if (keyWrapper == null) {
            LOGGER.warnf("No active key found for realm '%s' with algorithm '%s'. Falling back to unsigned metadata.", realm.getName(), alg);
            return Optional.empty();
        }

        // Create JsonWebToken with metadata as claims
        JsonWebToken jwt = createMetadataJwt(metadata, realm);

        // Validate lifespan configuration
        Optional<String> maybeLifespan = Optional.ofNullable(realm.getAttribute(SIGNED_METADATA_LIFESPAN_ATTR));
        if (maybeLifespan.isPresent()) {
            String lifespanVal = maybeLifespan.get();
            try {
                jwt.exp(Time.currentTime() + Long.parseLong(lifespanVal));
            } catch (NumberFormatException e) {
                LOGGER.warnf("Invalid lifespan duration for signed metadata: %s. Falling back to unsigned metadata.", lifespanVal);
                return Optional.empty(); // Return empty to indicate fallback to JSON
            }
        } else {
            jwt.exp(Time.currentTime() + 3600L);
        }

        // Build JWS with proper headers
        JWSBuilder jwsBuilder = new JWSBuilder()
                .type(SIGNED_METADATA_JWT_TYPE)
                .kid(keyWrapper.getKid());

        // Add x5c certificate chain if available
        addCertificateHeaders(jwsBuilder, keyWrapper, realm);

        // Sign the JWS
        SignatureProvider signerProvider = session.getProvider(SignatureProvider.class, alg);
        if (signerProvider == null) {
            LOGGER.warnf("No signature provider for algorithm: %s. Falling back to unsigned metadata.", alg);
            return Optional.empty();
        }

        SignatureSignerContext signer = signerProvider.signer(keyWrapper);
        if (signer == null) {
            LOGGER.warnf("No signer context for algorithm: %s. Falling back to unsigned metadata.", alg);
            return Optional.empty();
        }

        try {
            return Optional.of(jwsBuilder.jsonContent(jwt).sign(signer));
        } catch (Exception e) {
            LOGGER.warnf(e, "Failed to sign metadata. Falling back to unsigned metadata.");
            return Optional.empty();
        }
    }

    private String getSigningAlgorithm(RealmModel realm, KeycloakSession session) {
        List<String> supportedAlgorithms = getSupportedAsymmetricSignatureAlgorithms(session);

        if (supportedAlgorithms.isEmpty()) {
            throw new IllegalStateException("No asymmetric signing algorithms available for realm: " + realm.getName());
        }

        String configuredAlg = realm.getAttribute(SIGNED_METADATA_ALG_ATTR);
        if (configuredAlg != null) {
            if (!supportedAlgorithms.contains(configuredAlg)) {
                throw new IllegalStateException("Configured signing algorithm '" + configuredAlg + "' is not supported for realm: " + realm.getName());
            }
            // Use the configured algorithm if present and supported
            return configuredAlg;
        }

        // Prefer RS256 if available, otherwise use the first supported asymmetric algorithm
        return supportedAlgorithms.contains(RS256) ? RS256 : supportedAlgorithms.get(0);
    }

    private JsonWebToken createMetadataJwt(CredentialIssuer metadata, RealmModel realm) {
        JsonWebToken jwt = new JsonWebToken();

        // Set standard JWT claims
        jwt.subject(metadata.getCredentialIssuer());
        jwt.issuer(metadata.getCredentialIssuer());
        jwt.issuedNow();

        // Convert metadata to map and add as other claims
        Map<String, Object> metadataClaims = JsonSerialization.mapper.convertValue(metadata, Map.class);
        metadataClaims.forEach(jwt::setOtherClaims);

        return jwt;
    }

    private void addCertificateHeaders(JWSBuilder jwsBuilder, KeyWrapper keyWrapper, RealmModel realm) {
        if (keyWrapper.getCertificateChain() != null && !keyWrapper.getCertificateChain().isEmpty()) {
            jwsBuilder.x5c(keyWrapper.getCertificateChain());
        } else if (keyWrapper.getCertificate() != null) {
            jwsBuilder.x5c(List.of(keyWrapper.getCertificate()));
        } else {
            LOGGER.debugf("No certificate or certificate chain available for x5c header in realm: %s", realm.getName());
        }
    }

    /**
     * 返回凭证响应加密元数据；支持的算法由 Realm 可用密钥决定。
     * @param session Keycloak 会话
     * @return 响应加密元数据
     */
    public static CredentialResponseEncryptionMetadata getCredentialResponseEncryption(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();
        CredentialResponseEncryptionMetadata metadata = new CredentialResponseEncryptionMetadata();

        // Get supported algorithms from available encryption keys
        metadata.setAlgValuesSupported(getSupportedEncryptionAlgorithms(session))
                .setEncValuesSupported(getSupportedEncryptionMethods())
                .setZipValuesSupported(getSupportedZipAlgorithms(realm))
                .setEncryptionRequired(isEncryptionRequired(realm));

        return metadata;
    }

    /**
     * 返回凭证请求加密元数据；从 Realm 加密密钥构建 JWKS 与支持算法列表。
     */
    public static CredentialRequestEncryptionMetadata getCredentialRequestEncryption(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();

        // Build JWKS with public encryption keys
        JSONWebKeySet jwks = buildJwksForEncryption(session);

        // Request encryption requirement is independent from response encryption requirement.
        boolean requestEncryptionRequired = false;
        String requestEncRequiredAttr = realm.getAttribute(ATTR_REQUEST_ENCRYPTION_REQUIRED);
        if (requestEncRequiredAttr != null) {
            requestEncryptionRequired = Boolean.parseBoolean(requestEncRequiredAttr);
        }
        if (jwks.getKeys() == null || jwks.getKeys().length == 0) {
            if (requestEncryptionRequired) {
                LOGGER.error("Request encryption is required but no valid encryption keys are available.");
                throw new IllegalStateException("Missing encryption keys for required credential_request_encryption.");
            } else {
                LOGGER.warn("No valid encryption keys found; omitting credential_request_encryption metadata.");
                return null; // Entire object omitted
            }
        }

        // Build metadata
        CredentialRequestEncryptionMetadata metadata = new CredentialRequestEncryptionMetadata()
                .setJwks(jwks)
                .setEncValuesSupported(getSupportedEncryptionMethods())
                .setZipValuesSupported(getSupportedZipAlgorithms(realm))
                .setEncryptionRequired(requestEncryptionRequired);

        return metadata;
    }


    /** 返回支持的非对称加密算法列表。 */
    public static List<String> getSupportedEncryptionAlgorithms(KeycloakSession session) {

        List<String> supportedEncryptionAlgorithms = CryptoUtils.getSupportedAsymmetricEncryptionAlgorithms(session);
        if (supportedEncryptionAlgorithms.isEmpty()) {
            supportedEncryptionAlgorithms = List.of(JWEConstants.RSA_OAEP, JWEConstants.RSA_OAEP_256);
        }

        return supportedEncryptionAlgorithms;
    }


    /** 从 Realm 加密密钥（use=enc）构建 JWKS，排除对称 oct 密钥。 */
    private static JSONWebKeySet buildJwksForEncryption(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();
        JSONWebKeySet jwks = JWKSServerUtils.getRealmJwks(session, realm);

        // Filter for encryption keys only and exclude symmetric keys (oct)
        JWK[] encKeys = Arrays.stream(jwks.getKeys())
                .filter(jwk -> JWK.Use.ENCRYPTION.asString().equals(jwk.getPublicKeyUse()))
                .filter(jwk -> jwk.getKeyType() != null && !jwk.getKeyType().equals("oct"))
                .toArray(JWK[]::new);

        jwks.setKeys(encKeys);
        return jwks;
    }


    /** 从 Realm 属性读取支持的压缩算法（可选，当前仅 DEFLATE）。 */
    private static List<String> getSupportedZipAlgorithms(RealmModel realm) {
        String zipAlgs = realm.getAttribute(ATTR_REQUEST_ZIP_ALGS);
        if (zipAlgs != null && !zipAlgs.isEmpty()) {
            return Arrays.stream(zipAlgs.split(","))
                    .map(String::trim)
                    .filter(alg -> alg.equals(DEFLATE_COMPRESSION)) // Only support DEFLATE for now
                    .collect(Collectors.toList());
        }
        return null; // Omit if not configured
    }


    /** 返回支持的内容加密方法（如 A256GCM）。 */
    private static List<String> getSupportedEncryptionMethods() {
        return List.of(JWEConstants.A256GCM);
    }

    /** 根据 Realm 属性判断响应加密是否强制。 */
    private static boolean isEncryptionRequired(RealmModel realm) {
        String required = realm.getAttribute(ATTR_RESPONSE_ENCRYPTION_REQUIRED);
        return Boolean.parseBoolean(required);
    }


    /**
     * 返回当前会话支持的凭证配置映射。
     * <p>综合 {@link CredentialBuilder} 格式能力与 OID4VC 协议客户端范围。</p>
     */
    public static Map<String, SupportedCredentialConfiguration> getSupportedCredentials(KeycloakSession keycloakSession) {
        List<String> globalSupportedSigningAlgorithms = getSupportedAsymmetricSignatureAlgorithms(keycloakSession);

        RealmModel realm = keycloakSession.getContext().getRealm();
        Map<String, SupportedCredentialConfiguration> supportedCredentialConfigurations =
                keycloakSession.clientScopes()
                        .getClientScopesByProtocol(realm, OID4VCIConstants.OID4VC_PROTOCOL)
                        .map(CredentialScopeModel::new)
                        .map(credentialScope -> {
                            SupportedCredentialConfiguration config = SupportedCredentialConfiguration.parse(keycloakSession,
                                    credentialScope,
                                    globalSupportedSigningAlgorithms
                            );
                            applyFormatSpecificMetadata(keycloakSession, config, credentialScope);
                            return config;
                        })
                        .collect(Collectors.toMap(SupportedCredentialConfiguration::getId, sc -> sc, (sc1, sc2) -> sc1));

        return supportedCredentialConfigurations;
    }


    private static void applyFormatSpecificMetadata(KeycloakSession keycloakSession,
                                                    SupportedCredentialConfiguration config,
                                                    CredentialScopeModel credentialScope) {
        String format = config.getFormat();
        if (format == null) {
            return;
        }

        // Find the CredentialBuilder for this format using the factory pattern
        CredentialBuilder credentialBuilder = keycloakSession.getKeycloakSessionFactory()
                .getProviderFactoriesStream(CredentialBuilder.class)
                .map(factory -> (CredentialBuilderFactory) factory)
                .filter(factory -> format.equals(factory.getSupportedFormat()))
                .findFirst()
                .map(factory -> factory.create(keycloakSession, null))
                .orElse(null);

        if (credentialBuilder == null) {
            LOGGER.debugf("No CredentialBuilder found for format: %s", format);
            return;
        }

        credentialBuilder.contributeToMetadata(config, credentialScope);
    }

    public static SupportedCredentialConfiguration toSupportedCredentialConfiguration(KeycloakSession keycloakSession,
                                                                                      CredentialScopeModel credentialModel) {
        List<String> globalSupportedSigningAlgorithms = getSupportedAsymmetricSignatureAlgorithms(keycloakSession);

        SupportedCredentialConfiguration config = SupportedCredentialConfiguration.parse(keycloakSession,
                credentialModel,
                globalSupportedSigningAlgorithms);
        applyFormatSpecificMetadata(keycloakSession, config, credentialModel);
        return config;
    }

    /** @return 凭证签发者标识符 URL（Realm Issuer） */
    public static String getIssuer(KeycloakContext context) {
        UriInfo frontendUriInfo = context.getUri(UrlType.FRONTEND);
        return Urls.realmIssuer(frontendUriInfo.getBaseUri(),
                context.getRealm().getName());
    }

    /** @return c_nonce 端点 URL */
    public static String getNonceEndpoint(KeycloakContext context) {
        return getIssuer(context) + "/protocol/" + OID4VCLoginProtocolFactory.PROTOCOL_ID + "/" +
                OID4VCIssuerEndpoint.NONCE_PATH;
    }

    /** @return 凭证端点 URL */
    public static String getCredentialsEndpoint(KeycloakContext context) {
        return getIssuer(context) + "/protocol/" + OID4VCLoginProtocolFactory.PROTOCOL_ID + "/" + OID4VCIssuerEndpoint.CREDENTIAL_PATH;
    }

    /** @return 授权服务器列表（通常为签发者自身） */
    public static List<String> getAuthorizationServers(KeycloakSession session) {
        return List.of(getIssuer(session.getContext()));
    }

    /**
     * 返回支持的非对称签名算法（RSA、EC、EdDSA 等）。
     * <p>委托 {@link CryptoUtils}，与 OIDC Well-Known 共享实现。</p>
     */
    public static List<String> getSupportedAsymmetricSignatureAlgorithms(KeycloakSession session) {
        return CryptoUtils.getSupportedAsymmetricSignatureAlgorithms(session);
    }

    /**
     * 使用旧版 Realm 作用域 Well-Known 路由时附加弃用响应头并记录 WARN。
     * <p>旧：{@code /realms/{realm}/.well-known/openid-credential-issuer}</p>
     * <p>新：{@code /.well-known/openid-credential-issuer/realms/{realm}}</p>
     */
    private void addDeprecationHeadersIfOldRoute(KeycloakSession session) {
        String requestPath = session.getContext().getUri().getRequestUri().getPath();
        if (requestPath == null) {
            return;
        }

        int idxRealms = requestPath.indexOf("/realms/");
        int idxWellKnown = requestPath.indexOf("/.well-known/");
        boolean isOldRoute = idxRealms >= 0 && idxWellKnown > idxRealms;
        if (!isOldRoute) {
            return;
        }

        UriBuilder base = session.getContext().getUri().getBaseUriBuilder();
        String logKey = session.getContext().getRealm().getName();
        URI successor = ServerMetadataResource.wellKnownProviderUrl(base)
                .build(WELL_KNOWN_OPENID_CREDENTIAL_ISSUER, logKey);

        HttpResponse httpResponse = session.getContext().getHttpResponse();
        httpResponse.setHeader("Warning", "299 - \"Deprecated endpoint; use " + successor + "\"");
        httpResponse.setHeader("Deprecation", "true");
        httpResponse.setHeader("Link", "<" + successor + ">; rel=\"successor-version\"");

        LOGGER.warnf("Deprecated realm-scoped well-known endpoint accessed for OID4VCI in realm '%s'. Use %s instead.", logKey, successor);
    }

}
