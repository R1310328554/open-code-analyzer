/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.tracing;

import java.util.function.Consumer;
import java.util.function.Function;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;

/**
 * 空操作追踪提供者：当 {@link org.keycloak.common.Profile.Feature#OPENTELEMETRY} 未启用时使用。
 * <p>所有追踪方法均为无操作，返回无效 Span。</p>
 */
public class NoopTracingProvider implements TracingProvider {
    /** @return 无效 Span，表示无活跃追踪 */
    @Override
    public Span getCurrentSpan() {
        return Span.getInvalid();
    }

    /** @return 无效 Span，不创建真实追踪 */
    @Override
    public Span startSpan(String tracerName, String spanName) {
        return Span.getInvalid();
    }

    @Override
    public Span startSpan(SpanBuilder builder) {
        return Span.getInvalid();
    }

    @Override
    public void endSpan() {
        // 无操作
    }

    @Override
    public void error(Throwable error) {
        //noop
    }

    @Override
    public void trace(String tracerName, String spanName, Consumer<Span> execution) {
        execution.accept(getCurrentSpan());
    }

    @Override
    public <T> T trace(String tracerName, String spanName, Function<Span, T> execution) {
        return execution.apply(getCurrentSpan());
    }

    @Override
    public void trace(Class<?> tracerClass, String spanSuffix, Consumer<Span> execution) {
        trace((String) null, null, execution);
    }

    @Override
    public <T> T trace(Class<?> tracerClass, String spanSuffix, Function<Span, T> execution) {
        return trace((String) null, null, execution);
    }

    @Override
    public Tracer getTracer(String name, String scopeVersion) {
        return TracerProvider.noop().get("");
    }

    /** @return 始终为 {@code true}，空实现无未结束 Span */
    @Override
    public boolean validateAllSpansEnded() {
        return true;
    }

    @Override
    public void close() {

    }
}
