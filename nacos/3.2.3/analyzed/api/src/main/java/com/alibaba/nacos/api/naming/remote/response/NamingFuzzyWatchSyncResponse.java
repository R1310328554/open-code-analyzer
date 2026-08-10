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
 * 命名模糊监听同步请求的客户端应答。
 *
 * <p>客户端处理 {@link com.alibaba.nacos.api.naming.remote.request.NamingFuzzyWatchSyncRequest} 批次数据后返回；失败时使用 {@link #buildFailResponse} 上报原因。</p>
 *
 * @author tanyongquan
 */
public class NamingFuzzyWatchSyncResponse extends Response {
    
    /** 无参构造，默认成功响应。 */
    public NamingFuzzyWatchSyncResponse() {
    }
    
    /** 构建成功的同步应答。 */
    public static NamingFuzzyWatchSyncResponse buildSuccessResponse() {
        return new NamingFuzzyWatchSyncResponse();
    }
    
    /**
     * 构建失败的同步应答。
     *
     * @param message 错误消息
     * @return 带错误码的失败响应
     */
    public static NamingFuzzyWatchSyncResponse buildFailResponse(String message) {
        NamingFuzzyWatchSyncResponse result = new NamingFuzzyWatchSyncResponse();
        result.setErrorInfo(ResponseCode.FAIL.getCode(), message);
        return result;
    }
    
}
