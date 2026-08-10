/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.console;

import com.alibaba.nacos.common.http.param.MediaType;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.sys.env.EnvUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Nacos 控制台路径提示过滤器：当用户误访问 Server 根路径或 index.html 时，返回控制台默认端口与 context-path 提示，引导前往独立 Console 服务。
 * nacos console path filter.
 * @author cxhello
 * @date 2025/7/17
 */
public class NacosConsolePathTipFilter implements Filter {
    
    /** Server 侧 context-path 配置键。 */
    private static final String NACOS_SERVER_CONTEXT_PATH = "nacos.server.contextPath";
    
    /** 独立 Console 服务端口配置键。 */
    private static final String NACOS_CONSOLE_PORT = "nacos.console.port";
    
    /** 独立 Console context-path 配置键。 */
    private static final String NACOS_CONSOLE_CONTEXT_PATH = "nacos.console.contextPath";
    
    /** Server 默认 context-path。 */
    private static final String NACOS_SERVER_DEFAULT_CONTEXT_PATH = "/nacos";
    
    /** Console 默认端口。 */
    private static final String NACOS_CONSOLE_DEFAULT_PORT = "8080";
    
    /** 路径规范化时使用的根路径后缀。 */
    private static final String NACOS_CONSOLE_DEFAULT_PATH = "/";
    
    /** 拦截根路径访问并输出 Console 引导信息，其余请求继续过滤链。 */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain filterChain)
        throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest
            && servletResponse instanceof HttpServletResponse) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            String contextPath = normalizeContextPath(
                EnvUtil.getProperty(NACOS_SERVER_CONTEXT_PATH, NACOS_SERVER_DEFAULT_CONTEXT_PATH));
            String indexPath = contextPath + "index.html";
            String requestUri = httpServletRequest.getRequestURI();
            if (requestUri.equals(contextPath) || requestUri.equals(indexPath)) {
                writeConsoleInfo(httpServletResponse);
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
    
    /** 规范化 Server context-path，确保以 {@code /} 结尾（根路径除外）。 */
    private String normalizeContextPath(String contextPath) {
        if (StringUtils.isBlank(contextPath)) {
            return NACOS_CONSOLE_DEFAULT_PATH;
        }
        if (contextPath.length() > 1 && !contextPath.endsWith(NACOS_CONSOLE_DEFAULT_PATH)) {
            return contextPath + NACOS_CONSOLE_DEFAULT_PATH;
        }
        return contextPath;
    }
    
    /** 向响应写入 Console 默认端口与访问路径的纯文本提示。 */
    private void writeConsoleInfo(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType(MediaType.TEXT_PLAIN);
        String port = EnvUtil.getProperty(NACOS_CONSOLE_PORT, NACOS_CONSOLE_DEFAULT_PORT);
        String consoleContextPath = EnvUtil.getProperty(NACOS_CONSOLE_CONTEXT_PATH);
        consoleContextPath = StringUtils.isBlank(consoleContextPath) ? NACOS_CONSOLE_DEFAULT_PATH
            : consoleContextPath;
        httpServletResponse.getWriter().write(String.format(
            "Nacos Console default port is %s, and the path is %s.", port, consoleContextPath));
    }
    
}
