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

package com.alibaba.nacos.common.http.client;

import com.alibaba.nacos.common.constant.ResponseHandlerType;
import com.alibaba.nacos.common.http.client.handler.BeanResponseHandler;
import com.alibaba.nacos.common.http.client.handler.ByteArrayResponseHandler;
import com.alibaba.nacos.common.http.client.handler.ResponseHandler;
import com.alibaba.nacos.common.http.client.handler.RestResultResponseHandler;
import com.alibaba.nacos.common.http.client.handler.StringResponseHandler;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * For NacosRestTemplate and NacosAsyncRestTemplate, provide initialization and register of response converter.
 * <p>RestTemplate 抽象基类：预注册字符串、RestResult、字节数组与 Bean 等默认 {@link ResponseHandler}，支持按响应类型或自定义类型名选择并转换 HTTP 响应。</p>
 *
 * @author mai.jh
 */
@SuppressWarnings("all")
public abstract class AbstractNacosRestTemplate {
    
    private final Map<String, ResponseHandler> responseHandlerMap =
        new HashMap<String, ResponseHandler>();
    
    protected final Logger logger;
    
    public AbstractNacosRestTemplate(Logger logger) {
        this.logger = logger;
        initDefaultResponseHandler();
    }
    
    private void initDefaultResponseHandler() {
        // 注册内置响应处理器映射
        responseHandlerMap.put(ResponseHandlerType.STRING_TYPE, new StringResponseHandler());
        responseHandlerMap.put(ResponseHandlerType.RESTRESULT_TYPE,
            new RestResultResponseHandler());
        responseHandlerMap.put(ResponseHandlerType.BYTE_ARRAY_TYPE, new ByteArrayResponseHandler());
        responseHandlerMap.put(ResponseHandlerType.DEFAULT_BEAN_TYPE, new BeanResponseHandler());
    }
    
    /**
     * register customization Response Handler.
     * <p>按类型键注册自定义 {@link ResponseHandler}，覆盖或扩展默认映射。</p>
     *
     * @param responseHandler {@link ResponseHandler}
     */
    public void registerResponseHandler(String responseHandlerType,
        ResponseHandler responseHandler) {
        responseHandlerMap.put(responseHandlerType, responseHandler);
    }
    
    /**
     * Select a response handler by responseType.
     *
     * @param responseType responseType
     * @return ResponseHandler
      * <p>RestTemplate 响应处理器基类；详见类级说明。</p>
     */
    protected ResponseHandler selectResponseHandler(Type responseType) {
        ResponseHandler responseHandler = null;
        if (responseType == null) {
            responseHandler = responseHandlerMap.get(ResponseHandlerType.STRING_TYPE);
        }
        if (responseHandler == null) {
            JavaType javaType = JacksonUtils.constructJavaType(responseType);
            String name = javaType.getRawClass().getName();
            responseHandler = responseHandlerMap.get(name);
        }
        // 未匹配到专用处理器时回退到默认 Bean 反序列化处理器
        if (responseHandler == null) {
            responseHandler = responseHandlerMap.get(ResponseHandlerType.DEFAULT_BEAN_TYPE);
        }
        responseHandler.setResponseType(responseType);
        return responseHandler;
    }
}
