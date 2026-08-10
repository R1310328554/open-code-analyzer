/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.oidc.authorization;

import com.alibaba.nacos.plugin.auth.impl.oidc.constant.OidcConstants;

/**
 * IdP 授权端点返回的响应模型。
 *
 * <p>支持多种常见 JSON 格式（allowed/result/decision 字段），由 {@link #fromJson} 统一解析。</p>
 *
 * @author WangzJi
 */
public class AuthorizationResponse {
    
    /**
     * 是否允许访问。
     */
    private boolean allowed;
    
    /**
     * 拒绝原因（allowed 为 false 时有效）。
     */
    private String reason;
    
    /**
     * 错误码（若 IdP 返回）。
     */
    private String errorCode;
    
    public AuthorizationResponse() {
    }
    
    public AuthorizationResponse(boolean allowed) {
        this.allowed = allowed;
    }
    
    public AuthorizationResponse(boolean allowed, String reason) {
        this.allowed = allowed;
        this.reason = reason;
    }
    
    /**
     * 创建允许访问的响应。
     *
     * @return allowed=true 的响应实例
     */
    public static AuthorizationResponse allowed() {
        return new AuthorizationResponse(true);
    }
    
    /**
     * 创建拒绝访问的响应。
     *
     * @param reason 拒绝原因
     * @return allowed=false 的响应实例
     */
    public static AuthorizationResponse denied(String reason) {
        return new AuthorizationResponse(false, reason);
    }
    
    /**
     * 解析 IdP 返回的 JSON 响应。
     *
     * <p>兼容格式示例：</p>
     * <ul>
     *   <li>{@code {"allowed": true}}</li>
     *   <li>{@code {"allowed": false, "reason": "..."}}</li>
     *   <li>{@code {"result": "PERMIT"}} / {@code {"result": "DENY"}}（Keycloak）</li>
     *   <li>{@code {"decision": "Permit"}} / {@code {"decision": "Deny"}}</li>
     * </ul>
     *
     * @param json IdP 响应 JSON 字符串
     * @return 解析后的 AuthorizationResponse
     */
    public static AuthorizationResponse fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return denied("Empty response from IdP");
        }
        
        AuthorizationResponse response = new AuthorizationResponse();
        
        // 解析 allowed 字段
        if (json.contains(OidcConstants.JSON_FIELD_ALLOWED)) {
            response.allowed = json.contains("\"allowed\":true")
                || json.contains("\"allowed\": true");
        } else if (json.contains(OidcConstants.JSON_FIELD_RESULT)) {
            // Keycloak 风格的 result 字段
            response.allowed = json.toLowerCase().contains("\"result\":\"permit\"")
                || json.toLowerCase().contains("\"result\": \"permit\"");
        } else if (json.contains(OidcConstants.JSON_FIELD_DECISION)) {
            // 备选 decision 字段格式
            response.allowed = json.toLowerCase().contains("\"decision\":\"permit\"")
                || json.toLowerCase().contains("\"decision\": \"permit\"");
        }
        
        // 提取拒绝原因（兼容 reason/message/error_description）
        response.reason = extractJsonValue(json, "reason");
        if (response.reason == null) {
            response.reason = extractJsonValue(json, "message");
        }
        if (response.reason == null) {
            response.reason = extractJsonValue(json, "error_description");
        }
        
        // 提取错误码（兼容 error/errorCode）
        response.errorCode = extractJsonValue(json, "error");
        if (response.errorCode == null) {
            response.errorCode = extractJsonValue(json, "errorCode");
        }
        
        return response;
    }
    
    /**
     * 简易 JSON 字符串值提取（按 key 查找引号包裹的值）。
     *
     * @param json JSON 字符串
     * @param key  待提取的字段名
     * @return 字段值；未找到时返回 null
     */
    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return null;
        }
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) {
            return null;
        }
        
        int valueStart = json.indexOf("\"", colonIndex);
        if (valueStart == -1) {
            return null;
        }
        
        int valueEnd = json.indexOf("\"", valueStart + 1);
        if (valueEnd == -1) {
            return null;
        }
        
        return json.substring(valueStart + 1, valueEnd);
    }
    
    // Getter 与 Setter
    
    public boolean isAllowed() {
        return allowed;
    }
    
    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    @Override
    public String toString() {
        return "AuthorizationResponse{allowed=" + allowed + ", reason='" + reason + "'}";
    }
}
