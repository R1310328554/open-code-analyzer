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
package com.alibaba.csp.sentinel.transport.heartbeat.client;

import com.alibaba.csp.sentinel.config.SentinelConfig;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * 简易 HTTP 响应：状态行、响应头与 body 字节。
 * 通过 {@link #getBodyAsString()} 按 Content-Type charset 解码正文。
 *
 * @author leyou
 */
public class SimpleHttpResponse {

    /** 正文解码字符集，可从 Content-Type 解析。 */
    private Charset charset = Charset.forName(SentinelConfig.charset());

    /** HTTP 状态行（如 HTTP/1.1 200 OK）。 */
    private String statusLine;
    /** 数字状态码，懒解析自 statusLine。 */
    private int statusCode;
    /** 响应头 Map。 */
    private Map<String, String> headers;
    /** 响应体原始字节。 */
    private byte[] body;

    public SimpleHttpResponse(String statusLine, Map<String, String> headers) {
        this.statusLine = statusLine;
        this.headers = headers;
    }

    public SimpleHttpResponse(String statusLine, Map<String, String> headers, byte[] body) {
        this.statusLine = statusLine;
        this.headers = headers;
        this.body = body;
    }

    private void parseCharset() {
        String contentType = getHeader("Content-Type");
        for (String str : contentType.split(" ")) {
            if (str.toLowerCase().startsWith("charset=")) {
                charset = Charset.forName(str.split("=")[1]);
            }
        }
    }

    private void parseCode() {
        this.statusCode = Integer.parseInt(statusLine.split(" ")[1]);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public Integer getStatusCode() {
        if (statusCode == 0) {
            parseCode();
        }
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 按 key 获取响应头，忽略大小写。
     *
     * @param key 头名称
     * @return 头值，不存在时 null
     */
    public String getHeader(String key) {
        if (headers == null) {
            return null;
        }
        String value = headers.get(key);
        if (value != null) {
            return value;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** 按 charset 将 body 解码为字符串。 */
    public String getBodyAsString() {
        parseCharset();
        return new String(body, charset);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(statusLine)
                .append("\r\n");
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                buf.append(entry.getKey()).append(": ").append(entry.getValue())
                        .append("\r\n");
            }
        }
        buf.append("\r\n");
        buf.append(getBodyAsString());
        return buf.toString();
    }
}
