/*
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
 */

package com.alibaba.nacos.plugin.auth.api;

import java.util.HashMap;
import java.util.Map;

/**
 * 身份上下文，在认证/鉴权链路中传递请求侧提取的身份参数。
 *
 * <p>以键值对形式保存从 HTTP 请求、Token 或插件中间结果解析出的身份字段，
 * 供后续鉴权插件读取与扩展。</p>
 *
 * @author Wuyfee
 */
public class IdentityContext {
    
    /**
     * 从请求或上游插件写入的上下文参数表。
     */
    private final Map<String, Object> param = new HashMap<>();
    
    /**
     * 按键获取上下文参数。
     *
     * @param key 参数键
     * @return 参数值，不存在时返回 {@code null}
     */
    public Object getParameter(String key) {
        return param.get(key);
    }
    
    /**
     * 按键获取身份参数，支持类型转换与默认值回退。
     *
     * @param key          身份参数名
     * @param defaultValue 当值为 {@code null} 或类型不匹配时使用的默认值（不可为 {@code null}）
     * @param <T>          期望的返回值类型
     * @return 转换后的身份值，或默认值
     */
    public <T> T getParameter(String key, T defaultValue) {
        if (null == defaultValue) {
            throw new IllegalArgumentException(
                "defaultValue can't be null. Please use #getParameter(String key) replace");
        }
        try {
            Object result = param.get(key);
            if (null != result) {
                return (T) defaultValue.getClass().cast(result);
            }
            return defaultValue;
        } catch (ClassCastException exception) {
            return defaultValue;
        }
    }
    
    /**
     * 写入身份上下文键值对。
     *
     * @param key   参数键
     * @param value 参数值
     */
    public void setParameter(String key, Object value) {
        param.put(key, value);
    }
}
