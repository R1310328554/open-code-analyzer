/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.core.v2.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端扩展属性容器。
 *
 * <p>以键值对形式存储客户端同步与连接相关的附加信息，支持带默认值的类型安全读取。</p>
 *
 * @author xiweng.yy
 */
public class ClientAttributes implements Serializable {
    
    private static final long serialVersionUID = -5794675800507288793L;
    
    /** 属性键值映射表。 */
    private Map<String, Object> clientAttributes;
    
    /** 初始化空的属性映射。 */
    public ClientAttributes() {
        this.clientAttributes = new HashMap<>(1);
    }
    
    public Map<String, Object> getClientAttributes() {
        return clientAttributes;
    }
    
    public void setClientAttributes(Map<String, Object> clientAttributes) {
        this.clientAttributes = clientAttributes;
    }
    
    /** 添加或覆盖单个客户端属性。 */
    public void addClientAttribute(String key, Object value) {
        clientAttributes.put(key, value);
    }
    
    /**
     * 按键获取客户端属性并尝试类型转换。
     *
     * @param key 属性键
     * @param <T> 期望的属性类型
     * @return 属性值；不存在或类型不匹配时返回 {@code null}
     */
    public <T> T getClientAttribute(String key) {
        try {
            return (T) clientAttributes.get(key);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 按键获取客户端属性，失败时返回默认值。
     *
     * @param key          属性键
     * @param <T>          期望的属性类型
     * @param defaultValue 不存在或类型不匹配时的默认值
     * @return 属性值或默认值
     */
    public <T> T getClientAttribute(String key, T defaultValue) {
        Object result = clientAttributes.get(key);
        if (null == result) {
            return defaultValue;
        }
        try {
            return (T) result;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
