package org.keycloak.services.managers;

import jakarta.ws.rs.core.Response;

import org.keycloak.OAuthErrorException;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.encode.AccessTokenContext;
import org.keycloak.protocol.oidc.encode.DefaultTokenContextEncoderProvider;
import org.keycloak.protocol.oidc.encode.TokenContextEncoderProvider;
import org.keycloak.protocol.oidc.grants.OAuth2GrantType;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.ErrorResponseException;

import org.jboss.logging.Logger;

/**
 * 授权类型端点限制校验器。
 * <p>确保访问令牌仅在其 grant type 允许的端点使用；例如 Pre-Authorized Code 令牌限制在凭证端点，其他 grant type 仅访问各自预期端点。</p>
 */
public class GrantTypeEndpointRestrictionValidator implements TokenVerifier.Predicate<AccessToken> {
    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(GrantTypeEndpointRestrictionValidator.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;

    private GrantTypeEndpointRestrictionValidator(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 创建 grant type 端点限制校验用的 {@link TokenVerifier.Predicate}。
     * <p>可与 {@link TokenVerifier#withChecks()} 联用进行内联校验。</p>
     *
     * @param session Keycloak 会话
     * @return 校验 grant type 端点限制的谓词
     */
    public static TokenVerifier.Predicate<AccessToken> check(KeycloakSession session) {
        return new GrantTypeEndpointRestrictionValidator(session);
    }

    @Override
    public boolean test(AccessToken token) throws VerificationException {
        validate(token);
        return true;
    }

    /**
     * 根据令牌 grant type 校验其是否允许用于当前端点。
     *
     * @param token 待校验的访问令牌
     * @throws VerificationException 令牌校验失败
     * @throws ErrorResponseException 服务端配置错误
     */
    private void validate(AccessToken token) throws VerificationException {
        try {
            // 从令牌上下文恢复 grant type
            String grantType = recoverGrantType(token);

            // 无特定 grant type 时放行，保持向后兼容
            if (grantType == null) {
                return;
            }

            // 获取 grant type Provider 以校验端点限制
            OAuth2GrantType grantTypeProvider = session.getProvider(OAuth2GrantType.class, grantType);
            if (grantTypeProvider == null) {
                // 服务端配置错误：未注册对应 grant type Provider
                logger.errorf("Grant type restriction provider not available for: %s - server misconfiguration", grantType);
                throw new ErrorResponseException(OAuthErrorException.SERVER_ERROR,
                        "Internal error: grant type restriction provider not available", Response.Status.INTERNAL_SERVER_ERROR);
            }

            if (!grantTypeProvider.isTokenAllowed(session, token)) {
                throw new VerificationException("Token is not allowed for this endpoint. Grant type: " + grantType);
            }
        } catch (ErrorResponseException | VerificationException e) {
            throw e;
        } catch (Exception e) {
            logger.errorf(e, "Error checking grant type restriction");
            throw new VerificationException("Error verifying grant type restrictions: " + e.getMessage(), e);
        }
    }

    /**
     * 从令牌上下文恢复 grant type，兼容旧版令牌及多种格式。
     *
     * @param token 待提取 grant type 的访问令牌
     * @return grant type；无特定 grant type 时返回 null
     * @throws VerificationException 令牌上下文无效
     * @throws ErrorResponseException 服务端配置错误
     */
    private String recoverGrantType(AccessToken token) throws VerificationException {
        TokenContextEncoderProvider encoder = session.getProvider(TokenContextEncoderProvider.class);
        if (encoder == null) {
            logger.error("Token context encoder provider not available - server misconfiguration");
            throw new ErrorResponseException(OAuthErrorException.SERVER_ERROR,
                    "Internal error: token context encoder not available", Response.Status.INTERNAL_SERVER_ERROR);
        }

        AccessTokenContext tokenContext;
        try {
            tokenContext = encoder.getTokenContextFromTokenId(token.getId());
        } catch (IllegalArgumentException e) {
            // 令牌 ID 格式无效或未知，按旧版令牌处理
            logger.debugf("Cannot decode token context from token ID, treating as legacy token: %s", e.getMessage());
            return null;
        }

        if (tokenContext == null) {
            throw new VerificationException("Invalid token context");
        }

        String grantType = tokenContext.getGrantType();
        if (grantType == null || grantType.isEmpty() || DefaultTokenContextEncoderProvider.UNKNOWN.equals(grantType)) {
            // 标准 Keycloak 令牌无特定 grant type 上下文，为兼容标准 OIDC 流程而放行
            return null;
        }

        return grantType;
    }
}
