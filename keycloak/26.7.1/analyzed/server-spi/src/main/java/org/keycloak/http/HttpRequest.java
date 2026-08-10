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

package org.keycloak.http;

import java.security.cert.X509Certificate;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.models.KeycloakContext;

/**
 * <p>入站 HTTP 请求抽象：封装方法、表单参数、头信息与客户端证书等。
 *
 * <p>Represents an incoming HTTP request.
 *
 * <p>Instances of this class can be obtained from {@link KeycloakContext#getHttpRequest()}.
 */
public interface HttpRequest {

    /**
     * 返回 HTTP 方法（GET、POST 等）。
     * Returns the HTTP method.
     *
     * @return the HTTP method.
     */
    String getHttpMethod();

    /**
     * 返回已 HTML 解码的 {@code application/x-www-form-urlencoded} 表单参数。
     * <p>Returns the form parameters (e.g.: media type {@code application/x-www-form-urlencoded}) as a {@link MultivaluedMap} where the key and its correspondent value maps to the parameter name and
     * value, respectively.
     *
     * <p>The values are already decoded using HTML form decoding.
     *
     * @return the decoded form parameters
     */
    MultivaluedMap<String, String> getDecodedFormParameters();

    /**
     * 解析 {@code multipart/form-data} 请求的各字段部分。
     * Parses the parts from a multipart form request (e.g.: multipart/form-data media type).
     *
     * @return the parts from a multipart form request
     */
    MultivaluedMap<String, FormPartValue> getMultiPartFormParameters();

    /**
     * 返回 HTTP 请求头。
     * Returns the HTTP headers.
     *
     * @return the HTTP headers
     */
    HttpHeaders getHttpHeaders();

    /**
     * 返回 TLS 客户端 X509 证书链（双向 TLS 场景）。
     * Returns the client X509 certificate chain when processing TLS requests.
     *
     * @return the client certificate chain
     */
    X509Certificate[] getClientCertificateChain();

    /**
     * 返回当前请求路径的 {@link UriInfo}。
     * Returns a {@link UriInfo} instance for the path being requested.
     *
     * @return the {@link UriInfo} for the current path
     */
    UriInfo getUri();

    /**
     * 若启用可信代理且请求来自不可信来源则返回 false。
     * Returns false if the server is configured for trusted proxies and the
     * request is from an untrusted source.
     *
     * @return false if the server is configured for trusted proxies and the
     * request is from an untrusted source.
     */
    boolean isProxyTrusted();
}
