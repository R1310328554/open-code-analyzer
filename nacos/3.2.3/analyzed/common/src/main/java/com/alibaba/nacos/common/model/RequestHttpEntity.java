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

package com.alibaba.nacos.common.model;

import com.alibaba.nacos.common.http.HttpClientConfig;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;

import java.util.Map;

/**
 * Represents an HTTP request , consisting of headers and body.
 * <p>HTTP 请求实体：聚合请求头、查询参数、请求体与可选的 {@link HttpClientConfig}，供 {@link com.alibaba.nacos.common.http.client.NacosRestTemplate} 发起调用。</p>
 *
 * @author mai.jh
 */
public class RequestHttpEntity {
    
    /** 可变请求头容器，构造时从入参 Header 拷贝 */
    private final Header headers = Header.newInstance();
    
    /** 可选的 per-request HTTP 客户端配置，null 表示使用模板默认配置 */
    private final HttpClientConfig httpClientConfig;
    
    /** URL 查询参数封装，可为 null */
    private final Query query;
    
    /** 请求体对象（JSON/表单等），可为 null */
    private final Object body;
    
    public RequestHttpEntity(Header header, Query query) {
        this(null, header, query);
    }
    
    public RequestHttpEntity(Header header, Object body) {
        this(null, header, body);
    }
    
    public RequestHttpEntity(Header header, Query query, Object body) {
        this(null, header, query, body);
    }
    
    public RequestHttpEntity(HttpClientConfig httpClientConfig, Header header, Query query) {
        this(httpClientConfig, header, query, null);
    }
    
    public RequestHttpEntity(HttpClientConfig httpClientConfig, Header header, Object body) {
        this(httpClientConfig, header, null, body);
    }
    
    public RequestHttpEntity(HttpClientConfig httpClientConfig, Header header, Query query,
        Object body) {
        handleHeader(header);
        this.httpClientConfig = httpClientConfig;
        this.query = query;
        this.body = body;
    }
    
    /** 将外部 Header 合并到内部 headers 实例 */
    private void handleHeader(Header header) {
        if (header != null && !header.getHeader().isEmpty()) {
            Map<String, String> headerMap = header.getHeader();
            headers.addAll(headerMap);
        }
    }
    
    public Header getHeaders() {
        return headers;
    }
    
    public Query getQuery() {
        return query;
    }
    
    public Object getBody() {
        return body;
    }
    
    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }
    
    /** 判断请求是否无 body（GET 等场景常用） */
    public boolean isEmptyBody() {
        return body == null;
    }
    
}
