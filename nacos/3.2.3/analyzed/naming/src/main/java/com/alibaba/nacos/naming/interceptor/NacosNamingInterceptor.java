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

package com.alibaba.nacos.naming.interceptor;

/**
 * Nacos Naming 拦截器接口。
 *
 * <p>通过 SPI 扩展，在健康检查、推送等流程中对特定类型对象进行前置拦截与短路。</p>
 *
 * @author xiweng.yy
 */
public interface NacosNamingInterceptor<T extends Interceptable> {
    
    /**
     * 判断给定类型是否由本拦截器处理。
     *
     * <p>仅做类型匹配，不包含实际拦截逻辑。</p>
     *
     * @param type type
     * @return true if the input type is intercepted by this Interceptor, otherwise false
     */
    boolean isInterceptType(Class<?> type);
    
    /**
     * 执行拦截操作。
     *
     * <p>返回 true 表示已拦截，拦截链将停止并调用 {@link Interceptable#afterIntercept()}。</p>
     *
     * @param object need intercepted object
     * @return true if object is intercepted, otherwise false
     */
    boolean intercept(T object);
    
    /**
     * 拦截器执行顺序，数值越小越先执行。
     *
     * @return the order number of interceptor
     */
    int order();
}
