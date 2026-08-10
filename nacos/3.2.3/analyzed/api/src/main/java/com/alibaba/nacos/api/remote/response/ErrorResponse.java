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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;

/**
 * 通用错误响应，封装 RPC 调用失败信息。
 *
 * <p>服务端在处理请求异常或无法识别请求类型时返回；可通过静态工厂 {@link #build(int, String)} 或 {@link #build(Throwable)} 快速构造。</p>
 *
 * @author liuzunfei
 * @version $Id: UnKnowResponse.java, v 0.1 2020年07月16日 9:47 PM liuzunfei Exp $
 */
public class ErrorResponse extends Response {
    
    /**
     * 根据错误码与消息构建错误响应。
     *
     * @param errorCode 错误码
     * @param msg       错误描述
     * @return 错误响应实例
     */
    public static Response build(int errorCode, String msg) {
        ErrorResponse response = new ErrorResponse();
        response.setErrorInfo(errorCode, msg);
        return response;
    }
    
    /**
     * 根据异常构建错误响应。
     *
     * <p>自动识别 {@link NacosException} 与 {@link NacosRuntimeException} 的错误码。</p>
     *
     * @param exception 异常对象
     * @return 错误响应实例
     */
    public static Response build(Throwable exception) {
        int errorCode;
        if (exception instanceof NacosException) {
            errorCode = ((NacosException) exception).getErrCode();
        } else if (exception instanceof NacosRuntimeException) {
            errorCode = ((NacosRuntimeException) exception).getErrCode();
        } else {
            errorCode = ResponseCode.FAIL.getCode();
        }
        ErrorResponse response = new ErrorResponse();
        response.setErrorInfo(errorCode, exception.getMessage());
        response.setResultCode(errorCode);
        return response;
    }
    
}
