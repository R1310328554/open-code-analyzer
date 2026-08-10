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

package org.keycloak.http.simple;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

/**
 * 可链式配置的 HTTP 请求构建器：设置头、查询参数、JSON/表单实体并执行。
 * <p>由 {@link SimpleHttp} 创建；响应封装为 {@link SimpleHttpResponse}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author Vlastimil Elias (velias at redhat dot com)
 * @author David Klassen (daviddd.kl@gmail.com)
 */
public class SimpleHttpRequest {

    private final HttpClient client;
    private final RequestConfig requestConfig;

    private final ObjectMapper objectMapper;

    private final String url;
    private final SimpleHttpMethod method;
    private Map<String, String> headers;
    private Map<String, String> params;
    private Object entity;

    private final long maxConsumedResponseSize;

    SimpleHttpRequest(String url, SimpleHttpMethod method, HttpClient client, RequestConfig requestConfig, long maxConsumedResponseSize, ObjectMapper objectMapper) {
        this.client = client;
        this.requestConfig = requestConfig;
        this.url = url;
        this.method = method;
        this.maxConsumedResponseSize = maxConsumedResponseSize;
        this.objectMapper = objectMapper;
    }

    /** 添加或覆盖请求头。 */
    public SimpleHttpRequest header(String name, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
        return this;
    }

    /** @return 指定名称的请求头值，未设置时为 {@code null} */
    public String getHeader(String name) {
        if (headers != null) {
            return headers.get(name);
        }
        return null;
    }

    /** @return 不可修改的请求头映射，未设置时为 {@code null} */
    public Map<String, String> getHeaders() {
        if (headers == null) {
            return null;
        }
        return Collections.unmodifiableMap(headers);
    }

    public String getParam(String name) {
        if (params == null) {
            return null;
        }
        return params.get(name);
    }

    public Map<String, String> getParams() {
        if (params == null) {
            return null;
        }
        return Collections.unmodifiableMap(params);
    }

    public Object getEntity() {
        return entity;
    }

    public SimpleHttpMethod getMethod() {
        return method;
    }

    /** 将请求体设为 JSON 序列化对象（执行时默认 {@code Content-Type: application/json}）。 */
    public SimpleHttpRequest json(Object entity) {
        this.entity = entity;
        return this;
    }

    /** 设置原始 {@link org.apache.http.HttpEntity} 请求体。 */
    public SimpleHttpRequest entity(HttpEntity entity) {
        this.entity = entity;
        return this;
    }

    public SimpleHttpRequest params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    /** 添加查询或表单参数（GET 等附加到 URL，POST 等作为表单实体）。 */
    public SimpleHttpRequest param(String name, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(name, value);
        return this;
    }

    /** 设置 {@code Authorization: Bearer <token>} 头。 */
    public SimpleHttpRequest auth(String token) {
        header("Authorization", "Bearer " + token);
        return this;
    }

    /** 设置 HTTP Basic 认证头。 */
    public SimpleHttpRequest authBasic(final String username, final String password) {
        final String basicCredentials = String.format("%s:%s", username, password);
        header("Authorization", "Basic " + Base64.getEncoder().encodeToString(basicCredentials.getBytes()));
        return this;
    }

    /** 若未设置 Accept，则添加 {@code application/json}。 */
    public SimpleHttpRequest acceptJson() {
        if (headers == null || !headers.containsKey("Accept")) {
            header("Accept", "application/json");
        }
        return this;
    }

    /** 执行请求并将响应体解析为 {@link com.fasterxml.jackson.databind.JsonNode}。 */
    public JsonNode asJson() throws IOException {
        if (headers == null || !headers.containsKey("Accept")) {
            header("Accept", "application/json");
        }
        return objectMapper.readTree(asString());
    }

    public <T> T asJson(Class<T> type) throws IOException {
        if (headers == null || !headers.containsKey("Accept")) {
            header("Accept", "application/json");
        }
        return objectMapper.readValue(asString(), type);
    }

    public <T> T asJson(TypeReference<T> type) throws IOException {
        if (headers == null || !headers.containsKey("Accept")) {
            header("Accept", "application/json");
        }
        return objectMapper.readValue(asString(), type);
    }

    /** 执行请求并返回响应体字符串（自动关闭响应）。 */
    public String asString() throws IOException {
        try (SimpleHttpResponse response = makeRequest()) {
            return response.asString();
        }
    }

    /** 执行请求并返回 HTTP 状态码。 */
    public int asStatus() throws IOException {
        try (SimpleHttpResponse response = asResponse()) {
            return response.getStatus();
        }
    }

    /** 执行请求并返回 {@link SimpleHttpResponse}（调用方负责关闭）。 */
    public SimpleHttpResponse asResponse() throws IOException {
        return makeRequest();
    }

    private HttpRequestBase createHttpRequest() {
        return switch (method) {
            case GET -> new HttpGet(appendParameterToUrl(url));
            case DELETE -> new HttpDelete(appendParameterToUrl(url));
            case HEAD -> new HttpHead(appendParameterToUrl(url));
            case PUT -> new HttpPut(appendParameterToUrl(url));
            case PATCH -> new HttpPatch(appendParameterToUrl(url));
            case POST -> new HttpPost(url);
            case OPTIONS -> new HttpOptions(url);
        };
    }

    /** @return 不含查询参数的原始 URL */
    
    public String getUrl() {
        return url;
    }

    private SimpleHttpResponse makeRequest() throws IOException {
        HttpRequestBase httpRequest = createHttpRequest();

        if (httpRequest instanceof HttpPost || httpRequest instanceof  HttpPut || httpRequest instanceof HttpPatch) {
            if (params != null) {
                ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(getFormEntityFromParameter());
            } else if (entity instanceof HttpEntity) {
                ((HttpEntityEnclosingRequestBase) httpRequest).setEntity((HttpEntity) entity);
            } else if (entity != null) {
                if (headers == null || !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                    header(HttpHeaders.CONTENT_TYPE, "application/json");
                }
                ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(getJsonEntity());
            } else {
                throw new IllegalStateException("No content set");
            }
        }

        if (headers != null) {
            for (Map.Entry<String, String> h : headers.entrySet()) {
                httpRequest.setHeader(h.getKey(), h.getValue());
            }
        }

        if (requestConfig != null) {
            httpRequest.setConfig(requestConfig);
        }

        return new SimpleHttpResponse(client.execute(httpRequest), maxConsumedResponseSize, objectMapper);
    }

    private URI appendParameterToUrl(String url) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);

            if (params != null) {
                for (Map.Entry<String, String> p : params.entrySet()) {
                    uriBuilder.setParameter(p.getKey(), p.getValue());
                }
            }

            return uriBuilder.build();
        } catch (URISyntaxException ignored) {
            return null;
        }
    }

    private StringEntity getJsonEntity() throws IOException {
        return new StringEntity(objectMapper.writeValueAsString(entity), ContentType.getByMimeType(headers.get(HttpHeaders.CONTENT_TYPE)));
    }

    private UrlEncodedFormEntity getFormEntityFromParameter() {
        List<NameValuePair> urlParameters = new ArrayList<>();

        if (params != null) {
            for (Map.Entry<String, String> p : params.entrySet()) {
                urlParameters. add(new BasicNameValuePair(p.getKey(), p.getValue()));
            }
        }

        return new UrlEncodedFormEntity(urlParameters, StandardCharsets.UTF_8);
    }

}
