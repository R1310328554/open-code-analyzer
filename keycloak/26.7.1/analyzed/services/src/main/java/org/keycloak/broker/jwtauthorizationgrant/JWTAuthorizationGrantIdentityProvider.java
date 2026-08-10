package org.keycloak.broker.jwtauthorizationgrant;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.JWTAuthorizationGrantProvider;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.jose.jws.JWSHeader;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.keys.loader.PublicKeyStorageManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.JWTAuthorizationGrantValidationContext;
import org.keycloak.representations.IDToken;
import org.keycloak.services.Urls;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * JWT 授权授予身份提供者：验签外部 JWT 断言并映射为 {@link BrokeredIdentityContext}。
 */
public class JWTAuthorizationGrantIdentityProvider implements JWTAuthorizationGrantProvider<JWTAuthorizationGrantIdentityProviderConfig> {
    /** 日志记录器。 */
    private static final Logger LOGGER = Logger.getLogger(JWTAuthorizationGrantIdentityProvider.class);

    /** Keycloak 会话。 */
    private final KeycloakSession session;
    /** JWT 授权授予配置。 */
    private final JWTAuthorizationGrantConfig config;

    /** @param session Keycloak 会话
     * @param config 身份提供者配置 */
    public JWTAuthorizationGrantIdentityProvider(KeycloakSession session, JWTAuthorizationGrantConfig config) {
        this.session = session;
        this.config = config;
    }

    @Override
    /** 验签 JWT 断言并从 subject/preferred_username 构建联邦身份。 */
    public BrokeredIdentityContext validateAuthorizationGrantAssertion(JWTAuthorizationGrantValidationContext context) throws IdentityBrokerException {
        // 验证 JWT 断言签名
        if (!verifySignature(context.getJws())) {
            throw new IdentityBrokerException("Invalid signature");
        }

        BrokeredIdentityContext user = new BrokeredIdentityContext(context.getJWT().getSubject(), getConfig());
        String username = (String) context.getJWT().getOtherClaims().get(IDToken.PREFERRED_USERNAME);
        if (username == null) {
            username = context.getJWT().getSubject();
        }
        user.setUsername(username);
        return user;
    }

    @Override
    /** @return 配置的时钟偏差秒数 */
    public int getAllowedClockSkew() {
        return config.getJWTAuthorizationGrantAllowedClockSkew();
    }

    @Override
    /** @return 是否允许断言复用 */
    public boolean isAssertionReuseAllowed() {
        return config.isJWTAuthorizationGrantAssertionReuseAllowed();
    }

    @Override
    /** @return 允许的 audience 列表（领域 issuer 与 token 端点） */
    public List<String> getAllowedAudienceForJWTGrant() {
        RealmModel realm = session.getContext().getRealm();

        URI baseUri = session.getContext().getUri().getBaseUri();
        String issuer = Urls.realmIssuer(baseUri, realm.getName());
        String tokenEndpoint = Urls.tokenEndpoint(baseUri, realm.getName()).toString();
        return List.of(issuer, tokenEndpoint);
    }

    @Override
    /** @return 断言最大有效期（秒） */
    public int getMaxAllowedExpiration() {
        return config.getJWTAuthorizationGrantMaxAllowedAssertionExpiration();
    }

    @Override
    /** @return 期望签名算法，空白时返回 {@code null} */
    public String getAssertionSignatureAlg() {
        String alg = config.getJWTAuthorizationGrantAssertionSignatureAlg();
        return StringUtil.isBlank(alg) ? null : alg;
    }

    @Override
    /** @return 是否限制访问令牌过期时间 */
    public boolean isLimitAccessTokenExpiration() {
        return getConfig().isJwtAuthorizationGrantLimitAccessTokenExp();
    }

    @Override
    /** @return 强类型配置对象 */
    public JWTAuthorizationGrantIdentityProviderConfig getConfig() {
        return this.config instanceof  JWTAuthorizationGrantIdentityProviderConfig ? (JWTAuthorizationGrantIdentityProviderConfig)this.config : null;
    }

    /** 使用 IdP 公钥或 JWKS 验证 JWS 签名。 */
    private boolean verifySignature(JWSInput jws) {
        try {
            JWSHeader header = jws.getHeader();
            String alg = header.getRawAlgorithm();

            KeyWrapper publicKey = PublicKeyStorageManager.getIdentityProviderKeyWrapper(session, session.getContext().getRealm(), getConfig(), jws);
            if (publicKey == null) {
                LOGGER.debugf("Failed to verify token, key not found for algorithm %s", alg);
                return false;
            }

            SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, alg);
            if (signatureProvider == null) {
                LOGGER.debugf("Failed to verify token signature, signature provider not found for algorithm %s", alg);
                return false;
            }

            return signatureProvider.verifier(publicKey).verify(jws.getEncodedSignatureInput().getBytes(StandardCharsets.UTF_8), jws.getSignature());
        } catch (Exception e) {
            LOGGER.debug("Failed to verify token signature", e);
            return false;
        }
    }

    @Override
    /** 无资源需释放。 */
    public void close() {

    }
}
