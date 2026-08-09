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

package com.alibaba.csp.sentinel.adapter.gateway.zuul.filters;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayParamParser;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.RequestItemParser;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.RequestContextItemParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.api.ZuulGatewayApiMatcherManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.api.matcher.RequestContextApiMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.callback.ZuulGatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.BlockResponse;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackProvider;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Predicate;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import javax.servlet.http.HttpServletRequest;

import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.*;

/**
 * Zuul 前置过滤器，将所有 {@code proxyId} 与匹配的自定义 API 视为 Sentinel 资源。
 * 捕获 {@link BlockException} 时尝试查找并执行降级逻辑。
 *
 * @author tiger
 * @author Eric Zhao
 */
public class SentinelZuulPreFilter extends ZuulFilter {

    private final int order;

    private final GatewayParamParser<RequestContext> paramParser;

    public SentinelZuulPreFilter() {
        this(10000);
    }

    public SentinelZuulPreFilter(int order) {
        this(order, new RequestContextItemParser());
    }

    public SentinelZuulPreFilter(int order, RequestItemParser<RequestContext> requestItemParser) {
        AssertUtil.notNull(requestItemParser, "requestItemParser cannot be null");
        this.order = order;
        this.paramParser = new GatewayParamParser<>(requestItemParser);
    }

    @Override
    public String filterType() {
        return ZuulConstant.PRE_TYPE;
    }

    /**
     * 在路由过滤器之前执行，以便更准确地统计响应时间（RT）。
     */
    @Override
    public int filterOrder() {
        return order;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    private void doSentinelEntry(String resourceName, final int resType, RequestContext requestContext,
                                 Deque<EntryHolder> holders) throws BlockException {
        Object[] params = paramParser.parseParameterFor(resourceName, requestContext,
            new Predicate<GatewayFlowRule>() {
                @Override
                public boolean test(GatewayFlowRule r) {
                    return r.getResourceMode() == resType;
                }
            });
        AsyncEntry entry = SphU.asyncEntry(resourceName, ResourceTypeConstants.COMMON_API_GATEWAY,
                EntryType.IN, params);
        EntryHolder holder = new EntryHolder(entry, params);
        holders.push(holder);
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        String origin = parseOrigin(ctx.getRequest());
        String routeId = (String)ctx.get(ZuulConstant.PROXY_ID_KEY);

        Deque<EntryHolder> holders = new ArrayDeque<>();
        String fallBackRoute = routeId;
        try {
            if (StringUtil.isNotBlank(routeId)) {
                ContextUtil.enter(GATEWAY_CONTEXT_ROUTE_PREFIX + routeId, origin);
                doSentinelEntry(routeId, RESOURCE_MODE_ROUTE_ID, ctx, holders);
            }

            Set<String> matchingApis = pickMatchingApiDefinitions(ctx);
            if (!matchingApis.isEmpty() && ContextUtil.getContext() == null) {
                ContextUtil.enter(ZuulConstant.ZUUL_DEFAULT_CONTEXT, origin);
            }
            for (String apiName : matchingApis) {
                fallBackRoute = apiName;
                doSentinelEntry(apiName, RESOURCE_MODE_CUSTOM_API_NAME, ctx, holders);
            }
        } catch (BlockException ex) {
            ZuulBlockFallbackProvider zuulBlockFallbackProvider = ZuulBlockFallbackManager.getFallbackProvider(
                fallBackRoute);
            BlockResponse blockResponse = zuulBlockFallbackProvider.fallbackResponse(fallBackRoute, ex);
            // 阻止后续路由执行
            ctx.setRouteHost(null);
            ctx.set(ZuulConstant.SERVICE_ID_KEY, null);

            // 设置降级响应体。
            ctx.setResponseBody(blockResponse.toString());
            ctx.setResponseStatusCode(blockResponse.getCode());
            // 设置响应 Content-Type
            ctx.getResponse().setContentType("application/json; charset=utf-8");
        } finally {
            // 此处不退出 Entry，需在 post 过滤器中退出以正确记录 RT；
            // 因此将 Entry 暂存于请求上下文中。
            if (!holders.isEmpty()) {
                ctx.put(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY, holders);
            }
        }
        return null;
    }

    private String parseOrigin(HttpServletRequest request) {
        return ZuulGatewayCallbackManager.getOriginParser().parseOrigin(request);
    }

    private Set<String> pickMatchingApiDefinitions(RequestContext requestContext) {
        Set<String> apis = new HashSet<>();
        for (RequestContextApiMatcher matcher : ZuulGatewayApiMatcherManager.getApiMatcherMap().values()) {
            if (matcher.test(requestContext)) {
                apis.add(matcher.getApiName());
            }
        }
        return apis;
    }
}
