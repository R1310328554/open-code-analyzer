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

package com.alibaba.nacos.common.utils;

/**
 * HTTP 方法名常量：标准 REST 动词及 Nacos 自定义的 GET-LARGE、DELETE_LARGE
 * （参数过大时放 body 而非 URL）。
 * Http method constants.
 *
 * @author nkorange
 * @since 0.8.0
 */
public class HttpMethod {
    
    private HttpMethod() {
    }
    
    /** 标准 GET 方法 */
    public static final String GET = "GET";
    
    /**
     * Nacos 自定义：本质为 GET，参数过大无法放 URL 时放请求体。
     */
    public static final String GET_LARGE = "GET-LARGE";
    
    public static final String HEAD = "HEAD";
    
    public static final String POST = "POST";
    
    public static final String PUT = "PUT";
    
    public static final String PATCH = "PATCH";
    
    public static final String DELETE = "DELETE";
    
    /**
     * Nacos 自定义：本质为 DELETE，参数过大时放请求体。
     */
    public static final String DELETE_LARGE = "DELETE_LARGE";
    
    public static final String OPTIONS = "OPTIONS";
    
    public static final String TRACE = "TRACE";
}
