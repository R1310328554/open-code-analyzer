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
 * 属性源容器接口，聚合一个或多个 {@link PropertySource}。
 * <p>
 * 实现类（如 {@link MutablePropertySources}）维护搜索顺序；
 * {@link PropertySourcesPropertyResolver} 按迭代顺序解析属性。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see PropertySource
 */
public interface PropertySources extends Iterable<PropertySource<?>> {

    /**
     * 判断是否包含指定名称的属性源。
     * 
     * @param name 待查找的 {@linkplain PropertySource#getName() 属性源名称}
     */
    boolean contains(String name);

    /**
     * 按名称获取属性源，未找到返回 {@code null}。
     * 
     * @param name 待查找的 {@linkplain PropertySource#getName() 属性源名称}
     */
    PropertySource<?> get(String name);

}
