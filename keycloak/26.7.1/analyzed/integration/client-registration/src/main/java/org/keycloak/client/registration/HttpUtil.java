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

package org.keycloak.client.registration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.keycloak.common.util.StreamUtil;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * 客户端注册 API 的底层 HTTP 工具类。
 * <p>
 * 封装 GET/POST/PUT/DELETE 请求构建、认证头注入、状态码校验及
 * {@link HttpErrorException} 构造；由 {@link ClientRegistration} 内部使用。
 * </p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
class HttpUtil {

    private HttpClient httpClient;

    /** 客户端注册 API 的 base URI（不含路径段）。 */
    private String baseUri;

    /** 可选的请求级认证策略。 */
    private Auth auth;

    HttpUtil(HttpClient httpClient, String baseUri) {
        this.httpClient = httpClient;
        this.baseUri = baseUri;
    }

    /** 设置后续请求的 {@link Auth} 策略。 */
    void setAuth(Auth auth) {
        this.auth = auth;
    }

    /**
     * 发送 POST 请求创建资源，期望 HTTP 201。
     *
     * @return 响应正文流
     */
    InputStream doPost(String content, String contentType, Charset charset, String acceptType, String... path) throws ClientRegistrationException {
        try {
            HttpPost request = new HttpPost(getUrl(baseUri, path));

            request.setHeader(HttpHeaders.CONTENT_TYPE, contentType(contentType, charset));
            request.setHeader(HttpHeaders.ACCEPT, acceptType);
            request.setEntity(new StringEntity(content, charset));

            addAuth(request);

            HttpResponse response = httpClient.execute(request);
            InputStream responseStream = null;
            if (response.getEntity() != null) {
                responseStream = response.getEntity().getContent();
            }

            if (response.getStatusLine().getStatusCode() == 201) {
                return responseStream;
            } else {
                throw httpErrorException(response, responseStream);
            }
        } catch (IOException e) {
            throw new ClientRegistrationException("Failed to send request", e);
        }
    }
    
    /** 拼接 Content-Type 与 charset 参数。 */
    private String contentType(String contentType, Charset charset) {
    	return contentType + ";charset=" + charset.name();
    }

    /**
     * 发送 GET 请求；200 返回正文流，404 返回 {@code null}，其他状态抛出 {@link HttpErrorException}。
     */
    InputStream doGet(String acceptType, String... path) throws ClientRegistrationException {
        try {
            HttpGet request = new HttpGet(getUrl(baseUri, path));

            request.setHeader(HttpHeaders.ACCEPT, acceptType);

            addAuth(request);

            HttpResponse response = httpClient.execute(request);
            InputStream responseStream = null;
            if (response.getEntity() != null) {
                responseStream = response.getEntity().getContent();
            }

            if (response.getStatusLine().getStatusCode() == 200) {
                return responseStream;
            } else if (response.getStatusLine().getStatusCode() == 404) {
                responseStream.close();
                return null;
            } else {
                throw httpErrorException(response, responseStream);
            }
        } catch (IOException e) {
            throw new ClientRegistrationException("Failed to send request", e);
        }
    }

    /**
     * 发送 PUT 请求更新资源，期望 HTTP 200。
     */
    InputStream doPut(String content, String contentType, Charset charset, String acceptType, String... path) throws ClientRegistrationException {
        try {
            HttpPut request = new HttpPut(getUrl(baseUri, path));

            request.setHeader(HttpHeaders.CONTENT_TYPE, contentType(contentType, charset));
            request.setHeader(HttpHeaders.ACCEPT, acceptType);
            request.setEntity(new StringEntity(content, charset));

            addAuth(request);

            HttpResponse response = httpClient.execute(request);

            InputStream responseStream = null;
            if (response.getEntity() != null) {
                responseStream = response.getEntity().getContent();
            }

            if (response.getStatusLine().getStatusCode() == 200) {
                return responseStream;
            } else {
                throw httpErrorException(response, responseStream);
            }
        } catch (IOException e) {
            throw new ClientRegistrationException("Failed to send request", e);
        }
    }

    /**
     * 发送 DELETE 请求，期望 HTTP 204（无内容）。
     */
    void doDelete(String... path) throws ClientRegistrationException {
        try {
            HttpDelete request = new HttpDelete(getUrl(baseUri, path));

            addAuth(request);

            HttpResponse response = httpClient.execute(request);
            InputStream responseStream = null;
            if (response.getEntity() != null) {
                responseStream = response.getEntity().getContent();
            }

            if (response.getStatusLine().getStatusCode() != 204) {
                throw httpErrorException(response, responseStream);
            }
        } catch (IOException e) {
            throw new ClientRegistrationException("Failed to send request", e);
        }
    }

    /** 若底层客户端为 {@link CloseableHttpClient}，则关闭连接池。 */
    void close() throws ClientRegistrationException {
        if (httpClient instanceof CloseableHttpClient) {
            try {
                ((CloseableHttpClient) httpClient).close();
            } catch (IOException e) {
                throw new ClientRegistrationException("Failed to close http client", e);
            }
        }
    }

    /**
     * 将 base URI 与路径段拼接为完整 URL。
     *
     * @param baseUri 基础 URI
     * @param path  successive 路径段
     */
    static String getUrl(String baseUri, String... path) {
        StringBuilder s = new StringBuilder();
        s.append(baseUri);
        for (String p : path) {
            s.append('/');
            s.append(p);
        }
        return s.toString();
    }

    /** 若已配置 {@link Auth}，向请求注入 Authorization 头。 */
    private void addAuth(HttpRequestBase request) {
        if (auth != null) {
            auth.addAuth(request);
        }
    }

    /** 根据非成功响应构造 {@link HttpErrorException}，并读取错误正文。 */
    private HttpErrorException httpErrorException(HttpResponse response, InputStream responseStream) throws IOException {
        if (responseStream != null) {
            String errorResponse = StreamUtil.readString(responseStream);
            return new HttpErrorException(response.getStatusLine(), errorResponse);
        } else {
            return new HttpErrorException(response.getStatusLine(), null);
        }
    }

}
