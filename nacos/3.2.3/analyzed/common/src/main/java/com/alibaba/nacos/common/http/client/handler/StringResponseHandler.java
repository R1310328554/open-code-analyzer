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
import com.alibaba.nacos.common.utils.IoUtils;

import java.lang.reflect.Type;

/**
 * string response handler, Mainly converter response type as string type.
 * <p>字符串响应处理器：按响应头 charset 将 body 转为 {@link String}，封装为 {@link com.alibaba.nacos.common.http.HttpRestResult}，适用于纯文本或原始 JSON 字符串场景。</p>
 *
 * @author mai.jh
 */
public class StringResponseHandler extends AbstractResponseHandler<String> {
    
    /**
     * 读取响应流为字符串并组装 {@link HttpRestResult}。
     * <p>{@code responseType} 在此实现中未使用，body 始终为字符串。</p>
     */
    @Override
    public HttpRestResult<String> convertResult(HttpClientResponse response, Type responseType)
        throws Exception {
        final Header headers = response.getHeaders();
        String extractBody = IoUtils.toString(response.getBody(), headers.getCharset());
        return new HttpRestResult<>(headers, response.getStatusCode(), extractBody, null);
    }
}
