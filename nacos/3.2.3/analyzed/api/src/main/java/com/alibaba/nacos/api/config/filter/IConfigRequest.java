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

package com.alibaba.nacos.api.config.filter;

/**
 * 配置过滤器请求接口。
 *
 * <p>封装一次配置读写请求的参数与 {@link IConfigContext} 上下文。</p>
 *
 * @author Nacos
 */
public interface IConfigRequest {
    
    /**
     * 写入请求参数。
     *
     * @param key   参数键
     * @param value 参数值
     */
    void putParameter(String key, Object value);
    
    /**
     * 读取请求参数。
     *
     * @param key 参数键
     * @return 参数值
     */
    Object getParameter(String key);
    
    /**
     * 获取配置上下文。
     *
     * @return {@link IConfigContext} 实例
     */
    IConfigContext getConfigContext();
    
}
