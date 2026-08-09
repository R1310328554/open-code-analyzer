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

/**
 * 可枚举的 {@link PropertySource} 抽象基类。
 * <p>
 * 能够遍历底层 source 对象，列出全部属性名/值对；对外暴露 {@link #getPropertyNames()}，
 * 调用方无需直接访问底层对象即可 introspect 可用属性。
 * 同时使 {@link #containsProperty(String)} 可通过遍历属性名数组实现，
 * 避免昂贵的 {@link #getProperty(String)} 调用；实现类可缓存 {@link #getPropertyNames()} 结果以优化性能。
 * <p>
 * 多数框架内置 {@code PropertySource} 均可枚举；反例如 JNDI 属性源，
 * 因 JNDI 特性无法在任意时刻确定全部属性名，只能逐键 {@link #getProperty(String)} 探测是否存在。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @param <T> 底层 source 类型
 */
public abstract class EnumerablePropertySource<T> extends PropertySource<T> {

    public EnumerablePropertySource(String name, T source) {
        super(name, source);
    }

    protected EnumerablePropertySource(String name) {
        super(name);
    }

    /**
     * 判断本属性源是否包含指定名称的属性。
     * <p>
     * 默认实现遍历 {@link #getPropertyNames()} 返回的数组进行匹配。
     * 
     * @param name 待查找的属性名
     */
    @Override
    public boolean containsProperty(String name) {
        // 遍历已枚举的属性名，线性查找
        String[] propertyNames = getPropertyNames();
        if (propertyNames == null) {
            return false;
        }
        for (String temp : propertyNames) {
            if (temp.equals(name)) {
                return true;
            }
        }
        return false;

    }

    /**
     * 返回底层 {@linkplain #getSource() source} 对象中全部属性名（永不为 {@code null}）。
     */
    public abstract String[] getPropertyNames();

}
