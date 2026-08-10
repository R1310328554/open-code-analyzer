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
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.api.Permission;
import com.alibaba.nacos.plugin.auth.constant.OidcProtocolConstants;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.oidc.authorization.AuthorizationClient;
import com.alibaba.nacos.plugin.auth.impl.oidc.authorization.AuthorizationRequest;
import com.alibaba.nacos.plugin.auth.impl.oidc.authorization.AuthorizationResponse;
import com.alibaba.nacos.plugin.auth.impl.oidc.config.OidcAuthConfig;
import com.alibaba.nacos.plugin.auth.impl.oidc.constant.OidcConstants;
import com.alibaba.nacos.plugin.auth.impl.oidc.identity.OidcUserMapper;
import com.alibaba.nacos.plugin.auth.impl.oidc.identity.OidcUserMapper.OidcUser;
import com.alibaba.nacos.plugin.auth.impl.oidc.token.JwtTokenValidator;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OIDC 认证管理器。
 *
 * <p>负责 JWT 令牌校验、用户身份认证、权限委托（调用外部 IdP 授权端点），
 * 以及在身份上下文中存取 {@link OidcUser}。</p>
 *
 * @author WangzJi
 */
@SuppressWarnings("PMD")
public class OidcAuthenticationManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthenticationManager.class);
    
    private static volatile OidcAuthenticationManager instance;
    
    private final OidcAuthConfig config;
    
    private final JwtTokenValidator tokenValidator;
    
    private final OidcUserMapper userMapper;
    
    private OidcAuthenticationManager() {
        this.config = OidcAuthConfig.getInstance();
        this.tokenValidator = JwtTokenValidator.getInstance();
        this.userMapper = OidcUserMapper.getInstance();
    }
    
    /**
     * 获取单例实例。
     *
     * @return OidcAuthenticationManager 实例
     */
    public static OidcAuthenticationManager getInstance() {
        if (instance == null) {
            synchronized (OidcAuthenticationManager.class) {
                if (instance == null) {
                    instance = new OidcAuthenticationManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 通过 JWT 令牌认证用户。
     *
     * @param token 访问令牌或 ID 令牌
     * @return 认证成功的 OidcUser
     * @throws AccessException 认证失败时抛出
     */
    public OidcUser authenticate(String token) throws AccessException {
        if (StringUtils.isBlank(token)) {
            throw new AccessException("Token is required");
        }
        
        // 校验令牌签名与声明
        JWTClaimsSet claims = tokenValidator.validate(token);
        
        // 将 JWT 声明映射为 Nacos 用户
        OidcUser user = userMapper.mapToUser(claims);
        user.setToken(token);
        
        LOGGER.debug("User authenticated: {}", user.getUsername());
        return user;
    }
    
    /**
     * 从身份上下文中提取凭证并完成认证。
     *
     * @param identityContext 包含 Authorization 头或 accessToken 参数的身份上下文
     * @return 认证成功的 OidcUser
     * @throws AccessException 未找到有效令牌或认证失败时抛出
     */
    public OidcUser authenticate(IdentityContext identityContext) throws AccessException {
        // 优先从 Authorization 头提取 Bearer 令牌
        String token = extractBearerToken(identityContext);
        
        if (StringUtils.isBlank(token)) {
            // 回退至 accessToken 查询参数
            token = identityContext.getParameter(OidcProtocolConstants.ACCESS_TOKEN_PARAM, "");
        }
        
        if (StringUtils.isBlank(token)) {
            throw new AccessException("No valid OIDC token found");
        }
        
        return authenticate(token);
    }
    
    /**
     * 从身份上下文的 Authorization 头中提取 Bearer 令牌。
     *
     * @param identityContext 身份上下文
     * @return 令牌字符串；未找到时返回 null
     */
    private String extractBearerToken(IdentityContext identityContext) {
        String authHeader =
            identityContext.getParameter(OidcProtocolConstants.AUTHORIZATION_HEADER, "");
        if (StringUtils.isNotBlank(authHeader)
            && authHeader.startsWith(OidcProtocolConstants.BEARER_PREFIX)) {
            return authHeader.substring(OidcProtocolConstants.BEARER_PREFIX.length());
        }
        return null;
    }
    
    /**
     * 判断用户是否具备访问指定资源的权限。
     *
     * <p>授权决策完全委托外部 IdP，Nacos 本身不做权限判定。</p>
     *
     * @param user       已认证的 OidcUser
     * @param permission 待校验的权限
     * @return IdP 允许访问时返回 true
     */
    public boolean hasPermission(OidcUser user, Permission permission) {
        if (user == null) {
            return false;
        }
        
        // 组装授权请求（资源 URI + 操作）
        AuthorizationRequest request = AuthorizationRequest.builder()
            .token(user.getToken())
            .resourceType(permission.getResource().getType())
            .namespace(permission.getResource().getNamespaceId())
            .group(permission.getResource().getGroup())
            .resourceName(permission.getResource().getName())
            .action(permission.getAction())
            .build();
        
        // 调用 IdP 授权端点，由 IdP 返回允许/拒绝决策
        AuthorizationClient authzClient = AuthorizationClient.getInstance();
        AuthorizationResponse response = authzClient.authorize(request);
        
        if (response.isAllowed()) {
            LOGGER.debug("IdP authorized user {} for {}:{}", user.getUsername(),
                request.buildResourceUri(), permission.getAction());
            return true;
        } else {
            LOGGER.debug("IdP denied user {} access to {}:{}, reason: {}", user.getUsername(),
                request.buildResourceUri(), permission.getAction(), response.getReason());
            return false;
        }
    }
    
    /**
     * 判断用户是否为全局管理员。
     *
     * @param user OidcUser
     * @return 全局管理员返回 true
     */
    public boolean isGlobalAdmin(OidcUser user) {
        if (user == null) {
            return false;
        }
        return user.isGlobalAdmin();
    }
    
    /**
     * 从身份上下文中读取已认证用户（若存在）。
     *
     * @param identityContext 身份上下文
     * @return OidcUser；未认证时返回 null
     */
    public OidcUser getUserFromContext(IdentityContext identityContext) {
        Object user = identityContext.getParameter(OidcConstants.OAUTH2_USER_KEY);
        if (user instanceof OidcUser) {
            return (OidcUser) user;
        }
        return null;
    }
    
    /**
     * 将已认证用户写入身份上下文，供后续授权阶段使用。
     *
     * @param identityContext 身份上下文
     * @param user            待存储的 OidcUser
     */
    public void setUserInContext(IdentityContext identityContext, OidcUser user) {
        identityContext.setParameter(OidcConstants.OAUTH2_USER_KEY, user);
    }
}
