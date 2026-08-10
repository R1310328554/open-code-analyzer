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

import com.alibaba.nacos.common.http.client.request.HttpClientRequest;
import com.alibaba.nacos.common.http.client.response.HttpClientResponse;
import com.alibaba.nacos.common.model.RequestHttpEntity;

import java.net.URI;

/**
 * Intercepts client-side HTTP requests. Implementations of this interface can be.
 * <p>HTTP 客户端请求拦截器：在真正发起请求前判断是否拦截，若拦截则直接返回 {@link HttpClientResponse}，否则交由底层 {@link HttpClientRequest} 执行。</p>
 *
 * @author mai.jh
 */
public interface HttpClientRequestInterceptor {
    
    /**
     * is intercept.
     * <p>根据 URI、方法与请求实体判断当前拦截器是否接管本次调用。</p>
     *
     * @param uri uri
     * @param httpMethod http method
     * @param requestHttpEntity request entity
     * @return boolean
     */
    boolean isIntercept(URI uri, String httpMethod, RequestHttpEntity requestHttpEntity);
    
    /**
     * if isIntercept method is true Intercept the given request, and return a response Otherwise,
     * the {@link HttpClientRequest} will be used for execution.
     *
     * @return HttpClientResponse
      * <p>HTTP 请求拦截器接口；详见类级说明。</p>
     */
    HttpClientResponse intercept();
}
