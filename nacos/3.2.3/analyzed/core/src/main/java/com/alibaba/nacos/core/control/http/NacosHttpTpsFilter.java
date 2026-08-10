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

package com.alibaba.nacos.core.control.http;

import com.alibaba.nacos.api.remote.RpcScheduledExecutor;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.code.ControllerMethodsCache;
import com.alibaba.nacos.core.control.TpsControl;
import com.alibaba.nacos.core.control.TpsControlConfig;
import com.alibaba.nacos.plugin.control.ControlManagerCenter;
import com.alibaba.nacos.plugin.control.Loggers;
import com.alibaba.nacos.plugin.control.tps.TpsControlManager;
import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;
import com.alibaba.nacos.plugin.control.tps.response.TpsCheckResponse;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 请求 TPS 限流 Filter：解析 {@link TpsControl} 注解切点，经 {@link HttpTpsCheckRequestParser}构造 {@link TpsCheckRequest} 后委托 {@link TpsControlManager} 校验；超限则异步返回 503。
 * Nacos http tps control cut point filter.
 *
 * @author xiweng.yy
 */
public class NacosHttpTpsFilter implements Filter {
    
    /** Controller 方法缓存，用于从 HTTP 请求定位处理器方法。 */
    private ControllerMethodsCache controllerMethodsCache;
    
    /** TPS 控制管理器，执行限流校验。 */
    private TpsControlManager tpsControlManager;
    
    /**
     * 构造 Filter 并注入方法缓存。
     *
     * @param controllerMethodsCache Controller 方法缓存
     */
    public NacosHttpTpsFilter(ControllerMethodsCache controllerMethodsCache) {
        this.controllerMethodsCache = controllerMethodsCache;
    }
    
    /** {@inheritDoc} 委托父类默认初始化。 */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }
    
    /** 懒加载 {@link TpsControlManager} 单例。 */
    private void initTpsControlManager() {
        if (tpsControlManager == null) {
            tpsControlManager = ControlManagerCenter.getInstance().getTpsControlManager();
        }
    }
    
    /**
     * 执行 TPS 校验：命中 {@link TpsControl} 且全局开关开启时检查限流，失败则延迟返回 503，否则继续 Filter 链。
     *
     * @param servletRequest 请求
     * @param servletResponse 响应
     * @param filterChain 过滤器链
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain filterChain)
        throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        Method method = controllerMethodsCache.getMethod(httpServletRequest);
        try {
            if (method != null && method.isAnnotationPresent(TpsControl.class)
                && TpsControlConfig.isTpsControlEnabled()) {
                TpsControl tpsControl = method.getAnnotation(TpsControl.class);
                String pointName = tpsControl.pointName();
                String parserName =
                    StringUtils.isBlank(tpsControl.name()) ? pointName : tpsControl.name();
                HttpTpsCheckRequestParser parser =
                    HttpTpsCheckRequestParserRegistry.getParser(parserName);
                TpsCheckRequest httpTpsCheckRequest = null;
                if (parser != null) {
                    httpTpsCheckRequest = parser.parse(httpServletRequest);
                }
                if (httpTpsCheckRequest == null) {
                    httpTpsCheckRequest = new TpsCheckRequest();
                }
                if (StringUtils.isBlank(httpTpsCheckRequest.getPointName())) {
                    httpTpsCheckRequest.setPointName(pointName);
                }
                initTpsControlManager();
                TpsCheckResponse checkResponse = tpsControlManager.check(httpTpsCheckRequest);
                if (!checkResponse.isSuccess()) {
                    AsyncContext asyncContext = httpServletRequest.startAsync();
                    asyncContext.setTimeout(0);
                    RpcScheduledExecutor.CONTROL_SCHEDULER.schedule(
                        () -> generate503Response(httpServletRequest, response,
                            checkResponse.getMessage(),
                            asyncContext),
                        1000L, TimeUnit.MILLISECONDS);
                    return;
                }
                
            }
        } catch (Throwable throwable) {
            Loggers.TPS.warn("Fail to  http tps check", throwable);
        }
        
        filterChain.doFilter(httpServletRequest, response);
    }
    
    /** {@inheritDoc} 委托父类默认销毁逻辑。 */
    @Override
    public void destroy() {
        Filter.super.destroy();
    }
    
    /**
     * 生成 TPS 超限 503 响应并结束异步上下文。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param message 错误提示
     * @param asyncContext 异步上下文
     */
    void generate503Response(HttpServletRequest request, HttpServletResponse response,
        String message,
        AsyncContext asyncContext) {
        
        try {
            // 禁用缓存，避免客户端缓存限流错误页
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Cache-Control", "no-cache,no-store");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getOutputStream().println(message);
            asyncContext.complete();
        } catch (Exception ex) {
            Loggers.TPS.error("Error to generate tps 503 response", ex);
        }
    }
}
