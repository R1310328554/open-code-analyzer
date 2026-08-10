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

import com.alibaba.nacos.common.http.Callback;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.handler.ResponseHandler;
import com.alibaba.nacos.common.http.client.response.DefaultClientHttpResponse;
import com.alibaba.nacos.common.model.RequestHttpEntity;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

/**
 * {@link AsyncHttpClientRequest} implementation that uses apache async http client to execute streaming requests.
 * <p>基于 Apache HttpClient5 {@link org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient} 的异步请求实现：复用 {@link DefaultHttpClientRequest#build} 构造请求，在 {@link FutureCallback} 中转换响应并回调。</p>
 *
 * @author mai.jh
 */
public class DefaultAsyncHttpClientRequest implements AsyncHttpClientRequest {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(DefaultAsyncHttpClientRequest.class);
    
    /** Apache 异步 HTTP 客户端实例 */
    private final CloseableHttpAsyncClient asyncClient;
    
    /** 默认请求配置（超时等），可与单次请求配置合并 */
    private final RequestConfig defaultConfig;
    
    public DefaultAsyncHttpClientRequest(CloseableHttpAsyncClient asyncClient,
        DefaultConnectingIOReactor ioReactor, RequestConfig defaultConfig) {
        this.asyncClient = asyncClient;
        this.defaultConfig = defaultConfig;
        // 若 IO Reactor 未启动则先启动，避免 execute 时 IllegalStateException
        if (this.asyncClient.getStatus() != IOReactorStatus.ACTIVE) {
            this.asyncClient.start();
        }
    }
    
    @Override
    public <T> void execute(URI uri, String httpMethod, RequestHttpEntity requestHttpEntity,
        final ResponseHandler<T> responseHandler, final Callback<T> callback) throws Exception {
        HttpUriRequestBase httpRequestBase =
            DefaultHttpClientRequest.build(uri, httpMethod, requestHttpEntity, defaultConfig);
        // HttpClient5 起 IllegalStateException 改由 IOReactor 回调处理，此处用 FutureCallback 桥接业务 Callback
        FutureCallback<SimpleHttpResponse> futureCallback =
            new FutureCallback<SimpleHttpResponse>() {
                
                @Override
                /** 异步请求成功：包装为 {@link DefaultClientHttpResponse} 并交给 ResponseHandler */
                public void completed(SimpleHttpResponse result) {
                    // SimpleHttpResponse 体已在内存中，无需像旧版 HttpResponse 那样关闭流
                    DefaultClientHttpResponse response = new DefaultClientHttpResponse(result);
                    try {
                        HttpRestResult<T> httpRestResult = responseHandler.handle(response);
                        callback.onReceive(httpRestResult);
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                }
                
                @Override
                /** 网络或协议层失败，转发至 callback.onError */
                public void failed(Exception ex) {
                    callback.onError(ex);
                }
                
                @Override
                /** 请求被取消，通知 callback.onCancel */
                public void cancelled() {
                    callback.onCancel();
                }
            };
        asyncClient.execute(SimpleHttpRequest.copy(httpRequestBase), futureCallback);
    }
    
    @Override
    public void close() throws IOException {
        this.asyncClient.close();
    }
}
