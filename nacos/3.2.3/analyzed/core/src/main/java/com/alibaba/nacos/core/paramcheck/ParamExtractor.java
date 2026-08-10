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

package com.alibaba.nacos.core.paramcheck;

import java.util.List;

/**
 * 参数提取器通用接口：将请求对象 {@code T} 转换为待校验对象 {@code R} 列表。
 * ParamExtractor interface.
 *
 * @param <T> the type parameter
 * @author zhuoguang
 */
public interface ParamExtractor<T, R> {
    
    /**
     * 从请求对象中提取待校验参数列表。
     * Extract param.
     *
     * @param params the params
     * @return the list
     * @throws Exception the exception
     */
    List<R> extractParam(T params) throws Exception;
}
