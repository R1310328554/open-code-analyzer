package org.keycloak.http.simple;

import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;

/**
 * 基于 Apache HttpClient 的轻量 HTTP 客户端门面，支持链式构建 GET/POST 等请求。
 * <p>通常通过 {@link org.keycloak.models.KeycloakSession} 获取共享 {@link org.apache.http.client.HttpClient}。</p>
 */
public class SimpleHttp {

    /** 默认 JSON 序列化器，与 Keycloak {@link org.keycloak.util.JsonSerialization} 一致。 */
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = JsonSerialization.mapper;

    private final HttpClient client;
    private long maxConsumedResponseSize;
    private RequestConfig requestConfig;
    private ObjectMapper objectMapper;

    private SimpleHttp(HttpClient client, long maxConsumedResponseSize) {
        this.client = client;
        this.maxConsumedResponseSize = maxConsumedResponseSize;
        this.objectMapper = DEFAULT_OBJECT_MAPPER;
    }

    /** 从会话的 {@link org.keycloak.connections.httpclient.HttpClientProvider} 创建实例。 */
    public static SimpleHttp create(KeycloakSession session) {
        HttpClientProvider provider = session.getProvider(HttpClientProvider.class);
        return new SimpleHttp(provider.getHttpClient(), provider.getMaxConsumedResponseSize());
    }

    /** 使用指定 {@link org.apache.http.client.HttpClient} 创建实例（默认响应大小上限）。 */
    public static SimpleHttp create(HttpClient httpClient) {
        return new SimpleHttp(httpClient, HttpClientProvider.DEFAULT_MAX_CONSUMED_RESPONSE_SIZE);
    }

    /** 设置本次请求的 {@link org.apache.http.client.config.RequestConfig}（超时、代理等）。 */
    public SimpleHttp withRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    /** 设置 JSON 序列化/反序列化使用的 {@link com.fasterxml.jackson.databind.ObjectMapper}。 */
    public SimpleHttp withObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    /** 设置允许读取的最大响应体字节数，防止过大响应耗尽内存。 */
    public SimpleHttp withMaxConsumedResponseSize(long maxConsumedResponseSize) {
        this.maxConsumedResponseSize = maxConsumedResponseSize;
        return this;
    }

    private SimpleHttpRequest doRequest(String url, SimpleHttpMethod method) {
        return new SimpleHttpRequest(url, method, client, requestConfig, maxConsumedResponseSize, objectMapper);
    }

    /** 构建 GET 请求。 */
    public SimpleHttpRequest doGet(String url) {
        return doRequest(url, SimpleHttpMethod.GET);
    }

    /** 构建 POST 请求。 */
    public SimpleHttpRequest doPost(String url) {
        return doRequest(url, SimpleHttpMethod.POST);
    }

    /** 构建 PUT 请求。 */
    public SimpleHttpRequest doPut(String url) {
        return doRequest(url, SimpleHttpMethod.PUT);
    }

    /** 构建 DELETE 请求。 */
    public SimpleHttpRequest doDelete(String url) {
        return doRequest(url, SimpleHttpMethod.DELETE);
    }

    /** 构建 HEAD 请求。 */
    public SimpleHttpRequest doHead(String url) {
        return doRequest(url, SimpleHttpMethod.HEAD);
    }

    /** 构建 PATCH 请求。 */
    public SimpleHttpRequest doPatch(String url) {
        return doRequest(url, SimpleHttpMethod.PATCH);
    }

    /** 构建 OPTIONS 请求。 */
    public SimpleHttpRequest doOptions(String url) {
        return doRequest(url, SimpleHttpMethod.OPTIONS);
    }

}
