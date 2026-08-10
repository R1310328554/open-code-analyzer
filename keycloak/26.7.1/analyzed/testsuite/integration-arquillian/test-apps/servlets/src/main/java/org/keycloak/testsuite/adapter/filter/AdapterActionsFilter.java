/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.testsuite.adapter.filter;


import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.keycloak.common.util.Time;

import org.jboss.logging.Logger;

/**
 * 适配器侧特殊操作请求过滤器，用于在测试中执行诸如设置时间偏移等操作。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AdapterActionsFilter implements Filter {

    /** 时间偏移请求参数名，用于模拟令牌过期等超时场景。 */
    public static final String TIME_OFFSET_PARAM = "timeOffset";

    private static final Logger log = Logger.getLogger(AdapterActionsFilter.class);

    /** {@inheritDoc} 本过滤器无需初始化配置。 */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /** {@inheritDoc} 拦截请求并根据 {@link #TIME_OFFSET_PARAM} 更新全局时间偏移或继续过滤链。 */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletReq = (HttpServletRequest) request;
        HttpServletResponse servletResp = (HttpServletResponse) response;

        // 接受 timeOffset 参数以强制触发超时行为
        String timeOffsetParam = request.getParameter(TIME_OFFSET_PARAM);

        if (timeOffsetParam != null && !timeOffsetParam.isEmpty()) {
            int timeOffset = Integer.parseInt(timeOffsetParam);
            log.infof("Time offset updated to %d for application %s", timeOffset, servletReq.getRequestURI());
            Time.setOffset(timeOffset);
            writeResponse(servletResp, "Offset set successfully");
        } else {
            // 无特殊参数时继续正常请求处理
            chain.doFilter(request, response);
        }

    }

    /** {@inheritDoc} 销毁阶段无额外清理逻辑。 */
    @Override
    public void destroy() {

    }

    /**
     * 向客户端写入简单 HTML 响应。
     *
     * @param response HTTP 响应对象
     * @param responseText 响应正文文本
     */
    private void writeResponse(HttpServletResponse response, String responseText) throws IOException {
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.println("<html><body>" + responseText + "</body></html>");
        writer.flush();
    }
}
