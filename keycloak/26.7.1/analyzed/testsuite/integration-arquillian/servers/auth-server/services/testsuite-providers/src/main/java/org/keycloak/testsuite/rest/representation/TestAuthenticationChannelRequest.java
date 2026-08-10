/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.rest.representation;

import org.keycloak.protocol.oidc.grants.ciba.channel.AuthenticationChannelRequest;

/**
 * CIBA 认证通道请求的测试包装，保存 Bearer 令牌与原始请求体。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class TestAuthenticationChannelRequest {

    /** 原始 Bearer 访问令牌字符串。 */
    private String bearerToken;
    /** CIBA 认证通道请求对象。 */
    private AuthenticationChannelRequest request;

    /** 供反射使用的无参构造函数。 */
    public TestAuthenticationChannelRequest() {
        // for reflection
    }

    /**
     * 构造包含请求与 Bearer 令牌的实例。
     *
     * @param request 认证通道请求
     * @param bearerToken Bearer 令牌
     */
    public TestAuthenticationChannelRequest(AuthenticationChannelRequest request, String bearerToken) {
        setBearerToken(bearerToken);
        setRequest(request);
    }

    /** 设置 Bearer 令牌。 */
    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    /** 返回 Bearer 令牌。 */
    public String getBearerToken() {
        return bearerToken;
    }

    /** 设置认证通道请求。 */
    public void setRequest(AuthenticationChannelRequest request) {
        this.request = request;
    }

    /** 返回认证通道请求。 */
    public AuthenticationChannelRequest getRequest() {
        return request;
    }
}
