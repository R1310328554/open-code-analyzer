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

import com.taobao.arthas.core.env.convert.ConfigurableConversionService;

/**
 * 可配置的属性解析器接口：在 {@link PropertyResolver} 基础上扩展类型转换、
 * 占位符语法与必填项校验等配置能力。
 * <p>
 * 大多数 {@link PropertyResolver} 实现（如 {@link AbstractPropertyResolver}）
 * 都会实现此接口。
 *
 * @author Chris Beams
 * @since 3.1
 */
public interface ConfigurablePropertyResolver extends PropertyResolver {

    /**
     * 返回用于属性值类型转换的 {@link ConfigurableConversionService}。
     * <p>
     * 可通过返回的服务动态增删 {@code Converter} 实例：
     * 
     * <pre class="code">
     * ConfigurableConversionService cs = env.getConversionService();
     * cs.addConverter(new FooConverter());
     * </pre>
     * 
     * @see PropertyResolver#getProperty(String, Class)
     * @see org.springframework.core.convert.converter.ConverterRegistry#addConverter
     */
    ConfigurableConversionService getConversionService();

    /**
     * 替换整个 {@link ConfigurableConversionService} 实例。
     * <p>
     * <strong>提示：</strong>通常只需通过 {@link #getConversionService()} 增删
     * 单个 {@code Converter}，无需整体替换。
     * 
     * @see PropertyResolver#getProperty(String, Class)
     * @see #getConversionService()
     * @see org.springframework.core.convert.converter.ConverterRegistry#addConverter
     */
    void setConversionService(ConfigurableConversionService conversionService);

    /**
     * 设置占位符前缀（默认 "${"）。
     */
    void setPlaceholderPrefix(String placeholderPrefix);

    /**
     * 设置占位符后缀（默认 "}"）。
     */
    void setPlaceholderSuffix(String placeholderSuffix);

    /**
     * 设置占位符与默认值的分隔符（默认 ":"）；传 {@code null} 禁用默认值语法。
     */
    void setValueSeparator(String valueSeparator);

    /**
     * 设置嵌套占位符无法解析时的行为。
     * <p>
     * {@code false} 表示严格模式（抛异常），{@code true} 表示保留 ${...} 原样。
     * {@link #getProperty(String)} 及其变体须据此决定解析策略。
     * 
     * @since 3.2
     */
    void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders);

    /**
     * 声明必填属性键，由 {@link #validateRequiredProperties()} 统一校验。
     */
    void setRequiredProperties(String... requiredProperties);

    /**
     * 校验 {@link #setRequiredProperties} 声明的所有键均存在且非 {@code null}。
     * 
     * @throws MissingRequiredPropertiesException 存在缺失的必填属性时抛出
     */
    void validateRequiredProperties() throws MissingRequiredPropertiesException;

}
