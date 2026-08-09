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
package com.alibaba.csp.sentinel.adapter.spring.restclient.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

/**
 * RestClient 被 Sentinel 流控阻断时的降级处理器接口。
 *
 * @author QHT, uuuyuqi
 */
public interface RestClientFallback {

    /**
     * 处理被流控阻断的请求并返回降级响应。
     *
     * @param request HTTP request entity
     * @param body request body
     * @param execution request execution
     * @param ex the block exception
     * @return fallback response
     */
    ClientHttpResponse handle(HttpRequest request, byte[] body, 
                              ClientHttpRequestExecution execution, BlockException ex);
}