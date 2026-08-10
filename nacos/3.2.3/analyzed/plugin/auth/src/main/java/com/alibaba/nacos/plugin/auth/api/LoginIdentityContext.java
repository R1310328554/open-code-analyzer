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
import java.util.Set;

/**
 * 登录身份上下文，用于在客户端与 Nacos 服务端之间传递登录后的凭证信息。
 *
 * <p>与 {@link IdentityContext} 不同，本类仅存储字符串键值对，专用于客户端认证插件
 * 在 HTTP 请求头或参数中携带 token、用户名等登录态数据。</p>
 *
 * @author Nacos
 */
public class LoginIdentityContext {
    
    /**
     * 存储登录身份参数的键值映射。
     */
    private final Map<String, String> param = new HashMap<>();
    
    /**
     * 根据键获取登录身份参数值。
     *
     * @param key 参数键名
     * @return 对应的参数值，不存在时返回 {@code null}
     */
    public String getParameter(String key) {
        return param.get(key);
    }
    
    /**
     * 根据键获取登录身份参数值；若值为空则返回默认值。
     *
     * @param key          参数键名
     * @param defaultValue 当值为 {@code null} 时使用的默认值
     * @return 参数值或默认值
     */
    public String getParameter(String key, String defaultValue) {
        String val = param.get(key);
        return val == null ? defaultValue : val;
    }
    
    /**
     * 设置单个登录身份参数。
     *
     * @param key   参数键名
     * @param value 参数值
     */
    public void setParameter(String key, String value) {
        param.put(key, value);
    }
    
    /**
     * 批量写入登录身份参数。
     *
     * @param parameters 待合并的参数映射
     */
    public void setParameters(Map<String, String> parameters) {
        param.putAll(parameters);
    }
    
    /**
     * 获取所有已设置的参数键名集合。
     *
     * @return 参数键名集合
     */
    public Set<String> getAllKey() {
        return param.keySet();
    }
    
}
