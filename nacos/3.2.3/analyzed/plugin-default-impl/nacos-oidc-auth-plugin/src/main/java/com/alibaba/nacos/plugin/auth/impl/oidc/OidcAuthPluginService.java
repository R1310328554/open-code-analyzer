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
import com.alibaba.nacos.plugin.auth.api.Permission;
import com.alibaba.nacos.plugin.auth.api.Resource;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.OidcProtocolConstants;
import com.alibaba.nacos.plugin.auth.spi.server.AuthPluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * OIDC 认证插件服务实现。
 *
 * <p>作为 Nacos 认证插件 SPI 的入口，将身份校验与权限校验分别委托给
 * {@link IdentityProvider} 与 {@link AuthorityProvider} 的具体实现。</p>
 *
 * @author WangzJi
 */
@SuppressWarnings("PMD")
public class OidcAuthPluginService implements AuthPluginService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthPluginService.class);
    
    /**
     * 本插件在请求中识别的身份凭证名称列表（Authorization 头与 accessToken 参数）。
     */
    private static final List<String> IDENTITY_NAMES = Arrays.asList(
        OidcProtocolConstants.AUTHORIZATION_HEADER,
        OidcProtocolConstants.ACCESS_TOKEN_PARAM);
    
    private volatile IdentityProvider identityProvider;
    private volatile AuthorityProvider authorityProvider;
    
    @Override
    public Collection<String> identityNames() {
        return IDENTITY_NAMES;
    }
    
    @Override
    public boolean enableAuth(ActionTypes action, String type) {
        // 对所有操作类型与资源类型均启用 OIDC 认证
        return true;
    }
    
    @Override
    public AuthResult validateIdentity(IdentityContext identityContext, Resource resource) {
        initializeIfNeeded();
        return identityProvider.validateIdentity(identityContext, resource);
    }
    
    @Override
    public AuthResult validateAuthority(IdentityContext identityContext, Permission permission) {
        initializeIfNeeded();
        return authorityProvider.validateAuthority(identityContext, permission);
    }
    
    @Override
    public String getAuthServiceName() {
        return OidcProtocolConstants.AUTH_PLUGIN_TYPE;
    }
    
    @Override
    public boolean isLoginEnabled() {
        // 启用登录能力，具体流程由 OidcLoginController 处理
        return true;
    }
    
    @Override
    public boolean isAdminRequest() {
        // 无需初始化本地管理员账户，用户管理完全由外部 IdP 负责
        return false;
    }
    
    /**
     * 延迟初始化身份与权限提供者（双重检查锁，线程安全）。
     */
    private void initializeIfNeeded() {
        if (identityProvider == null || authorityProvider == null) {
            synchronized (this) {
                if (identityProvider == null) {
                    identityProvider = new OidcIdentityProvider();
                }
                if (authorityProvider == null) {
                    authorityProvider = new OidcAuthorityProvider();
                }
            }
        }
    }
}
