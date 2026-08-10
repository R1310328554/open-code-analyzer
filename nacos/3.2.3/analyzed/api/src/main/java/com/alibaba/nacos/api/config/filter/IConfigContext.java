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
 * 配置过滤器上下文接口。
 *
 * <p>在 {@link IConfigFilter} 链式处理过程中传递键值参数，供各过滤器读写共享状态。</p>
 *
 * @author Nacos
 */
public interface IConfigContext {
    
    /**
     * 按键获取上下文参数。
     *
     * @param key 参数键
     * @return 参数值，不存在时返回 {@code null}
     */
    Object getParameter(String key);
    
    /**
     * 设置上下文参数。
     *
     * @param key   参数键
     * @param value 参数值
     */
    void setParameter(String key, Object value);
}
