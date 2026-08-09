/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.arthas.core.env;

import java.util.Map;

import org.apache.logging.log4j.util.PropertiesPropertySource;

/**
 * 基于 {@code Map} 的 {@link PropertySource} 实现。
 * <p>
 * 直接从 {@code Map<String, Object>} 读取键值；{@link #containsProperty(String)} 与
 * {@link #getPropertyNames()} 分别委托 {@code containsKey} 与 {@code keySet}。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see PropertiesPropertySource
 */
public class MapPropertySource extends EnumerablePropertySource<Map<String, Object>> {

    public MapPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    @Override
    public Object getProperty(String name) {
        // 直接 map 查找，不存在则返回 null
        return this.source.get(name);
    }

    @Override
    public boolean containsProperty(String name) {
        // 比父类遍历 getPropertyNames 更高效
        return this.source.containsKey(name);
    }

    @Override
    public String[] getPropertyNames() {
        // 将 keySet 转为 String 数组供枚举
        return this.source.keySet().toArray(new String[0]);
    }

}
