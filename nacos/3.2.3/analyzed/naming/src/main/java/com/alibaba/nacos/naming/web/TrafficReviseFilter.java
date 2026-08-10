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

package com.alibaba.nacos.naming.web;

import com.alibaba.nacos.common.utils.HttpMethod;
import com.alibaba.nacos.sys.env.Constants;
import com.alibaba.nacos.core.utils.WebUtils;
import com.alibaba.nacos.naming.cluster.ServerStatus;
import com.alibaba.nacos.naming.cluster.ServerStatusManager;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * 入站流量修订过滤器。
 *
 * <p>按 {@link SwitchDomain} 限流 URL、{@link ServerStatusManager} 节点状态（UP/READ_ONLY/WRITE_ONLY）决定放行或返回 503。</p>
 *
 * @author nkorange
 * @since 1.0.0
 */
public class TrafficReviseFilter implements Filter {
    
    @Autowired
    private ServerStatusManager serverStatusManager;
    
    @Autowired
    private SwitchDomain switchDomain;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        // 若配置了 URL 前缀限流，直接返回指定 HTTP 状态码
        String urlString = req.getRequestURI() + "?" + req.getQueryString();
        Map<String, Integer> limitedUrlMap = switchDomain.getLimitedUrlMap();
        
        if (limitedUrlMap != null && limitedUrlMap.size() > 0) {
            for (Map.Entry<String, Integer> entry : limitedUrlMap.entrySet()) {
                String limitedUrl = entry.getKey();
                if (StringUtils.startsWith(urlString, limitedUrl)) {
                    resp.setStatus(entry.getValue());
                    return;
                }
            }
        }
        
        // 节点 UP 时全部放行
        if (serverStatusManager.getServerStatus() == ServerStatus.UP) {
            filterChain.doFilter(req, resp);
            return;
        }
        
        // 来自集群对等节点的请求始终放行
        String agent = WebUtils.getUserAgent(req);
        
        if (StringUtils.startsWith(agent, Constants.NACOS_SERVER_HEADER)) {
            filterChain.doFilter(req, resp);
            return;
        }
        
        // WRITE_ONLY 状态下允许非 GET 写操作
        if (serverStatusManager.getServerStatus() == ServerStatus.WRITE_ONLY && !HttpMethod.GET
            .equals(req.getMethod())) {
            filterChain.doFilter(req, resp);
            return;
        }
        
        // READ_ONLY 状态下允许 GET 读操作
        if (serverStatusManager.getServerStatus() == ServerStatus.READ_ONLY
            && HttpMethod.GET.equals(req.getMethod())) {
            filterChain.doFilter(req, resp);
            return;
        }
        
        final String statusMsg =
            "server is " + serverStatusManager.getServerStatus().name() + "now";
        Optional<String> errorMsg = serverStatusManager.getErrorMsg();
        if (errorMsg.isPresent()) {
            resp.getWriter().write(statusMsg + ", detailed error message: " + errorMsg.get());
        } else {
            resp.getWriter().write(statusMsg + ", please try again later!");
        }
        resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }
}
