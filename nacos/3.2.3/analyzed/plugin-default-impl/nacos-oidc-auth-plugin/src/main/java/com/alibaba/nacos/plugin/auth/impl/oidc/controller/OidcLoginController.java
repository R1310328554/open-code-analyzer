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

package com.alibaba.nacos.plugin.auth.impl.oidc.controller;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.exception.AccessException;
import com.alibaba.nacos.plugin.auth.impl.oidc.authenticate.AuthorizationCodeHandler;
import com.alibaba.nacos.plugin.auth.impl.oidc.config.OidcAuthConfig;
import com.alibaba.nacos.plugin.auth.impl.oidc.constant.OidcConstants;
import com.alibaba.nacos.plugin.auth.impl.oidc.identity.OidcUserMapper.OidcUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * OIDC 登录 REST 控制器。
 *
 * <p>处理授权码流程的登录发起、IdP 回调、登出及前端配置查询，
 * 通过短期 Cookie 向控制台传递令牌（集群友好，无服务端会话）。</p>
 *
 * @author WangzJi
 */
@RestController
@RequestMapping("/v1/auth/oidc")
@SuppressWarnings("PMD")
public class OidcLoginController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcLoginController.class);
    
    /**
     * Cookie 有效期（秒），默认 60 秒。
     *
     * <p>短期有效：前端读取后同步至 localStorage 并立即清除 Cookie。</p>
     */
    private static final int COOKIE_EXPIRATION_SECONDS = 60;
    
    private volatile AuthorizationCodeHandler authHandler;
    
    private volatile OidcAuthConfig config;
    
    /**
     * 发起 OIDC 登录，重定向用户至 IdP 授权页。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws IOException 重定向失败时抛出
     */
    @Since("3.2.0")
    @GetMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            initializeIfNeeded();
            
            // 构建 OIDC 回调 URL
            String callbackUrl = buildCallbackUrl(request);
            
            // 获取 IdP 授权跳转 URL
            String authUrl = authHandler.buildAuthorizationUrl(callbackUrl);
            
            LOGGER.info("Redirecting to IdP for authentication");
            response.sendRedirect(authUrl);
            
        } catch (AccessException e) {
            LOGGER.error("Failed to initiate OIDC login: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to initiate login: " + e.getMessage());
        }
    }
    
    /**
     * OIDC 回调端点，处理 IdP 返回的授权码。
     *
     * @param code             授权码
     * @param state            CSRF 防护 state 参数
     * @param error            IdP 返回的错误码
     * @param errorDescription 错误描述
     * @param request          HTTP 请求
     * @param response         HTTP 响应
     * @return 认证结果（成功时通过 Cookie + 重定向传递令牌）
     */
    @Since("3.2.0")
    @GetMapping("/callback")
    public Result<Map<String, Object>> callback(
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String state,
        @RequestParam(required = false) String error,
        @RequestParam(name = "error_description", required = false) String errorDescription,
        HttpServletRequest request,
        HttpServletResponse response) throws IOException {
        
        try {
            initializeIfNeeded();
            
            // 检查 IdP 是否返回错误
            if (StringUtils.isNotBlank(error)) {
                LOGGER.warn("OIDC authentication error: {} - {}", error, errorDescription);
                String errorMsg = errorDescription != null ? errorDescription : error;
                String errorRedirectUrl = buildBaseUrl(request) + "/#/login?error="
                    + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
                response.sendRedirect(errorRedirectUrl);
                return null;
            }
            
            // 校验必需参数（code 与 state）
            if (StringUtils.isBlank(code)) {
                String errorRedirectUrl = buildBaseUrl(request) + "/#/login?error="
                    + URLEncoder.encode("Missing authorization code", StandardCharsets.UTF_8);
                response.sendRedirect(errorRedirectUrl);
                return null;
            }
            if (StringUtils.isBlank(state)) {
                String errorRedirectUrl = buildBaseUrl(request) + "/#/login?error="
                    + URLEncoder.encode("Missing state parameter", StandardCharsets.UTF_8);
                response.sendRedirect(errorRedirectUrl);
                return null;
            }
            
            // 回调 URL 须与登录请求时一致
            String callbackUrl = buildCallbackUrl(request);
            
            // 用授权码换取令牌并完成用户认证
            OidcUser user = authHandler.exchangeCodeForUser(code, state, callbackUrl);
            
            LOGGER.info("OIDC authentication successful for user: {}", user.getUsername());
            
            // 通过短期 Cookie 传递令牌（集群友好，无服务端会话存储）
            // 前端读取 Cookie 后同步至 localStorage 并清除
            String contextPath = request.getContextPath();
            String cookiePath = StringUtils.isBlank(contextPath) ? "/" : contextPath + "/";
            
            // accessToken Cookie（允许前端读取以便同步 localStorage）
            Cookie accessTokenCookie = new Cookie("accessToken", user.getToken());
            accessTokenCookie.setHttpOnly(false); // 允许前端 JavaScript 读取
            accessTokenCookie.setSecure(isHttps(request));
            accessTokenCookie.setPath(cookiePath);
            accessTokenCookie.setMaxAge(COOKIE_EXPIRATION_SECONDS);
            response.addCookie(accessTokenCookie);
            
            // username Cookie（URL 编码）
            Cookie usernameCookie = new Cookie("username",
                URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8));
            usernameCookie.setHttpOnly(false);
            usernameCookie.setSecure(isHttps(request));
            usernameCookie.setPath(cookiePath);
            usernameCookie.setMaxAge(COOKIE_EXPIRATION_SECONDS);
            response.addCookie(usernameCookie);
            
            // 重定向至控制台首页（URL 中不携带令牌参数）
            String successRedirectUrl = buildBaseUrl(request) + "/#/";
            response.sendRedirect(successRedirectUrl);
            return null;
            
        } catch (AccessException e) {
            LOGGER.warn("OIDC callback failed: {}", e.getMessage());
            String errorRedirectUrl = buildBaseUrl(request) + "/#/login?error="
                + URLEncoder.encode(e.getErrMsg(), StandardCharsets.UTF_8);
            response.sendRedirect(errorRedirectUrl);
            return null;
        } catch (Exception e) {
            LOGGER.error("OIDC callback error", e);
            String errorRedirectUrl = buildBaseUrl(request) + "/#/login?error="
                + URLEncoder.encode("Authentication failed: " + e.getMessage(),
                    StandardCharsets.UTF_8);
            response.sendRedirect(errorRedirectUrl);
            return null;
        }
    }
    
    /**
     * 用户登出，可选重定向至 IdP 完成 RP 发起登出。
     *
     * @param idToken  可选 ID Token（作为 id_token_hint）
     * @param redirect 是否重定向至 IdP 登出页
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @return 登出结果
     * @throws IOException 重定向失败时抛出
     */
    @Since("3.2.0")
    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<String> logout(
        @RequestParam(required = false) String idToken,
        @RequestParam(defaultValue = "false") boolean redirect,
        HttpServletRequest request,
        HttpServletResponse response) throws IOException {
        
        try {
            initializeIfNeeded();
            
            // 若请求重定向且 IdP 支持 RP 发起登出
            if (redirect) {
                String postLogoutUri = buildBaseUrl(request);
                String logoutUrl = authHandler.buildLogoutUrl(idToken, postLogoutUri);
                
                if (StringUtils.isNotBlank(logoutUrl)) {
                    LOGGER.info("Redirecting to IdP for logout");
                    response.sendRedirect(logoutUrl);
                    return null;
                }
            }
            
            LOGGER.info("User logged out");
            return Result.success("Logged out successfully");
            
        } catch (Exception e) {
            LOGGER.error("Logout error", e);
            return Result.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Logout failed", null);
        }
    }
    
    /**
     * 返回 OIDC 配置信息（供控制台前端使用）。
     *
     * <p>控制台据此识别 OIDC 模式并隐藏本地用户/角色/权限管理入口。</p>
     *
     * @return OIDC 配置摘要
     */
    @Since("3.2.0")
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        try {
            initializeIfNeeded();
            
            Map<String, Object> configInfo = new HashMap<>(8);
            configInfo.put("enabled", config.isValid());
            configInfo.put("authType", "oidc");
            configInfo.put("loginUrl", "/v1/auth/oidc/login");
            // OIDC 模式下用户/角色/权限管理由 IdP 负责
            configInfo.put("userManagementEnabled", false);
            configInfo.put("roleManagementEnabled", false);
            configInfo.put("permissionManagementEnabled", false);
            
            return Result.success(configInfo);
            
        } catch (Exception e) {
            LOGGER.error("Failed to get OIDC config", e);
            return Result.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to get configuration", null);
        }
    }
    
    /**
     * 根据当前请求构建 OIDC 回调 URL。
     *
     * @param request HTTP 请求
     * @return 完整回调 URL
     */
    private String buildCallbackUrl(HttpServletRequest request) {
        String baseUrl = buildBaseUrl(request);
        return baseUrl + "/v1/auth/oidc/callback";
    }
    
    /**
     * 从请求中提取基础 URL（scheme + host + port + contextPath）。
     *
     * @param request HTTP 请求
     * @return 基础 URL 字符串
     */
    private String buildBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        // 非标准端口时追加端口号
        boolean isNonStandardHttpPort = OidcConstants.HTTP_PROTOCOL.equals(scheme)
            && serverPort != OidcConstants.DEFAULT_HTTP_PORT;
        boolean isNonStandardHttpsPort = OidcConstants.HTTPS_PROTOCOL.equals(scheme)
            && serverPort != OidcConstants.DEFAULT_HTTPS_PORT;
        if (isNonStandardHttpPort || isNonStandardHttpsPort) {
            url.append(":").append(serverPort);
        }
        
        url.append(contextPath);
        return url.toString();
    }
    
    /**
     * 延迟初始化配置与授权码处理器。
     */
    private void initializeIfNeeded() {
        if (config == null) {
            synchronized (this) {
                if (config == null) {
                    config = OidcAuthConfig.getInstance();
                    authHandler = AuthorizationCodeHandler.getInstance();
                }
            }
        }
    }
    
    /**
     * 判断当前请求是否使用 HTTPS。
     *
     * @param request HTTP 请求
     * @return HTTPS 时返回 true
     */
    private boolean isHttps(HttpServletRequest request) {
        return OidcConstants.HTTPS_PROTOCOL.equals(request.getScheme());
    }
}
