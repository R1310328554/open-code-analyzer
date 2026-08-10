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

package com.alibaba.nacos.console.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * XSS 防护过滤器：为每个响应设置 Content-Security-Policy，限制脚本仅来自同源。
 * XSS filter.
 * @author onewe
 */
public class XssFilter extends OncePerRequestFilter {
    
    /** CSP 响应头名称 */
    private static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";
    
    /** 仅允许加载同源脚本的 CSP 策略值 */
    private static final String CONTENT_SECURITY_POLICY = "script-src 'self'";
    
    /**
     * 在请求链继续前写入 CSP 响应头，降低反射型 XSS 风险。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {
        
        response.setHeader(CONTENT_SECURITY_POLICY_HEADER, CONTENT_SECURITY_POLICY);
        filterChain.doFilter(request, response);
    }
}
