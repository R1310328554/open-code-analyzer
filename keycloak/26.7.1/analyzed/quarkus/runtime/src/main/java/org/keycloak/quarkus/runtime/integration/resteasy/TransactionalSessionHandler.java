/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.integration.resteasy;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow.Publisher;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.utils.KeycloakSessionUtil;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ClientProxy;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestMulti;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.common.core.BlockingOperationSupport;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;
import org.jboss.resteasy.reactive.server.handlers.InvocationHandler;
import org.jboss.resteasy.reactive.server.spi.EndpointInvoker;

/**
 * 带 Keycloak 事务与会话绑定的 RESTEasy Reactive 调用处理器，
 * 在阻塞线程上开启 JTA 事务，并在异步返回类型场景下提前清理线程绑定资源。
 */
public final class TransactionalSessionHandler extends InvocationHandler implements org.keycloak.quarkus.runtime.transaction.TransactionalSessionHandler {

    /*
     * 参见 AsyncReturnTypeScanner —— 似乎没有更简单的方式抢在对应 handler 之前介入，
     * 因此在此集中维护相关异步返回类型集合。
     *
     * 若有遗漏，KeycloakBeanProducer.dispose 中的日志应能提示
     *
     * Resteasy Reactive 专有类型一并列出以保持完整；目前内外部逻辑尚未预期使用它们
     */
    public static final Set<Class<?>> ASYNC_TYPES = Set.of(
        CompletionStage.class,
        CompletableFuture.class,
        Uni.class,
        Multi.class,
        RestMulti.class,
        Publisher.class,
        org.reactivestreams.Publisher.class,
        RestResponse.class
    );

    /** @param invoker 被包装的端点调用器 */
    public TransactionalSessionHandler(EndpointInvoker invoker) {
        super(invoker);
    }

    @Override
    public void handle(ResteasyReactiveRequestContext requestContext) throws Exception {
        // 解析子资源时同一请求可能多次进入本方法

        requestContext.requireCDIRequestScope();

        KeycloakSession currentSession = ClientProxy.unwrap(Arc.container().instance(KeycloakSession.class).get());

        // 调用底层 invoker 前确保线程绑定资源已设置
        KeycloakSessionUtil.setKeycloakSession(currentSession);
        if (BlockingOperationSupport.isBlockingAllowed()) {
            // ClientProxy.unwrap() 会在首次方法调用或 unwrap 时解析懒初始化代理
            KeycloakTransactionManager transactionManager = currentSession.getTransactionManager();
            if (!transactionManager.isActive()) {
                // 本 handler 始终在阻塞线程上运行
                beginTransaction(currentSession);
            }
        }

        super.handle(requestContext);

        // 异步场景：发起线程已结束，需清理线程绑定状态
        if ((requestContext.getAsyncResponse() != null || ASYNC_TYPES
                .contains(requestContext.getResteasyReactiveResourceInfo().getMethod().getReturnType()))) {
            close(currentSession);
        }
    }
}
