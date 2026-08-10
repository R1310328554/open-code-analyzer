package org.keycloak.infinispan.module.factory;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.infinispan.telemetry.InfinispanSpan;
import org.infinispan.telemetry.InfinispanSpanAttributes;
import org.infinispan.telemetry.InfinispanSpanContext;
import org.infinispan.telemetry.InfinispanTelemetry;

/**
 * 基于 OpenTelemetry 的 {@link InfinispanTelemetry} 实现。
 * <p>
 * 为 Infinispan 缓存请求创建 SERVER 类型 Span，并附加缓存名、类别与节点地址等属性；
 * 同时实现 {@link TextMapGetter} 以支持跨进程追踪上下文传播。
 */
public class OpenTelemetryService implements InfinispanTelemetry, TextMapGetter<InfinispanSpanContext> {

    /** Infinispan 服务端追踪 instrumentation 名称。 */
    private static final String INFINISPAN_SERVER_TRACING_NAME = "org.infinispan.server.tracing";
    /** instrumentation 语义版本。 */
    private static final String INFINISPAN_SERVER_TRACING_VERSION = "1.0.0";

    /** 用于创建缓存请求 Span 的 OpenTelemetry Tracer。 */
    private final Tracer tracer;
    /** 当前 Infinispan 节点名，写入 Span 的 server.address 属性。 */
    private volatile String nodeName = "n/a";

    /**
     * @param openTelemetry Quarkus/CDI 提供的 OpenTelemetry 实例
     */
    public OpenTelemetryService(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer(INFINISPAN_SERVER_TRACING_NAME, INFINISPAN_SERVER_TRACING_VERSION);
    }

    /** {@inheritDoc} 在同进程内继承当前上下文，为所有请求创建 Span（不做属性过滤）。 */
    @Override
    public <T> InfinispanSpan<T> startTraceRequest(String operationName, InfinispanSpanAttributes attributes) {
        // 原版 Infinispan 支持按 trace 属性过滤；此处追踪全部请求

        var builder = tracer.spanBuilder(operationName)
                .setSpanKind(SpanKind.SERVER);
        // 父上下文自动继承，因父 Span 在同进程内创建

        return createOpenTelemetrySpan(builder, attributes);
    }

    /** {@inheritDoc} 显式指定父 Span 上下文后创建 SERVER Span。 */
    @Override
    public <T> InfinispanSpan<T> startTraceRequest(String operationName, InfinispanSpanAttributes attributes, InfinispanSpanContext context) {
        // 原版 Infinispan 支持按 trace 属性过滤；此处追踪全部请求

        var builder = tracer.spanBuilder(operationName)
                .setSpanKind(SpanKind.SERVER)
                .setParent(Context.current().with(Span.current()));

        return createOpenTelemetrySpan(builder, attributes);
    }

    /** {@inheritDoc} 设置节点名，供 Span 的 server.address 属性使用。 */
    @Override
    public void setNodeName(String nodeName) {
        if (nodeName != null) {
            this.nodeName = nodeName;
        }
    }

    /** 为 Span 附加缓存名、类别与节点地址，并包装为 {@link OpenTelemetrySpan}。 */
    private <T> InfinispanSpan<T> createOpenTelemetrySpan(SpanBuilder builder, InfinispanSpanAttributes attributes) {
        attributes.cacheName().ifPresent(cacheName -> builder.setAttribute("cache", cacheName));
        builder.setAttribute("category", attributes.category().toString());
        builder.setAttribute("server.address", nodeName);
        return new OpenTelemetrySpan<>(builder.startSpan());
    }

    /** {@inheritDoc} 返回 Span 上下文中可传播的键集合。 */
    @Override
    public Iterable<String> keys(InfinispanSpanContext ctx) {
        return ctx.keys();
    }

    /** {@inheritDoc} 按键读取 Span 上下文中的传播值。 */
    @Override
    public String get(InfinispanSpanContext ctx, String key) {
        assert ctx != null;
        return ctx.getKey(key);
    }
}
