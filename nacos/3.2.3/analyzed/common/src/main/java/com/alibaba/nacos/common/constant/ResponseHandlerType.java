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

package com.alibaba.nacos.common.constant;

/**
 * Response Handler Type.
 * <p>HTTP 响应体反序列化处理器类型标识常量，用于按返回类型选择 {@code ResponseHandler}。</p>
 *
 * @author mai.jh
 */
public final class ResponseHandlerType {
    
    /** 字符串类型处理器标识 */
    public static final String STRING_TYPE = "java.lang.String";
    
    /** {@link com.alibaba.nacos.common.model.RestResult} 类型处理器标识 */
    public static final String RESTRESULT_TYPE = "com.alibaba.nacos.common.model.RestResult";
    
    /** 字节数组类型处理器标识 */
    public static final String BYTE_ARRAY_TYPE = byte[].class.getName();
    
    /** 默认 Bean 处理器标识 */
    public static final String DEFAULT_BEAN_TYPE = "default_bean_handler";
    
}
