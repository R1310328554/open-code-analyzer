/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.http.client;

import com.alibaba.nacos.common.http.HttpClientConfig;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.HttpUtils;
import com.alibaba.nacos.common.http.client.handler.ResponseHandler;
import com.alibaba.nacos.common.http.client.request.DefaultHttpClientRequest;
import com.alibaba.nacos.common.http.client.request.HttpClientRequest;
import com.alibaba.nacos.common.http.client.request.JdkHttpClientRequest;
import com.alibaba.nacos.common.http.client.response.HttpClientResponse;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.MediaType;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.model.RequestHttpEntity;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.HttpMethod;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Nacos rest template Interface specifying a basic set of RESTful operations.
 * <p>Nacos 同步 REST 客户端模板：提供 GET/POST/PUT/DELETE 及 Json/Form/File 等便捷方法，支持 {@link HttpClientConfig}  per-request 配置与 {@link HttpClientRequestInterceptor} 拦截链，返回带响应头的 {@link HttpRestResult}。</p>
 *
 * @author mai.jh
 * @see HttpClientRequest
 * @see HttpClientResponse
 */
public class NacosRestTemplate extends AbstractNacosRestTemplate {
    
    /** 底层同步 HTTP 请求客户端 */
    private final HttpClientRequest requestClient;
    
    /** 请求拦截器列表，非空时通过 {@link InterceptingHttpClientRequest} 包装执行 */
    private final List<HttpClientRequestInterceptor> interceptors = new ArrayList<>();
    
    public NacosRestTemplate(Logger logger, HttpClientRequest requestClient) {
        super(logger);
        this.requestClient = requestClient;
    }
    
    /**
     * http get URL request params are expanded using the given query {@link Query}.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param query        http query param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> get(String url, Header header, Query query, Type responseType)
        throws Exception {
        return execute(url, HttpMethod.GET, new RequestHttpEntity(header, query), responseType);
    }
    
    /**
     * http get URL request params are expanded using the given query {@link Query}.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * <p>{@code config} Specify the request config via {@link HttpClientConfig}
     *
     * @param url          url
     * @param config       http config
     * @param header       headers
     * @param query        http query param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> get(String url, HttpClientConfig config, Header header,
        Query query, Type responseType)
        throws Exception {
        RequestHttpEntity requestHttpEntity = new RequestHttpEntity(config, header, query);
        return execute(url, HttpMethod.GET, requestHttpEntity, responseType);
    }
    
    /**
     * get request, may be pulling a lot of data URL request params are expanded using the given query {@link Query},
     * More request parameters can be set via body.
     *
     * <p>This method can only be used when HttpClientRequest is implemented by {@link DefaultHttpClientRequest}, note:
     * {@link JdkHttpClientRequest} Implementation does not support this method.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param query        http query param
     * @param body         get with body
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> getLarge(String url, Header header, Query query, Object body,
        Type responseType)
        throws Exception {
        return execute(url, HttpMethod.GET_LARGE, new RequestHttpEntity(header, query, body),
            responseType);
    }
    
    /**
     * http delete URL request params are expanded using the given query {@link Query}.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param query        http query param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> delete(String url, Header header, Query query, Type responseType)
        throws Exception {
        return execute(url, HttpMethod.DELETE, new RequestHttpEntity(header, query), responseType);
    }
    
    /**
     * http delete URL request params are expanded using the given query {@link Query}.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * <p>{@code config} Specify the request config via {@link HttpClientConfig}
     *
     * @param url          url
     * @param config       http config
     * @param header       http header param
     * @param query        http query param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> delete(String url, HttpClientConfig config, Header header,
        Query query,
        Type responseType) throws Exception {
        return execute(url, HttpMethod.DELETE, new RequestHttpEntity(config, header, query),
            responseType);
    }
    
    /**
     * http put Create a new resource by PUTting the given body to http request.
     *
     * <p>URL request params are expanded using the given query {@link Query}.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param query        http query param
     * @param body         http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> put(String url, Header header, Query query, Object body,
        Type responseType)
        throws Exception {
        return execute(url, HttpMethod.PUT, new RequestHttpEntity(header, query, body),
            responseType);
    }
    
    /**
     * http put json Create a new resource by PUTting the given body to http request, http header contentType default
     * 'application/json;charset=UTF-8'.
     *
     * <p>URL request params are expanded using the given query {@link Query}.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param query        http query param
     * @param body         http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> putJson(String url, Header header, Query query, String body,
        Type responseType)
        throws Exception {
        RequestHttpEntity requestHttpEntity =
            new RequestHttpEntity(header.setContentType(MediaType.APPLICATION_JSON),
                query, body);
        return execute(url, HttpMethod.PUT, requestHttpEntity, responseType);
    }
    
    /**
     * http put json Create a new resource by PUTting the given body to http request, http header contentType default
     * 'application/json;charset=UTF-8'.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param body         http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> putJson(String url, Header header, String body, Type responseType)
        throws Exception {
        RequestHttpEntity requestHttpEntity =
            new RequestHttpEntity(header.setContentType(MediaType.APPLICATION_JSON),
                body);
        return execute(url, HttpMethod.PUT, requestHttpEntity, responseType);
    }
    
    /**
     * http put from Create a new resource by PUTting the given map {@code bodyValues} to http request, http header
     * contentType default 'application/x-www-form-urlencoded;charset=utf-8'.
     *
     * <p>URL request params are expanded using the given query {@code Query}.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param query        http query param
     * @param bodyValues   http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> putForm(String url, Header header, Query query,
        Map<String, String> bodyValues,
        Type responseType) throws Exception {
        RequestHttpEntity requestHttpEntity = new RequestHttpEntity(
            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED), query, bodyValues);
        return execute(url, HttpMethod.PUT, requestHttpEntity, responseType);
    }
    
    /**
     * http put from Create a new resource by PUTting the given map {@code bodyValues} to http request, http header
     * contentType default 'application/x-www-form-urlencoded;charset=utf-8'.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param bodyValues   http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> putForm(String url, Header header, Map<String, String> bodyValues,
        Type responseType)
        throws Exception {
        RequestHttpEntity requestHttpEntity = new RequestHttpEntity(
            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED), bodyValues);
        return execute(url, HttpMethod.PUT, requestHttpEntity, responseType);
    }
    
    /**
     * http put from Create a new resource by PUTting the given map {@code bodyValues} to http request, http header
     * contentType default 'application/x-www-form-urlencoded;charset=utf-8'.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * <p>{@code config} Specify the request config via {@link HttpClientConfig}
     *
     * @param url          url
     * @param config       http config
     * @param header       http header param
     * @param bodyValues   http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> putForm(String url, HttpClientConfig config, Header header,
        Map<String, String> bodyValues, Type responseType) throws Exception {
        RequestHttpEntity requestHttpEntity = new RequestHttpEntity(config,
            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED), bodyValues);
        return execute(url, HttpMethod.PUT, requestHttpEntity, responseType);
    }
    
    /**
     * http post Create a new resource by POSTing the given object to the http request.
     *
     * <p>URL request params are expanded using the given query {@link Query}.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param query        http query param
     * @param body         http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> post(String url, Header header, Query query, Object body,
        Type responseType)
        throws Exception {
        return execute(url, HttpMethod.POST, new RequestHttpEntity(header, query, body),
            responseType);
    }
    
    /**
     * http post json Create a new resource by POSTing the given object to the http request, http header contentType
     * default 'application/json;charset=UTF-8'.
     *
     * <p>URL request params are expanded using the given query {@link Query}.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param query        http query param
     * @param body         http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> postJson(String url, Header header, Query query, String body,
        Type responseType)
        throws Exception {
        RequestHttpEntity requestHttpEntity =
            new RequestHttpEntity(header.setContentType(MediaType.APPLICATION_JSON),
                query, body);
        return execute(url, HttpMethod.POST, requestHttpEntity, responseType);
    }
    
    /**
     * http post json Create a new resource by POSTing the given object to the http request, http header contentType
     * default 'application/json;charset=UTF-8'.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param body         http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> postJson(String url, Header header, String body, Type responseType)
        throws Exception {
        RequestHttpEntity requestHttpEntity =
            new RequestHttpEntity(header.setContentType(MediaType.APPLICATION_JSON),
                body);
        return execute(url, HttpMethod.POST, requestHttpEntity, responseType);
    }
    
    /**
     * http post from Create a new resource by PUTting the given map {@code bodyValues} to http request, http header
     * contentType default 'application/x-www-form-urlencoded;charset=utf-8'.
     *
     * <p>URL request params are expanded using the given query {@link Query}.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param query        http query param
     * @param bodyValues   http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> postForm(String url, Header header, Query query,
        Map<String, String> bodyValues,
        Type responseType) throws Exception {
        RequestHttpEntity requestHttpEntity = new RequestHttpEntity(
            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED), query, bodyValues);
        return execute(url, HttpMethod.POST, requestHttpEntity, responseType);
    }
    
    /**
     * http post from Create a new resource by PUTting the given map {@code bodyValues} to http request, http header
     * contentType default 'application/x-www-form-urlencoded;charset=utf-8'.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * @param url          url
     * @param header       http header param
     * @param bodyValues   http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> postForm(String url, Header header, Map<String, String> bodyValues,
        Type responseType)
        throws Exception {
        RequestHttpEntity requestHttpEntity = new RequestHttpEntity(
            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED), bodyValues);
        return execute(url, HttpMethod.POST, requestHttpEntity, responseType);
    }
    
    /**
     * http post from Create a new resource by PUTting the given map {@code bodyValues} to http request, http header
     * contentType default 'application/x-www-form-urlencoded;charset=utf-8'.
     *
     * <p>{@code responseType} can be an HttpRestResult or HttpRestResult data {@code T} type.
     *
     * <p>{@code config} Specify the request config via {@link HttpClientConfig}
     *
     * @param url          url
     * @param config       http config
     * @param header       http header param
     * @param bodyValues   http body param
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> postForm(String url, HttpClientConfig config, Header header,
        Map<String, String> bodyValues, Type responseType) throws Exception {
        RequestHttpEntity requestHttpEntity = new RequestHttpEntity(config,
            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED), bodyValues);
        return execute(url, HttpMethod.POST, requestHttpEntity, responseType);
    }
    
    /**
     * 上传文件 POST 请求。
     * <p>使用 {@link HttpClientConfig} 指定超时等参数，请求体为 {@link File}。</p>
     *
     * @param url          目标 URL
     * @param config       HTTP 客户端配置
     * @param header       请求头
     * @param file         待上传文件
     * @param responseType 响应类型
     * @return {@link HttpRestResult}
     * @throws Exception 请求或 IO 异常
     */
    public <T> HttpRestResult<T> postFile(String url, HttpClientConfig config, Header header,
        File file, Type responseType) throws Exception {
        RequestHttpEntity requestHttpEntity = new RequestHttpEntity(config, header, file);
        return execute(url, HttpMethod.POST, requestHttpEntity, responseType);
    }
    
    /**
     * Execute the HTTP method to the given URI template, writing the given request entity to the request, and returns
     * the response as {@link HttpRestResult}.
     *
     * @param url          url
     * @param header       http header param
     * @param query        http query param
     * @param bodyValues   http body param
     * @param httpMethod   http method
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> exchangeForm(String url, Header header, Query query,
        Map<String, String> bodyValues,
        String httpMethod, Type responseType) throws Exception {
        RequestHttpEntity requestHttpEntity = new RequestHttpEntity(
            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED), query, bodyValues);
        return execute(url, httpMethod, requestHttpEntity, responseType);
    }
    
    /**
     * Execute the HTTP method to the given URI template, writing the given request entity to the request, and returns
     * the response as {@link HttpRestResult}.
     *
     * @param url          url
     * @param config       HttpClientConfig
     * @param header       http header param
     * @param query        http query param
     * @param body         http body param
     * @param httpMethod   http method
     * @param responseType return type
     * @return {@link HttpRestResult}
     * @throws Exception ex
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public <T> HttpRestResult<T> exchange(String url, HttpClientConfig config, Header header,
        Query query,
        Object body, String httpMethod, Type responseType) throws Exception {
        RequestHttpEntity requestHttpEntity = new RequestHttpEntity(config, header, query, body);
        return execute(url, httpMethod, requestHttpEntity, responseType);
    }
    
    /**
     * Set the request interceptors that this accessor should use.
     *
     * @param interceptors {@link HttpClientRequestInterceptor}
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public void setInterceptors(List<HttpClientRequestInterceptor> interceptors) {
        if (this.interceptors != interceptors) {
            this.interceptors.clear();
            this.interceptors.addAll(interceptors);
        }
    }
    
    /**
     * Return the request interceptors that this accessor uses.
     *
     * <p>The returned {@link List} is active and may get appended to.
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public List<HttpClientRequestInterceptor> getInterceptors() {
        return interceptors;
    }
    
    @SuppressWarnings("unchecked")
    private <T> HttpRestResult<T> execute(String url, String httpMethod,
        RequestHttpEntity requestEntity,
        Type responseType) throws Exception {
        URI uri = HttpUtils.buildUri(url, requestEntity.getQuery());
        if (logger.isDebugEnabled()) {
            logger.debug("HTTP method: {}, url: {}, body: {}", httpMethod, uri,
                requestEntity.getBody());
        }
        
        ResponseHandler<T> responseHandler = super.selectResponseHandler(responseType);
        HttpClientResponse response = null;
        try {
            response = this.requestClient().execute(uri, httpMethod, requestEntity);
            return responseHandler.handle(response);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
    private HttpClientRequest requestClient() {
        if (CollectionUtils.isNotEmpty(interceptors)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Execute via interceptors :{}", interceptors);
            }
            return new InterceptingHttpClientRequest(requestClient, interceptors.iterator());
        }
        return requestClient;
    }
    
    /**
     * close request client.
      * <p>同步 REST 模板；详见类级说明。</p>
     */
    public void close() throws Exception {
        requestClient.close();
    }
    
}
