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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.taobao.arthas.core.env.convert.ConfigurableConversionService;
import com.taobao.arthas.core.env.convert.DefaultConversionService;

/**
 * 属性解析器的抽象基类，提供占位符替换、类型转换与必填项校验等通用能力。
 * <p>
 * 子类只需实现 {@link #getPropertyAsRawString(String)} 即可对接任意底层属性源
 * （如 {@link PropertySourcesPropertyResolver}）。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
public abstract class AbstractPropertyResolver implements ConfigurablePropertyResolver {

    /** 属性值类型转换服务，默认使用 {@link DefaultConversionService} */
    protected ConfigurableConversionService conversionService = new DefaultConversionService();

    /** 非严格占位符助手（无法解析时保留原样） */
    private PropertyPlaceholderHelper nonStrictHelper;

    /** 严格占位符助手（无法解析时抛异常） */
    private PropertyPlaceholderHelper strictHelper;

    /** 嵌套占位符无法解析时是否忽略（默认 false，即严格模式） */
    private boolean ignoreUnresolvableNestedPlaceholders = false;

    /** 占位符前缀，默认 "${" */
    private String placeholderPrefix = SystemPropertyUtils.PLACEHOLDER_PREFIX;

    /** 占位符后缀，默认 "}" */
    private String placeholderSuffix = SystemPropertyUtils.PLACEHOLDER_SUFFIX;

    /** 占位符与默认值的分隔符，默认 ":" */
    private String valueSeparator = SystemPropertyUtils.VALUE_SEPARATOR;

    /** 必填属性键集合，由 {@link #validateRequiredProperties()} 校验 */
    private final Set<String> requiredProperties = new LinkedHashSet<String>();

    public ConfigurableConversionService getConversionService() {
        return this.conversionService;
    }

    public void setConversionService(ConfigurableConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * 设置占位符前缀。
     * <p>
     * 默认值为 "${"。
     * 
     * @see org.springframework.util.SystemPropertyUtils#PLACEHOLDER_PREFIX
     */
    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * 设置占位符后缀。
     * <p>
     * 默认值为 "}"。
     * 
     * @see org.springframework.util.SystemPropertyUtils#PLACEHOLDER_SUFFIX
     */
    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * 设置占位符与默认值之间的分隔符；传 {@code null} 表示不处理默认值语法。
     * <p>
     * 默认值为 ":"。
     * 
     * @see org.springframework.util.SystemPropertyUtils#VALUE_SEPARATOR
     */
    @Override
    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    /**
     * 设置嵌套占位符无法解析时的行为。
     * <p>
     * {@code false}（默认）表示严格解析，无法解析则抛异常；
     * {@code true} 表示保留未解析的 ${...} 原样。
     * 
     * @since 3.2
     */
    @Override
    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        this.ignoreUnresolvableNestedPlaceholders = ignoreUnresolvableNestedPlaceholders;
    }

    @Override
    public void setRequiredProperties(String... requiredProperties) {
        this.requiredProperties.addAll(Arrays.asList(requiredProperties));
    }

    @Override
    public void validateRequiredProperties() {
        MissingRequiredPropertiesException ex = new MissingRequiredPropertiesException();
        for (String key : this.requiredProperties) {
            if (this.getProperty(key) == null) {
                ex.addMissingRequiredProperty(key);
            }
        }
        if (!ex.getMissingRequiredProperties().isEmpty()) {
            throw ex;
        }
    }

    @Override
    public boolean containsProperty(String key) {
        return (getProperty(key) != null);
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, String.class);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value != null ? value : defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        T value = getProperty(key, targetType);
        return (value != null ? value : defaultValue);
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        String value = getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Required key '" + key + "' not found");
        }
        return value;
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> valueType) throws IllegalStateException {
        T value = getProperty(key, valueType);
        if (value == null) {
            throw new IllegalStateException("Required key '" + key + "' not found");
        }
        return value;
    }

    @Override
    public String resolvePlaceholders(String text) {
        if (this.nonStrictHelper == null) {
            this.nonStrictHelper = createPlaceholderHelper(true);
        }
        return doResolvePlaceholders(text, this.nonStrictHelper);
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        if (this.strictHelper == null) {
            this.strictHelper = createPlaceholderHelper(false);
        }
        return doResolvePlaceholders(text, this.strictHelper);
    }

    /**
     * 解析字符串中的嵌套占位符，行为取决于 {@link #setIgnoreUnresolvableNestedPlaceholders}。
     * <p>
     * 由 {@link #getProperty} 及其变体隐式调用；与 {@link #resolvePlaceholders}、
     * {@link #resolveRequiredPlaceholders} 不同，后者各自处理无法解析的情况。
     * 
     * @since 3.2
     * @see #setIgnoreUnresolvableNestedPlaceholders
     */
    protected String resolveNestedPlaceholders(String value) {
        return (this.ignoreUnresolvableNestedPlaceholders ? resolvePlaceholders(value)
                : resolveRequiredPlaceholders(value));
    }

    private PropertyPlaceholderHelper createPlaceholderHelper(boolean ignoreUnresolvablePlaceholders) {
        return new PropertyPlaceholderHelper(this.placeholderPrefix, this.placeholderSuffix, this.valueSeparator,
                ignoreUnresolvablePlaceholders);
    }

    private String doResolvePlaceholders(String text, PropertyPlaceholderHelper helper) {
        return helper.replacePlaceholders(text, new PropertyPlaceholderHelper.PlaceholderResolver() {
            public String resolvePlaceholder(String placeholderName) {
                return getPropertyAsRawString(placeholderName);
            }
        });
    }

    /**
     * 获取指定属性的原始字符串值，不进行嵌套占位符解析。
     * 
     * @param key 属性名
     * @return 属性值，未找到返回 {@code null}
     */
    protected abstract String getPropertyAsRawString(String key);

}
