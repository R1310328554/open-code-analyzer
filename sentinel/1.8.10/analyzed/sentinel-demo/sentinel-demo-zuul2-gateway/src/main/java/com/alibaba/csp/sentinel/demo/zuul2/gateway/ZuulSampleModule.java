package com.alibaba.csp.sentinel.demo.zuul2.gateway;

import com.google.inject.AbstractModule;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.netty.common.accesslog.AccessLogPublisher;
import com.netflix.netty.common.status.ServerStatusManager;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.BasicRequestCompleteHandler;
import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.RequestCompleteHandler;
import com.netflix.zuul.context.SessionContextDecorator;
import com.netflix.zuul.context.ZuulSessionContextDecorator;
import com.netflix.zuul.init.ZuulFiltersModule;
import com.netflix.zuul.netty.server.BaseServerStartup;
import com.netflix.zuul.netty.server.ClientRequestReceiver;
import com.netflix.zuul.origins.BasicNettyOriginManager;
import com.netflix.zuul.origins.OriginManager;
import com.netflix.zuul.stats.BasicRequestMetricsPublisher;
import com.netflix.zuul.stats.RequestMetricsPublisher;

/**
 * Zuul 2 示例 Guice 模块：绑定 Netty 服务器、过滤器链与监控组件。
 *
 * Author: Arthur Gonigberg
 * Date: November 20, 2017
 */
public class ZuulSampleModule extends AbstractModule {
    @Override
    protected void configure() {
        // 示例专用绑定
        bind(BaseServerStartup.class).to(SampleServerStartup.class);

        // 使用 BasicNettyOriginManager 作为 Origin 管理器
        bind(OriginManager.class).to(BasicNettyOriginManager.class);

        // Zuul 核心过滤器加载
        install(new ZuulFiltersModule());
        bind(FilterFileManager.class).asEagerSingleton();

        install(new ZuulClasspathFiltersModule());
        // 通用服务器组件绑定
        // 健康检查与服务发现状态
        bind(ServerStatusManager.class);
        // 请求到达时装饰 SessionContext
        bind(SessionContextDecorator.class).to(ZuulSessionContextDecorator.class);
        // Spectator/Atlas 指标注册表
        bind(Registry.class).to(DefaultRegistry.class);
        // 请求完成后的指标回调
        bind(RequestCompleteHandler.class).to(BasicRequestCompleteHandler.class);
        // Eureka 发现客户端可选参数
        bind(AbstractDiscoveryClientOptionalArgs.class).to(DiscoveryClient.DiscoveryClientOptionalArgs.class);
        // 请求耗时指标发布器
        bind(RequestMetricsPublisher.class).to(BasicRequestMetricsPublisher.class);

        // 访问日志（含请求 UUID）
        bind(AccessLogPublisher.class).toInstance(new AccessLogPublisher("ACCESS",
                (channel, httpRequest) -> ClientRequestReceiver.getRequestFromChannel(channel).getContext().getUUID()));
    }
}
