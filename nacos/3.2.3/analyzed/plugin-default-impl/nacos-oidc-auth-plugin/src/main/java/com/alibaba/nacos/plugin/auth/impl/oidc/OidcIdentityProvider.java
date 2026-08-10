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

package com.alibaba.nacos.plugin.auth.impl.oidc;

import com.alibaba.nacos.plugin.auth.api.AuthResult;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.api.Resource;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.oidc.authenticate.OidcAuthenticationManager;
import com.alibaba.nacos.plugin.auth.impl.oidc.config.OidcAuthConfig;
import com.alibaba.nacos.plugin.auth.impl.oidc.identity.OidcUserMapper.OidcUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * {@link IdentityProvider} 的 OIDC 实现。
 *
 * <p>从请求身份上下文中提取 JWT 令牌并完成校验，将认证后的 {@link OidcUser}
 * 写回上下文供后续权限校验使用。</p>
 *
 * @author WangzJi
 */
public class OidcIdentityProvider implements IdentityProvider {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcIdentityProvider.class);
    
    private volatile OidcAuthenticationManager authManager;
    private volatile OidcAuthConfig config;
    
    @Override
    public AuthResult validateIdentity(IdentityContext identityContext, Resource resource) {
        try {
            initializeIfNeeded();
            
            // 从令牌中认证用户身份
            OidcUser user = authManager.authenticate(identityContext);
            
            // 将用户对象写入上下文，供后续授权阶段复用
            authManager.setUserInContext(identityContext, user);
            
            // 同步写入身份 ID，便于日志与审计追踪
            identityContext.setParameter(
                Constants.Identity.IDENTITY_ID,
                user.getUsername());
            
            LOGGER.debug("OIDC identity validated: {}", user.getUsername());
            return AuthResult.successResult(user);
            
        } catch (AccessException e) {
            LOGGER.warn("OIDC identity validation failed: {}", e.getMessage());
            return AuthResult.failureResult(HttpStatus.UNAUTHORIZED.value(), e.getErrMsg());
        } catch (Exception e) {
            LOGGER.error("OIDC identity validation error", e);
            return AuthResult.failureResult(HttpStatus.UNAUTHORIZED.value(),
                "Authentication failed");
        }
    }
    
    private void initializeIfNeeded() {
        if (config == null) {
            synchronized (this) {
                if (config == null) {
                    config = OidcAuthConfig.getInstance();
                    authManager = OidcAuthenticationManager.getInstance();
                    LOGGER.info("OidcIdentityProvider initialized");
                }
            }
        }
    }
}
