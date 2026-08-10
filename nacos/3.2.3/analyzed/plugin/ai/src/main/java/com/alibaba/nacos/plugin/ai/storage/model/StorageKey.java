/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.ai.storage.model;

/**
 * AI 资源存储抽象的统一存储键。
 *
 * <p>类似 Nacos 对 dataId/group/tenant 的封装，向上层提供一致结构；
 * 对具体实现而言，它是携带 provider 标识的不透明键。</p>
 *
 * @author mosong.lp
 * @since 3.2.0
 */
public class StorageKey {
    
    /**
     * 存储 provider 标识，例如 {@code "nacos_config"}、{@code "oss"}。
     * 与 Storage JSON 中的 provider 字段对应。
     */
    private String provider;
    
    /**
     * 具体存储实现使用的内部键，对上层不透明，例如：
     * <ul>
     *   <li>nacos_config: {@code "namespace:group:dataId"}</li>
     *   <li>oss: {@code "bucket/objectPath"}</li>
     * </ul>
     */
    private String key;
    
    /** 无参构造。 */
    public StorageKey() {
    }
    
    /**
     * 构造指定 provider 与内部键的存储键。
     *
     * @param provider 存储 provider 标识
     * @param key      实现相关的内部键
     */
    public StorageKey(String provider, String key) {
        this.provider = provider;
        this.key = key;
    }
    
    /** @return 存储 provider 标识 */
    public String getProvider() {
        return provider;
    }
    
    /** @param provider 存储 provider 标识 */
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    /** @return 实现相关的内部键 */
    public String getKey() {
        return key;
    }
    
    /** @param key 实现相关的内部键 */
    public void setKey(String key) {
        this.key = key;
    }
    
    @Override
    public String toString() {
        return "StorageKey{provider='" + provider + "', key='" + key + "'}";
    }
}
