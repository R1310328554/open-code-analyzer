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

package com.alibaba.nacos.config.server.filter;

import com.alibaba.nacos.config.server.constant.Constants;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

import static com.alibaba.nacos.config.server.utils.LogUtil.DEFAULT_LOG;

/**
 * Web 编码过滤器：统一请求/响应 UTF-8 字符集，
 * 并在 init 时缓存 ServletContext 根路径供测试与静态资源使用。
 * Web encode filter.
 *
 * @author Nacos
 */
public class NacosWebFilter implements Filter {
    
    /** 缓存的 Web 应用根路径（realPath） */
    private static String webRootPath;
    
    /** 获取缓存的 Web 根路径 */
    public static String rootPath() {
        return webRootPath;
    }
    
    /**
     * 测试用：手动设置 Web 根路径。
     *
     * @param path web path.
     */
    public static void setWebRootPath(String path) {
        webRootPath = path;
    }
    
    /** 初始化时从 ServletContext 读取 realPath 并缓存 */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext ctx = filterConfig.getServletContext();
        setWebRootPath(ctx.getRealPath("/"));
    }
    
    /**
     * 设置请求 UTF-8 编码与 JSON 响应 Content-Type，继续过滤链。
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        request.setCharacterEncoding(Constants.ENCODE);
        response.setContentType("application/json;charset=" + Constants.ENCODE);
        
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException ioe) {
            DEFAULT_LOG.debug("Filter catch exception, " + ioe.toString(), ioe);
            throw ioe;
        }
    }
    
    @Override
    public void destroy() {
    }
}
