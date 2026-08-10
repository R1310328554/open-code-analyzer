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

package com.alibaba.nacos.api.remote.response;

import com.alibaba.nacos.api.remote.Payload;

/**
 * 远程 RPC 响应的抽象基类，实现 {@link com.alibaba.nacos.api.remote.Payload}。
 *
 * <p>携带 {@link #resultCode}、{@link #errorCode}、{@link #message} 与 {@link #requestId}；通过 {@link #isSuccess()} 判断调用是否成功，失败时可调用 {@link #setErrorInfo(int, String)} 填充错误详情。</p>
 *
 * @author liuzunfei
 * @version $Id: Response.java, v 0.1 2020年07月13日 6:03 PM liuzunfei Exp $
 */
public abstract class Response implements Payload {
    
    /** 结果码，默认 {@link ResponseCode#SUCCESS}。 */
    int resultCode = ResponseCode.SUCCESS.getCode();
    
    /** 业务错误码（失败时有效）。 */
    int errorCode;
    
    /** 错误或提示消息。 */
    String message;
    
    /** 关联的请求 ID。 */
    String requestId;
    
    /** 返回关联的请求 ID。 */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * 设置关联的请求 ID。
     *
     * @param requestId 请求标识
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    /**
     * 判断响应是否成功。
     *
     * @return {@code resultCode} 为 SUCCESS 时返回 {@code true}
     */
    public boolean isSuccess() {
        return this.resultCode == ResponseCode.SUCCESS.getCode();
    }
    
    /** 返回结果码。 */
    public int getResultCode() {
        return resultCode;
    }
    
    /**
     * 设置结果码。
     *
     * @param resultCode 结果码
     */
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }
    
    /** 返回消息内容。 */
    public String getMessage() {
        return message;
    }
    
    /**
     * 设置消息内容。
     *
     * @param message 消息字符串
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /** 返回业务错误码。 */
    public int getErrorCode() {
        return errorCode;
    }
    
    /**
     * 设置业务错误码。
     *
     * @param errorCode 错误码
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
    
    /** 一次性设置失败结果码、错误码与错误消息。 */
    public void setErrorInfo(int errorCode, String errorMsg) {
        this.resultCode = ResponseCode.FAIL.getCode();
        this.errorCode = errorCode;
        this.message = errorMsg;
    }
    
    @Override
    public String toString() {
        return "Response{" + "resultCode=" + resultCode + ", errorCode=" + errorCode + ", message='"
            + message + '\''
            + ", requestId='" + requestId + '\'' + '}';
    }
}
