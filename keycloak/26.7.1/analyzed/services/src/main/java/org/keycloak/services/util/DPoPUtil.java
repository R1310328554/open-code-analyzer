/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.TokenVerifier;
import org.keycloak.common.Profile;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.CryptoUtils;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureProviderFactory;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.exceptions.TokenVerificationException;
import org.keycloak.http.HttpRequest;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.crypto.HashUtils;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenResponseMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.TokenIntrospectionTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.dpop.DPoP;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.cors.Cors;
import org.keycloak.util.JWKSUtils;
import org.keycloak.util.TokenUtil;
import org.keycloak.utils.StringUtil;

import org.apache.commons.codec.binary.Hex;

import static org.keycloak.OAuth2Constants.DPOP_HTTP_HEADER;
import static org.keycloak.OAuth2Constants.DPOP_JWT_HEADER_TYPE;
import static org.keycloak.utils.StringUtil.isNotBlank;

/**
 * DPoP（Demonstrating Proof-of-Possession）工具类。
 * <p>验证 DPoP 证明 JWT、防重放、绑定访问令牌公钥指纹（cnf.jkt），
 * 并通过临时 ProtocolMapper 将令牌类型设为 DPoP。</p>
 *
 * @author <a href="mailto:dmitryt@backbase.com">Dmitry Telegin</a>
 */
public class DPoPUtil {

    /** DPoP 证明默认有效期（秒） */
    public static final int DEFAULT_PROOF_LIFETIME = 10;
    /** 允许的时钟偏差（秒） */
    public static final int DEFAULT_ALLOWED_CLOCK_SKEW = 15; // sec;
    /** cnf.jkt 类型标识 */
    public static final String DPOP_JKT_TYPE = "DPoP";
    /** DPoP 访问令牌类型 */
    public static final String DPOP_TOKEN_TYPE = "DPoP";
    /** Authorization 头 scheme 名称 */
    public static final String DPOP_SCHEME = "DPoP";
    /** 会话中缓存已验证 DPoP 证明的属性键 */
    public final static String DPOP_SESSION_ATTRIBUTE = "dpop";
    /** 仅将 DPoP 绑定应用于刷新令牌的会话属性键 */
    public final static String DPOP_BINDING_ONLY_REFRESH_TOKEN_SESSION_ATTRIBUTE = "dpop-binding-only-refresh-token";

    /** DPoP 客户端配置模式 */
    public enum Mode {
        /** 强制启用 DPoP */
        ENABLED,
        /** 可选 DPoP */
        OPTIONAL,
        /** 禁用 DPoP */
        DISABLED
    }

    private static URI normalize(URI uri) {
        return UriBuilder.fromUri(uri).replaceQuery("").fragment(null).build();
    }

    /**
     * 创建不可在管理 UI 修改的临时 ProtocolMapper，用于将访问令牌绑定到 DPoP 公钥。
     * <p>采用 ProtocolMapper 方案以通用地支持所有授权类型（含自定义 grantType）的 DPoP 绑定。</p>
     */
    public static Stream<Map.Entry<ProtocolMapperModel, ProtocolMapper>> getTransientProtocolMapper() {
        final String PROVIDER_ID = DPOP_SCHEME.toLowerCase(Locale.ROOT) + "-protocol-mapper";

        ProtocolMapperModel protocolMapperModel = new ProtocolMapperModel();
        protocolMapperModel.setId(DPOP_SCHEME);
        protocolMapperModel.setName(DPOP_SCHEME);
        protocolMapperModel.setProtocolMapper(PROVIDER_ID);
        protocolMapperModel.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, "false");
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO, "false");
        config.put(OIDCAttributeMapperHelper.INCLUDE_IN_INTROSPECTION, "false");
        protocolMapperModel.setConfig(config);

        ProtocolMapper dpopProtocolMapper = new DpopProtocolMapper(PROVIDER_ID);
        return Stream.of(Map.entry(protocolMapperModel, dpopProtocolMapper));
    }

    /**
     * 若 DPoP 特性已启用且客户端要求或请求携带 DPoP 头，则验证证明并存入会话。
     */
    public static void handleDPoPHeader(KeycloakSession keycloakSession,
                                        EventBuilder event,
                                        Cors cors,
                                        OIDCAdvancedConfigWrapper clientConfig) {
        if (!Profile.isFeatureEnabled(Profile.Feature.DPOP)) {
            return;
        }

        HttpRequest request = keycloakSession.getContext().getHttpRequest();
        final boolean isClientRequiresDpop = clientConfig != null && clientConfig.isUseDPoP();
        final String dpopHeaderValue = request.getHttpHeaders().getHeaderString(DPOP_HTTP_HEADER);
        final boolean isDpopHeaderPresent = dpopHeaderValue != null;

        if (!isClientRequiresDpop && !isDpopHeaderPresent) {
            return;
        }

        try {
            DPoP dPoP = new DPoPUtil.Validator(keycloakSession).request(request).uriInfo(keycloakSession.getContext().getUri()).validate();
            keycloakSession.setAttribute(DPoPUtil.DPOP_SESSION_ATTRIBUTE, dPoP);
        } catch (VerificationException ex) {
            event.detail(Details.REASON, ex.getMessage());
            event.error(Errors.INVALID_DPOP_PROOF);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, ex.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    private static DPoP validateDPoP(KeycloakSession session, URI uri, String method, String token, String accessToken, int lifetime, int clockSkew) throws VerificationException {

        if (token == null || token.trim().isEmpty()) {
            throw new VerificationException("DPoP proof is missing");
        }

        TokenVerifier<DPoP> verifier = TokenVerifier.create(token, DPoP.class);
        JWSHeader header;

        try {
            header = verifier.getHeader();
        } catch (VerificationException ex) {
            throw new VerificationException("DPoP header verification failure");
        }

        if (!DPOP_JWT_HEADER_TYPE.equals(header.getType())) {
            throw new VerificationException("Invalid or missing type in DPoP header: " + header.getType());
        }

        if (header.getAlgorithm() == null) {
            throw new VerificationException("No alg in DPoP header");
        }

        String algorithm = header.getAlgorithm().name();
        if (!getDPoPSupportedAlgorithms(session).contains(algorithm)) {
            throw new VerificationException("Unsupported DPoP algorithm: " + header.getAlgorithm());
        }

        JWK jwk = header.getKey();

        KeyWrapper key;
        if (jwk == null) {
            throw new VerificationException("No JWK in DPoP header");
        } else {
            key = JWKSUtils.getKeyWrapper(jwk);
            if (key == null) {
                throw new VerificationException("Unsupported key type in DPoP header");
            }
            if (key.getPublicKey() == null) {
                throw new VerificationException("No public key in DPoP header");
            }
            // JWKSUtils.getKeyWrapper() 实际上不会返回私钥，但仍需显式检查
            KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
            if (sessionFactory.getProviderFactory(SignatureProvider.class, algorithm) instanceof SignatureProviderFactory providerFactory) {
                Set<String> jwkOther = jwk.getOtherClaims().keySet();
                if (key.getPrivateKey() != null || !Collections.disjoint(jwkOther, providerFactory.getJwkPrivateKeyClaims())) {
                    throw new VerificationException("Private key material must not be present in DPoP JWK");
                }
            } else {
                throw new VerificationException("No signature provider factory for: " + algorithm);
            }
        }

        key.setAlgorithm(header.getAlgorithm().name());

        SignatureVerifierContext signatureVerifier = CryptoUtils.getSignatureProvider(session, algorithm).verifier(key);
        verifier.verifierContext(signatureVerifier);
        verifier.withChecks(
                DPoPClaimsCheck.INSTANCE,
                new DPoPHTTPCheck(uri, method),
                new DPoPIsActiveCheck(session, lifetime, clockSkew),
                new DPoPReplayCheck(session, lifetime + clockSkew));

        if (accessToken != null) {
            verifier.withChecks(new DPoPAccessTokenHashCheck(accessToken));
        }

        try {
            DPoP dPoP = verifier.verify().getToken();
            dPoP.setThumbprint(JWKSUtils.computeThumbprint(jwk));
            return dPoP;
        } catch (DPoPVerificationException ex) {
            throw ex;
        } catch (VerificationException ex) {
            throw new VerificationException("DPoP verification failure: " + ex.getMessage(), ex);
        }

    }

    private static final Pattern WHITESPACES = Pattern.compile("\\s+");

    /** 为访问令牌验证器附加 DPoP scheme 与绑定检查。 */
    public static TokenVerifier<AccessToken> withDPoPVerifier(TokenVerifier<AccessToken> verifier, RealmModel realm, DPoPUtil.Validator validator) {
        if (Profile.isFeatureEnabled(Profile.Feature.DPOP)) {
            verifier = verifier.tokenType(List.of(TokenUtil.TOKEN_TYPE_BEARER, TokenUtil.TOKEN_TYPE_DPOP))
                    .withChecks(token -> {
                        boolean isSchemeDPoP = false;
                        if (StringUtil.isNotBlank(validator.authHeader)) {
                            String[] split = WHITESPACES.split(validator.authHeader);
                            isSchemeDPoP = TokenUtil.TOKEN_TYPE_DPOP.equalsIgnoreCase(split[0]);
                        }

                        if (!isSchemeDPoP && DPoPUtil.DPOP_TOKEN_TYPE.equalsIgnoreCase(token.getType())) {
                            throw new VerificationException("The access token type is DPoP but Authorization Header is not DPoP");
                        }
                        if (isSchemeDPoP && !DPoPUtil.DPOP_TOKEN_TYPE.equalsIgnoreCase(token.getType())) {
                            throw new VerificationException("The access token type is not DPoP but Authorization Header is DPoP");
                        }
                        ClientModel clientModel = realm.getClientByClientId(token.getIssuedFor());
                        if (clientModel == null) {
                            throw new VerificationException("Client not found");
                        }
                        if (OIDCAdvancedConfigWrapper.fromClientModel(clientModel).isUseDPoP() && !isSchemeDPoP) {
                            throw new VerificationException("This client requires DPoP, but no DPoP Authorization header is present");
                        }
                        if (isSchemeDPoP) {
                            if (validator.accessToken == null) {
                                throw new VerificationException("Access Token not set for validator");
                            }
                            DPoP dPoP = validator.validate();
                            DPoPUtil.validateBinding(token, dPoP);
                        }
                        return true;
                    });
        }
        return verifier;
    }

    /** 验证访问令牌 cnf.jkt 与 DPoP 证明公钥指纹是否匹配。 */
    public static void validateBinding(AccessToken token, DPoP dPoP) throws VerificationException {
        try {
            TokenVerifier.createWithoutSignature(token)
                    .withChecks(new DPoPUtil.DPoPBindingCheck(dPoP))
                    .verify();
        } catch (TokenVerificationException ex) {
            throw ex;
        } catch (VerificationException ex) {
            throw new VerificationException("Token verification failure", ex);
        }
    }


    /** 验证授权请求中的 dpop_jkt 与 DPoP 证明公钥指纹是否一致。 */
    public static void validateDPoPJkt(String dpopJkt, KeycloakSession session, EventBuilder event, Cors cors) {
        if (dpopJkt == null) {
            // 授权请求未携带 dpop_jkt 时无需校验指纹匹配
            return;
        }
        // 授权请求携带 dpop_jkt 时必须校验 DPoP 证明公钥指纹
        DPoP dPoP = session.getAttribute(DPoPUtil.DPOP_SESSION_ATTRIBUTE, DPoP.class);
        if (dPoP == null) {
            String errorMessage = "DPoP Proof missing";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.INVALID_REQUEST);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, errorMessage, Response.Status.BAD_REQUEST);
        }
        if (!dpopJkt.equals(dPoP.getThumbprint())){
            String errorMessage = "DPoP Proof public key thumbprint does not match dpop_jkt";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.INVALID_REQUEST);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_REQUEST, errorMessage, Response.Status.BAD_REQUEST);
        }

    }

    private static class DPoPClaimsCheck implements TokenVerifier.Predicate<DPoP> {

        static final TokenVerifier.Predicate<DPoP> INSTANCE = new DPoPClaimsCheck();

        @Override
        public boolean test(DPoP t) throws DPoPVerificationException {
            Long iat = t.getIat();
            String jti = t.getId();
            String htu = t.getHttpUri();
            String htm = t.getHttpMethod();

            if (iat != null &&
                isNotBlank(jti) &&
                isNotBlank(htm) &&
                isNotBlank(htu)) {
                return true;
            } else {
                throw new DPoPVerificationException(t, "DPoP mandatory claims are missing");
            }
        }

    }

    private static List<String> getSupportedAlgorithms(KeycloakSession session, Class<? extends Provider> clazz, boolean includeNone) {
        Stream<String> supportedAlgorithms = session.getKeycloakSessionFactory().getProviderFactoriesStream(clazz)
                .map(ProviderFactory::getId);

        if (includeNone) {
            supportedAlgorithms = Stream.concat(supportedAlgorithms, Stream.of("none"));
        }
        return supportedAlgorithms.collect(Collectors.toList());
    }

    /** 返回服务器支持的非对称 DPoP 签名算法列表。 */
    public static List<String> getDPoPSupportedAlgorithms(KeycloakSession session) {
        return getSupportedAlgorithms(session, SignatureProvider.class, false).stream()
                .map(algorithm -> new AbstractMap.SimpleEntry<>(algorithm, session.getProvider(SignatureProvider.class, algorithm)))
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> entry.getValue().isAsymmetricAlgorithm())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private static class DPoPHTTPCheck implements TokenVerifier.Predicate<DPoP> {

        private final URI uri;
        private final String method;

        DPoPHTTPCheck(URI uri, String method) {
            this.uri = uri;
            this.method = method;
        }

        @Override
        public boolean test(DPoP t) throws DPoPVerificationException {
            try {
                if (!normalize(new URI(t.getHttpUri())).equals(normalize(uri)))
                    throw new DPoPVerificationException(t, "DPoP HTTP URL mismatch");

                if (!method.equals(t.getHttpMethod()))
                    throw new DPoPVerificationException(t, "DPoP HTTP method mismatch");
            } catch (URISyntaxException ex) {
                throw new DPoPVerificationException(t, "Malformed HTTP URL in DPoP proof");
            }

            return true;
        }

    }

    private static class DPoPReplayCheck implements TokenVerifier.Predicate<DPoP> {

        private final KeycloakSession session;
        private final int lifetime;

        public DPoPReplayCheck(KeycloakSession session, int lifetime) {
            this.session = session;
            this.lifetime = lifetime;
        }

        @Override
        public boolean test(DPoP t) throws DPoPVerificationException {
            SingleUseObjectProvider singleUseCache = session.singleUseObjects();
            String hashKey = t.getId() + "\n" + t.getHttpUri();
            String hashString = Hex.encodeHexString(HashUtils.hash("SHA1", hashKey.getBytes()));
            if (!singleUseCache.putIfAbsent(hashString, (int)(t.getIat() + lifetime - Time.currentTime()))) {
                throw new DPoPVerificationException(t, "DPoP proof has already been used");
            }
            return true;
        }

    }

    private static class DPoPIsActiveCheck implements TokenVerifier.Predicate<DPoP> {

        private final int lifetime;
        private final int clockSkew;

        public DPoPIsActiveCheck(KeycloakSession session, int lifetime, int clockSkew) {
            this.lifetime = lifetime;
            this.clockSkew = clockSkew;
        }

        @Override
        public boolean test(DPoP t) throws DPoPVerificationException {
            long time = Time.currentTime();
            Long iat = t.getIat();

            // 考虑时钟偏差有两种情况：客户端时钟快于/慢于 Keycloak
            //   情况 1：客户端时钟快于 Keycloak
            //   情况 2：客户端时钟慢于 Keycloak
            //
            // 修正情况 1：iat/nbf - clockSkew <= 当前时间
            //   current keycloak clock => "iat" - clock skew
            //   current keycloak clock => "nbf" - clock skew
            // 修正情况 2：当前时间 <= exp/iat + lifetime + clockSkew
            //   current keycloak clock <= "exp" + clock skew
            //     or
            //   current keycloak clock <= "iat" + life time + clock skew
            //
            // 综合有效时间窗口：iat - clockSkew <= 当前时间 <= iat + lifetime + clockSkew
            //    "iat" - clock skew <= keycloak's clock <= "exp" + clock skew
            //      or
            //    "iat" - clock skew <= keycloak's clock <= iat" + life time + clock skew
            //
            // 声明值以秒为单位，故使用 <=/>= 而非 </>

            if (!(iat <= time + clockSkew && iat >= time - lifetime - clockSkew)) {
                throw new DPoPVerificationException(t, "DPoP proof is not active");
            }
            return true;
        }
    }

    private static class DPoPAccessTokenHashCheck implements TokenVerifier.Predicate<DPoP> {

        private final String hash;

        public DPoPAccessTokenHashCheck(String tokenString) {
            hash = HashUtils.accessTokenHash(OAuth2Constants.DPOP_DEFAULT_ALGORITHM.toString(), tokenString, true);
        }

        @Override
        public boolean test(DPoP t) throws DPoPVerificationException {
            if (t.getAccessTokenHash() == null) {
                throw new DPoPVerificationException(t, "No access token hash in DPoP proof");
            }
            if (!t.getAccessTokenHash().equals(hash)) {
                throw new DPoPVerificationException(t, "DPoP proof access token hash mismatch");
            }
            return true;
        }

    }

    private static class DPoPBindingCheck implements TokenVerifier.Predicate<AccessToken> {

        private final DPoP proof;

        public DPoPBindingCheck(DPoP proof) {
            this.proof = proof;
        }

        @Override
        public boolean test(AccessToken t) throws VerificationException {
            String thumbprint = proof.getThumbprint();

            AccessToken.Confirmation confirmation = t.getConfirmation();
            if (confirmation == null) {
                throw new TokenVerificationException(t, "No DPoP confirmation in access token");
            }
            String keyThumbprint = confirmation.getKeyThumbprint();
            if (keyThumbprint == null) {
                throw new TokenVerificationException(t, "No DPoP key thumbprint in access token");
            }
            if (!keyThumbprint.equals(thumbprint)) {
                throw new TokenVerificationException(t, "DPoP confirmation doesn't match DPoP proof");
            }
            return true;
        }

    }

    /** DPoP 证明验证失败异常。 */
    public static class DPoPVerificationException extends TokenVerificationException {

        public DPoPVerificationException(DPoP token, String message) {
            super(token, message);
        }

    }

    /** DPoP 证明验证器（流式构建 URI/方法/令牌后调用 validate）。 */
    public static class Validator {

        private URI uri;
        private String method;
        private String dPoP;
        private String accessToken;
        private String authHeader;
        private int clockSkew = DEFAULT_ALLOWED_CLOCK_SKEW;
        private int lifetime = DEFAULT_PROOF_LIFETIME;

        private final KeycloakSession session;

        public Validator(KeycloakSession session) {
            this.session = session;
        }

        /** 从 HTTP 请求提取 URI、方法、DPoP 头与 Authorization 头。 */
        public Validator request(HttpRequest request) {
            this.uri = request.getUri().getAbsolutePath();
            this.method = request.getHttpMethod();
            this.dPoP = request.getHttpHeaders().getHeaderString(DPOP_HTTP_HEADER);
            this.authHeader = request.getHttpHeaders().getHeaderString(HttpHeaders.AUTHORIZATION);
            return this;
        }

        public Validator dPoP(String dPoP) {
            this.dPoP = dPoP;
            return this;
        }

        public Validator accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Validator uriInfo(UriInfo uriInfo) {
            this.uri = uriInfo.getAbsolutePath();
            return this;
        }

        public Validator uri(String uri) throws URISyntaxException {
            this.uri = new URI(uri);
            return this;
        }

        public Validator method(String method) {
            this.method = method;
            return this;
        }

        /** 执行 DPoP 证明完整验证链。 */
        public DPoP validate() throws VerificationException {
            return validateDPoP(session, uri, method, dPoP, accessToken, lifetime, clockSkew);
        }

    }

    /**
     * 临时 DPoP ProtocolMapper（不在 Admin UI 配置）。
     * <p>在令牌请求时动态创建，将生成的访问令牌绑定到 DPoP 头中的公钥。</p>
     */
    private static final class DpopProtocolMapper extends AbstractOIDCProtocolMapper
            implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper,
            OIDCAccessTokenResponseMapper {

        private final String providerId;

        public DpopProtocolMapper(String providerId) {
            this.providerId = providerId;
        }

        @Override
        public String getDisplayCategory() {
            return TOKEN_MAPPER_CATEGORY;
        }

        @Override
        public String getDisplayType() {
            return DPOP_SCHEME;
        }

        @Override
        public String getHelpText() {
            return "not needed";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {
            return List.of(new ProviderConfigProperty("multivalued", ProtocolMapperUtils.MULTIVALUED, "",
                                                      ProviderConfigProperty.BOOLEAN_TYPE, false));
        }

        @Override
        public String getId() {
            return providerId;
        }

        @Override
        public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
            boolean isDPoPSupported = Profile.isFeatureEnabled(Profile.Feature.DPOP);
            if (!isDPoPSupported) {
                return super.transformAccessToken(token, mappingModel, session, userSession, clientSessionCtx);
            }
            DPoP dPoP = session.getAttribute(DPOP_SESSION_ATTRIBUTE, DPoP.class);
            if (dPoP == null) {
                return super.transformAccessToken(token, mappingModel, session, userSession, clientSessionCtx);
            }

            Boolean bindOnlyRefreshToken = session.getAttributeOrDefault(DPOP_BINDING_ONLY_REFRESH_TOKEN_SESSION_ATTRIBUTE, false);
            if (bindOnlyRefreshToken) {
                return super.transformAccessToken(token, mappingModel, session, userSession, clientSessionCtx);
            }
            AccessToken.Confirmation cnf = (AccessToken.Confirmation) token.getOtherClaims().get(OAuth2Constants.CNF);
            if (cnf == null) {
                cnf = new AccessToken.Confirmation();
                token.setConfirmation(cnf);
            }
            cnf.setKeyThumbprint(dPoP.getThumbprint());
            cnf.setJktType(DPOP_JKT_TYPE);
            // 确保令牌类型设为 DPoP（在构建 AccessTokenResponse 时生效）
            token.type(DPOP_TOKEN_TYPE);
            return super.transformAccessToken(token, mappingModel, session, userSession, clientSessionCtx);
        }
    }
}
