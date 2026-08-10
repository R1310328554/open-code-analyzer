/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.CommonParams;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.utils.OverrideParameterRequestWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 服务名兼容过滤器，适配 1.x 客户端与旧 OpenAPI。
 * <p>
 * 旧版 {@link DistroFilter} 会自动拼接 group 与服务名；本过滤器将 {@code groupName@@serviceName} 写回请求参数并校验格式。
 * </p>
 *
 * @author xiweng.yy
 */
public class ServiceNameFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        try {
            String serviceName = request.getParameter(CommonParams.SERVICE_NAME);
            
            if (StringUtils.isNotBlank(serviceName)) {
                serviceName = serviceName.trim();
            }
            String groupName = request.getParameter(CommonParams.GROUP_NAME);
            if (StringUtils.isBlank(groupName)) {
                groupName = Constants.DEFAULT_GROUP;
            }
            
            // 将 group 与服务名合并为 groupName@@serviceName 标准格式
            String groupedServiceName = serviceName;
            if (StringUtils.isNotBlank(serviceName)
                && !serviceName.contains(Constants.SERVICE_INFO_SPLITER)) {
                groupedServiceName = groupName + Constants.SERVICE_INFO_SPLITER + serviceName;
            }
            if (StringUtils.isNotBlank(groupedServiceName)) {
                try {
                    NamingUtils.checkServiceNameFormat(groupedServiceName);
                } catch (IllegalArgumentException e) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Service name filter error," + ExceptionUtil.getAllExceptionMsg(e));
                }
            }
            OverrideParameterRequestWrapper requestWrapper =
                OverrideParameterRequestWrapper.buildRequest(request);
            requestWrapper.addParameter(CommonParams.SERVICE_NAME, groupedServiceName);
            filterChain.doFilter(requestWrapper, servletResponse);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Service name filter error," + ExceptionUtil.getAllExceptionMsg(e));
        }
    }
}
