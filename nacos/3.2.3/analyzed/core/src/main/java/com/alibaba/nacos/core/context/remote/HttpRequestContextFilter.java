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

package com.alibaba.nacos.core.context.remote;

import com.alibaba.nacos.common.constant.HttpHeaderConsts;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.context.RequestContext;
import com.alibaba.nacos.core.context.RequestContextHolder;
import com.alibaba.nacos.core.context.addition.BasicContext;
import com.alibaba.nacos.core.utils.WebUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.hc.core5.http.HttpHeaders;

import java.io.IOException;

import static com.alibaba.nacos.api.common.Constants.CLIENT_APPNAME_HEADER;

/**
 * HTTP 请求上下文过滤器：从 {@link HttpServletRequest} 提取协议、URI、编码、地址与 UA 等信息写入 {@link RequestContext}，并在 finally 中清理 ThreadLocal。
 * The Filter to add request context for HTTP protocol.
 *
 * @author xiweng.yy
 */
public class HttpRequestContextFilter implements Filter {
    
    /** HTTP 请求目标格式化模板：{@code 方法 URI}。 */
    private static final String PATTERN_REQUEST_TARGET = "%s %s";
    
    /** 填充 HTTP 请求上下文并继续过滤链，结束时移除 ThreadLocal。 */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain filterChain)
        throws IOException, ServletException {
        RequestContext requestContext = RequestContextHolder.getContext();
        try {
            requestContext.getBasicContext().setRequestProtocol(BasicContext.HTTP_PROTOCOL);
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            setRequestTarget(request, requestContext);
            setEncoding(request, requestContext);
            setAddressContext(request, requestContext);
            setOtherBasicContext(request, requestContext);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            RequestContextHolder.removeContext();
        }
    }
    
    /** 设置 {@code METHOD URI} 形式的请求目标。 */
    private void setRequestTarget(HttpServletRequest request, RequestContext requestContext) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        requestContext.getBasicContext()
            .setRequestTarget(String.format(PATTERN_REQUEST_TARGET, method, uri));
    }
    
    /** 若请求声明了字符编码则写入基础上下文。 */
    private void setEncoding(HttpServletRequest request, RequestContext requestContext) {
        String encoding = request.getCharacterEncoding();
        if (StringUtils.isNotBlank(encoding)) {
            requestContext.getBasicContext().setEncoding(encoding);
        }
    }
    
    /** 填充 remote/source IP、端口与 Host 到地址上下文。 */
    private void setAddressContext(HttpServletRequest request, RequestContext requestContext) {
        String remoteAddress = request.getRemoteAddr();
        int remotePort = request.getRemotePort();
        String sourceIp = WebUtils.getRemoteIp(request);
        String host = request.getHeader(HttpHeaders.HOST);
        requestContext.getBasicContext().getAddressContext().setRemoteIp(remoteAddress);
        requestContext.getBasicContext().getAddressContext().setRemotePort(remotePort);
        requestContext.getBasicContext().getAddressContext().setSourceIp(sourceIp);
        requestContext.getBasicContext().getAddressContext().setHost(host);
    }
    
    /** 设置 User-Agent 与客户端应用名（若请求头存在）。 */
    private void setOtherBasicContext(HttpServletRequest request, RequestContext requestContext) {
        String userAgent = WebUtils.getUserAgent(request);
        requestContext.getBasicContext().setUserAgent(userAgent);
        String app = getAppName(request);
        if (StringUtils.isNotBlank(app)) {
            requestContext.getBasicContext().setApp(app);
        }
    }
    
    /** 从标准或兼容请求头解析应用名。 */
    private String getAppName(HttpServletRequest request) {
        String app = request.getHeader(HttpHeaderConsts.APP_FILED);
        if (StringUtils.isBlank(app)) {
            app = request.getHeader(CLIENT_APPNAME_HEADER);
        }
        return app;
    }
}
