/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.client.util;

import org.keycloak.authorization.client.Configuration;
import org.keycloak.protocol.oidc.client.authentication.ClientCredentialsProvider;

import org.apache.http.client.methods.RequestBuilder;

/**
 * <p>授权客户端 HTTP 请求构建器，封装 GET/POST/PUT/DELETE 等常用动词。
 *
 * <p>每次调用返回 {@link HttpMethod} 实例，用于链式配置认证、请求体与响应解析后再执行。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class Http {

    private final Configuration configuration;
    private final ClientCredentialsProvider authenticator;

    /** 使用客户端配置与凭证提供方创建 HTTP 工具。 */
    public Http(Configuration configuration, ClientCredentialsProvider authenticator) {
        this.configuration = configuration;
        this.authenticator = authenticator;
    }

    /** 构建 GET 请求。 */
    public <R> HttpMethod<R> get(String path) {
        return method(RequestBuilder.get().setUri(path));
    }

    /** 构建 POST 请求。 */
    public <R> HttpMethod<R> post(String path) {
        return method(RequestBuilder.post().setUri(path));
    }

    /** 构建 PUT 请求。 */
    public <R> HttpMethod<R> put(String path) {
        return method(RequestBuilder.put().setUri(path));
    }

    /** 构建 DELETE 请求。 */
    public <R> HttpMethod<R> delete(String path) {
        return method(RequestBuilder.delete().setUri(path));
    }

    /** 根据 Apache {@link RequestBuilder} 创建 {@link HttpMethod}。 */
    private <R> HttpMethod<R> method(RequestBuilder builder) {
        return new HttpMethod(this.configuration, authenticator, builder);
    }
}
