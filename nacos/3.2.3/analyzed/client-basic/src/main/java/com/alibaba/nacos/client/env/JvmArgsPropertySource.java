/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.env;

import java.util.Properties;

/**
 * 基于 {@link System#getProperties()} 的 JVM 启动参数属性源（{@code -Dkey=value}）。
 * <p>类型为 {@link SourceType#JVM}，优先级通常低于显式设置的 {@link PropertiesPropertySource}。</p>
 */
class JvmArgsPropertySource extends AbstractPropertySource {
    
    /** JVM 系统属性快照引用（与 {@link System#getProperties()} 共享同一实例） */
    private final Properties properties;
    
    /** 构造时绑定当前 JVM 系统属性表。 */
    JvmArgsPropertySource() {
        this.properties = System.getProperties();
    }
    
    @Override
    SourceType getType() {
        return SourceType.JVM;
    }
    
    @Override
    String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    @Override
    boolean containsKey(String key) {
        return properties.containsKey(key);
    }
    
    /** {@inheritDoc} 返回系统属性的 defensive copy，避免外部修改影响全局表。 */
    @Override
    Properties asProperties() {
        Properties properties = new Properties();
        properties.putAll(this.properties);
        return properties;
    }
}
