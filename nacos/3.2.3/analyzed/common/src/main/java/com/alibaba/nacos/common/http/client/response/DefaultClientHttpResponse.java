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
import com.alibaba.nacos.common.utils.IoUtils;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * ApacheClientHttpResponse implementation {@link HttpClientResponse}.
 * <p>Apache {@link SimpleHttpResponse} 的 {@link HttpClientResponse} 适配器：懒加载 Nacos {@link Header}，body 以字节数组转为 {@link InputStream}。</p>
 *
 * @author mai.jh
 */
public class DefaultClientHttpResponse implements HttpClientResponse {
    
    /** 底层 Apache 简单响应对象 */
    private SimpleHttpResponse response;
    
    /** 由 body 字节缓存转换的输入流，close 时释放 */
    private InputStream responseStream;
    
    private Header responseHeader;
    
    public DefaultClientHttpResponse(SimpleHttpResponse response) {
        this.response = response;
    }
    
    @Override
    public int getStatusCode() {
        return this.response.getCode();
    }
    
    @Override
    public String getStatusText() {
        return this.response.getReasonPhrase();
    }
    
    @Override
    public Header getHeaders() {
        // 首次访问时从 Apache Header 数组构建 Nacos Header
        if (this.responseHeader == null) {
            this.responseHeader = Header.newInstance();
            org.apache.hc.core5.http.Header[] allHeaders = response.getHeaders();
            for (org.apache.hc.core5.http.Header header : allHeaders) {
                this.responseHeader.addParam(header.getName(), header.getValue());
            }
        }
        return this.responseHeader;
    }
    
    @Override
    public InputStream getBody() {
        byte[] bodyBytes = response.getBody().getBodyBytes();
        // 有 body 则包装为 ByteArrayInputStream，否则返回空流
        if (bodyBytes != null) {
            this.responseStream = new ByteArrayInputStream(bodyBytes);
        } else {
            this.responseStream = new ByteArrayInputStream(new byte[0]);
        }
        return this.responseStream;
    }
    
    @Override
    public void close() {
        IoUtils.closeQuietly(this.responseStream);
    }
}
