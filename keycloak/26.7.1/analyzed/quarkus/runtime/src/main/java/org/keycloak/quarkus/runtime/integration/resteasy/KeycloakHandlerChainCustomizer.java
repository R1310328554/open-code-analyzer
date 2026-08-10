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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.resteasy.reactive.common.model.ResourceClass;
import org.jboss.resteasy.reactive.server.handlers.FormBodyHandler;
import org.jboss.resteasy.reactive.server.model.HandlerChainCustomizer;
import org.jboss.resteasy.reactive.server.model.ServerResourceMethod;
import org.jboss.resteasy.reactive.server.spi.EndpointInvoker;
import org.jboss.resteasy.reactive.server.spi.ServerRestHandler;

import static jakarta.ws.rs.HttpMethod.PATCH;
import static jakarta.ws.rs.HttpMethod.POST;
import static jakarta.ws.rs.HttpMethod.PUT;

/**
 * Keycloak RESTEasy Reactive 处理器链定制器：
 * 替换默认调用处理器为带事务的 {@link TransactionalSessionHandler}，
 * 并在方法调用前后注入表单解析与响应 Content-Type 设置。
 */
public final class KeycloakHandlerChainCustomizer implements HandlerChainCustomizer {

    private final FormBodyHandler formBodyHandler = new FormBodyHandler(true, () -> Runnable::run, Set.of());

    /** 使用 {@link TransactionalSessionHandler} 包装端点调用，确保 Keycloak 会话与事务正确绑定。 */
    @Override
    public ServerRestHandler alternateInvocationHandler(EndpointInvoker invoker) {
        return new TransactionalSessionHandler(invoker);
    }

    /** 按阶段向处理器链追加表单解析或响应媒体类型处理器。 */
    @Override
    public List<ServerRestHandler> handlers(Phase phase, ResourceClass resourceClass,
            ServerResourceMethod resourceMethod) {
        List<ServerRestHandler> handlers = new ArrayList<>();

        switch (phase) {
            case BEFORE_METHOD_INVOKE:
                // 对 POST/PUT/PATCH 且未强制 @FormParam 的方法预解析表单体
                if (!resourceMethod.isFormParamRequired() &&
                    (PATCH.equalsIgnoreCase(resourceMethod.getHttpMethod()) ||
                     POST.equalsIgnoreCase(resourceMethod.getHttpMethod()) ||
                     PUT.equalsIgnoreCase(resourceMethod.getHttpMethod()))) {
                    handlers.add(formBodyHandler);
                }
                break;
            case AFTER_METHOD_INVOKE:
                handlers.add(new SetResponseContentTypeHandler(resourceMethod.getProduces()));
        }

        return handlers;
    }
}
