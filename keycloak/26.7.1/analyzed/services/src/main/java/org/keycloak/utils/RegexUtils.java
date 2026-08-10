/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 正则表达式校验与匹配工具类。
 * <p>提供语法校验、长度限制及 ReDoS 防护。</p>
 */
public class RegexUtils {

    /** 默认正则最大长度，用于限制复杂度、缓解 ReDoS 攻击。 */
    public static final int DEFAULT_MAX_LENGTH = 512;

    /**
     * 校验正则表达式语法是否合法（默认最大长度 {@link #DEFAULT_MAX_LENGTH}，允许分组）。
     *
     * @param regexp 待校验的正则表达式
     * @return 合法返回 {@code true}，否则 {@code false}
     */
    public static boolean isValidRegex(String regexp) {
        return isValidRegex(regexp, DEFAULT_MAX_LENGTH, true);
    }

    /**
     * 校验正则表达式语法是否合法。
     *
     * @param regexp 待校验的正则表达式
     * @param maxLength 允许的最大长度
     * @param allowGroups 是否允许捕获/非捕获分组（括号）
     * @return 合法返回 {@code true}；为 {@code null}、超长、含禁用分组或语法错误时返回 {@code false}
     */
    public static boolean isValidRegex(String regexp, int maxLength, boolean allowGroups) {
        if (regexp == null || regexp.length() > maxLength) {
            return false;
        }
        if (!allowGroups && (regexp.contains("(") || regexp.contains(")"))) {
            return false;
        }
        try {
            Pattern.compile(regexp);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    /**
     * 判断值是否匹配给定正则；{@link List} 时任一元素匹配即返回 true。
     *
     * @param regex 正则表达式
     * @param value 待匹配值（可为 List）
     * @return 匹配返回 {@code true}
     */
    public static boolean valueMatchesRegex(String regex, Object value) {
        if (value instanceof List) {
            List list = (List) value;
            for (Object val : list) {
                if (valueMatchesRegex(regex, val)) {
                    return true;
                }
            }
        } else {
            if (value != null) {
                String stringValue = value.toString();
                return stringValue != null && stringValue.matches(regex);
            }
        }
        return false;
    }
}
