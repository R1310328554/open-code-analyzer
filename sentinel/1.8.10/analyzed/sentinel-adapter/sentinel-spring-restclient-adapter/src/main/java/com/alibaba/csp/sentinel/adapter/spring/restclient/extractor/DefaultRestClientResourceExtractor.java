/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.restclient.extractor;

import java.net.URI;

import org.springframework.http.HttpRequest;

/**
 * RestClient 默认资源名提取器。
 * 
 * <p>资源名格式：{@code METHOD:scheme://host[:port]/path}
 * 
 * <p>示例：
 * <ul>
 *   <li>{@code GET:https://httpbin.org/get}</li>
 *   <li>{@code POST:http://localhost:8080/api/users}</li>
 *   <li>{@code GET:http://localhost:8080/api/users/123}</li>
 * </ul>
 *
 * <p>注意：默认不包含查询参数。如需包含查询参数，请使用自定义提取器。
 *
 * @author QHT, uuuyuqi
 */
public class DefaultRestClientResourceExtractor implements RestClientResourceExtractor {

    @Override
    public String extract(HttpRequest request) {
        URI uri = request.getURI();
        return request.getMethod().toString() + ":" + 
               uri.getScheme() + "://" + 
               uri.getHost() + 
               (uri.getPort() == -1 ? "" : ":" + uri.getPort()) + 
               uri.getPath();
    }
}