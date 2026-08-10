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

package com.alibaba.nacos.api.remote.request;

import com.alibaba.nacos.api.remote.Payload;
import java.util.Map;
import java.util.TreeMap;

/**
 * 远程 RPC 请求的抽象基类，实现 {@link com.alibaba.nacos.api.remote.Payload}。
 *
 * <p>携带 {@link #requestId} 与大小写不敏感的 {@link #headers} 扩展头；子类通过 {@link #getModule()} 声明所属业务模块。</p>
 *
 * @author liuzunfei
 */
public abstract class Request implements Payload {
    
    /** 请求扩展头（键大小写不敏感）。 */
    private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    
    /** 请求唯一标识，用于关联响应。 */
    private String requestId;
    
    /**
     * 写入单个扩展头。
     *
     * @param key   头键
     * @param value 头值
     */
    public void putHeader(String key, String value) {
        headers.put(key, value);
    }
    
    /**
     * 批量写入扩展头。
     *
     * @param headers 待合并的头映射
     */
    public void putAllHeader(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        this.headers.putAll(headers);
    }
    
    /**
     * 读取扩展头值。
     *
     * @param key 头键
     * @return 头值，不存在时返回 {@code null}
     */
    public String getHeader(String key) {
        return headers.get(key);
    }
    
    /**
     * 读取扩展头值，不存在时返回默认值。
     *
     * @param key          头键
     * @param defaultValue 默认值
     * @return 头值或默认值
     */
    public String getHeader(String key, String defaultValue) {
        String value = headers.get(key);
        return (value == null) ? defaultValue : value;
    }
    
    /** 返回请求 ID。 */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * 设置请求 ID。
     *
     * @param requestId 请求标识
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    /**
     * 返回请求所属业务模块标识。
     *
     * @return 模块名（如 config、naming、internal）
     */
    public abstract String getModule();
    
    /** 返回全部扩展头映射。 */
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    /** 清空所有扩展头。 */
    public void clearHeaders() {
        this.headers.clear();
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "headers=" + headers + ", requestId='"
            + requestId + '\'' + '}';
    }
}
