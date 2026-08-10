package org.keycloak.infinispan.module.factory;

import java.util.Objects;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import org.infinispan.telemetry.InfinispanSpan;
import org.infinispan.telemetry.SafeAutoClosable;

/**
 * 将 OpenTelemetry {@link Span} 适配为 Infinispan {@link InfinispanSpan} 的包装类。
 */
public class OpenTelemetrySpan<T> implements InfinispanSpan<T> {

    /** 底层 OpenTelemetry Span 实例。 */
    private final Span span;

    /**
     * @param span 非空的 OpenTelemetry Span
     */
    public OpenTelemetrySpan(Span span) {
        this.span = Objects.requireNonNull(span);
    }

    /** {@inheritDoc} 将 Span 设为当前上下文，返回关闭时恢复上下文的 {@link SafeAutoClosable}。 */
    @Override
    public SafeAutoClosable makeCurrent() {
        //noinspection resource
        Scope scope = span.makeCurrent();
        return scope::close;
    }

    /** {@inheritDoc} 正常结束 Span。 */
    @Override
    public void complete() {
        span.end();
    }

    /** {@inheritDoc} 记录异常并将 Span 状态标记为 ERROR。 */
    @Override
    public void recordException(Throwable throwable) {
        span.setStatus(StatusCode.ERROR, "Error during the cache request processing");
        span.recordException(throwable);
    }
}
