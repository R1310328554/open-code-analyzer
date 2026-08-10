/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.lock.remote.response;

import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseCode;

/**
 * 分布式锁操作 gRPC 响应。
 *
 * <p>封装加锁/解锁结果或失败原因，继承 {@link Response} 通用响应字段。</p>
 *
 * @author 985492783@qq.com
 * @description AcquireLockResponse
 * @date 2023/6/29 13:51
 */
public class LockOperationResponse extends Response {
    
    /** 操作结果（通常为 {@link Boolean}）。 */
    private Object result;
    
    /** 无参构造，供序列化或框架实例化使用。 */
    public LockOperationResponse() {
        
    }
    
    /**
     * 以布尔结果构造响应。
     *
     * @param result 操作是否成功
     */
    public LockOperationResponse(Boolean result) {
        this.result = result;
    }
    
    /**
     * 创建成功响应。
     *
     * @param result 操作结果
     * @return 锁操作成功响应
     */
    public static LockOperationResponse success(Boolean result) {
        LockOperationResponse response = new LockOperationResponse(result);
        return response;
    }
    
    /**
     * 创建失败响应。
     *
     * @param message 失败原因描述
     * @return 锁操作失败响应
     */
    public static LockOperationResponse fail(String message) {
        LockOperationResponse response = new LockOperationResponse(false);
        response.setResultCode(ResponseCode.FAIL.getCode());
        response.setMessage(message);
        return response;
    }
    
    /** 获取操作结果。 */
    public Object getResult() {
        return result;
    }
    
    /** 设置操作结果。 */
    public void setResult(Object result) {
        this.result = result;
    }
}
