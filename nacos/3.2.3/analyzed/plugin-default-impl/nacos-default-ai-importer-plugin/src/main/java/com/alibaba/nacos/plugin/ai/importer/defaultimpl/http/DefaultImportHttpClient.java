/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.ai.importer.defaultimpl.http;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * 内置 AI 导入插件共享 HTTP 客户端。
 *
 * <p>基于 {@link java.net.http.HttpClient} 发起 GET 请求， 强制执行 HTTPS、禁止跟随重定向、限制响应体大小， 并可选允许 HTTP 或内网/本地地址（由导入源属性控制）。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class DefaultImportHttpClient {
    
    /** 导入源属性：是否允许明文 HTTP（kebab-case）。 */
    public static final String PROPERTY_ALLOW_HTTP = "allow-http";
    
    public static final String PROPERTY_ALLOW_HTTP_CAMEL = "allowHttp";
    
    /** 导入源属性：是否允许访问内网/本地地址（kebab-case）。 */
    public static final String PROPERTY_ALLOW_PRIVATE_NETWORK = "allow-private-network";
    
    public static final String PROPERTY_ALLOW_PRIVATE_NETWORK_CAMEL = "allowPrivateNetwork";
    
    private static final String HTTPS_SCHEME = "https";
    
    private static final String HTTP_SCHEME = "http";
    
    private static final String LOCALHOST = "localhost";
    
    private static final String LOCALHOST_SUFFIX = ".localhost";
    
    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
    
    private static final int DEFAULT_READ_TIMEOUT_SECONDS = 20;
    
    private static final long DEFAULT_MAX_RESPONSE_BYTES = 10L * 1024L * 1024L;
    
    private final HttpClient httpClient;
    
    private final DnsResolver dnsResolver;
    
    /** 使用默认超时与不跟随重定向策略构造客户端。 */
    public DefaultImportHttpClient() {
        this(HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECONDS))
            .build());
    }
    
    public DefaultImportHttpClient(HttpClient httpClient) {
        this(httpClient, InetAddress::getAllByName);
    }
    
    /**
     * 使用自定义 {@link HttpClient} 与 DNS 解析器构造（便于测试）。
     *
     * @param httpClient HTTP client
     * @param dnsResolver DNS resolver
     */
    public DefaultImportHttpClient(HttpClient httpClient, DnsResolver dnsResolver) {
        this.httpClient = httpClient;
        this.dnsResolver = dnsResolver;
    }
    
    /**
     * 以默认读超时发送 GET 请求。
     *
     * @param source import source
     * @param url request URL
     * @param accept optional Accept header
     * @return HTTP response
     * @throws Exception if validation or request fails
     */
    public ImportHttpResponse get(AiResourceImportSource source, String url, String accept)
        throws Exception {
        return get(source, url, DEFAULT_READ_TIMEOUT_SECONDS, accept);
    }
    
    /**
     * 校验 URL 与网络安全策略后发送 GET 请求。
     *
     * @param source import source
     * @param url request URL
     * @param readTimeoutSeconds request read timeout in seconds
     * @param accept optional Accept header
     * @return HTTP response
     * @throws Exception if validation or request fails
     */
    public ImportHttpResponse get(AiResourceImportSource source, String url,
        int readTimeoutSeconds, String accept) throws Exception {
        URI uri = parseUrl(url);
        checkRequestTarget(source, uri);
        long maxResponseBytes = resolveMaxResponseBytes(source);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(readTimeoutSeconds))
            .GET();
        if (StringUtils.isNotBlank(accept)) {
            builder.header("Accept", accept);
        }
        HttpResponse<byte[]> response = httpClient.send(builder.build(),
            new LimitedByteArrayBodyHandler(maxResponseBytes));
        byte[] body = response.body() == null ? new byte[0] : response.body();
        checkResponseSize(body, maxResponseBytes);
        return new ImportHttpResponse(uri.toString(), response.statusCode(), response.headers(),
            body);
    }
    
    private URI parseUrl(String url) throws NacosException {
        if (StringUtils.isBlank(url)) {
            throw invalid("AI resource import request URL must not be empty.");
        }
        try {
            URI result = URI.create(url.trim());
            if (!result.isAbsolute()) {
                throw invalid("AI resource import request URL must be an absolute URL.");
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw invalid("AI resource import request URL is invalid.");
        }
    }
    
    private void checkRequestTarget(AiResourceImportSource source, URI uri)
        throws NacosException {
        String scheme =
            uri.getScheme() == null ? null : uri.getScheme().toLowerCase(Locale.ENGLISH);
        if (!HTTPS_SCHEME.equals(scheme) && !HTTP_SCHEME.equals(scheme)) {
            throw invalid("AI resource import request URL must use http or https.");
        }
        if (HTTP_SCHEME.equals(scheme) && !isSourcePropertyEnabled(source, PROPERTY_ALLOW_HTTP,
            PROPERTY_ALLOW_HTTP_CAMEL)) {
            throw invalid(
                "AI resource import request URL must use https unless allow-http is enabled.");
        }
        if (StringUtils.isBlank(uri.getHost())) {
            throw invalid("AI resource import request URL host must not be empty.");
        }
        if (isUnsafeHost(uri.getHost()) && !isSourcePropertyEnabled(source,
            PROPERTY_ALLOW_PRIVATE_NETWORK, PROPERTY_ALLOW_PRIVATE_NETWORK_CAMEL)) {
            throw invalid(
                "AI resource import request URL resolves to a private or local target.");
        }
    }
    
    private boolean isUnsafeHost(String host) throws NacosException {
        String normalized = InternetAddressUtil.removeBrackets(host).toLowerCase(Locale.ENGLISH);
        if (LOCALHOST.equals(normalized) || normalized.endsWith(LOCALHOST_SUFFIX)) {
            return true;
        }
        InetAddress[] addresses = resolveHost(normalized);
        for (InetAddress each : addresses) {
            if (isUnsafeAddress(each)) {
                return true;
            }
        }
        return false;
    }
    
    private InetAddress[] resolveHost(String host) throws NacosException {
        try {
            InetAddress[] result = dnsResolver.resolve(host);
            if (result == null || result.length == 0) {
                throw invalid("AI resource import request URL host cannot be resolved.");
            }
            return result;
        } catch (UnknownHostException e) {
            throw invalid("AI resource import request URL host cannot be resolved.");
        }
    }
    
    private boolean isUnsafeAddress(InetAddress address) {
        return address.isAnyLocalAddress() || address.isLoopbackAddress()
            || address.isLinkLocalAddress() || address.isSiteLocalAddress()
            || address.isMulticastAddress() || isUniqueLocalIpv6Address(address);
    }
    
    private boolean isUniqueLocalIpv6Address(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
    }
    
    private boolean isSourcePropertyEnabled(AiResourceImportSource source, String kebabKey,
        String camelKey) {
        if (source == null) {
            return false;
        }
        Map<String, String> properties = source.getProperties();
        if (properties == null || properties.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(properties.get(kebabKey))
            || Boolean.parseBoolean(properties.get(camelKey));
    }
    
    private long resolveMaxResponseBytes(AiResourceImportSource source) {
        if (source != null && source.getMaxArtifactSize() > 0) {
            return source.getMaxArtifactSize();
        }
        return DEFAULT_MAX_RESPONSE_BYTES;
    }
    
    private void checkResponseSize(byte[] body, long maxResponseBytes) throws NacosException {
        if (body != null && body.length > maxResponseBytes) {
            throw invalid("AI resource import response size exceeds source limit.");
        }
    }
    
    private NacosException invalid(String message) {
        return new NacosApiException(NacosException.INVALID_PARAM,
            ErrorCode.PARAMETER_VALIDATE_ERROR, message);
    }
    
    /** 可插拔 DNS 解析器，用于 SSRF 防护中的地址判定。 */
    public interface DnsResolver {
        
        /**
         * Resolve host to network addresses.
         *
         * @param host request host
         * @return resolved addresses
         * @throws UnknownHostException if the host cannot be resolved
          * <p>Nacos 3.2.3：Logback 1.2 日志适配与 AI 资源导入（MCP Registry、Skill well-known、skills.sh）及 skill-scanner Markdown 解析；详见上方类说明。</p>
         */
        InetAddress[] resolve(String host) throws UnknownHostException;
    }
    
    /** 限制响应体最大字节数的 BodyHandler 工厂。 */
    private static class LimitedByteArrayBodyHandler implements HttpResponse.BodyHandler<byte[]> {
        
        private final long maxBytes;
        
        LimitedByteArrayBodyHandler(long maxBytes) {
            this.maxBytes = maxBytes;
        }
        
        @Override
        public HttpResponse.BodySubscriber<byte[]> apply(HttpResponse.ResponseInfo responseInfo) {
            return new LimitedByteArrayBodySubscriber(maxBytes);
        }
    }
    
    private static class LimitedByteArrayBodySubscriber
        implements HttpResponse.BodySubscriber<byte[]> {
        
        private final long maxBytes;
        
        private final CompletableFuture<byte[]> body = new CompletableFuture<>();
        
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        private Flow.Subscription subscription;
        
        private long totalBytes;
        
        LimitedByteArrayBodySubscriber(long maxBytes) {
            this.maxBytes = maxBytes;
        }
        
        @Override
        public CompletionStage<byte[]> getBody() {
            return body;
        }
        
        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(Long.MAX_VALUE);
        }
        
        @Override
        public void onNext(List<ByteBuffer> items) {
            if (body.isDone()) {
                return;
            }
            for (ByteBuffer each : items) {
                int remaining = each.remaining();
                if (totalBytes + remaining > maxBytes) {
                    fail(new IOException("AI resource import response size exceeds source limit."));
                    return;
                }
                byte[] chunk = new byte[remaining];
                each.get(chunk);
                output.write(chunk, 0, chunk.length);
                totalBytes += remaining;
            }
        }
        
        @Override
        public void onError(Throwable throwable) {
            body.completeExceptionally(throwable);
        }
        
        @Override
        public void onComplete() {
            body.complete(output.toByteArray());
        }
        
        private void fail(Throwable throwable) {
            if (subscription != null) {
                subscription.cancel();
            }
            body.completeExceptionally(throwable);
        }
    }
}
