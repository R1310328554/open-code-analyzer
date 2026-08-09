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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.transport.endpoint.Endpoint;

/**
 * 简易 HTTP 请求描述：目标端点、路径、超时、字符集与参数 Map。
 * 支持链式 setter 与 {@link #addParam}。
 *
 * @author leyou
 * @author Leo Li
 */
public class SimpleHttpRequest {

    /** 目标 Dashboard 端点。 */
    private Endpoint endpoint;
    /** API 路径（如心跳注册路径）。 */
    private String requestPath = "";
    /** Socket 读写超时（毫秒）。 */
    private int soTimeout = 3000;
    /** 请求参数（GET 拼 URL，POST 写 body）。 */
    private Map<String, String> params;
    /** 请求编码字符集，默认 Sentinel 全局 charset。 */
    private Charset charset = Charset.forName(SentinelConfig.charset());

    /** @param endpoint 目标端点
     * @param requestPath API 路径 */
    public SimpleHttpRequest(Endpoint endpoint, String requestPath) {
        this.endpoint = endpoint;
        this.requestPath = requestPath;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public SimpleHttpRequest setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public SimpleHttpRequest setRequestPath(String requestPath) {
        this.requestPath = requestPath;
        return this;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public SimpleHttpRequest setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public SimpleHttpRequest setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public Charset getCharset() {
        return charset;
    }

    public SimpleHttpRequest setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /** 链式添加单个请求参数。 */
    public SimpleHttpRequest addParam(String key, String value) {
        if (StringUtil.isBlank(key)) {
            throw new IllegalArgumentException("参数键不能为空");
        }
        if (params == null) {
            params = new HashMap<String, String>();
        }
        params.put(key, value);
        return this;
    }
}
