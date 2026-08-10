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

package com.alibaba.nacos.plugin.auth.impl.oidc.token;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.oidc.config.OidcAuthConfig;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * OAuth2/OIDC JWT 令牌校验器。
 *
 * <p>基于 {@link JwksProvider} 提供的公钥验证签名，并校验过期时间、Issuer、
 * Audience 等声明。支持 IdP 密钥轮换场景下的 JWKS 刷新重试。</p>
 *
 * @author WangzJi
 */
public class JwtTokenValidator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenValidator.class);
    
    /** 单例实例。 */
    private static volatile JwtTokenValidator instance;
    
    /** OIDC 认证配置。 */
    private final OidcAuthConfig config;
    
    /** JWKS 公钥提供者。 */
    private final JwksProvider jwksProvider;
    
    /** 懒加载的 JWT 处理器，持有当前 JWKS 对应的验签逻辑。 */
    private volatile ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    
    /** 校验时必须存在的 JWT 声明字段集合。 */
    private static final Set<String> REQUIRED_CLAIMS =
        new HashSet<>(Arrays.asList("sub", "iss", "exp", "iat"));
    
    /** OIDC 场景支持的 JWS 签名算法集合。 */
    private static final Set<JWSAlgorithm> SUPPORTED_ALGORITHMS = new HashSet<>(Arrays.asList(
        JWSAlgorithm.RS256, JWSAlgorithm.RS384, JWSAlgorithm.RS512,
        JWSAlgorithm.ES256, JWSAlgorithm.ES384, JWSAlgorithm.ES512,
        JWSAlgorithm.PS256, JWSAlgorithm.PS384, JWSAlgorithm.PS512));
    
    private JwtTokenValidator() {
        this.config = OidcAuthConfig.getInstance();
        this.jwksProvider = JwksProvider.getInstance();
    }
    
    /**
     * 获取单例实例。
     *
     * @return JwtTokenValidator 实例
     */
    public static JwtTokenValidator getInstance() {
        if (instance == null) {
            synchronized (JwtTokenValidator.class) {
                if (instance == null) {
                    instance = new JwtTokenValidator();
                }
            }
        }
        return instance;
    }
    
    /**
     * 校验 JWT 令牌并返回已验证的声明集合。
     *
     * <p>签名验证失败时会尝试刷新 JWKS 并重试一次，以应对 IdP 密钥轮换。</p>
     *
     * @param token JWT 令牌字符串
     * @return 校验通过的 JWT 声明
     * @throws AccessException 令牌为空、格式非法或校验未通过时抛出
     */
    public JWTClaimsSet validate(String token) throws AccessException {
        if (StringUtils.isBlank(token)) {
            throw new AccessException("Token is empty");
        }
        
        try {
            // 懒加载初始化 JWT 处理器
            ConfigurableJWTProcessor<SecurityContext> processor = getJwtProcessor();
            
            // process 内部完成解析与签名验证
            JWTClaimsSet claims = processor.process(token, null);
            
            // 额外业务层声明校验
            validateClaims(claims);
            
            LOGGER.debug("Token validated successfully for subject: {}", claims.getSubject());
            return claims;
            
        } catch (ParseException e) {
            LOGGER.warn("Failed to parse JWT token: {}", e.getMessage());
            throw new AccessException("Invalid token format");
        } catch (BadJOSEException e) {
            LOGGER.warn("JWT signature verification failed: {}", e.getMessage());
            // 密钥轮换场景：刷新 JWKS 后重试一次
            return retryWithRefreshedJwks(token, e);
        } catch (JOSEException e) {
            LOGGER.warn("JWT processing error: {}", e.getMessage());
            throw new AccessException("Token processing error");
        } catch (AccessException e) {
            throw e;
        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.error("Invalid token data: {}", e.getMessage(), e);
            throw new AccessException("Invalid token format: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during token validation: {} - {}",
                e.getClass().getSimpleName(), e.getMessage(), e);
            throw new AccessException("Token validation failed: " + e.getClass().getSimpleName());
        }
    }
    
    /**
     * 懒加载获取 JWT 处理器，首次调用时基于当前 JWKS 构建。
     */
    private ConfigurableJWTProcessor<SecurityContext> getJwtProcessor() throws AccessException {
        if (jwtProcessor == null) {
            synchronized (this) {
                if (jwtProcessor == null) {
                    try {
                        jwtProcessor = createJwtProcessor(jwksProvider.getJwkSet());
                    } catch (IOException e) {
                        throw new AccessException(
                            "Failed to initialize JWT processor: " + e.getMessage());
                    }
                }
            }
        }
        return jwtProcessor;
    }
    
    /**
     * 根据给定 JWKS 创建配置好验签与声明校验规则的 JWT 处理器。
     */
    private ConfigurableJWTProcessor<SecurityContext> createJwtProcessor(JWKSet jwkSet) {
        ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
            SUPPORTED_ALGORITHMS,
            new ImmutableJWKSet<>(jwkSet));
        processor.setJWSKeySelector(keySelector);
        
        // 配置 Issuer 与必填声明校验器
        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
            new JWTClaimsSet.Builder()
                .issuer(config.getIssuerUri())
                .build(),
            REQUIRED_CLAIMS));
        
        return processor;
    }
    
    /**
     * 签名验证失败后刷新 JWKS 并重试校验（应对密钥轮换）。
     *
     * @param token               原始 JWT 令牌
     * @param originalException   首次验签失败的异常
     * @return 重试成功后得到的声明集合
     * @throws AccessException 刷新后仍校验失败时抛出
     */
    private JWTClaimsSet retryWithRefreshedJwks(String token, Exception originalException)
        throws AccessException {
        LOGGER.info("Retrying token validation with refreshed JWKS");
        
        try {
            // 强制刷新 JWKS 缓存
            JWKSet jwkSet = jwksProvider.refreshJwkSet();
            
            // 用新公钥重建处理器
            synchronized (this) {
                this.jwtProcessor = createJwtProcessor(jwkSet);
            }
            
            JWTClaimsSet claims = this.jwtProcessor.process(token, null);
            validateClaims(claims);
            
            LOGGER.info("Token validated successfully after JWKS refresh");
            return claims;
            
        } catch (Exception e) {
            LOGGER.warn("Token validation failed even after JWKS refresh: {}", e.getMessage());
            throw new AccessException("Token signature verification failed");
        }
    }
    
    /**
     * 执行业务层附加声明校验：过期、生效时间、Audience 与 Issuer。
     *
     * @param claims JWT 声明集合
     * @throws AccessException 任一校验项未通过时抛出
     */
    private void validateClaims(JWTClaimsSet claims) throws AccessException {
        // 校验过期时间
        Date expirationTime = claims.getExpirationTime();
        if (expirationTime == null || expirationTime.before(new Date())) {
            throw new AccessException("Token has expired");
        }
        
        // 校验 not-before（若存在）
        Date notBeforeTime = claims.getNotBeforeTime();
        if (notBeforeTime != null && notBeforeTime.after(new Date())) {
            throw new AccessException("Token is not yet valid");
        }
        
        // 校验 audience（若已配置 clientId）
        String clientId = config.getClientId();
        if (StringUtils.isNotBlank(clientId)) {
            List<String> audience = claims.getAudience();
            if (audience != null && !audience.isEmpty() && !audience.contains(clientId)) {
                // 回退检查 azp（authorized party）声明
                String azp = (String) claims.getClaim("azp");
                if (!clientId.equals(azp)) {
                    String message = String.format(
                        "Token audience mismatch. Expected: %s, Got: %s, azp: %s",
                        clientId, audience, azp);
                    
                    if (config.isStrictAudienceValidation()) {
                        LOGGER.error("{} - Strict validation enabled, rejecting token. "
                            + "This token may be intended for a different client.", message);
                        throw new AccessException("Token audience validation failed");
                    } else {
                        LOGGER.warn("{} - Strict validation disabled, accepting token. "
                            + "Set 'nacos.core.auth.plugin.oidc.strict-audience-validation=true' for better security.",
                            message);
                    }
                }
            }
        }
        
        // 校验 Issuer，兼容末尾斜杠差异
        String issuer = claims.getIssuer();
        String expectedIssuer = config.getIssuerUri();
        if (StringUtils.isNotBlank(expectedIssuer) && !expectedIssuer.equals(issuer)) {
            String normalizedExpected = expectedIssuer.endsWith("/")
                ? expectedIssuer.substring(0, expectedIssuer.length() - 1)
                : expectedIssuer;
            String normalizedIssuer = issuer != null && issuer.endsWith("/")
                ? issuer.substring(0, issuer.length() - 1)
                : issuer;
            
            if (!normalizedExpected.equals(normalizedIssuer)) {
                throw new AccessException("Token issuer mismatch");
            }
        }
    }
    
    /**
     * 从 JWT 声明中提取用户名。
     *
     * <p>优先使用配置项指定的 claim，依次回退 preferred_username、email，最后使用 sub。</p>
     *
     * @param claims JWT 声明集合
     * @return 解析得到的用户名
     */
    public String extractUsername(JWTClaimsSet claims) {
        String usernameClaim = config.getUsernameClaim();
        
        // 优先读取配置的 claim
        Object username = claims.getClaim(usernameClaim);
        if (username != null) {
            return username.toString();
        }
        
        // 回退常见用户名声明
        String preferredUsername = (String) claims.getClaim("preferred_username");
        if (StringUtils.isNotBlank(preferredUsername)) {
            return preferredUsername;
        }
        
        String email = (String) claims.getClaim("email");
        if (StringUtils.isNotBlank(email)) {
            return email;
        }
        
        // 最终回退到 subject
        return claims.getSubject();
    }
    
    /**
     * 从 JWT 声明中提取角色列表。
     *
     * <p>依次尝试配置的 roles claim、Keycloak 的 realm_access/resource_access 结构
     * 以及 groups 声明；均未命中时返回空列表并记录警告日志。</p>
     *
     * @param claims JWT 声明集合
     * @return 角色名称列表，可能为空
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(JWTClaimsSet claims) {
        String rolesClaim = config.getRolesClaim();
        
        // 尝试配置的 roles claim
        Object roles = claims.getClaim(rolesClaim);
        if (roles instanceof List) {
            return (List<String>) roles;
        }
        
        // Keycloak realm_access.roles 格式
        Object realmAccess = claims.getClaim("realm_access");
        if (realmAccess instanceof java.util.Map) {
            Object realmRoles = ((java.util.Map<String, Object>) realmAccess).get("roles");
            if (realmRoles instanceof List) {
                return (List<String>) realmRoles;
            }
        }
        
        // Keycloak resource_access.<client_id>.roles 格式
        Object resourceAccess = claims.getClaim("resource_access");
        if (resourceAccess instanceof java.util.Map) {
            String clientId = config.getClientId();
            if (StringUtils.isNotBlank(clientId)) {
                Object clientAccess =
                    ((java.util.Map<String, Object>) resourceAccess).get(clientId);
                if (clientAccess instanceof java.util.Map) {
                    Object clientRoles =
                        ((java.util.Map<String, Object>) clientAccess).get("roles");
                    if (clientRoles instanceof List) {
                        return (List<String>) clientRoles;
                    }
                }
            }
        }
        
        // 部分 IdP 使用的 groups 声明
        Object groups = claims.getClaim("groups");
        if (groups instanceof List) {
            return (List<String>) groups;
        }
        
        // 未找到角色信息时记录诊断日志
        LOGGER.warn(
            "No roles found in JWT claims for user: {}. Checked claim paths: {}, realm_access.roles, "
                + "resource_access.{}.roles, groups. Token may be missing role information.",
            claims.getSubject(), rolesClaim, config.getClientId());
        return Collections.emptyList();
    }
    
    /**
     * 判断声明中的角色是否包含配置的管理员角色。
     *
     * @param claims JWT 声明集合
     * @return 若用户拥有管理员角色则返回 {@code true}
     */
    public boolean isAdmin(JWTClaimsSet claims) {
        List<String> roles = extractRoles(claims);
        String adminRole = config.getAdminRole();
        return roles.contains(adminRole);
    }
}
