/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.context.addition;

import com.alibaba.nacos.common.utils.VersionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 引擎上下文：保存 Nacos 服务端版本及运行环境相关的键值扩展，供日志、监控与插件读取。
 * Nacos engine context, to store some environment and engine information context. Such as version or system information.
 *
 * @author xiweng.yy
 */
public class EngineContext {
    
    /** Nacos 服务端版本号，例如 v2.4.0。 */
    private String version;
    
    /** 可扩展的引擎级字符串键值表。 */
    private final Map<String, String> contexts;
    
    /** 以当前构建版本初始化并创建扩展映射。 */
    public EngineContext() {
        version = VersionUtils.version;
        contexts = new HashMap<>(1);
    }
    
    /** 返回服务端版本。 */
    public String getVersion() {
        return version;
    }
    
    /** 覆盖服务端版本（测试或定制场景）。 */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /** 按 key 读取扩展值，不存在返回 null。 */
    public String getContext(String key) {
        return contexts.get(key);
    }
    
    /** 按 key 读取扩展值，不存在返回默认值。 */
    public String getContext(String key, String defaultValue) {
        return contexts.getOrDefault(key, defaultValue);
    }
    
    /** 写入或更新扩展键值。 */
    public void setContext(String key, String value) {
        contexts.put(key, value);
    }
}
