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

import io.netty.util.AsciiString;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * HTTP 请求方法（及 RTSP、ICAP 等衍生协议的方法名）。
 * <p>
 * 内置常用方法常量；{@link #valueOf(String)} 对标准方法返回缓存实例。
 */
public class HttpMethod implements Comparable<HttpMethod> {

    /**
     * The OPTIONS method represents a request for information about the communication options
     * available on the request/response chain identified by the Request-URI. This method allows
     * the client to determine the options and/or requirements associated with a resource, or the
     * capabilities of a server, without implying a resource action or initiating a resource
     * retrieval.
     */
    public static final HttpMethod OPTIONS = new HttpMethod(AsciiString.cached("OPTIONS"));

    /**
     * GET：获取 Request-URI 所标识资源的表示。
     */
    public static final HttpMethod GET = new HttpMethod(AsciiString.cached("GET"));

    /**
     * The HEAD method is identical to GET except that the server MUST NOT return a message-body
     * in the response.
     */
    public static final HttpMethod HEAD = new HttpMethod(AsciiString.cached("HEAD"));

    /**
     * POST：向 Request-URI 标识的资源提交实体数据。
     */
    public static final HttpMethod POST = new HttpMethod(AsciiString.cached("POST"));

    /**
     * PUT：将请求实体存储到 Request-URI 指定位置。
     */
    public static final HttpMethod PUT = new HttpMethod(AsciiString.cached("PUT"));

    /**
     * The PATCH method requests that a set of changes described in the
     * request entity be applied to the resource identified by the Request-URI.
     */
    public static final HttpMethod PATCH = new HttpMethod(AsciiString.cached("PATCH"));

    /**
     * DELETE：删除 Request-URI 标识的资源。
     */
    public static final HttpMethod DELETE = new HttpMethod(AsciiString.cached("DELETE"));

    /**
     * The TRACE method is used to invoke a remote, application-layer loop- back of the request
     * message.
     */
    public static final HttpMethod TRACE = new HttpMethod(AsciiString.cached("TRACE"));

    /**
     * CONNECT：经代理建立隧道（常用于 HTTPS）。
     */
    public static final HttpMethod CONNECT = new HttpMethod(AsciiString.cached("CONNECT"));

    /**
     * The QUERY method requests that the request target process the enclosed content in a safe and
     * idempotent manner and then respond with the result of that processing.
     */
    public static final HttpMethod QUERY = new HttpMethod(AsciiString.cached("QUERY"));

    /**
     * 按名称返回 {@link HttpMethod}；标准方法返回缓存实例，否则新建。
     */
    public static HttpMethod valueOf(String name) {
        switch (name) {
            case "OPTIONS": return HttpMethod.OPTIONS;
            case "GET":     return HttpMethod.GET;
            case "HEAD":    return HttpMethod.HEAD;
            case "POST":    return HttpMethod.POST;
            case "PUT":     return HttpMethod.PUT;
            case "PATCH":   return HttpMethod.PATCH;
            case "DELETE":  return HttpMethod.DELETE;
            case "TRACE":   return HttpMethod.TRACE;
            case "CONNECT": return HttpMethod.CONNECT;
            case "QUERY":   return HttpMethod.QUERY;
            default:        return new HttpMethod(name);
        }
    }

    /** 方法名（{@link AsciiString} 缓存）。 */
    private final AsciiString name;

    /**
     * 内置常量专用构造器；字面量已保证为合法 HTTP token，无需运行时校验。
     */
    private HttpMethod(AsciiString name) {
        this.name = name;
    }

    /**
     * 以指定名称创建 HTTP 方法；仅扩展 RTSP/ICAP 等协议时需要。
     */
    public HttpMethod(String name) {
        checkNotNull(name, "name");
        // 方法名非空且仅含合法 HTTP token 字符
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        int index = HttpHeaderValidationUtil.validateToken(name);
        if (index != -1) {
            throw new IllegalArgumentException(
                    "Illegal character in HTTP Method: 0x" + Integer.toHexString(name.charAt(index)));
        }
        this.name = AsciiString.cached(name);
    }

    /**
     * 返回方法名字符串。
     */
    public String name() {
        return name.toString();
    }

    /**
     * 返回 {@link AsciiString} 形式的方法名。
     */
    public AsciiString asciiName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HttpMethod)) {
            return false;
        }

        HttpMethod that = (HttpMethod) o;
        return name().equals(that.name());
    }

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public int compareTo(HttpMethod o) {
        if (o == this) {
            return 0;
        }
        return name().compareTo(o.name());
    }

}
