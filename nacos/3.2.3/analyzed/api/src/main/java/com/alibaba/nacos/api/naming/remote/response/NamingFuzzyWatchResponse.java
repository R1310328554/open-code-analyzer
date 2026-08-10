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

package com.alibaba.nacos.api.naming.remote.response;

import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseCode;

/**
 * 命名模糊监听注册请求的远程响应。
 *
 * <p>服务端处理 {@link com.alibaba.nacos.api.naming.remote.request.NamingFuzzyWatchRequest} 后返回；失败时通过 {@link #buildFailResponse} 携带错误信息。</p>
 *
 * @author tanyongquan
 */
public class NamingFuzzyWatchResponse extends Response {
    
    /** 无参构造，默认成功响应。 */
    public NamingFuzzyWatchResponse() {
    }
    
    /** 构建成功的模糊监听响应。 */
    public static NamingFuzzyWatchResponse buildSuccessResponse() {
        return new NamingFuzzyWatchResponse();
    }
    
    /**
     * 构建失败的模糊监听响应。
     *
     * @param message 错误消息
     * @return 带错误码的失败响应
     */
    public static NamingFuzzyWatchResponse buildFailResponse(String message) {
        NamingFuzzyWatchResponse result = new NamingFuzzyWatchResponse();
        result.setErrorInfo(ResponseCode.FAIL.getCode(), message);
        return result;
    }
    
}
