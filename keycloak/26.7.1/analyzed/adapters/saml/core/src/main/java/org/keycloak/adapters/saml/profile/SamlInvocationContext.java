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

package org.keycloak.adapters.saml.profile;

/**
 * SAML 调用上下文，携带单次请求中的 SAML 协议参数。
 *
 * <p>将 {@code SAMLRequest}、{@code SAMLResponse} 与 {@code RelayState} 聚合传递，
 * 供 {@link AbstractSamlAuthenticationHandler#doHandle} 统一分发处理。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class SamlInvocationContext {

    /** 入站 SAML 请求（如 LogoutRequest）的编码载荷。 */
    private String samlRequest;
    /** 入站 SAML 响应（如 LoginResponse）的编码载荷。 */
    private String samlResponse;
    /** 跨请求关联状态，用于登录/登出回调。 */
    private String relayState;

    /** 构造空上下文（三项均为 {@code null}）。 */
    public SamlInvocationContext() {
        this(null, null, null);
    }

    /**
     * 构造指定参数的 SAML 调用上下文。
     *
     * @param samlRequest  SAML 请求参数
     * @param samlResponse SAML 响应参数
     * @param relayState   RelayState 参数
     */
    public SamlInvocationContext(String samlRequest, String samlResponse, String relayState) {
        this.samlRequest = samlRequest;
        this.samlResponse = samlResponse;
        this.relayState = relayState;
    }

    /** @return SAML 请求载荷 */
    public String getSamlRequest() {
        return this.samlRequest;
    }

    /** @return SAML 响应载荷 */
    public String getSamlResponse() {
        return this.samlResponse;
    }

    /** @return RelayState 值 */
    public String getRelayState() {
        return this.relayState;
    }
}
