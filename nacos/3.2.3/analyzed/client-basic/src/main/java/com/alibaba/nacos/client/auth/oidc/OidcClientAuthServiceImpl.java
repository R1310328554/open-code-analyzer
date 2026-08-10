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

package com.alibaba.nacos.client.auth.oidc;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;
import com.alibaba.nacos.plugin.auth.api.RequestResource;
import com.alibaba.nacos.plugin.auth.constant.OidcProtocolConstants;
import com.alibaba.nacos.plugin.auth.spi.client.AbstractClientAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * OIDC Client Authentication Service implementation.
 * <p>基于 OIDC 的 Nacos 客户端鉴权实现：通过 OAuth2 Client Credentials 向 IdP 换取 accessToken，并写入 {@link LoginIdentityContext} 供后续请求携带 Bearer 头或 accessToken 参数。</p>
 *
 * <p>Implements the {@link AbstractClientAuthService} SPI to provide OIDC-based
 * authentication for Nacos clients. Uses the OAuth2 Client Credentials Grant
 * to obtain access tokens from the Identity Provider.
 *
 * <p>Flow:
 * <ol>
 *   <li>{@link #login(Properties)} is called periodically by the framework</li>
 *   <li>On first call with OIDC configured: performs OIDC Discovery and obtains access token</li>
 *   <li>On subsequent calls: checks if token needs refresh and refreshes if needed</li>
 *   <li>{@link #getLoginIdentityContext(RequestResource)} returns the context with accessToken</li>
 * </ol>
 *
 * @author wangzji
 */
public class OidcClientAuthServiceImpl extends AbstractClientAuthService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcClientAuthServiceImpl.class);
    
    /** 保护上下文初始化与令牌刷新的互斥锁 */
    private final Object refreshLock = new Object();
    
    /** OIDC 配置与 Discovery 状态 */
    private volatile OidcClientContext context;
    
    /** 访问令牌生命周期管理 */
    private volatile OidcTokenHolder tokenHolder;
    
    /** 对外暴露的登录身份上下文 */
    private volatile LoginIdentityContext loginIdentityContext = new LoginIdentityContext();
    
    /**
     * Whether OIDC has been determined to be unconfigured.
     * 置为 true 后后续 {@link #login(Properties)} 直接跳过 OIDC 流程（未配置 client-id/secret）。
     */
    private volatile boolean oidcNotConfigured = false;
    
    @Override
    public Boolean login(Properties properties) {
        try {
            // 快速路径：已判定未配置 OIDC 则不再尝试
            if (oidcNotConfigured) {
                return true;
            }
            
            // 步骤 1：首次调用时双检锁初始化上下文
            if (context == null) {
                synchronized (refreshLock) {
                    if (context == null) {
                        OidcClientContext newContext = new OidcClientContext();
                        boolean configured = newContext.init(properties);
                        if (!configured) {
                            oidcNotConfigured = true;
                            LOGGER.debug(
                                "[OIDC-CLIENT] OIDC not configured (missing client-id/client-secret), skipping");
                            return true;
                        }
                        this.tokenHolder = new OidcTokenHolder();
                        this.context = newContext;
                        LOGGER.info("[OIDC-CLIENT] OIDC client configured, client-id: {}",
                            context.getClientId());
                    }
                }
            }
            
            // 步骤 2：尚未 Discovery 时拉取 .well-known 配置
            if (!context.isDiscovered()) {
                boolean discoveryResult = context.discover();
                if (!discoveryResult) {
                    LOGGER.warn(
                        "[OIDC-CLIENT] OIDC Discovery failed, will retry on next login cycle");
                    return false;
                }
            }
            
            // 步骤 3：令牌过期或进入刷新窗口时双检锁刷新
            if (tokenHolder.isExpiredOrNeedRefresh()) {
                synchronized (refreshLock) {
                    if (tokenHolder.isExpiredOrNeedRefresh()) {
                        boolean tokenResult = tokenHolder.fetchToken(context);
                        if (!tokenResult) {
                            LOGGER.warn(
                                "[OIDC-CLIENT] Token fetch failed, will retry on next login cycle");
                            return false;
                        }
                        
                        // 步骤 4：同步更新 Authorization Bearer 与 accessToken 双通道参数
                        LoginIdentityContext newCtx = new LoginIdentityContext();
                        String token = tokenHolder.getAccessToken();
                        newCtx.setParameter(OidcProtocolConstants.AUTHORIZATION_HEADER,
                            OidcProtocolConstants.BEARER_PREFIX + token);
                        newCtx.setParameter(OidcProtocolConstants.ACCESS_TOKEN_PARAM, token);
                        this.loginIdentityContext = newCtx;
                        
                        LOGGER.debug(
                            "[OIDC-CLIENT] LoginIdentityContext updated with new access token");
                    }
                }
            }
            
            return true;
        } catch (Throwable throwable) {
            LOGGER.warn("[OIDC-CLIENT] login failed, error: ", throwable);
            return false;
        }
    }
    
    /** {@inheritDoc} 返回含 Bearer 与 accessToken 的 OIDC 身份上下文。 */
    @Override
    public LoginIdentityContext getLoginIdentityContext(RequestResource resource) {
        return this.loginIdentityContext;
    }
    
    /** {@inheritDoc} 记录 OIDC 客户端鉴权服务关闭日志。 */
    @Override
    public void shutdown() throws NacosException {
        LOGGER.info("[OIDC-CLIENT] Shutting down OIDC client auth service");
    }
}
