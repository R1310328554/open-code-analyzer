/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.BaseWebMvcConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 请求在转发（forward）、包含（include）等调度动作中可能被重复处理
 * （参见 {@link javax.servlet.ServletRequest#getDispatcherType()}），
 * 因此本拦截器仅处理初始请求。通过<b>引用计数</b>跟踪调度"洋葱"层，
 * 以判断当前是否处于初始类型 {@code REQUEST} 的请求。
 * 这意味着实践中极少遇到的子请求不会被 Sentinel 记录。
 * <p>
 * 在业务代码中实现转发子请求的示例：
 * <pre>
 * initalRequest() {
 *     ModelAndView mav = new ModelAndView();
 *     mav.setViewName("another");
 *     return mav;
 * }
 * </pre>
 *
 * @author kaizi2009
 * @since 1.7.1
 */
public abstract class AbstractSentinelInterceptor implements AsyncHandlerInterceptor {

    public static final String SENTINEL_SPRING_WEB_CONTEXT_NAME = "sentinel_spring_web_context";
    private static final String EMPTY_ORIGIN = "";

    private final BaseWebMvcConfig baseWebMvcConfig;

    public AbstractSentinelInterceptor(BaseWebMvcConfig config) {
        AssertUtil.notNull(config, "BaseWebMvcConfig should not be null");
        AssertUtil.assertNotBlank(config.getRequestAttributeName(), "requestAttributeName should not be blank");
        this.baseWebMvcConfig = config;
    }

    /**
     * @param request
     * @param rcKey
     * @param step
     * @return 递增后的引用计数（初始值为 0，随后递增）
     */
    private Integer increaseReference(HttpServletRequest request, String rcKey, int step) {
        Object obj = request.getAttribute(rcKey);

        if (obj == null) {
            // 初始值
            obj = 0;
        }

        Integer newRc = (Integer) obj + step;
        request.setAttribute(rcKey, newRc);
        return newRc;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        try {
            String resourceName = getResourceName(request);

            if (StringUtil.isEmpty(resourceName)) {
                return true;
            }

            if (increaseReference(request, this.baseWebMvcConfig.getRequestRefName(), 1) != 1) {
                return true;
            }

            // 使用已注册的来源解析器解析请求来源。
            String origin = parseOrigin(request);
            String contextName = getContextName(request);
            ContextUtil.enter(contextName, origin);
            Entry entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.IN);
            request.setAttribute(baseWebMvcConfig.getRequestAttributeName(), entry);
            return true;
        } catch (BlockException e) {
            try {
                handleBlockException(request, response, e);
            } finally {
                ContextUtil.exit();
            }
            return false;
        }
    }

    /**
     * 返回目标 Web 资源的名称。
     *
     * @param request web request
     * @return the resource name of the target web resource.
     */
    protected abstract String getResourceName(HttpServletRequest request);

    /**
     * 返回目标 Web 资源的上下文名称。
     *
     * @param request web request
     * @return the context name of the target web resource.
     */
    protected String getContextName(HttpServletRequest request) {
        return SENTINEL_SPRING_WEB_CONTEXT_NAME;
    }


    /**
     * 当处理器启动异步请求时，{@code DispatcherServlet} 会退出且不调用 {@code postHandle} 与 {@code afterCompletion}。
     * 本方法在并发执行处理器时替代上述回调，用于退出上下文并清理线程局部变量。
     *
     * @param request  the current request
     * @param response the current response
     * @param handler  the handler (or {@link HandlerMethod}) that started async
     *                 execution, for type and/or instance examination
     */
    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response,
                                               Object handler) throws Exception {
        exit(request);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        exit(request, ex);
    }

    private void exit(HttpServletRequest request) {
        exit(request, null);
    }

    private void exit(HttpServletRequest request, Exception ex) {
        if (increaseReference(request, this.baseWebMvcConfig.getRequestRefName(), -1) != 0) {
            return;
        }

        Entry entry = getEntryInRequest(request, baseWebMvcConfig.getRequestAttributeName());
        if (entry == null) {
            // 不应出现的情况
            RecordLog.warn("[{}] No entry found in request, key: {}",
                    getClass().getSimpleName(), baseWebMvcConfig.getRequestAttributeName());
            return;
        }

        traceExceptionAndExit(entry, ex);
        removeEntryInRequest(request);
        ContextUtil.exit();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    protected Entry getEntryInRequest(HttpServletRequest request, String attrKey) {
        Object entryObject = request.getAttribute(attrKey);
        return entryObject == null ? null : (Entry) entryObject;
    }

    protected void removeEntryInRequest(HttpServletRequest request) {
        request.removeAttribute(baseWebMvcConfig.getRequestAttributeName());
    }

    protected void traceExceptionAndExit(Entry entry, Exception ex) {
        if (entry == null) {
            return;
        }
        HttpServletRequest request = getHttpServletRequest();
        if (request != null
                && ex == null
                && increaseReference(request, this.baseWebMvcConfig.getRequestRefName() + ":" + BaseWebMvcConfig.REQUEST_REF_EXCEPTION_NAME, 1) == 1) {
            // 每个拦截器仅捕获一次异常
            ex = (Exception) request.getAttribute(BaseWebMvcConfig.REQUEST_REF_EXCEPTION_NAME);
        }

        if (ex != null) {
            Tracer.traceEntry(ex, entry);
        }
        entry.exit();
    }

    protected void handleBlockException(HttpServletRequest request, HttpServletResponse response, BlockException e)
            throws Exception {
        if (baseWebMvcConfig.getBlockExceptionHandler() != null) {
            baseWebMvcConfig.getBlockExceptionHandler().handle(request, response, e);
        } else {
            // 直接抛出 BlockException，由用户在 Spring 全局异常处理器中处理。
            throw e;
        }
    }

    protected String parseOrigin(HttpServletRequest request) {
        String origin = EMPTY_ORIGIN;
        if (baseWebMvcConfig.getOriginParser() != null) {
            origin = baseWebMvcConfig.getOriginParser().parseOrigin(request);
            if (StringUtil.isEmpty(origin)) {
                return EMPTY_ORIGIN;
            }
        }
        return origin;
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        return Objects.isNull(servletRequestAttributes) ? null : servletRequestAttributes.getRequest();
    }
}
