/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证失败时的 Spring Security 入口点（已废弃）。
 *
 * <p>认证异常时记录错误日志并向客户端返回 HTTP 401 Unauthorized。</p>
 *
 * @author wfnuser
 */
@Deprecated
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    
    /** 处理未认证请求：写日志并发送 401 响应。 */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException e)
        throws IOException, ServletException {
        LOGGER.error("Responding with unauthorized error. Message:{}, url:{}", e.getMessage(),
            request.getRequestURI());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
