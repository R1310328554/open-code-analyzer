/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.auth.context;

import com.alibaba.nacos.auth.config.NacosAuthConfig;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.constant.Constants;
import com.alibaba.nacos.plugin.auth.spi.server.AuthPluginManager;
import com.alibaba.nacos.plugin.auth.spi.server.AuthPluginService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * HTTP 请求的身份上下文构建器。
 *
 * <p>从请求头/参数提取远程 IP 及鉴权插件声明的身份字段，HTTP 头名大小写不敏感。</p>
 *
 * @author Nacos
 */
public class HttpIdentityContextBuilder implements IdentityContextBuilder<HttpServletRequest> {
    
    /** 代理链转发客户端 IP 的标准请求头。 */
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    
    /** {@link #X_FORWARDED_FOR} 多 IP 分隔符。 */
    private static final String X_FORWARDED_FOR_SPLIT_SYMBOL = ",";
    
    /** 鉴权配置，用于定位鉴权插件。 */
    private final NacosAuthConfig authConfig;
    
    /** 注入鉴权配置。 */
    public HttpIdentityContextBuilder(NacosAuthConfig authConfig) {
        this.authConfig = authConfig;
    }
    
    /**
     * 从 HTTP 请求构建 {@link IdentityContext}。
     *
     * @param request HTTP 请求
     * @return 含远程 IP 与插件身份参数的身份上下文
     */
    @Override
    public IdentityContext build(HttpServletRequest request) {
        IdentityContext result = new IdentityContext();
        getRemoteIp(request, result);
        Optional<AuthPluginService> authPluginService = AuthPluginManager.getInstance()
            .findAuthServiceSpiImpl(authConfig.getNacosAuthSystemType());
        if (!authPluginService.isPresent()) {
            return result;
        }
        // RFC2616：HTTP 头与 URI 大小写不敏感，使用 CASE_INSENSITIVE_ORDER 的 TreeMap 匹配身份键并保留原始键名。
        Map<String, String> identityNames = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String each : authPluginService.get().identityNames()) {
            identityNames.put(each, each);
        }
        getIdentityFromHeader(request, result, identityNames);
        getIdentityFromParameter(request, result, identityNames);
        return result;
    }
    
    /** 从 HTTP 请求头提取插件声明的身份字段。 */
    private void getIdentityFromHeader(HttpServletRequest request, IdentityContext result,
        Map<String, String> identityNames) {
        Enumeration<String> headerEnu = request.getHeaderNames();
        while (headerEnu.hasMoreElements()) {
            String paraName = headerEnu.nextElement();
            if (identityNames.containsKey(paraName)) {
                result.setParameter(identityNames.get(paraName), request.getHeader(paraName));
            }
        }
    }
    
    /** 从 HTTP 请求参数提取插件声明的身份字段。 */
    private void getIdentityFromParameter(HttpServletRequest request, IdentityContext result,
        Map<String, String> identityNames) {
        Enumeration<String> paramEnu = request.getParameterNames();
        while (paramEnu.hasMoreElements()) {
            String paraName = paramEnu.nextElement();
            if (identityNames.containsKey(paraName)) {
                result.setParameter(identityNames.get(paraName), request.getParameter(paraName));
            }
        }
    }
    
    /** 解析客户端真实 IP（优先 X-Forwarded-For，其次 X-Real-IP 与 remoteAddr）。 */
    private void getRemoteIp(HttpServletRequest request, IdentityContext result) {
        String remoteIp = StringUtils.EMPTY;
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (!StringUtils.isBlank(xForwardedFor)) {
            remoteIp = xForwardedFor.split(X_FORWARDED_FOR_SPLIT_SYMBOL)[0].trim();
        }
        if (StringUtils.isBlank(remoteIp)) {
            String nginxHeader = request.getHeader(Constants.Identity.X_REAL_IP);
            remoteIp = StringUtils.isBlank(nginxHeader) ? request.getRemoteAddr() : nginxHeader;
        }
        result.setParameter(Constants.Identity.REMOTE_IP, remoteIp);
    }
}
