/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.filter;

import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.config.NacosAuthConfig;
import com.alibaba.nacos.auth.serveridentity.ServerIdentityResult;
import com.alibaba.nacos.core.auth.AbstractWebAuthFilter;
import com.alibaba.nacos.core.code.ControllerMethodsCache;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Nacos 控制台 Web 鉴权过滤器：继承 {@link AbstractWebAuthFilter}，按控制台鉴权配置拦截请求。
 * Nacos Console web auth filter.
 *
 * @author xiweng.yy
 */
public class NacosConsoleAuthFilter extends AbstractWebAuthFilter {
    
    /** 控制台鉴权配置，用于判断是否启用鉴权 */
    private final NacosAuthConfig authConfig;
    
    /** 构造过滤器并注入鉴权配置与控制器方法缓存（供 {@link Secured} 解析） */
    public NacosConsoleAuthFilter(NacosAuthConfig authConfig, ControllerMethodsCache methodsCache) {
        super(authConfig, methodsCache);
        this.authConfig = authConfig;
    }
    
    /** {@inheritDoc} 是否启用控制台鉴权，读取 {@link NacosAuthConfig#isAuthEnabled()} */
    @Override
    protected boolean isAuthEnabled() {
        return authConfig.isAuthEnabled();
    }
    
    /** {@inheritDoc} 控制台不启用 Server Identity 校验，始终返回未匹配 */
    @Override
    protected ServerIdentityResult checkServerIdentity(HttpServletRequest request,
        Secured secured) {
        return ServerIdentityResult.noMatched();
    }
}
