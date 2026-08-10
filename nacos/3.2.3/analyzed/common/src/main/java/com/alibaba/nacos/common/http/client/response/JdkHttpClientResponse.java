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

import com.alibaba.nacos.common.constant.HttpHeaderConsts;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.utils.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * JDk http client response implement.
 * <p>基于 {@link HttpURLConnection} 的 {@link HttpClientResponse} 实现：支持 gzip 解压、JDK 多值响应头保留，错误响应优先读 errorStream。</p>
 *
 * @author mai.jh
 */
public class JdkHttpClientResponse implements HttpClientResponse {
    
    /** 已连接的 JDK HTTP 连接，提供状态与头信息 */
    private final HttpURLConnection conn;
    
    private InputStream responseStream;
    
    private Header responseHeader;
    
    /** 需解压的 Content-Encoding 值 */
    private static final String CONTENT_ENCODING = "gzip";
    
    public JdkHttpClientResponse(HttpURLConnection conn) {
        this.conn = conn;
    }
    
    @Override
    public Header getHeaders() {
        if (this.responseHeader == null) {
            this.responseHeader = Header.newInstance();
        }
        for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
            this.responseHeader.addOriginalResponseHeader(entry.getKey(), entry.getValue());
        }
        return this.responseHeader;
    }
    
    @Override
    public InputStream getBody() throws IOException {
        Header headers = getHeaders();
        // 4xx/5xx 时 getInputStream 可能不可用，优先使用 errorStream
        InputStream errorStream = this.conn.getErrorStream();
        this.responseStream = (errorStream != null ? errorStream : this.conn.getInputStream());
        String contentEncoding = headers.getValue(HttpHeaderConsts.CONTENT_ENCODING);
        // Content-Encoding 为 gzip 时解压为字节数组再包装为 InputStream
        if (CONTENT_ENCODING.equals(contentEncoding)) {
            byte[] bytes = IoUtils.tryDecompress(this.responseStream);
            return new ByteArrayInputStream(bytes);
        }
        return this.responseStream;
    }
    
    @Override
    public int getStatusCode() throws IOException {
        return this.conn.getResponseCode();
    }
    
    @Override
    public String getStatusText() throws IOException {
        return this.conn.getResponseMessage();
    }
    
    @Override
    public void close() {
        IoUtils.closeQuietly(this.responseStream);
    }
}
