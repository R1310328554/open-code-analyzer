/*
 *
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.core.control.http;

import com.alibaba.nacos.plugin.control.tps.request.TpsCheckRequest;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP TPS 校验请求解析器抽象基类：构造时自动注册到 {@link HttpTpsCheckRequestParserRegistry}，子类实现如何将 {@link HttpServletRequest} 转为 {@link TpsCheckRequest}。
 * http tps check request parser.
 *
 * @author shiyiyue
 */
public abstract class HttpTpsCheckRequestParser {
    
    /** 构造并完成解析器自注册。 */
    public HttpTpsCheckRequestParser() {
        registerParser();
    }
    
    /** 将当前解析器注册到全局注册表。 */
    public void registerParser() {
        HttpTpsCheckRequestParserRegistry.register(this);
    }
    
    /**
     * 将 HTTP 请求解析为 TPS 校验请求对象。
     *
     * @param httpServletRequest HTTP 请求
     * @return {@link TpsCheckRequest} 实例
     */
    public abstract TpsCheckRequest parse(HttpServletRequest httpServletRequest);
    
    /**
     * 返回关联的 TPS 控制点名称。
     *
     * @return 控制点标识
     */
    public abstract String getPointName();
    
    /**
     * 返回解析器注册名（注册表 key）。
     *
     * @return 解析器唯一名称
     */
    public abstract String getName();
    
}
