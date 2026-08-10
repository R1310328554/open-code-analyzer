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

package com.alibaba.nacos.common.http.client.response;

import com.alibaba.nacos.common.http.param.Header;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a client-side HTTP response.
 * In new version of Apache Http Components, {@code HttpResponse} has been replaced by {@link SimpleHttpResponse}.
 * Cause in this class body content no longer be {@link InputStream} anymore, we don't need to close it anymore.
 * <p>HTTP 客户端响应抽象：统一访问状态码、状态文本、{@link Header} 与 body 流；Apache 5.x 响应体已内存化，实现类 {@link #close()} 主要关闭适配出的 InputStream。</p>
 *
 * @author mai.jh
 */
public interface HttpClientResponse extends Closeable {
    
    /**
     * Return the headers of this message.
     * <p>返回 Nacos 统一 {@link Header}，永不为 null。</p>
     *
     * @return a corresponding HttpHeaders object (never {@code null})
     */
    Header getHeaders();
    
    /**
     * Return the body of the message as an input stream.
     * <p>以输入流形式读取响应体，charset 由 Header 解析。</p>
     *
     * @return String response body
     * @throws IOException IOException
     */
    InputStream getBody() throws IOException;
    
    /**
     * Return the HTTP status code.
     *
     * @return the HTTP status as an integer
     * @throws IOException IOException
      * <p>HTTP 响应抽象；详见接口说明。</p>
     */
    int getStatusCode() throws IOException;
    
    /**
     * Return the HTTP status text of the response.
     *
     * @return the HTTP status text
     * @throws IOException IOException
      * <p>HTTP 响应抽象；详见接口说明。</p>
     */
    String getStatusText() throws IOException;
    
    /**
     * close response InputStream.
     * <p>关闭 {@link #getBody()} 返回的流并释放相关资源。</p>
     */
    @Override
    void close();
}
