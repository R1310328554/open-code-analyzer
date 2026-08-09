/*
 * Copyright 2002-2016 the original author or authors.
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
 * 属性解析器接口，抽象从任意底层源读取配置的能力。
 * <p>
 * 提供存在性检测、带默认值/类型转换的取值、必填属性校验及
 * {@code ${...}} 占位符解析；{@link Environment} 继承本接口。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see Environment
 * @see PropertySourcesPropertyResolver
 */
public interface PropertyResolver {

    /** 判断指定键是否可解析（值非 {@code null}） */

    boolean containsProperty(String key);

    /**
     * 解析字符串属性，无法解析时返回 {@code null}。
     * 
     * @param key 属性名
     * @see #getProperty(String, String)
     * @see #getProperty(String, Class)
     * @see #getRequiredProperty(String)
     */
    String getProperty(String key);

    /**
     * 解析属性，未找到时返回 {@code defaultValue}。
     * 
     * @param key 属性名
     * @param defaultValue 默认值
     * @see #getRequiredProperty(String)
     * @see #getProperty(String, Class)
     */
    String getProperty(String key, String defaultValue);

    /**
     * Return the property value associated with the given key, or {@code null} if
     * the key cannot be resolved.
     * 
     * @param key        the property name to resolve
     * @param targetType the expected type of the property value
     * @see #getRequiredProperty(String, Class)
     */
    <T> T getProperty(String key, Class<T> targetType);

    /**
     * Return the property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     * 
     * @param key          the property name to resolve
     * @param targetType   the expected type of the property value
     * @param defaultValue the default value to return if no value is found
     * @see #getRequiredProperty(String, Class)
     */
    <T> T getProperty(String key, Class<T> targetType, T defaultValue);

    /**
     * Return the property value associated with the given key (never {@code null}).
     * 
     * @throws IllegalStateException if the key cannot be resolved
     * @see #getRequiredProperty(String, Class)
     */
    String getRequiredProperty(String key) throws IllegalStateException;

    /**
     * Return the property value associated with the given key, converted to the
     * given targetType (never {@code null}).
     * 
     * @throws IllegalStateException if the given key cannot be resolved
     */
    <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;

    /**
     * 宽松解析文本中的 {@code ${...}} 占位符，无法解析且无默认值时原样保留。
     * 
     * @param text 待解析文本
     * @return 解析后的字符串（永不为 {@code null}）
     * @throws IllegalArgumentException 若 text 为 {@code null}
     * @see #resolveRequiredPlaceholders
     * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders(String)
     */
    String resolvePlaceholders(String text);

    /**
     * 严格解析占位符，任一占位符无法解析则抛出 {@link IllegalArgumentException}。
     * 
     * @return 解析后的字符串（永不为 {@code null}）
     * @throws IllegalArgumentException text 为 null 或存在无法解析的占位符
     * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders(String,
     *      boolean)
     */
    String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}
