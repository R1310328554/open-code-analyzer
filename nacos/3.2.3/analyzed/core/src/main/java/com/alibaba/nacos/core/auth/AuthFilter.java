/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.auth;

import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.config.NacosAuthConfig;
import com.alibaba.nacos.auth.serveridentity.ServerIdentityResult;
import com.alibaba.nacos.core.code.ControllerMethodsCache;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 开放 API HTTP 鉴权过滤器：处理除 {@link ApiType#ADMIN_API} 外的 Secured 接口，并在集群升级期间对内部 API 兼容旧版无身份头逻辑。
 * Unified filter to handle authentication and authorization.
 *
 * @author nkorange
 * @since 1.2.0
 */
public class AuthFilter extends AbstractWebAuthFilter {
    
    private final NacosAuthConfig authConfig;
    
    private final InnerApiAuthEnabled innerApiAuthEnabled;
    
    public AuthFilter(NacosAuthConfig authConfig, ControllerMethodsCache methodsCache,
        InnerApiAuthEnabled innerApiAuthEnabled) {
        super(authConfig, methodsCache);
        this.authConfig = authConfig;
        this.innerApiAuthEnabled = innerApiAuthEnabled;
    }
    
    @Override
    protected boolean isAuthEnabled() {
        return authConfig.isAuthEnabled();
    }
    
    @Override
    protected boolean isMatchFilter(Secured secured) {
        // 管理端 API 交由 {@link AuthAdminFilter} 处理
        return !ApiType.ADMIN_API.equals(secured.apiType());
    }
    
    @Override
    protected ServerIdentityResult checkServerIdentity(HttpServletRequest request,
        Secured secured) {
        // 升级过渡期：旧版节点内部 API 可能无 server identity，沿用旧逻辑放行
        if (ApiType.INNER_API.equals(secured.apiType()) && !innerApiAuthEnabled.isEnabled()) {
            return ServerIdentityResult.success();
        }
        return super.checkServerIdentity(request, secured);
    }
}
