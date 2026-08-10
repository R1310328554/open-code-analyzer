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

package com.alibaba.nacos.api.naming.pojo.healthcheck.impl;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;
import com.alibaba.nacos.api.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * HTTP 协议健康检查器实现，向实例发起 HTTP 请求并根据响应码判定健康状态。
 *
 * <p>可配置探测路径、自定义请求头及期望 HTTP 状态码，默认期望 {@code 200}。</p>
 *
 * @author yangyi
 */
public class Http extends AbstractHealthChecker {
    
    /** 类型常量 {@code HTTP}。 */
    public static final String TYPE = "HTTP";
    
    private static final long serialVersionUID = 551826315222362349L;
    
    /** HTTP 探测路径，默认为空字符串。 */
    private String path = "";
    
    /** 原始请求头字符串，格式为 {@code Key:Value}，多条以分隔符连接。 */
    private String headers = "";
    
    /** 期望的 HTTP 响应状态码，默认 200。 */
    private int expectedResponseCode = 200;
    
    /** 构造 HTTP 类型健康检查器。 */
    public Http() {
        super(Http.TYPE);
    }
    
    /** 获取期望响应状态码。 */
    public int getExpectedResponseCode() {
        return this.expectedResponseCode;
    }
    
    /** 设置期望响应状态码。 */
    public void setExpectedResponseCode(final int expectedResponseCode) {
        this.expectedResponseCode = expectedResponseCode;
    }
    
    /** 获取 HTTP 探测路径。 */
    public String getPath() {
        return this.path;
    }
    
    /** 设置 HTTP 探测路径。 */
    public void setPath(final String path) {
        this.path = path;
    }
    
    /** 获取原始请求头字符串。 */
    public String getHeaders() {
        return this.headers;
    }
    
    /** 设置原始请求头字符串。 */
    public void setHeaders(final String headers) {
        this.headers = headers;
    }
    
    /**
     * 将 {@link #headers} 解析为键值 Map，供探测客户端使用。
     *
     * @return 请求头 Map；无有效头时返回空 Map
     */
    @JsonIgnore
    public Map<String, String> getCustomHeaders() {
        if (StringUtils.isBlank(headers)) {
            return Collections.emptyMap();
        }
        final Map<String, String> headerMap = new HashMap<>(16);
        for (final String s : headers.split(Constants.NAMING_HTTP_HEADER_SPLITTER)) {
            final String[] splits = s.split(":");
            if (splits.length != 2) {
                continue;
            }
            headerMap.put(StringUtils.trim(splits[0]), StringUtils.trim(splits[1]));
        }
        return headerMap;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(path, headers, expectedResponseCode);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Http)) {
            return false;
        }
        
        final Http other = (Http) obj;
        
        if (!StringUtils.equals(path, other.getPath())) {
            return false;
        }
        if (!StringUtils.equals(headers, other.getHeaders())) {
            return false;
        }
        return expectedResponseCode == other.getExpectedResponseCode();
    }
    
    @Override
    public Http clone() throws CloneNotSupportedException {
        final Http config = new Http();
        config.setPath(getPath());
        config.setHeaders(getHeaders());
        config.setExpectedResponseCode(getExpectedResponseCode());
        return config;
    }
}
