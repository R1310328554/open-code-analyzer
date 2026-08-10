package org.keycloak.broker.jwtauthorizationgrant;


import java.util.Map;

import static org.keycloak.broker.oidc.OIDCIdentityProviderConfig.JWKS_URL;
import static org.keycloak.broker.oidc.OIDCIdentityProviderConfig.USE_JWKS_URL;
import static org.keycloak.models.IdentityProviderModel.ISSUER;

/**
 * JWT 授权授予（RFC 7523）身份提供者配置键与访问器。
 */
public interface JWTAuthorizationGrantConfig {

    /** 配置键：是否启用 JWT 授权授予。 */
    String JWT_AUTHORIZATION_GRANT_ENABLED = "jwtAuthorizationGrantEnabled";

    /** 配置键：是否允许重复使用同一 JWT 断言。 */
    String JWT_AUTHORIZATION_GRANT_ASSERTION_REUSE_ALLOWED = "jwtAuthorizationGrantAssertionReuseAllowed";

    /** 配置键：断言允许的最大有效期（秒）。 */
    String JWT_AUTHORIZATION_GRANT_MAX_ALLOWED_ASSERTION_EXPIRATION = "jwtAuthorizationGrantMaxAllowedAssertionExpiration";

    /** 配置键：期望的断言签名算法。 */
    String JWT_AUTHORIZATION_GRANT_ASSERTION_SIGNATURE_ALG = "jwtAuthorizationGrantAssertionSignatureAlg";

    /** 配置键：是否限制由此流程签发的访问令牌过期时间。 */
    String JWT_AUTHORIZATION_GRANT_LIMIT_ACCESS_TOKEN_EXP = "jwtAuthorizationGrantLimitAccessTokenExp";

    /** 配置键：允许的时钟偏差（秒）。 */
    String JWT_AUTHORIZATION_GRANT_ALLOWED_CLOCK_SKEW = "jwtAuthorizationGrantAllowedClockSkew";

    /** 配置键：PEM 公钥（静态验签）。 */
    String PUBLIC_KEY_SIGNATURE_VERIFIER = "publicKeySignatureVerifier";

    /** 配置键：静态公钥对应的 kid。 */
    String PUBLIC_KEY_SIGNATURE_VERIFIER_KEY_ID = "publicKeySignatureVerifierKeyId";

    /** @return 原始配置映射 */
    Map<String, String> getConfig();

    /** @return 是否启用 JWT 授权授予 */
    default boolean isJWTAuthorizationGrantEnabled() {
        return Boolean.parseBoolean(getConfig().getOrDefault(JWT_AUTHORIZATION_GRANT_ENABLED, "false"));
    }

    /** 设置 JWT 授权授予开关。 */
    default void setJWTAuthorizationGrantEnabled(boolean jwtAuthorizationGrantEnableds) {
        getConfig().put(JWT_AUTHORIZATION_GRANT_ENABLED, String.valueOf(jwtAuthorizationGrantEnableds));
    }

    /** @return 是否允许断言复用 */
    default boolean isJWTAuthorizationGrantAssertionReuseAllowed() {
        return Boolean.parseBoolean(getConfig().getOrDefault(JWT_AUTHORIZATION_GRANT_ASSERTION_REUSE_ALLOWED, "false"));
    }

    /** @return 断言最大有效期（秒），默认 300 */
    default int getJWTAuthorizationGrantMaxAllowedAssertionExpiration() {
        return Integer.parseInt(getConfig().getOrDefault(JWT_AUTHORIZATION_GRANT_MAX_ALLOWED_ASSERTION_EXPIRATION, "300"));
    }

    /** @return 期望签名算法 */
    default String getJWTAuthorizationGrantAssertionSignatureAlg() {
        return getConfig().get(JWT_AUTHORIZATION_GRANT_ASSERTION_SIGNATURE_ALG);
    }

    /** @return 是否限制访问令牌过期时间 */
    default boolean isJwtAuthorizationGrantLimitAccessTokenExp() {
        return Boolean.parseBoolean(getConfig().getOrDefault(JWT_AUTHORIZATION_GRANT_LIMIT_ACCESS_TOKEN_EXP, "false"));
    }

    /** @return 时钟偏差秒数，解析失败时返回 0 */
    default int getJWTAuthorizationGrantAllowedClockSkew() {
        String allowedClockSkew = getConfig().get(JWT_AUTHORIZATION_GRANT_ALLOWED_CLOCK_SKEW);
        if (allowedClockSkew == null || allowedClockSkew.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(getConfig().get(JWT_AUTHORIZATION_GRANT_ALLOWED_CLOCK_SKEW));
        } catch (NumberFormatException e) {
            // 解析失败时使用默认值 0
            return 0;
        }
    }

    /** @return PEM 格式验签公钥 */
    default String getPublicKeySignatureVerifier() {
        return getConfig().get(PUBLIC_KEY_SIGNATURE_VERIFIER);
    }

    /** 设置或清除静态验签公钥。 */
    default void setPublicKeySignatureVerifier(String signingCertificate) {
        if (signingCertificate == null) {
            getConfig().remove(PUBLIC_KEY_SIGNATURE_VERIFIER);
        } else {
            getConfig().put(PUBLIC_KEY_SIGNATURE_VERIFIER, signingCertificate);
        }
    }

    /** @return 静态公钥 kid */
    default String getPublicKeySignatureVerifierKeyId() {
        return getConfig().get(PUBLIC_KEY_SIGNATURE_VERIFIER_KEY_ID);
    }

    /** 设置或清除静态公钥 kid。 */
    default void setPublicKeySignatureVerifierKeyId(String publicKeySignatureVerifierKeyId) {
        if (publicKeySignatureVerifierKeyId == null) {
            getConfig().remove(PUBLIC_KEY_SIGNATURE_VERIFIER_KEY_ID);
        } else {
            getConfig().put(PUBLIC_KEY_SIGNATURE_VERIFIER_KEY_ID, publicKeySignatureVerifierKeyId);
        }
    }

    /** @return 是否通过 JWKS URL 获取公钥 */
    default boolean isUseJwksUrl() {
        return Boolean.parseBoolean(getConfig().get(USE_JWKS_URL));
    }

    /** 设置 JWKS URL 模式开关。 */
    default void setUseJwksUrl(boolean useJwksUrl) {
        getConfig().put(USE_JWKS_URL, String.valueOf(useJwksUrl));
    }

    /** @return 断言 issuer 期望值 */
    default String getIssuer() {
        return getConfig().get(ISSUER);
    }

    /** 设置 issuer 配置。 */
    default void setIssuer(String issuer) {
        getConfig().put(ISSUER, issuer);
    }

    /** @return JWKS 端点 URL */
    default String getJwksUrl() {
        return getConfig().get(JWKS_URL);
    }

    /** 设置 JWKS URL。 */
    default void setJwksUrl(String jwksUrl) {
        getConfig().put(JWKS_URL, jwksUrl);
    }

    /** @return 身份提供者内部 ID */
    String getInternalId();

    /** @return 身份提供者别名 */
    String getAlias();
}
