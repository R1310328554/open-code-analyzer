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

package org.keycloak.models.sessions.infinispan.changes;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.keycloak.models.KeycloakSession;

import io.opentelemetry.api.trace.Span;

/**
 * 封装会话存储的延迟更新任务。
 * <p>
 * 在 Keycloak 事务提交后异步执行，并通过 {@link CompletableFuture} 与 OpenTelemetry Span 跟踪完成状态。
 *
 * @author Alexander Schwartz
 */
public class PersistentUpdate {

    /** 延迟执行的更新逻辑。 */
    private final Consumer<KeycloakSession> task;
    /** 异步完成信号。 */
    private final CompletableFuture<Void> future = new CompletableFuture<>();
    /** 创建任务时的追踪 Span。 */
    private final Span span;

    public PersistentUpdate(Consumer<KeycloakSession> task) {
        this.task = task;
        this.span = Span.current();
    }

    /** 在当前 Keycloak 会话上下文中执行延迟更新。 */
    public void perform(KeycloakSession session) {
        task.accept(session);
    }

    /** 标记更新成功完成。 */
    public void complete() {
        future.complete(null);
    }

    /** 标记更新失败并传播异常。 */
    public void fail(Throwable throwable) {
        future.completeExceptionally(throwable);
    }

    public CompletableFuture<Void> future() {
        return future;
    }

    public Span getSpan() {
        return span;
    }
}
