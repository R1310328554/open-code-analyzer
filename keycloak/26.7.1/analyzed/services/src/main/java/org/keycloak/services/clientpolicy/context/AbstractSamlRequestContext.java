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

package org.keycloak.services.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;


/**
 * 任意 SAML 请求的抽象上下文：携带收到的请求对象、发起请求的 {@link ClientModel} 及协议绑定类型。
 * <p>供 SAML 相关 {@link ClientPolicyContext} 实现复用，便于条件/Executor 访问客户端与绑定信息。</p>
 *
 * @author rmartinc
 * @param <T> SAML 请求类型
 */
public abstract class AbstractSamlRequestContext<T> implements ClientPolicyContext, ClientModelContext {

    /** 收到的 SAML 请求对象。 */
    protected final T request;
    /** 发起请求的客户端模型。 */
    protected final ClientModel client;
    /** 处理该请求的 Keycloak 协议绑定类型。 */
    protected final String protocolBinding;

    /**
     * @param request SAML 请求对象
     * @param client 客户端模型
     * @param protocolBinding 协议绑定类型
     */
    public AbstractSamlRequestContext(final T request, final ClientModel client, final String protocolBinding) {
        this.request = request;
        this.client = client;
        this.protocolBinding = protocolBinding;
    }

    /** {@inheritDoc} 由子类返回具体 {@link ClientPolicyEvent}。 */
    @Override
    public abstract ClientPolicyEvent getEvent();

    /** @return 收到的 SAML 请求对象 */
    public T getRequest() {
        return request;
    }

    /** {@inheritDoc} @return 发起请求的客户端 */
    @Override
    public ClientModel getClient() {
        return client;
    }

    /** @return 处理请求的协议绑定类型 */
    public String getProtocolBinding() {
        return protocolBinding;
    }
}
