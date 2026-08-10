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

package com.alibaba.nacos.common.http.client.handler;

import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.response.HttpClientResponse;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.common.utils.JacksonUtils;

import java.lang.reflect.Type;

/**
 * RestResult response handler, Mainly converter response type as {@link RestResult} type.
 * <p>{@link RestResult} 响应处理器：将 HTTP 响应体反序列化为 {@link RestResult}，再映射为带响应头的 {@link com.alibaba.nacos.common.http.HttpRestResult}，供 Nacos 统一 REST 封装调用方使用。</p>
 *
 * @author mai.jh
 */
public class RestResultResponseHandler<T> extends AbstractResponseHandler<T> {
    
    /**
     * 将响应体解析为 {@link RestResult} 并转换为 {@link HttpRestResult}。
     * <p>使用 Jackson 按 {@code responseType} 反序列化 body，保留原始响应头。</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public HttpRestResult<T> convertResult(HttpClientResponse response, Type responseType)
        throws Exception {
        final Header headers = response.getHeaders();
        T extractBody = JacksonUtils.toObj(response.getBody(), responseType);
        HttpRestResult<T> httpRestResult = convert((RestResult<T>) extractBody);
        httpRestResult.setHeader(headers);
        return httpRestResult;
    }
    
    /** 将 {@link RestResult} 的 code/data/message 拷贝到 {@link HttpRestResult} */
    private static <T> HttpRestResult<T> convert(RestResult<T> restResult) {
        HttpRestResult<T> httpRestResult = new HttpRestResult<>();
        httpRestResult.setCode(restResult.getCode());
        httpRestResult.setData(restResult.getData());
        httpRestResult.setMessage(restResult.getMessage());
        return httpRestResult;
    }
    
}
