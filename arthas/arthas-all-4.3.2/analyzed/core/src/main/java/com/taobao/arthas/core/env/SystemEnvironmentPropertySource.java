/*
 * Copyright 2002-2015 the original author or authors.
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

/**
 * 专用于系统环境变量的 {@link MapPropertySource} 子类。
 * <p>
 * 兼容 Shell 限制：环境变量名不能含点号/连字符，故 {@link #getProperty(String)} 会尝试
 * 原名、下划线替换、大写及组合变体（如 {@code foo.bar} → {@code FOO_BAR}）。
 * 便于通过 {@code SPRING_PROFILES_ACTIVE} 等形式指定 profile。
 *
 * <p>
 * For example, a call to {@code getProperty("foo.bar")} will attempt to find a
 * value for the original property or any 'equivalent' property, returning the
 * first found:
 * <ul>
 * <li>{@code foo.bar} - the original name</li>
 * <li>{@code foo_bar} - with underscores for periods (if any)</li>
 * <li>{@code FOO.BAR} - original, with upper case</li>
 * <li>{@code FOO_BAR} - with underscores and upper case</li>
 * </ul>
 * Any hyphen variant of the above would work as well, or even mix dot/hyphen
 * variants.
 *
 * <p>
 * The same applies for calls to {@link #containsProperty(String)}, which
 * returns {@code true} if any of the above properties are present, otherwise
 * {@code false}.
 *
 * <p>
 * This feature is particularly useful when specifying active or default
 * profiles as environment variables. The following is not allowable under Bash:
 *
 * <pre class="code">
 * spring.profiles.active=p1 java -classpath ... MyApp
 * </pre>
 *
 * However, the following syntax is permitted and is also more conventional:
 *
 * <pre class="code">
 * SPRING_PROFILES_ACTIVE=p1 java -classpath ... MyApp
 * </pre>
 *
 * <p>
 * Enable debug- or trace-level logging for this class (or package) for messages
 * explaining when these 'property name resolutions' occur.
 *
 * <p>
 * This property source is included by default in {@link StandardEnvironment}
 * and all its subclasses.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see StandardEnvironment
 * @see AbstractEnvironment#getSystemEnvironment()
 * @see AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
 */
public class SystemEnvironmentPropertySource extends MapPropertySource {

    /**
     * Create a new {@code SystemEnvironmentPropertySource} with the given name and
     * delegating to the given {@code MapPropertySource}.
     */
    public SystemEnvironmentPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    /**
     * Return {@code true} if a property with the given name or any
     * underscore/uppercase variant thereof exists in this property source.
     */
    @Override
    public boolean containsProperty(String name) {
        return (getProperty(name) != null);
    }

    /**
     * This implementation returns {@code true} if a property with the given name or
     * any underscore/uppercase variant thereof exists in this property source.
     */
    @Override
    public Object getProperty(String name) {
        String actualName = resolvePropertyName(name);
        return super.getProperty(actualName);
    }

    /**
     * Check to see if this property source contains a property with the given name,
     * or any underscore / uppercase variation thereof. Return the resolved name if
     * one is found or otherwise the original name. Never returns {@code null}.
     */
    /** 解析属性名：依次尝试原名、大写、点/连字符转下划线等变体，找到即返回 */
    protected final String resolvePropertyName(String name) {
        String resolvedName = checkPropertyName(name);
        if (resolvedName != null) {
            return resolvedName;
        }
        String uppercasedName = name.toUpperCase();
        if (!name.equals(uppercasedName)) {
            resolvedName = checkPropertyName(uppercasedName);
            if (resolvedName != null) {
                return resolvedName;
            }
        }
        return name;
    }

    /** 在底层 map 中按多种命名变体查找实际存在的键 */
    private String checkPropertyName(String name) {
        // 1. 原样匹配
        if (containsKey(name)) {
            return name;
        }
        // 2. 仅将点号替换为下划线
        String noDotName = name.replace('.', '_');
        if (!name.equals(noDotName) && containsKey(noDotName)) {
            return noDotName;
        }
        // 3. 仅将连字符替换为下划线
        String noHyphenName = name.replace('-', '_');
        if (!name.equals(noHyphenName) && containsKey(noHyphenName)) {
            return noHyphenName;
        }
        // 4. 点号与连字符均替换为下划线
        String noDotNoHyphenName = noDotName.replace('-', '_');
        if (!noDotName.equals(noDotNoHyphenName) && containsKey(noDotNoHyphenName)) {
            return noDotNoHyphenName;
        }
        // 5. 全部变体均未命中
        return null;
    }

    private boolean containsKey(String name) {
        return (isSecurityManagerPresent() ? this.source.keySet().contains(name) : this.source.containsKey(name));
    }

    /** 是否存在 SecurityManager，影响 containsKey 是否直接调 map.containsKey */
    protected boolean isSecurityManagerPresent() {
        return (System.getSecurityManager() != null);
    }

}
