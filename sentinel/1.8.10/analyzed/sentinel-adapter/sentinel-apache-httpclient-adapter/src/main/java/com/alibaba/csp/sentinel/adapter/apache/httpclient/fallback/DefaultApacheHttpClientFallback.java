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
package com.alibaba.csp.sentinel.adapter.apache.httpclient.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * Apache HttpClient 默认降级实现，将 {@link BlockException} 包装为 {@link SentinelRpcException}。
 *
 * @author zhaoyuguang
 */
public class DefaultApacheHttpClientFallback implements ApacheHttpClientFallback {

    /** 包装阻断异常并抛出 {@link SentinelRpcException}。 */
    @Override
    public CloseableHttpResponse handle(HttpRequestWrapper request, BlockException e) {
        // 将阻断异常包装后抛出。
        throw new SentinelRpcException(e);
    }
}
