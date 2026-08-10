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
 * 属性源抽象基类，统一 JVM 参数、系统环境变量与内存 {@link Properties} 的读取接口。
 * <p>各具体实现通过 {@link #getType()} 标识来源，供 {@link SearchableProperties} 按优先级检索。</p>
 */
abstract class AbstractPropertySource {
    
    /**
     * 返回本属性源的类型标识。
     * @return 对应的 {@link SourceType}
     */
    abstract SourceType getType();
    
    /**
     * 按 key 读取字符串属性；不存在时返回 {@code null}。
     * @param key 属性键
     * @return 属性值，未命中时为 {@code null}
     */
    abstract String getProperty(String key);
    
    /**
     * 判断指定 key 是否存在于本属性源。
     * @param key 待检测的键
     * @return 存在返回 {@code true}，否则 {@code false}
     */
    abstract boolean containsKey(String key);
    
    /**
     * 将本属性源快照导出为 {@link Properties}。
     * @return 属性快照
     */
    abstract Properties asProperties();
    
}
