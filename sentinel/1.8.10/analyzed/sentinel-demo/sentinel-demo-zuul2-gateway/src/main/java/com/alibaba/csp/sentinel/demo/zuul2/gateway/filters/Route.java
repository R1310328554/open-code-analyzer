package com.alibaba.csp.sentinel.demo.zuul2.gateway.filters;

import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpInboundSyncFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.netty.filter.ZuulEndPointRunner;

/** 入站路由过滤器：按请求路径设置 Endpoint 与 routeVIP。 */
public class Route extends HttpInboundSyncFilter {
	@Override
	public HttpRequestMessage apply(HttpRequestMessage request) {
		SessionContext context = request.getContext();
		switch (request.getPath()) {
			// /images -> 代理到 images VIP
			case "/images":
				context.setEndpoint(ZuulEndPointRunner.PROXY_ENDPOINT_FILTER_NAME);
				context.setRouteVIP("images");
				break;
			// /comments -> 代理到 comments VIP
			case "/comments":
				context.setEndpoint(ZuulEndPointRunner.PROXY_ENDPOINT_FILTER_NAME);
				context.setRouteVIP("comments");
				break;
			// 未知路径 -> 404 Endpoint
			default:
				context.setEndpoint(NotFoundEndpoint.class.getCanonicalName());
		}
		return request;
	}

	@Override
	public int filterOrder() {
		return 0;
	}

	@Override
	public boolean shouldFilter(HttpRequestMessage msg) {
		return true;
	}
}
