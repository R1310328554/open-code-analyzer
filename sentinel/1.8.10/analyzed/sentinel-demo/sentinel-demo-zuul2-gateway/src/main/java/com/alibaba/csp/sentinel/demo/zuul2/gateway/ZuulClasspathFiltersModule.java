package com.alibaba.csp.sentinel.demo.zuul2.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.endpoint.SentinelZuulEndpoint;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.inbound.SentinelZuulInboundFilter;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.outbound.SentinelZuulOutboundFilter;
import com.alibaba.csp.sentinel.demo.zuul2.gateway.filters.Route;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.netflix.zuul.BasicFilterUsageNotifier;
import com.netflix.zuul.DynamicCodeCompiler;
import com.netflix.zuul.FilterFactory;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.guice.GuiceFilterFactory;


/** Guice 模块：注册 Sentinel Zuul 2 过滤器与演示路由。 */
public class ZuulClasspathFiltersModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DynamicCodeCompiler.class).to(GroovyCompiler.class);
        bind(FilterFactory.class).to(GuiceFilterFactory.class);

        bind(FilterUsageNotifier.class).to(BasicFilterUsageNotifier.class);

        Multibinder<ZuulFilter> filterMultibinder = Multibinder.newSetBinder(binder(), ZuulFilter.class);
        // 入站 Sentinel 过滤器（order=500）
        filterMultibinder.addBinding().toInstance(new SentinelZuulInboundFilter(500));
        // 出站 Sentinel 过滤器（order=500）
        filterMultibinder.addBinding().toInstance(new SentinelZuulOutboundFilter(500));
        // Sentinel 阻塞异常 Endpoint
        filterMultibinder.addBinding().toInstance(new SentinelZuulEndpoint());
        // 演示用路径路由过滤器
        filterMultibinder.addBinding().toInstance(new Route());
    }
}
