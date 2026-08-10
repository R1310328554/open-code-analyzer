/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.api;

/**
 * 认证插件统一的认证结果封装。
 *
 * <p>携带成功/失败标志、错误码、错误消息及可选的业务载荷，供上层鉴权流程
 * 判断请求是否通过并获取附加数据。</p>
 *
 * @author xiweng.yy
 */
public class AuthResult<T> {
    
    private static final String MESSAGE_FORMAT = "Code: %d, Message: %s.";
    
    /** 认证是否成功。 */
    private boolean success;
    
    /** 失败时的错误码。 */
    private int errorCode;
    
    /** 失败时的错误描述。 */
    private String errorMessage;
    
    /**
     * 可选附加数据；部分认证场景需向调用方返回 token、用户标识等信息。
     */
    private T data;
    
    /**
     * 构建无附加数据的成功结果。
     *
     * @return 成功结果
     */
    public static AuthResult successResult() {
        AuthResult result = new AuthResult();
        result.setSuccess(true);
        return result;
    }
    
    /**
     * 构建带附加数据的成功结果。
     *
     * @param data 附加认证数据
     * @return 成功结果
     */
    public static AuthResult successResult(Object data) {
        AuthResult result = new AuthResult();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }
    
    /**
     * 构建失败结果。
     *
     * @param errorCode    错误码
     * @param errorMessage 错误描述
     * @return 失败结果
     */
    public static AuthResult failureResult(int errorCode, String errorMessage) {
        AuthResult result = new AuthResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }
    
    /** @return 认证是否成功 */
    public boolean isSuccess() {
        return success;
    }
    
    /** @param success 认证是否成功 */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /** @return 错误码 */
    public int getErrorCode() {
        return errorCode;
    }
    
    /** @param errorCode 错误码 */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
    
    /** @return 错误描述 */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /** @param errorMessage 错误描述 */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /** @return 附加认证数据 */
    public T getData() {
        return data;
    }
    
    /** @param data 附加认证数据 */
    public void setData(T data) {
        this.data = data;
    }
    
    /**
     * 将错误码与消息格式化为可读字符串。
     *
     * @return 格式化后的错误摘要
     */
    public String format() {
        return String.format(MESSAGE_FORMAT, errorCode, errorMessage);
    }
}
