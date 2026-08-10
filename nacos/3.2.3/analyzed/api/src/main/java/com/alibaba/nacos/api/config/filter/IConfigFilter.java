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

import com.alibaba.nacos.api.exception.NacosException;

import java.util.Properties;

/**
 * 配置过滤器接口。
 *
 * <p>请勿直接实现本接口，应继承 {@link AbstractConfigFilter} 以获得默认实现与排序支持。</p>
 *
 * @author Nacos
 * @see AbstractConfigFilter
 */
public interface IConfigFilter {
    
    /**
     * 初始化过滤器。
     *
     * @param properties 过滤器配置属性
     */
    void init(Properties properties);
    
    /**
     * 执行过滤逻辑。
     *
     * <p>可在处理前后修改 {@link IConfigRequest} 与 {@link IConfigResponse}，
     * 并通过 {@link IConfigFilterChain#doFilter} 继续调用链中下一过滤器。</p>
     *
     * @param request     配置请求
     * @param response    配置响应
     * @param filterChain 过滤器链
     * @throws NacosException 过滤过程异常
     */
    void doFilter(IConfigRequest request, IConfigResponse response, IConfigFilterChain filterChain)
        throws NacosException;
    
    /**
     * 获取过滤器执行顺序。
     *
     * @return 顺序值，数值越小越先执行
     */
    int getOrder();
    
    /**
     * 获取过滤器名称。
     *
     * @return 过滤器唯一标识名
     */
    String getFilterName();
    
}
