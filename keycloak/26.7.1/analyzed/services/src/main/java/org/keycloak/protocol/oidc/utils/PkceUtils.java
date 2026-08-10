package org.keycloak.protocol.oidc.utils;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.crypto.HashException;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.jose.jws.crypto.HashUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.CorsErrorResponseException;
import org.keycloak.services.cors.Cors;

import org.jboss.logging.Logger;

/**
 * PKCE（Proof Key for Code Exchange）工具：生成/编码 code_verifier 与 code_challenge，并在 token 端点校验。
 * <p>遵循 RFC 7636。</p>
 */
public class PkceUtils {

    private static final Logger logger = Logger.getLogger(PkceUtils.class);

    private static final Pattern VALID_CODE_VERIFIER_PATTERN = Pattern.compile("^[0-9a-zA-Z\\-\\.~_]+$");

    /** 生成符合 RFC 7636 的随机 code_verifier（Base64URL 编码 64 字节） */
    public static String generateCodeVerifier() {
        return Base64Url.encode(SecretGenerator.getInstance().randomBytes(64));
    }

    /**
     * 按 method 将 code_verifier 编码为 code_challenge。
     * @param codeVerifier 原始 verifier
     * @param codeChallengeMethod plain 或 S256
     * @return code_challenge，失败时 null
     */
        try {
            switch (codeChallengeMethod) {
                case OAuth2Constants.PKCE_METHOD_S256:
                    return generateS256CodeChallenge(codeVerifier);
                case OAuth2Constants.PKCE_METHOD_PLAIN:
                    // plain 模式直接返回 verifier
                default:
                    return codeVerifier;
            }
        } catch(Exception ex) {
            return null;
        }
    }

    // RFC 7636 §4.6：S256 code_challenge 计算
    /** 计算 S256 code_challenge：BASE64URL(SHA256(code_verifier)) */
    public static String generateS256CodeChallenge(String codeVerifier) throws HashException {
        return HashUtils.sha256UrlEncodedHash(codeVerifier, StandardCharsets.ISO_8859_1);
    }

    /**
     * 校验 code_verifier 与 code_challenge 是否匹配。
     * @param verifier code_verifier
     * @param codeChallenge 授权请求中的 code_challenge
     * @param codeChallengeMethod plain 或 S256
     * @return 匹配则 true
     */

        try {
            switch (codeChallengeMethod) {
                case OAuth2Constants.PKCE_METHOD_PLAIN:
                    return verifier.equals(codeChallenge);
                case OAuth2Constants.PKCE_METHOD_S256:
                    return generateS256CodeChallenge(verifier).equals(codeChallenge);
                default:
                    return false;
            }
        } catch(Exception ex) {
            return false;
        }
    }

    /** 强制 PKCE 客户端：必须提供 code_verifier 并通过校验 */
    public static void checkParamsForPkceEnforcedClient(String codeVerifier, String codeChallenge, String codeChallengeMethod, String authUserId, String authUsername, EventBuilder event, Cors cors) {
        // 强制 PKCE 时 code_verifier 不可缺失
        if (codeVerifier == null) {
            String errorMessage = "PKCE code verifier not specified";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.CODE_VERIFIER_MISSING);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_GRANT, errorMessage, Response.Status.BAD_REQUEST);
        }
        verifyCodeVerifier(codeVerifier, codeChallenge, codeChallengeMethod, authUserId, authUsername, event, cors);
    }

    /** 非强制 PKCE 客户端：challenge 与 verifier 须成对出现 */
    public static void checkParamsForPkceNotEnforcedClient(String codeVerifier, String codeChallenge, String codeChallengeMethod, String authUserId, String authUsername, EventBuilder event, Cors cors) {
        if (codeChallenge != null && codeVerifier == null) {
            String errorMessage = "PKCE code verifier not specified";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.CODE_VERIFIER_MISSING);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_GRANT, errorMessage, Response.Status.BAD_REQUEST);
        }

        if (codeChallenge == null && codeVerifier != null) {
            String errorMessage = "PKCE code verifier specified but challenge not present in authorization";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.INVALID_CODE_VERIFIER);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_GRANT, errorMessage, Response.Status.BAD_REQUEST);
        }

        if (codeChallenge != null) {
            verifyCodeVerifier(codeVerifier, codeChallenge, codeChallengeMethod, authUserId, authUsername, event, cors);
        }
    }

    /**
     * 校验 code_verifier 格式与 challenge 匹配；失败时抛出 {@link CorsErrorResponseException}。
     * @param codeVerifier token 请求中的 verifier
     * @param codeChallenge 授权阶段保存的 challenge
     * @param codeChallengeMethod plain 或 S256
     */
        // 校验 code_verifier 长度与字符集（RFC 7636）

        if (!isValidPkceCodeVerifier(codeVerifier)) {
            String errorReason = "Invalid code verifier";
            String errorMessage = "PKCE verification failed: " + errorReason;
            event.detail(Details.REASON, errorReason);
            event.error(Errors.INVALID_CODE_VERIFIER);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_GRANT, errorMessage, Response.Status.BAD_REQUEST);
        }

        logger.debugf("PKCE supporting Client, codeVerifier = %s", codeVerifier);
        String codeVerifierEncoded = codeVerifier;
        try {
            // RFC 7636 §4.2：plain 或 S256 编码 verifier 后与 challenge 比对
            // plain or S256
            if (codeChallengeMethod != null && codeChallengeMethod.equals(OAuth2Constants.PKCE_METHOD_S256)) {
                logger.debugf("PKCE codeChallengeMethod = %s", codeChallengeMethod);
                codeVerifierEncoded = PkceUtils.generateS256CodeChallenge(codeVerifier);
            } else {
                logger.debug("PKCE codeChallengeMethod is plain");
                codeVerifierEncoded = codeVerifier;
            }
        } catch (Exception nae) {
            String errorReason = "Unsupported algorithm specified";
            String errorMessage = "PKCE verification failed: " + errorReason;
            event.detail(Details.REASON, errorReason);
            event.error(Errors.PKCE_VERIFICATION_FAILED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_GRANT, errorMessage, Response.Status.BAD_REQUEST);
        }
        if (!codeChallenge.equals(codeVerifierEncoded)) {
            String errorReason = "Code mismatch";
            String errorMessage = "PKCE verification failed: " + errorReason;
            event.detail(Details.REASON, errorReason);
            event.error(Errors.PKCE_VERIFICATION_FAILED);
            throw new CorsErrorResponseException(cors, OAuthErrorException.INVALID_GRANT, errorMessage, Response.Status.BAD_REQUEST);
        } else {
            logger.debugf("PKCE verification success. codeVerifierEncoded = %s, codeChallenge = %s", codeVerifierEncoded, codeChallenge);
        }
    }

    /** 校验 code_verifier 长度与允许的字符集 */
    private static boolean isValidPkceCodeVerifier(String codeVerifier) {
        if (codeVerifier.length() < OIDCLoginProtocol.PKCE_CODE_VERIFIER_MIN_LENGTH) {
            logger.debugf(" Error: PKCE codeVerifier length under lower limit , codeVerifier = %s", codeVerifier);
            return false;
        }
        if (codeVerifier.length() > OIDCLoginProtocol.PKCE_CODE_VERIFIER_MAX_LENGTH) {
            logger.debugf(" Error: PKCE codeVerifier length over upper limit , codeVerifier = %s", codeVerifier);
            return false;
        }
        Matcher m = VALID_CODE_VERIFIER_PATTERN.matcher(codeVerifier);
        return m.matches();
    }
}
