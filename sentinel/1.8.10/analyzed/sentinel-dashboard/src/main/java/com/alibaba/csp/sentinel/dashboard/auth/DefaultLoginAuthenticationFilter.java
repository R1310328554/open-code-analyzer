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
package com.alibaba.csp.sentinel.dashboard.auth;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>默认登录认证 Servlet 过滤器。</p>
 *
 * <p>以下 URL 通常无需认证，例如：</p>
 * <ul>
 * <li>首页：{@code /}</li>
 * <li>登录/登出：{@code /login}、{@code /logout}</li>
 * <li>机器注册：{@code /registry/machine}</li>
 * <li>静态资源</li>
 * </ul>
 * <p>
 * 排除 URL 与后缀可在 {@code application.properties} 中配置。
 * </p>
 *
 * @author cdfive
 * @since 1.6.0
 */
public class DefaultLoginAuthenticationFilter implements LoginAuthenticationFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final String URL_SUFFIX_DOT = ".";

    /** 无需认证的 URL 列表（如 /auth/login、/registry/machine 等）。 */

    @Value("#{'${auth.filter.exclude-urls}'.split(',')}")
    private List<String> authFilterExcludeUrls;

    /** 无需认证的 URL 后缀列表（如 htm、html、js 等）。 */

    @Value("#{'${auth.filter.exclude-url-suffixes}'.split(',')}")
    private List<String> authFilterExcludeUrlSuffixes;

    /** 基于 {@link AuthService} 的认证实现。 */

    private final AuthService<HttpServletRequest> authService;

    public DefaultLoginAuthenticationFilter(AuthService<HttpServletRequest> authService) {
        this.authService = authService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String servletPath = httpRequest.getServletPath();

        // 跳过无需认证的 URL
        boolean authFilterExcludeMatch = authFilterExcludeUrls.stream()
                .anyMatch(authFilterExcludeUrl -> PATH_MATCHER.match(authFilterExcludeUrl, servletPath));
        if (authFilterExcludeMatch) {
            chain.doFilter(request, response);
            return;
        }

        // 跳过匹配排除后缀的 URL
        for (String authFilterExcludeUrlSuffix : authFilterExcludeUrlSuffixes) {
            if (StringUtils.isBlank(authFilterExcludeUrlSuffix)) {
                continue;
            }

            // 自动补点前缀，配置文件中后缀可不带 "."
            if (!authFilterExcludeUrlSuffix.startsWith(URL_SUFFIX_DOT)) {
                authFilterExcludeUrlSuffix = URL_SUFFIX_DOT + authFilterExcludeUrlSuffix;
            }

            if (servletPath.endsWith(authFilterExcludeUrlSuffix)) {
                chain.doFilter(request, response);
                return;
            }
        }

        AuthService.AuthUser authUser = authService.getAuthUser(httpRequest);

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (authUser == null) {
            // 认证失败时返回 401
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
