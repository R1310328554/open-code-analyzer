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

/**
 * 客户端对服务端推送消息的 ACK 确认请求。
 *
 * <p>推送方在收到此 ACK 后调用 {@link com.alibaba.nacos.api.remote.PushCallBack#onSuccess()}；失败时携带 {@link #exception} 并标记 {@link #success} 为 {@code false}。</p>
 *
 * @author liuzunfei
 * @version $Id: PushAckRequest.java, v 0.1 2020年07月29日 8:25 PM liuzunfei Exp $
 */
public class PushAckRequest extends InternalRequest {
    
    /** 被确认推送的原始请求 ID。 */
    private String requestId;
    
    /** 推送处理是否成功。 */
    private boolean success;
    
    /** 推送失败时的异常信息。 */
    private Exception exception;
    
    /**
     * 构建推送 ACK 请求。
     *
     * @param requestId 原始推送请求 ID
     * @param success   是否成功
     * @return ACK 请求实例
     */
    public static PushAckRequest build(String requestId, boolean success) {
        PushAckRequest request = new PushAckRequest();
        request.requestId = requestId;
        request.success = success;
        return request;
    }
    
    /** 返回被确认推送的请求 ID。 */
    @Override
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * 设置被确认推送的请求 ID。
     *
     * @param requestId 请求标识
     */
    @Override
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    /** 返回推送处理是否成功。 */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 设置推送处理结果。
     *
     * @param success 是否成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * 设置推送失败时的异常。
     *
     * @param exception 异常对象
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }
    
    /** 返回推送失败时的异常。 */
    public Exception getException() {
        return exception;
    }
}
