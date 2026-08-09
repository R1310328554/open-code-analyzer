/*
 * Copyright 2002-2018 the original author or authors.
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 只读 {@code Map<String, String>}，底层为系统属性或环境变量。
 * <p>
 * 当 {@link SecurityManager} 禁止访问 {@link System#getProperties()} 或
 * {@link System#getenv()} 时使用；{@link #keySet()}/{@link #entrySet()}/{@link #values()}
 * 恒返回空集合，但 {@link #get(Object)} 仍可能按单键权限返回值。
 *
 * @author Arjen Poutsma
 * @author Chris Beams
 * @since 3.0
 */
abstract class ReadOnlySystemAttributesMap implements Map<String, String> {

    @Override
    public boolean containsKey(Object key) {
        return (get(key) != null);
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if
     * this map contains no mapping for the key.
     * 
     * @param key the name of the system attribute to retrieve
     * @throws IllegalArgumentException if given key is non-String
     */
    @Override
    public String get(Object key) {
        if (!(key instanceof String)) {
            throw new IllegalArgumentException(
                    "Type of key [" + key.getClass().getName() + "] must be java.lang.String");
        }
        return getSystemAttribute((String) key);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Template method that returns the underlying system attribute.
     * <p>
     * Implementations typically call {@link System#getProperty(String)} or
     * {@link System#getenv(String)} here.
     */
    /**
     * 模板方法：读取单个系统属性或环境变量。
     * <p>实现类通常调用 {@link System#getProperty(String)} 或 {@link System#getenv(String)}。
     */
    protected abstract String getSystemAttribute(String attributeName);

    // 以下为 Map 变更操作，只读实现一律不支持

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        // SecurityManager 下无法枚举全部键，返回空集
        return Collections.emptySet();
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> values() {
        return Collections.emptySet();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return Collections.emptySet();
    }

}
