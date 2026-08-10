/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.auth.context;

import com.alibaba.nacos.plugin.auth.api.IdentityContext;

/**
 * 身份上下文构建器接口。
 *
 * <p>将协议层请求对象转换为 {@link IdentityContext}，供鉴权插件使用。</p>
 *
 * @author wuyfee
 */
public interface IdentityContextBuilder<T> {
    
    /**
     * 从请求对象构建身份上下文。
     *
     * @param request 协议请求
     * @return 身份上下文
     */
    IdentityContext build(T request);
    
}
