/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http.cors;

import static io.netty.util.internal.ObjectUtil.checkNotNullWithIAE;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * {@link CorsConfig} 构建器，链式配置 Origin、方法、头、凭证与预检选项。
 */
public final class CorsConfigBuilder {

    /**
     * 创建允许任意 Origin（{@code *}）的构建器。
     *
     * @return Builder to support method chaining.
     */
    public static CorsConfigBuilder forAnyOrigin() {
        return new CorsConfigBuilder();
    }

    /**
     * 创建仅允许单个指定 Origin 的构建器。
     *
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public static CorsConfigBuilder forOrigin(final String origin) {
        if ("*".equals(origin)) {
            return new CorsConfigBuilder();
        }
        return new CorsConfigBuilder(origin);
    }

    /**
     * 创建允许多个 Origin 的构建器。
     *
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public static CorsConfigBuilder forOrigins(final String... origins) {
        return new CorsConfigBuilder(origins);
    }

    final Set<String> origins;
    final boolean anyOrigin;
    boolean allowNullOrigin;
    boolean enabled = true;
    boolean allowCredentials;
    final Set<String> exposeHeaders = new HashSet<String>();
    long maxAge;
    final Set<HttpMethod> requestMethods = new HashSet<HttpMethod>();
    final Set<String> requestHeaders = new HashSet<String>();
    final Map<CharSequence, Callable<?>> preflightHeaders = new HashMap<CharSequence, Callable<?>>();
    private boolean noPreflightHeaders;
    boolean shortCircuit;
    boolean allowPrivateNetwork;

    /**
     * Creates a new Builder instance with the origin passed in.
     *
     * @param origins the origin to be used for this builder.
     */
    CorsConfigBuilder(final String... origins) {
        this.origins = new LinkedHashSet<String>(Arrays.asList(origins));
        anyOrigin = false;
    }

    /**
     * Creates a new Builder instance allowing any origin, "*" which is the
     * wildcard origin.
     *
     */
    CorsConfigBuilder() {
        anyOrigin = true;
        origins = Collections.emptySet();
    }

    /**
     * 允许 Origin 为 {@code null} 的跨域响应。
     *
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder allowNullOrigin() {
        allowNullOrigin = true;
        return this;
    }

    /**
     * 关闭 CORS 支持。
     *
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder disable() {
        enabled = false;
        return this;
    }

    /**
     * Specifies the headers to be exposed to calling clients.
     *
     * During a simple CORS request, only certain response headers are made available by the
     * browser, for example using:
     * <pre>
     * xhr.getResponseHeader("Content-Type");
     * </pre>
     *
     * The headers that are available by default are:
     * <ul>
     * <li>Cache-Control</li>
     * <li>Content-Language</li>
     * <li>Content-Type</li>
     * <li>Expires</li>
     * <li>Last-Modified</li>
     * <li>Pragma</li>
     * </ul>
     *
     * To expose other headers they need to be specified which is what this method enables by
     * adding the headers to the CORS 'Access-Control-Expose-Headers' response header.
     *
     * @param headers the values to be added to the 'Access-Control-Expose-Headers' response header
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder exposeHeaders(final String... headers) {
        exposeHeaders.addAll(Arrays.asList(headers));
        return this;
    }

    /**
     * Specifies the headers to be exposed to calling clients.
     *
     * During a simple CORS request, only certain response headers are made available by the
     * browser, for example using:
     * <pre>
     * xhr.getResponseHeader(HttpHeaderNames.CONTENT_TYPE);
     * </pre>
     *
     * The headers that are available by default are:
     * <ul>
     * <li>Cache-Control</li>
     * <li>Content-Language</li>
     * <li>Content-Type</li>
     * <li>Expires</li>
     * <li>Last-Modified</li>
     * <li>Pragma</li>
     * </ul>
     *
     * To expose other headers they need to be specified which is what this method enables by
     * adding the headers to the CORS 'Access-Control-Expose-Headers' response header.
     *
     * @param headers the values to be added to the 'Access-Control-Expose-Headers' response header
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder exposeHeaders(final CharSequence... headers) {
        for (CharSequence header: headers) {
            exposeHeaders.add(header.toString());
        }
        return this;
    }

    /**
     * 允许跨域请求携带 Cookie，设置 Allow-Credentials 为 true。
     * Calling this method will set the CORS 'Access-Control-Allow-Credentials'
     * response header to true.
     *
     * Please note, that cookie support needs to be enabled on the client side as well.
     * The client needs to opt-in to send cookies by calling:
     * <pre>
     * xhr.withCredentials = true;
     * </pre>
     * The default value for 'withCredentials' is false in which case no cookies are sent.
     * Setting this to true will included cookies in cross origin requests.
     *
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder allowCredentials() {
        allowCredentials = true;
        return this;
    }

    /**
     * 设置预检缓存 Max-Age（秒）。
     *
     * @param max the maximum time, in seconds, that the preflight response may be cached.
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder maxAge(final long max) {
        maxAge = max;
        return this;
    }

    /**
     * 配置允许的 HTTP 方法。
     *
     * @param methods the {@link HttpMethod}s that should be allowed.
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder allowedRequestMethods(final HttpMethod... methods) {
        requestMethods.addAll(Arrays.asList(methods));
        return this;
    }

    /**
     * Specifies the if headers that should be returned in the CORS 'Access-Control-Allow-Headers'
     * response header.
     *
     * If a client specifies headers on the request, for example by calling:
     * <pre>
     * xhr.setRequestHeader('My-Custom-Header', "SomeValue");
     * </pre>
     * the server will receive the above header name in the 'Access-Control-Request-Headers' of the
     * preflight request. The server will then decide if it allows this header to be sent for the
     * real request (remember that a preflight is not the real request but a request asking the server
     * if it allow a request).
     *
     * @param headers the headers to be added to the preflight 'Access-Control-Allow-Headers' response header.
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder allowedRequestHeaders(final String... headers) {
        requestHeaders.addAll(Arrays.asList(headers));
        return this;
    }

    /**
     * Specifies the if headers that should be returned in the CORS 'Access-Control-Allow-Headers'
     * response header.
     *
     * If a client specifies headers on the request, for example by calling:
     * <pre>
     * xhr.setRequestHeader('My-Custom-Header', "SomeValue");
     * </pre>
     * the server will receive the above header name in the 'Access-Control-Request-Headers' of the
     * preflight request. The server will then decide if it allows this header to be sent for the
     * real request (remember that a preflight is not the real request but a request asking the server
     * if it allow a request).
     *
     * @param headers the headers to be added to the preflight 'Access-Control-Allow-Headers' response header.
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder allowedRequestHeaders(final CharSequence... headers) {
        for (CharSequence header: headers) {
            requestHeaders.add(header.toString());
        }
        return this;
    }

    /**
     * Returns HTTP response headers that should be added to a CORS preflight response.
     *
     * An intermediary like a load balancer might require that a CORS preflight request
     * have certain headers set. This enables such headers to be added.
     *
     * @param name the name of the HTTP header.
     * @param values the values for the HTTP header.
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder preflightResponseHeader(final CharSequence name, final Object... values) {
        if (values.length == 1) {
            preflightHeaders.put(name, new ConstantValueGenerator(values[0]));
        } else {
            preflightResponseHeader(name, Arrays.asList(values));
        }
        return this;
    }

    /**
     * Returns HTTP response headers that should be added to a CORS preflight response.
     *
     * An intermediary like a load balancer might require that a CORS preflight request
     * have certain headers set. This enables such headers to be added.
     *
     * @param name the name of the HTTP header.
     * @param value the values for the HTTP header.
     * @param <T> the type of values that the Iterable contains.
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public <T> CorsConfigBuilder preflightResponseHeader(final CharSequence name, final Iterable<T> value) {
        preflightHeaders.put(name, new ConstantValueGenerator(value));
        return this;
    }

    /**
     * Returns HTTP response headers that should be added to a CORS preflight response.
     *
     * An intermediary like a load balancer might require that a CORS preflight request
     * have certain headers set. This enables such headers to be added.
     *
     * Some values must be dynamically created when the HTTP response is created, for
     * example the 'Date' response header. This can be accomplished by using a Callable
     * which will have its 'call' method invoked when the HTTP response is created.
     *
     * @param name the name of the HTTP header.
     * @param valueGenerator a Callable which will be invoked at HTTP response creation.
     * @param <T> the type of the value that the Callable can return.
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public <T> CorsConfigBuilder preflightResponseHeader(final CharSequence name, final Callable<T> valueGenerator) {
        preflightHeaders.put(name, valueGenerator);
        return this;
    }

    /**
     * Specifies that no preflight response headers should be added to a preflight response.
     *
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder noPreflightResponseHeaders() {
        noPreflightHeaders = true;
        return this;
    }

    /**
     * 启用短路：非法 Origin 直接 403，不进入后续 handler。
     *
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder shortCircuit() {
        shortCircuit = true;
        return this;
    }

    /**
     * 允许从公网站点访问私有网络（Private Network Access）。
     *
     * @return {@link CorsConfigBuilder} to support method chaining.
     */
    public CorsConfigBuilder allowPrivateNetwork() {
        allowPrivateNetwork = true;
        return this;
    }

    /**
     * 根据当前配置构建不可变的 {@link CorsConfig}。
     *
     * @return {@link CorsConfig} the configured CorsConfig instance.
     */
    public CorsConfig build() {
        if (preflightHeaders.isEmpty() && !noPreflightHeaders) {
            preflightHeaders.put(HttpHeaderNames.DATE, DateValueGenerator.INSTANCE);
            preflightHeaders.put(HttpHeaderNames.CONTENT_LENGTH, new ConstantValueGenerator("0"));
        }
        return new CorsConfig(this);
    }

    /**
     * This class is used for preflight HTTP response values that do not need to be
     * generated, but instead the value is "static" in that the same value will be returned
     * for each call.
     */
    private static final class ConstantValueGenerator implements Callable<Object> {

        private final Object value;

        /**
         * Sole constructor.
         *
         * @param value the value that will be returned when the call method is invoked.
         */
        private ConstantValueGenerator(final Object value) {
            this.value = checkNotNullWithIAE(value, "value");
        }

        @Override
        public Object call() {
            return value;
        }
    }

    /**
     * This callable is used for the DATE preflight HTTP response HTTP header.
     * It's value must be generated when the response is generated, hence will be
     * different for every call.
     */
    private static final class DateValueGenerator implements Callable<Date> {

        static final DateValueGenerator INSTANCE = new DateValueGenerator();

        @Override
        public Date call() throws Exception {
            return new Date();
        }
    }
}
