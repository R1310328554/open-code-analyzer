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

package com.alibaba.nacos.client.config.filter.impl;

import com.alibaba.nacos.api.config.filter.IConfigContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置过滤器上下文，在过滤链中传递扩展参数。
 *
 * <p>实现 {@link IConfigContext}，供各 {@link IConfigFilter} 读写临时数据。</p>
 *
 * @author Nacos
 */
public class ConfigContext implements IConfigContext {
    
    /** 上下文键值存储。 */
    private final Map<String, Object> param = new HashMap<>();
    
    @Override
    /** 按键获取上下文参数。 */
    public Object getParameter(String key) {
        return param.get(key);
    }
    
    @Override
    /** 设置上下文参数。 */
    public void setParameter(String key, Object value) {
        param.put(key, value);
    }
    
}
