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

/**
 * 配置过滤器链接口。
 *
 * <p>由框架实现，供 {@link IConfigFilter#doFilter} 调用以驱动后续过滤器执行。</p>
 *
 * @author Nacos
 */
public interface IConfigFilterChain {
    
    /**
     * 继续执行过滤器链中的下一环节。
     *
     * @param request  配置请求
     * @param response 配置响应
     * @throws NacosException 过滤过程异常
     */
    void doFilter(IConfigRequest request, IConfigResponse response) throws NacosException;
    
}
