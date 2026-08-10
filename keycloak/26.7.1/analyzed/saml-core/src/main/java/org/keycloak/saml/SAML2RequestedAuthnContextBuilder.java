/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.saml;

import java.util.LinkedList;
import java.util.List;

import org.keycloak.dom.saml.v2.protocol.AuthnContextComparisonType;
import org.keycloak.dom.saml.v2.protocol.RequestedAuthnContextType;

/**
 * SAML 2.0 RequestedAuthnContext 元素构建器，声明 SP 期望的认证上下文。
 */
public class SAML2RequestedAuthnContextBuilder {
    /** 底层 RequestedAuthnContext 模型对象。 */
    private final RequestedAuthnContextType requestedAuthnContextType;
    /** 比较方式（exact / minimum / maximum / better）。 */
    private AuthnContextComparisonType comparison;
    /** AuthnContextClassRef URI 列表。 */
    private List<String> requestedAuthnContextClassRefList;
    /** AuthnContextDeclRef URI 列表。 */
    private List<String> requestedAuthnContextDeclRefList;

    /** 创建空的 RequestedAuthnContext 构建器。 */
    public SAML2RequestedAuthnContextBuilder() {
        this.requestedAuthnContextType = new RequestedAuthnContextType();
        this.requestedAuthnContextClassRefList = new LinkedList<String>();
        this.requestedAuthnContextDeclRefList = new LinkedList<String>();
    }

    /**
     * 设置比较方式（Comparison 属性）。
     *
     * @param comparison 比较枚举值
     * @return 当前构建器
     */
    public SAML2RequestedAuthnContextBuilder setComparison(AuthnContextComparisonType comparison) {
        this.comparison = comparison;
        return this;
    }

    /**
     * 添加 AuthnContextClassRef。
     *
     * @param authnContextClassRef 认证上下文类 URI
     * @return 当前构建器
     */
    public SAML2RequestedAuthnContextBuilder addAuthnContextClassRef(String authnContextClassRef) {
        this.requestedAuthnContextClassRefList.add(authnContextClassRef);
        return this;
    }

    /**
     * 添加 AuthnContextDeclRef。
     *
     * @param authnContextDeclRef 认证上下文声明 URI
     * @return 当前构建器
     */
    public SAML2RequestedAuthnContextBuilder addAuthnContextDeclRef(String authnContextDeclRef) {
        this.requestedAuthnContextDeclRefList.add(authnContextDeclRef);
        return this;
    }

    /**
     * 构建并返回 {@link RequestedAuthnContextType}。
     *
     * @return 配置完毕的认证上下文对象
     */
    public RequestedAuthnContextType build() {
        if (this.comparison != null)
            this.requestedAuthnContextType.setComparison(this.comparison);

        for (String requestedAuthnContextClassRef: this.requestedAuthnContextClassRefList)
            if (requestedAuthnContextClassRef != null && !requestedAuthnContextClassRef.isEmpty())
                this.requestedAuthnContextType.addAuthnContextClassRef(requestedAuthnContextClassRef);

        for (String requestedAuthnContextDeclRef: this.requestedAuthnContextDeclRefList)
            if (requestedAuthnContextDeclRef != null && !requestedAuthnContextDeclRef.isEmpty())
                this.requestedAuthnContextType.addAuthnContextDeclRef(requestedAuthnContextDeclRef);

        return this.requestedAuthnContextType;
    }
}