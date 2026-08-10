/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.utils;

/**
 * 字符串工具类（源自 Apache Commons Lang3）。
 *
 * <p>提供空值判断、空白检测、裁剪与比较等常用操作，供 Nacos API 层复用。</p>
 *
 * @author <a href="mailto:lin-mt@outlook.com">lin-mt</a>
 */
public class StringUtils {
    
    /** 空字符串常量 {@code ""}。
     *
     * @since 2.0
     */
    public static final String EMPTY = "";
    
    /**
     * <p>判断字符序列是否为 {@code null} 或长度为 0。</p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the CharSequence. That functionality is available in isBlank().</p>
     *
     * @param cs 待检查的字符序列，可为 {@code null}
     * @return 为空或 {@code null} 时返回 {@code true}
     * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
    
    /**
     * <p>判断字符序列是否为 {@code null}、空串或仅含空白字符。</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs 待检查的字符序列，可为 {@code null}
     * @return 为 {@code null}、空串或纯空白时返回 {@code true}
     * @since 2.0
     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
     */
    public static boolean isBlank(final CharSequence cs) {
        final int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    // 裁剪
    //-----------------------------------------------------------------------
    
    /**
     * <p>去除字符串首尾控制字符（{@code char <= 32}），{@code null} 输入返回 {@code null}。</p>
     *
     * <p>The String is trimmed using {@link String#trim()}.
     * Trim removes start and end characters &lt;= 32.</p>
     *
     * <pre>
     * StringUtils.trim(null)          = null
     * StringUtils.trim("")            = ""
     * StringUtils.trim("     ")       = ""
     * StringUtils.trim("abc")         = "abc"
     * StringUtils.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str 待裁剪字符串，可为 {@code null}
     * @return 裁剪结果，输入为 {@code null} 时返回 {@code null}
     */
    public static String trim(final String str) {
        return str == null ? null : str.trim();
    }
    
    // 相等比较
    //-----------------------------------------------------------------------
    
    /**
     * <p>比较两个字符序列是否逐字符相等（区分大小写）。</p>
     *
     * <p>{@code null}s are handled without exceptions. Two {@code null}
     * references are considered to be equal. The comparison is case sensitive.</p>
     *
     * <pre>
     * StringUtils.equals(null, null)   = true
     * StringUtils.equals(null, "abc")  = false
     * StringUtils.equals("abc", null)  = false
     * StringUtils.equals("abc", "abc") = true
     * StringUtils.equals("abc", "ABC") = false
     * </pre>
     *
     * @param cs1 第一个字符序列，可为 {@code null}
     * @param cs2 第二个字符序列，可为 {@code null}
     * @return 两者相等或均为 {@code null} 时返回 {@code true}
     * @see Object#equals(Object)
     * @since 3.0 Changed signature from equals(String, String) to equals(CharSequence, CharSequence)
     */
    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        return StringUtils.regionMatches(cs1, false, 0, cs2, 0,
            Math.max(cs1.length(), cs2.length()));
    }
    
    /**
     * {@link String#regionMatches} 的轻量实现，支持 {@link CharSequence}。
     *
     * @param cs         源字符序列
     * @param ignoreCase 是否忽略大小写
     * @param thisStart  源序列起始偏移
     * @param substring  待匹配子序列
     * @param start      子序列起始偏移
     * @param length     匹配区域长度
     * @return 区域匹配成功返回 {@code true}
     */
    public static boolean regionMatches(final CharSequence cs, final boolean ignoreCase,
        final int thisStart,
        final CharSequence substring, final int start, final int length) {
        if (cs instanceof String && substring instanceof String) {
            return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start,
                length);
        }
        int index1 = thisStart;
        int index2 = start;
        int tmpLen = length;
        
        while (tmpLen-- > 0) {
            final char c1 = cs.charAt(index1++);
            final char c2 = substring.charAt(index2++);
            
            if (c1 == c2) {
                continue;
            }
            
            if (!ignoreCase) {
                return false;
            }
            
            // 与 String.regionMatches() 相同的大小写折叠比较：
            if (Character.toUpperCase(c1) != Character.toUpperCase(c2)
                && Character.toLowerCase(c1) != Character
                    .toLowerCase(c2)) {
                return false;
            }
        }
        
        return true;
    }
}
