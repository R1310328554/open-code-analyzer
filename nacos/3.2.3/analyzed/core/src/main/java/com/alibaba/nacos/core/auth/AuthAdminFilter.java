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
import com.alibaba.nacos.core.code.ControllerMethodsCache;

/**
 * 管理端（ADMIN_API）HTTP 鉴权过滤器：仅处理 {@link ApiType#ADMIN_API} 类型接口的认证与授权。
 * Unified filter to handle authentication and authorization.
 *
 * @author nkorange
 * @since 1.2.0
 */
public class AuthAdminFilter extends AbstractWebAuthFilter {
    
    private final NacosAuthConfig authConfig;
    
    public AuthAdminFilter(NacosAuthConfig authConfig, ControllerMethodsCache methodsCache) {
        super(authConfig, methodsCache);
        this.authConfig = authConfig;
    }
    
    @Override
    protected boolean isAuthEnabled() {
        return authConfig.isAuthEnabled();
    }
    
    @Override
    protected boolean isMatchFilter(Secured secured) {
        // 非管理端 API 由 {@link AuthFilter} 处理
        return ApiType.ADMIN_API.equals(secured.apiType());
    }
}
