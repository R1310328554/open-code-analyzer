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

import javax.servlet.Filter;

/**
 * <p>登录认证 Servlet 过滤器接口。</p>
 *
 * <p>部分 URL 无需认证，例如：</p>
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
 * @author wxq
 * @since 1.6.0
 */
public interface LoginAuthenticationFilter extends Filter {

}
