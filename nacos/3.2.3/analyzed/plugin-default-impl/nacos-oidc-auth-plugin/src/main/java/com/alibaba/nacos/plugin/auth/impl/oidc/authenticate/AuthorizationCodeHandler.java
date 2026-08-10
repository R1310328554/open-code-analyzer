/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.oidc.authenticate;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.oidc.config.OidcAuthConfig;
import com.alibaba.nacos.plugin.auth.impl.oidc.constant.OidcConstants;
import com.alibaba.nacos.plugin.auth.impl.oidc.identity.OidcUserMapper;
import com.alibaba.nacos.plugin.auth.impl.oidc.identity.OidcUserMapper.OidcUser;
import com.alibaba.nacos.plugin.auth.impl.oidc.token.JwtTokenValidator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * OIDC 授权码（Authorization Code）流程处理器。
 *
 * <p>负责构建 IdP 授权跳转 URL、用授权码换取令牌、校验 ID Token 中的 nonce，
 * 以及构建 RP 发起的登出 URL。state 采用 HMAC 自包含签名，无需服务端会话存储，集群友好。</p>
 *
 * @author WangzJi
 */
public class AuthorizationCodeHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationCodeHandler.class);
    
    private static volatile AuthorizationCodeHandler instance;
    
    private final OidcAuthConfig config;
    
    private final JwtTokenValidator tokenValidator;
    
    private final OidcUserMapper userMapper;
    
    private final SecureRandom secureRandom;
    
    /**
     * state 参数过期时间（毫秒），默认 10 分钟。
     */
    private static final long STATE_EXPIRATION_MS = 10 * 60 * 1000L;
    
    /**
     * state 签名使用的 HMAC 算法（HmacSHA256）。
     */
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    
    private AuthorizationCodeHandler() {
        this.config = OidcAuthConfig.getInstance();
        this.tokenValidator = JwtTokenValidator.getInstance();
        this.userMapper = OidcUserMapper.getInstance();
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * 获取单例实例。
     *
     * @return AuthorizationCodeHandler 实例
     */
    public static AuthorizationCodeHandler getInstance() {
        if (instance == null) {
            synchronized (AuthorizationCodeHandler.class) {
                if (instance == null) {
                    instance = new AuthorizationCodeHandler();
                }
            }
        }
        return instance;
    }
    
    /**
     * 构建 IdP 授权跳转 URL，供浏览器重定向至身份提供商登录页。
     *
     * @param redirectUri 认证完成后的回调 URI
     * @return 完整的授权 URL
     * @throws AccessException 配置无效或构建失败时抛出
     */
    public String buildAuthorizationUrl(String redirectUri) throws AccessException {
        try {
            String authEndpoint = config.getAuthorizationEndpoint();
            if (StringUtils.isBlank(authEndpoint)) {
                throw new AccessException("Authorization endpoint not configured");
            }
            
            // 生成 nonce 防重放，并计算 state 过期时间
            String nonce = generateSecureToken();
            long expirationTime = System.currentTimeMillis() + STATE_EXPIRATION_MS;
            
            // 构建自包含签名 state：base64(nonce.expTime.signature)，无需服务端缓存
            String state = buildSignedState(nonce, expirationTime);
            
            // 组装 OIDC 认证请求（授权码模式 + scope + nonce）
            AuthenticationRequest authRequest = new AuthenticationRequest.Builder(
                new ResponseType("code"),
                new Scope(config.getScope().split(" ")),
                new ClientID(config.getClientId()),
                URI.create(redirectUri))
                .endpointURI(URI.create(authEndpoint))
                .state(new State(state))
                .nonce(new Nonce(nonce))
                .build();
            
            String authUrl = authRequest.toURI().toString();
            LOGGER.debug("Built authorization URL: {}", authUrl);
            return authUrl;
            
        } catch (AccessException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Failed to build authorization URL", e);
            throw new AccessException("Failed to initiate login: " + e.getMessage());
        }
    }
    
    /**
     * 用授权码换取令牌并完成用户认证。
     *
     * @param code        IdP 回调返回的授权码
     * @param state       CSRF 防护用的 state 参数
     * @param redirectUri 与登录请求一致的回调 URI
     * @return 认证成功的 OidcUser
     * @throws AccessException 认证失败时抛出
     */
    public OidcUser exchangeCodeForUser(String code, String state, String redirectUri)
        throws AccessException {
        try {
            // 校验并解码自包含 state（无需查缓存）
            StateData stateData = verifyAndDecodeState(state);
            if (stateData == null) {
                throw new AccessException("Invalid or expired state parameter");
            }
            
            // 向 IdP 令牌端点换取 OIDC 令牌
            OIDCTokens tokens = exchangeCodeForTokens(code, redirectUri);
            
            // 校验 ID Token 签名与声明
            String idTokenString = tokens.getIDTokenString();
            JWTClaimsSet claims = tokenValidator.validate(idTokenString);
            
            // 校验 nonce 一致性，防止令牌重放攻击
            String tokenNonce = (String) claims.getClaim("nonce");
            
            if (tokenNonce == null) {
                String message = "Nonce not present in ID token";
                if (config.isStrictNonceValidation()) {
                    LOGGER.error("{} - Strict validation enabled, rejecting authentication",
                        message);
                    throw new AccessException(message
                        + ". Set 'nacos.core.auth.plugin.oidc.strict-nonce-validation=false' "
                        + "if your IdP doesn't support nonce.");
                } else {
                    LOGGER.warn("{} - Strict validation disabled, allowing authentication. "
                        + "This reduces protection against replay attacks.", message);
                }
            } else if (!stateData.nonce.equals(tokenNonce)) {
                String message = String.format("Nonce mismatch: expected %s, got %s",
                    stateData.nonce, tokenNonce);
                LOGGER.error("{} - Possible token replay attack detected", message);
                throw new AccessException(message);
            }
            
            // 将 JWT 声明映射为 Nacos 用户对象
            OidcUser user = userMapper.mapToUser(claims);
            user.setToken(tokens.getAccessToken().getValue());
            
            LOGGER.info("User authenticated via authorization code: {}", user.getUsername());
            return user;
            
        } catch (AccessException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Failed to exchange code for tokens", e);
            throw new AccessException("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * 向 IdP 令牌端点发送授权码换取 OIDC 令牌。
     *
     * @param code        授权码
     * @param redirectUri 回调 URI
     * @return OIDC 令牌集合
     * @throws Exception 交换失败时抛出
     */
    private OIDCTokens exchangeCodeForTokens(String code, String redirectUri) throws Exception {
        String tokenEndpoint = config.getTokenEndpoint();
        if (StringUtils.isBlank(tokenEndpoint)) {
            throw new AccessException("Token endpoint not configured");
        }
        
        // 构建授权码 grant 请求
        AuthorizationCode authCode = new AuthorizationCode(code);
        AuthorizationGrant grant = new AuthorizationCodeGrant(authCode, URI.create(redirectUri));
        
        // 客户端密钥认证（Client Secret Basic）
        ClientAuthentication clientAuth = new ClientSecretBasic(
            new ClientID(config.getClientId()),
            new Secret(config.getClientSecret()));
        
        // 发送令牌请求并解析 OIDC 响应
        TokenRequest tokenRequest = new TokenRequest(
            URI.create(tokenEndpoint),
            clientAuth,
            grant);
        
        TokenResponse tokenResponse =
            OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());
        
        if (!tokenResponse.indicatesSuccess()) {
            String error = tokenResponse.toErrorResponse().getErrorObject().getDescription();
            LOGGER.error("Token exchange failed: {}", error);
            throw new AccessException("Token exchange failed: " + error);
        }
        
        OIDCTokenResponse oidcResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
        return oidcResponse.getOIDCTokens();
    }
    
    /**
     * 生成安全的随机 token（用于 state/nonce）。
     *
     * @return Base64 URL 编码的随机字符串
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * 构建自包含的 HMAC 签名 state 参数。
     *
     * <p>格式：base64(nonce.expirationTime.signature)，无需服务端存储，集群友好。</p>
     *
     * @param nonce          nonce 值
     * @param expirationTime 过期时间戳（毫秒）
     * @return 签名后的 state 字符串
     */
    private String buildSignedState(String nonce, long expirationTime) {
        String payload = nonce + "." + expirationTime;
        String signature = hmacSign(payload);
        String stateContent = payload + "." + signature;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
            stateContent.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 校验并解码签名 state 参数。
     *
     * @param state 回调请求中的 state 参数
     * @return 校验通过返回 StateData，否则返回 null
     */
    private StateData verifyAndDecodeState(String state) {
        try {
            String decoded =
                new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\.");
            if (parts.length != 3) {
                LOGGER.warn("Invalid state format: expected 3 parts, got {}", parts.length);
                return null;
            }
            
            String nonce = parts[0];
            long expTime = Long.parseLong(parts[1]);
            String signature = parts[2];
            
            // 校验 HMAC 签名
            String payload = nonce + "." + expTime;
            if (!hmacVerify(payload, signature)) {
                LOGGER.warn("State signature verification failed");
                return null;
            }
            
            // 校验是否已过期
            if (System.currentTimeMillis() > expTime) {
                LOGGER.warn("State has expired");
                return null;
            }
            
            return new StateData(nonce, expTime);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid expiration time in state: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid base64 encoding in state: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.warn("Failed to decode state: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 使用 HMAC-SHA256 对 payload 签名。
     *
     * @param payload 待签名的载荷
     * @return Base64 URL 编码的签名
     */
    private String hmacSign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                getSigningKey().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign payload", e);
        }
    }
    
    /**
     * 校验 HMAC 签名是否与 payload 匹配。
     *
     * @param payload   原始载荷
     * @param signature 待校验的签名
     * @return 签名有效返回 true
     */
    private boolean hmacVerify(String payload, String signature) {
        String expectedSignature = hmacSign(payload);
        return expectedSignature.equals(signature);
    }
    
    /**
     * 获取 HMAC 签名密钥（使用 client secret）。
     *
     * @return 签名密钥字符串
     */
    private String getSigningKey() {
        String clientSecret = config.getClientSecret();
        if (StringUtils.isBlank(clientSecret)) {
            throw new IllegalStateException("Client secret is required for state signing");
        }
        return clientSecret;
    }
    
    /**
     * 构建 RP 发起的登出 URL（依赖 IdP 的 end_session_endpoint）。
     *
     * @param idToken     ID Token，作为 id_token_hint
     * @param redirectUri 登出后的重定向 URI
     * @return 登出 URL；IdP 不支持时返回 null
     */
    public String buildLogoutUrl(String idToken, String redirectUri) {
        String endSessionEndpoint = config.getEndSessionEndpoint();
        if (StringUtils.isBlank(endSessionEndpoint)) {
            return null;
        }
        
        StringBuilder logoutUrl = new StringBuilder(endSessionEndpoint);
        logoutUrl.append(OidcConstants.QUERY_STRING_SEPARATOR);
        
        if (StringUtils.isNotBlank(idToken)) {
            logoutUrl.append("id_token_hint=").append(idToken);
        }
        
        if (StringUtils.isNotBlank(redirectUri)) {
            char lastChar = logoutUrl.charAt(logoutUrl.length() - 1);
            if (lastChar != OidcConstants.QUERY_STRING_SEPARATOR.charAt(0)) {
                logoutUrl.append("&");
            }
            logoutUrl.append("post_logout_redirect_uri=").append(redirectUri);
        }
        
        logoutUrl.append("&client_id=").append(config.getClientId());
        
        return logoutUrl.toString();
    }
    
    /**
     * CSRF 防护用的 state 解析结果（nonce + 过期时间）。
     */
    private static class StateData {
        
        final String nonce;
        
        final long expirationTime;
        
        StateData(String nonce, long expirationTime) {
            this.nonce = nonce;
            this.expirationTime = expirationTime;
        }
    }
}
