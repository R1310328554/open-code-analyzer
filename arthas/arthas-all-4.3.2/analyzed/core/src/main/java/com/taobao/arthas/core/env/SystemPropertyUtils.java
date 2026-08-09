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
 * 系统属性占位符解析工具，常用于文件路径等文本。
 * <p>
 * 将 {@code ${user.dir}} 等形式替换为 {@link System#getProperty(String)} 或
 * {@link System#getenv(String)} 的值；支持 {@code key:default} 默认值语法。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @since 1.2.5
 * @see #PLACEHOLDER_PREFIX
 * @see #PLACEHOLDER_SUFFIX
 * @see System#getProperty(String)
 */
public abstract class SystemPropertyUtils {

    /** 系统属性占位符前缀："${" */

    public static final String PLACEHOLDER_PREFIX = "${";

    /** 系统属性占位符后缀："}" */

    public static final String PLACEHOLDER_SUFFIX = "}";

    /** 占位符内键与默认值的分隔符：":" */

    public static final String VALUE_SEPARATOR = ":";

    /** 严格模式：无法解析的占位符抛异常 */
    private static final PropertyPlaceholderHelper strictHelper = new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX,
            PLACEHOLDER_SUFFIX, VALUE_SEPARATOR, false);

    /** 宽松模式：无法解析的占位符原样保留 */
    private static final PropertyPlaceholderHelper nonStrictHelper = new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX,
            PLACEHOLDER_SUFFIX, VALUE_SEPARATOR, true);

    /**
     * Resolve {@code ${...}} placeholders in the given text, replacing them with
     * corresponding system property values.
     * 
     * @param text the String to resolve
     * @return the resolved String
     * @throws IllegalArgumentException if there is an unresolvable placeholder
     * @see #PLACEHOLDER_PREFIX
     * @see #PLACEHOLDER_SUFFIX
     */
    public static String resolvePlaceholders(String text) {
        return resolvePlaceholders(text, false);
    }

    /**
     * Resolve {@code ${...}} placeholders in the given text, replacing them with
     * corresponding system property values. Unresolvable placeholders with no
     * default value are ignored and passed through unchanged if the flag is set to
     * {@code true}.
     * 
     * @param text                           the String to resolve
     * @param ignoreUnresolvablePlaceholders whether unresolved placeholders are to
     *                                       be ignored
     * @return the resolved String
     * @throws IllegalArgumentException if there is an unresolvable placeholder
     * @see #PLACEHOLDER_PREFIX
     * @see #PLACEHOLDER_SUFFIX and the "ignoreUnresolvablePlaceholders" flag is
     *      {@code false}
     */
    public static String resolvePlaceholders(String text, boolean ignoreUnresolvablePlaceholders) {
        PropertyPlaceholderHelper helper = (ignoreUnresolvablePlaceholders ? nonStrictHelper : strictHelper);
        return helper.replacePlaceholders(text, new SystemPropertyPlaceholderResolver(text));
    }

    /** 占位符解析器：先查系统属性，再回退到环境变量 */

    private static class SystemPropertyPlaceholderResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

        private final String text;

        public SystemPropertyPlaceholderResolver(String text) {
            this.text = text;
        }

        @Override
        public String resolvePlaceholder(String placeholderName) {
            try {
                String propVal = System.getProperty(placeholderName);
                if (propVal == null) {
                    // 系统属性未命中时回退到环境变量
                    propVal = System.getenv(placeholderName);
                }
                return propVal;
            } catch (Throwable ex) {
                // SecurityManager 等异常时打印 stderr 并返回 null
                System.err.println("Could not resolve placeholder '" + placeholderName + "' in [" + this.text
                        + "] as system property: " + ex);
                return null;
            }
        }
    }

}
