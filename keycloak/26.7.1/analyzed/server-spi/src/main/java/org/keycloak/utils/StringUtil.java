/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.utils;

import java.util.Collection;

/**
 * 字符串工具类：提供空白判断、逻辑连接拼接、控制字符清理及空格规范化等方法。
 */
public class StringUtil {

    /**
     * 字符串为 {@code null} 或空白时返回 {@code true}。
     * Returns true if string is null or blank
     */
    public static boolean isBlank(String str) {
        return !(isNotBlank(str));
    }

    /**
     * 字符串非 {@code null} 且非空白时返回 {@code true}。
     * Returns true if string is not null and not blank
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }

    /**
     * 字符串为 {@code null} 或空串时返回 {@code true}。
     * Returns true if string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 用逗号分隔多个值，最后一项前插入逻辑连接词（如 "or"）。
     * 示例：{@code joinValuesWithLogicalCondition("or", Arrays.asList("foo", "bar", "baz", "caz"))}
     * 返回 {@code "foo, bar, baz or caz"}。
     *
     * Calling:
     * <pre>joinValuesWithLogicalCondition("or", Arrays.asList("foo", "bar", "baz", "caz" ))</pre>
     * will return "foo, bar, baz or caz"
     *
     * @param conditionText condition
     * @param values values to be joined with the condition at the end
     * @return see the example above
     */
    public static String joinValuesWithLogicalCondition(String conditionText, Collection<String> values) {
        StringBuilder options = new StringBuilder();
        int i = 1;
        for (String o : values) {
            if (i == values.size()) {
                options.append(" " + conditionText + " ");
            } else if (i > 1) {
                options.append(", ");
            }
            options.append(o);
            i++;
        }
        return options.toString();
    }

    /**
     * 移除 ANSI 转义序列与控制字符，防止日志注入攻击。
     * 不会解码合法的 URL 编码字符（如 {@code %20}），以保留重定向 URI 校验等场景的原始编码。
     *
     * Removes ANSI escape codes and control characters from a string to prevent log injection attacks.
     * This method:
     * 1. Removes URL-encoded ANSI escape sequences (e.g., %1B[31m)
     * 2. Removes literal ANSI escape sequences (e.g., \u001b[31m)
     * 3. Removes URL-encoded control characters (e.g., %0D, %0A, %7F)
     * 4. Removes any remaining literal control characters
     *
     * Note: This method does NOT decode legitimate URL-encoded characters (e.g., %20, %2F)
     * to preserve the original encoding for use cases like redirect URI validation.
     *
     * @param str The string to sanitize
     * @return The sanitized string without ANSI codes and control characters
     */
    public static String removeControlCharacters(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        str = str.replaceAll("\u001B\\[([0-9;]*)[a-zA-Z]", "");
        str = str.replaceAll("(?i)%1B\\[([0-9;]*)[a-zA-Z]", "");
        str = str.replaceAll("(?i)%0[0-9A-F]|(?i)%1[0-9A-F]|(?i)%7F", "");
        str = str.replaceAll("[\u0000-\u001F\u007F]+", "");
        str = str.replaceAll("\\[([0-9;]*)m", "");
        return str;
    }


    /**
     * 将各类空白字符（\t、\n、\r 等）规范为空格；若指定引号字符则对其转义。
     *
     * Utility method that substitutes any isWhitespace char to common space ' ' or character 20.
     * The idea is removing any weird space character in the string like \t, \n, \r.
     * If quotes character is passed the quotes char is escaped to mark is not the end
     * of the value (for example escaped \" if quotes char " is found in the string).
     *
     * @param str The string to normalize
     * @param quotes The quotes to escape (for example " or '). It can be null.
     * @return The string without weird whitespaces and quotes escaped
     */
    public static String sanitizeSpacesAndQuotes(String str, Character quotes) {
        // 思路借鉴 commons-lang StringUtils.normalizeSpace
        // idea taken from commons-lang StringUtils.normalizeSpace
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = null;
        for (int i = 0; i < str.length(); i++) {
            final char actualChar = str.charAt(i);
            if ((Character.isWhitespace(actualChar) && actualChar != ' ') || actualChar == 160) {
                if (sb == null) {
                    sb = new StringBuilder(str.length() + 10).append(str.substring(0, i));
                }
                sb.append(' ');
            } else if (quotes != null && actualChar == quotes) {
                if (sb == null) {
                    sb = new StringBuilder(str.length() + 10).append(str.substring(0, i));
                }
                sb.append('\\').append(actualChar);
            } else if (sb != null) {
                sb.append(actualChar);
            }
        }
        return sb == null? str : sb.toString();
    }

    /** 若字符串以指定后缀结尾则移除该后缀。
     * @param str 原字符串
     * @param suffix 要移除的后缀
     * @return 处理后的字符串 */
    public static String removeSuffix(String str, String suffix) {
        int index = str.lastIndexOf(suffix);
        if (str.endsWith(suffix) && index > 0) {
            str = str.substring(0, index);
        }
        return str;
    }
}
