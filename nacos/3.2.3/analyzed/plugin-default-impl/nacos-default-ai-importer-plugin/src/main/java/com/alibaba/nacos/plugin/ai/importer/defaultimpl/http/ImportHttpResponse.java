/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.ai.importer.defaultimpl.http;

import java.net.http.HttpHeaders;

/**
 * 内置 AI 导入 HTTP 客户端返回的响应封装。
 *
 * <p>包含最终 URL、状态码、响应头与 body 字节数组， 并提供 {@link #isSuccess()} 与 {@link #getContentType()} 便捷方法。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class ImportHttpResponse {
    
    /** 实际请求的 URL（重定向解析后）。 */
    private final String url;
    
    /** HTTP 状态码。 */
    private final int statusCode;
    
    /** 响应头集合。 */
    private final HttpHeaders headers;
    
    /** 响应体字节数组（永不为 null）。 */
    private final byte[] body;
    
    public ImportHttpResponse(String url, int statusCode, HttpHeaders headers, byte[] body) {
        this.url = url;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body == null ? new byte[0] : body;
    }
    
    /** 判断状态码是否为 2xx。 */
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
    
    public String getUrl() {
        return url;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public byte[] getBody() {
        return body;
    }
    
    /** 返回 Content-Type 响应头，缺失时为空串。 */
    public String getContentType() {
        return headers.firstValue("Content-Type").orElse("");
    }
}
