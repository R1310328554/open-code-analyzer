/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http;

import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.headersFactory;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 默认 {@link HttpRequest} 实现。
 * <p>
 * 包含 HTTP 方法、URI 与头部；可选校验请求行 token 合法性。
 */
public class DefaultHttpRequest extends DefaultHttpMessage implements HttpRequest {
    private static final int HASH_CODE_PRIME = 31;
    /** HTTP 方法（GET/POST 等）。 */
    private HttpMethod method;
    /** 请求 URI 或路径。 */
    private String uri;
    /** 是否在构造/setter 时校验请求行 token。 */
    private final boolean validateRequestLine;

    /**
     * 创建 HTTP 请求实例。
     *
     * @param httpVersion the HTTP version of the request
     * @param method      the HTTP method of the request
     * @param uri         the URI or path of the request
     */
    public DefaultHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
        this(httpVersion, method, uri, headersFactory().newHeaders());
    }

    /**
     * Creates a new instance.
     *
     * @param httpVersion       the HTTP version of the request
     * @param method            the HTTP method of the request
     * @param uri               the URI or path of the request
     * @param validateHeaders   validate the header names and values when adding them to the {@link HttpHeaders}
     * @deprecated Prefer the {@link #DefaultHttpRequest(HttpVersion, HttpMethod, String)} constructor instead,
     * to always have header validation enabled.
     */
    @Deprecated
    public DefaultHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, boolean validateHeaders) {
        this(httpVersion, method, uri, headersFactory().withValidation(validateHeaders));
    }

    /**
     * Creates a new instance.
     *
     * @param httpVersion       the HTTP version of the request
     * @param method            the HTTP method of the request
     * @param uri               the URI or path of the request
     * @param headersFactory    the {@link HttpHeadersFactory} used to create the headers for this Request.
     * The recommended default is {@link DefaultHttpHeadersFactory#headersFactory()}.
     */
    public DefaultHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri,
                              HttpHeadersFactory headersFactory) {
        this(httpVersion, method, uri, headersFactory.newHeaders());
    }

    /**
     * Creates a new instance.
     *
     * @param httpVersion       the HTTP version of the request
     * @param method            the HTTP method of the request
     * @param uri               the URI or path of the request
     * @param headers           the Headers for this Request
     */
    public DefaultHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, HttpHeaders headers) {
        this(httpVersion, method, uri, headers, true);
    }

    /**
     * Creates a new instance.
     *
     * @param httpVersion       the HTTP version of the request
     * @param method            the HTTP method of the request
     * @param uri               the URI or path of the request
     * @param headers           the Headers for this Request
     */
    public DefaultHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, HttpHeaders headers,
                              boolean validateRequestLine) {
        super(httpVersion, headers);
        this.method = checkNotNull(method, "method");
        this.uri = checkNotNull(uri, "uri");
        this.validateRequestLine = validateRequestLine;
        if (validateRequestLine) {
            // 校验 method 与 uri 不含非法字符
            HttpUtil.validateRequestLineTokens(method, uri);
        }
    }

    @Override
    @Deprecated
    public HttpMethod getMethod() {
        return method();
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    @Deprecated
    public String getUri() {
        return uri();
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public HttpRequest setMethod(HttpMethod method) {
        checkNotNull(method, "method");
        if (validateRequestLine) {
            HttpUtil.validateRequestLineTokens(method, uri);
        }
        this.method = method;
        return this;
    }

    @Override
    public HttpRequest setUri(String uri) {
        checkNotNull(uri, "uri");
        if (validateRequestLine) {
            HttpUtil.validateRequestLineTokens(method, uri);
        }
        this.uri = uri;
        return this;
    }

    @Override
    public HttpRequest setProtocolVersion(HttpVersion version) {
        super.setProtocolVersion(version);
        return this;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = HASH_CODE_PRIME * result + method.hashCode();
        result = HASH_CODE_PRIME * result + uri.hashCode();
        result = HASH_CODE_PRIME * result + super.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultHttpRequest)) {
            return false;
        }

        DefaultHttpRequest other = (DefaultHttpRequest) o;

        return method().equals(other.method()) &&
               uri().equalsIgnoreCase(other.uri()) && // URI 比较忽略大小写
               super.equals(o);
    }

    @Override
    public String toString() {
        return HttpMessageUtil.appendRequest(new StringBuilder(256), this).toString();
    }
}
