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

package com.alibaba.nacos.common.http.client.request;

import com.alibaba.nacos.common.http.client.response.HttpClientResponse;
import com.alibaba.nacos.common.model.RequestHttpEntity;

import java.io.Closeable;
import java.net.URI;

/**
 * Represents a client-side HTTP request. Created via an implementation execute.
 * <p>同步 HTTP 客户端请求接口：实现类（Apache/JDK）负责发送请求并返回 {@link com.alibaba.nacos.common.http.client.response.HttpClientResponse}，同时继承 {@link java.io.Closeable} 以释放底层连接。</p>
 *
 * @author mai.jh
 */
public interface HttpClientRequest extends Closeable {
    
    /**
     * execute http request.
     * <p>阻塞执行 HTTP 请求，由 {@link RequestHttpEntity} 携带头、body 与可选客户端配置。</p>
     *
     * @param uri               http url
     * @param httpMethod        http request method
     * @param requestHttpEntity http request entity
     * @return HttpClientResponse
     * @throws Exception ex
     */
    HttpClientResponse execute(URI uri, String httpMethod, RequestHttpEntity requestHttpEntity)
        throws Exception;
}
