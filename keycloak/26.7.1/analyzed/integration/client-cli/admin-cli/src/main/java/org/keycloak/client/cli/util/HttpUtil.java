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
package org.keycloak.client.cli.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLContext;

import org.keycloak.util.JsonSerialization;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

/**
 * Keycloak Admin CLI 的 HTTP 客户端工具集。
 * <p>
 * 封装 Apache HttpClient 单例、TLS 信任库配置、OAuth 令牌请求、通用 REST 调用
 * 及 Admin API URL 拼装；提供简化版（返回 {@link InputStream}）与完整版
 * （返回 {@link HeadersBodyStatus}）两套 API。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class HttpUtil {

    /** {@code Content-Type}/{@code Accept}：XML。 */
    public static final String APPLICATION_XML = "application/xml";
    /** {@code Content-Type}/{@code Accept}：JSON。 */
    public static final String APPLICATION_JSON = "application/json";
    /** OAuth 令牌端点表单编码类型。 */
    public static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    /** URL 编码与查询参数默认字符集。 */
    public static final String UTF_8 = "utf-8";

    /** 懒加载共享 HttpClient 实例。 */
    private static HttpClient httpClient;
    /** 可选自定义 SSL 套接字工厂（信任库或跳过校验）。 */
    private static SSLConnectionSocketFactory sslsf;
    /** 保证 TLS 跳过校验警告仅输出一次。 */
    private static final AtomicBoolean tlsWarningEmitted = new AtomicBoolean();

    /**
     * 发送 GET 请求，成功时返回响应正文流。
     *
     * @param url 目标 URL
     * @param acceptType Accept 头值
     * @param authorization Authorization 头，可为 {@code null}
     * @return 2xx 响应正文流
     */
    public static InputStream doGet(String url, String acceptType, String authorization) {
        try {
            HttpGet request = new HttpGet(url);
            request.setHeader(HttpHeaders.ACCEPT, acceptType);
            return doRequest(authorization, request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
        }
    }

    /** 发送 POST 请求（字符串正文），成功时返回响应正文流。 */
    public static InputStream doPost(String url, String contentType, String acceptType, String content, String authorization) {
        try {
            return doPostOrPut(contentType, acceptType, content, authorization, new HttpPost(url));
        } catch (IOException e) {
            throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
        }
    }

    /** 发送 PUT 请求（字符串正文），成功时返回响应正文流。 */
    public static InputStream doPut(String url, String contentType, String acceptType, String content, String authorization) {
        try {
            return doPostOrPut(contentType, acceptType, content, authorization, new HttpPut(url));
        } catch (IOException e) {
            throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
        }
    }

    /** 发送无正文的 DELETE 请求。 */
    public static void doDelete(String url, String authorization) {
        try {
            HttpDelete request = new HttpDelete(url);
            doRequest(authorization, request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
        }
    }


    /** 完整 GET：返回头、正文与状态行。 */
    public static HeadersBodyStatus doGet(String url, HeadersBody request) throws IOException {
        return doRequest("get", url, request);
    }

    /** 完整 POST：返回头、正文与状态行。 */
    public static HeadersBodyStatus doPost(String url, HeadersBody request) throws IOException {
        return doRequest("post", url, request);
    }

    /** 完整 PUT：返回头、正文与状态行。 */
    public static HeadersBodyStatus doPut(String url, HeadersBody request) throws IOException {
        return doRequest("put", url, request);
    }

    /** 完整 DELETE（可带正文）：返回头、正文与状态行。 */
    public static HeadersBodyStatus doDelete(String url, HeadersBody request) throws IOException {
        return doRequest("delete", url, request);
    }

    /** 通用 HTTP 请求（默认不解析 Jakarta validation violations）。 */
    public static HeadersBodyStatus doRequest(String type, String url, HeadersBody request) throws IOException {
        return doRequest(type, url, request, false);
    }

    /**
     * 按方法名发送 HTTP 请求并返回完整响应封装。
     *
     * @param type 方法名（get/post/put/delete/patch/options/head）
     * @param url 目标 URL
     * @param request 请求头与可选正文
     * @param allowJakartaValidation 是否在错误响应中格式化 violations
     * @return 含状态行、响应头与正文的 {@link HeadersBodyStatus}
     */
    public static HeadersBodyStatus doRequest(String type, String url, HeadersBody request, boolean allowJakartaValidation) throws IOException {
        HttpRequestBase req;
        switch (type) {
            case "get":
                req = new HttpGet(url);
                break;
            case "post":
                req = new HttpPost(url);
                break;
            case "put":
                req = new HttpPut(url);
                break;
            case "delete":
                req = new HttpDelete(url);
                break;
            case "patch":
                req = new HttpPatch(url);
                break;
            case "options":
                req = new HttpOptions(url);
                break;
            case "head":
                req = new HttpHead(url);
                break;
            default:
                throw new RuntimeException("Method not supported: " + type);
        }
        addHeaders(req, request.getHeaders());

        if (request.getBody() != null) {
            if (req instanceof HttpEntityEnclosingRequestBase == false) {
                throw new RuntimeException("Request type does not support body: " + type);
            }
            ((HttpEntityEnclosingRequestBase) req).setEntity(new InputStreamEntity(request.getBody()));
        }

        HttpResponse res = getHttpClient().execute(req);
        InputStream responseStream = null;
        if (res.getEntity() != null) {
            responseStream = res.getEntity().getContent();
        } else {
            responseStream = new InputStream() {
                @Override
                public int read () throws IOException {
                    return -1;
                }
            };
        }

        Headers headers = new Headers();
        HeaderIterator it = res.headerIterator();
        while (it.hasNext()) {
            org.apache.http.Header header = it.nextHeader();
            headers.add(header.getName(), header.getValue());
        }

        return new HeadersBodyStatus(res.getStatusLine().toString(), headers, responseStream, allowJakartaValidation);
    }

    private static void addHeaders(HttpRequestBase request, Headers headers) {
        for (Header header: headers) {
            request.setHeader(header.getName(), header.getValue());
        }
    }

    private static InputStream doPostOrPut(String contentType, String acceptType, String content, String authorization, HttpEntityEnclosingRequestBase request) throws IOException {
        request.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        request.setHeader(HttpHeaders.ACCEPT, acceptType);
        if (content != null) {
            request.setEntity(new StringEntity(content));
        }

        return doRequest(authorization, request);
    }

    private static InputStream doRequest(String authorization, HttpRequestBase request) throws IOException {
        addAuth(request, authorization);

        HttpResponse response = getHttpClient().execute(request);
        InputStream responseStream = null;
        if (response.getEntity() != null) {
            responseStream = response.getEntity().getContent();
        }

        int code = response.getStatusLine().getStatusCode();
        if (code >= 200 && code < 300) {
            return responseStream;
        } else if (code >= HttpURLConnection.HTTP_MOVED_PERM && code <= HttpURLConnection.HTTP_SEE_OTHER) {
            throw new RuntimeException(
                    "Request was unsuccessful as the server responded with a redirect to %s - please check that your server URL is correct."
                            .formatted(Optional.ofNullable(response.getFirstHeader("Location")).map(org.apache.http.Header::getValue).orElse("UNKNOWN")));
        } else {
            Map<String, String> error = null;
            try {
                org.apache.http.Header header = response.getEntity().getContentType();
                if (header != null && APPLICATION_JSON.equals(header.getValue())) {
                    error = JsonSerialization.readValue(responseStream, Map.class);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to read error response - " + e.getMessage(), e);
            } finally {
                responseStream.close();
            }

            String message = null;
            if (error != null) {
                message = error.get("error_description") + " [" + error.get("error") + "]";
            }
            throw new RuntimeException(message != null ? message : response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        }
    }

    private static void addAuth(HttpRequestBase request, String authorization) {
        if (authorization != null) {
            request.setHeader(HttpHeaders.AUTHORIZATION, authorization);
        }
    }

    /** 重置 HttpClient 与 SSL 工厂（配置变更后需调用）。 */
    public static void clearHttpClient() {
        httpClient = null;
        sslsf = null;
    }

    /** 返回懒加载共享 {@link HttpClient}（使用系统代理属性）。 */
    public static HttpClient getHttpClient() {
        if (httpClient == null) {
            if (sslsf != null) {
                httpClient = HttpClientBuilder.create().useSystemProperties().setSSLSocketFactory(sslsf).build();
            } else {
                httpClient = HttpClientBuilder.create().useSystemProperties().build();
            }
        }
        return httpClient;
    }

    /** UTF-8 URL 编码工具方法。 */
    public static String urlencode(String value) {
        try {
            return URLEncoder.encode(value, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to urlencode", e);
        }
    }

    /**
     * 加载 JKS 信任库并配置 TLS 客户端。
     *
     * @param file 信任库文件
     * @param password 信任库密码，可为 {@code null}
     */
    public static void setTruststore(File file, String password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        if (!file.isFile()) {
            throw new RuntimeException("Truststore file not found: " + file.getAbsolutePath());
        }
        SSLContext theContext = SSLContexts.custom()
                .useProtocol("TLS")
                .loadTrustMaterial(file, password == null ? null : password.toCharArray(), TrustSelfSignedStrategy.INSTANCE)
                .build();
        sslsf = new SSLConnectionSocketFactory(theContext);
    }

    /**
     * 跳过 TLS 证书校验（仅用于开发/测试，生产环境强烈不推荐）。
     * <p>
     * 警告信息在同一 CLI 进程内仅打印一次。
     */
    public static void setSkipCertificateValidation() {
        if (!tlsWarningEmitted.getAndSet(true)) {
            // 静态工具可能在单次命令中多次配置 TLS（如登录后再次请求），
            // 用布尔标志避免重复输出警告。
            System.err.println("The server is configured to use TLS but there is no truststore specified.");
            System.err.println("The tool will skip certificate validation. This is highly discouraged for production use cases");
        }

        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (Exception e) {
            throw new RuntimeException("Failed setting up TLS", e);
        }
    }

    /** 从 {@code Location} 响应头 URL 提取末段资源 ID。 */
    public static String extractIdFromLocation(String location) {
        int last = location.lastIndexOf("/");
        if (last != -1) {
            return location.substring(last + 1);
        }
        return null;
    }

    /**
     * 向 URI 追加键值对查询参数（变参形式，成对出现）。
     *
     * @param uri 基础 URI
     * @param queryParams 交替的键与值
     * @return 带查询串的 URI
     */
    public static String addQueryParamsToUri(String uri, String ... queryParams) {
        if (queryParams == null) {
            return uri;
        }

        if (queryParams.length % 2 != 0) {
            throw new RuntimeException("Value missing for query parameter: " + queryParams[queryParams.length-1]);
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < queryParams.length; i += 2) {
            params.put(queryParams[i], queryParams[i+1]);
        }
        return addQueryParamsToUri(uri, params);
    }

    /** 向 URI 追加 Map 形式的查询参数（UTF-8 编码）。 */
    public static String addQueryParamsToUri(String uri, Map<String, String> queryParams) {

        if (queryParams.size() == 0) {
            return uri;
        }

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> params: queryParams.entrySet()) {
            try {
                if (query.length() > 0) {
                    query.append("&");
                }
                query.append(params.getKey()).append("=").append(URLEncoder.encode(params.getValue(), "utf-8"));
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to encode query params: " + params.getKey() + "=" + params.getValue());
            }
        }

        return uri + (uri.indexOf("?") == -1 ? "?" : "&") + query;
    }

    /**
     * 将相对 Admin API 路径拼成完整 URL。
     * <p>
     * 绝对 http(s) URL 原样返回；{@code realms}、{@code serverinfo} 等特殊路径
     * 相对 admin 根拼接，其余默认加 {@code realms/{realm}/} 前缀。
     */
    public static String composeResourceUrl(String adminRoot, String realm, String uri) {
        if (!uri.startsWith("http:") && !uri.startsWith("https:")) {
            if ("realms".equals(uri) || uri.startsWith("realms/")) {
                uri = normalize(adminRoot) + uri;
            } else if ("serverinfo".equals(uri)) {
                    uri = normalize(adminRoot) + uri;
            } else {
                uri = normalize(adminRoot) + "realms/" + realm + "/" + uri;
            }
        }
        return uri;
    }

    /** 确保 URL 路径以 {@code /} 结尾。 */
    public static String normalize(String value) {
        return value.endsWith("/") ? value : value + "/";
    }

    /**
     * 校验响应成功；404 时包装为「资源未找到」消息。
     *
     * @param url 请求 URL（用于 404 提示）
     * @param response 待校验响应
     */
    public static void checkSuccess(String url, HeadersBodyStatus response) {
        try {
            response.checkSuccess();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new RuntimeException("Resource not found for url: " + url, e);
            }
            throw e;
        }
    }

    /**
     * GET JSON 并反序列化为指定类型。
     *
     * @param type 目标 Java 类型
     * @param resourceUrl 资源 URL
     * @param auth Authorization 头值，可为 {@code null}
     * @return 反序列化结果
     */
    public static <T> T doGetJSON(Class<T> type, String resourceUrl, String auth) {

        Headers headers = new Headers();
        if (auth != null) {
            headers.add("Authorization", auth);
        }
        headers.add("Accept", "application/json");

        HeadersBodyStatus response;
        try {
            response = HttpUtil.doRequest("get", resourceUrl, new HeadersBody(headers));
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: GET " + resourceUrl, e);
        }

        checkSuccess(resourceUrl, response);

        T result;
        try {
            result = JsonSerialization.readValue(response.getBody(), type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON response", e);
        }

        return result;
    }

    /** POST JSON 正文并校验 2xx 响应。 */
    public static void doPostJSON(String resourceUrl, String auth, Object content) {
        Headers headers = new Headers();
        if (auth != null) {
            headers.add("Authorization", auth);
        }
        headers.add("Content-Type", "application/json");

        HeadersBodyStatus response;

        byte[] body;
        try {
            body = JsonSerialization.writeValueAsBytes(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        try {
            response = HttpUtil.doRequest("post", resourceUrl, new HeadersBody(headers, new ByteArrayInputStream(body)));
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: POST " + resourceUrl + "\n" + new String(body), e);
        }

        checkSuccess(resourceUrl, response);
    }

    /** DELETE 并携带 JSON 正文（如批量删除），校验 2xx 响应。 */
    public static void doDeleteJSON(String resourceUrl, String auth, Object content) {
        Headers headers = new Headers();
        if (auth != null) {
            headers.add("Authorization", auth);
        }
        headers.add("Content-Type", "application/json");

        HeadersBodyStatus response;

        byte[] body;
        try {
            body = JsonSerialization.writeValueAsBytes(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        try {
            response = HttpUtil.doRequest("delete", resourceUrl, new HeadersBody(headers, new ByteArrayInputStream(body)));
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: DELETE " + resourceUrl + "\n" + new String(body), e);
        }

        checkSuccess(resourceUrl, response);
    }

    /** 去掉末尾字符（用于复数资源名转单数，如 {@code users} → {@code user}）。 */
    public static String singularize(String value) {
        return value.substring(0, value.length()-1);
    }
}
