/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taobao.arthas.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

/**
 * 字符串、类名、修饰符与命令参数解析的通用工具集。
 * <p>
 * 融合 Arthas 特有逻辑（类名规范化、方法签名拆分、字节数可读化等）
 * 与 Spring/Apache Commons 风格的文本处理方法，供命令输出与 CLI 解析复用。
 */
public abstract class StringUtils {
    /** 随机字符串采样字符集（大小写字母 + 数字）。 */
    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 获取异常的原因描述
     *
     * @param t 异常
     * @return 异常原因
     */
    public static String cause(Throwable t) {
        if (null != t.getCause()) {
            return cause(t.getCause());
        }
        return t.getMessage();
    }

    /**
     * 将一个对象转换为字符串
     *
     * @param obj 目标对象
     * @return 字符串
     */
    public static String objectToString(Object obj) {
        if (null == obj) {
            return Constants.EMPTY_STRING;
        }
        try {
            return obj.toString();
        } catch (Throwable t) {
            logger.error("objectToString error, obj class: {}", obj.getClass(), t);
            return "ERROR DATA!!! Method toString() throw exception. obj class: " + obj.getClass()
                    + ", exception class: " + t.getClass()
                    + ", exception message: " + t.getMessage();
        }
    }

    /**
     * 翻译类名称
     *
     * @param clazz Java类
     * @return 翻译值
     */
    public static String classname(Class<?> clazz) {
        if (clazz.isArray()) {
            StringBuilder sb = new StringBuilder(clazz.getName());
            sb.delete(0, 2);
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ';') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("[]");
            return sb.toString();
        } else {
            return clazz.getName();
        }
    }

    /**
     * 翻译类名称<br/>
     * 将 java/lang/String 的名称翻译成 java.lang.String
     *
     * @param className 类名称 java/lang/String
     * @return 翻译后名称 java.lang.String
     */
    public static String normalizeClassName(String className) {
        return StringUtils.replace(className, "/", ".");
    }

    /** 以分隔符拼接多个类型的可读类名。 */
    public static String concat(String separator, Class<?>... types) {
        if (types == null || types.length == 0) {
            return Constants.EMPTY_STRING;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            builder.append(classname(types[i]));
            if (i < types.length - 1) {
                builder.append(separator);
            }
        }

        return builder.toString();
    }

    /** 以分隔符拼接多个字符串。 */
    public static String concat(String separator, String... strs) {
        if (strs == null || strs.length == 0) {
            return Constants.EMPTY_STRING;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            builder.append(strs[i]);
            if (i < strs.length - 1) {
                builder.append(separator);
            }
        }

        return builder.toString();
    }

    /**
     * 翻译Modifier值
     *
     * @param mod modifier
     * @return 翻译值
     */
    public static String modifier(int mod, char splitter) {
        StringBuilder sb = new StringBuilder();
        if (Modifier.isAbstract(mod)) {
            sb.append("abstract").append(splitter);
        }
        if (Modifier.isFinal(mod)) {
            sb.append("final").append(splitter);
        }
        if (Modifier.isInterface(mod)) {
            sb.append("interface").append(splitter);
        }
        if (Modifier.isNative(mod)) {
            sb.append("native").append(splitter);
        }
        if (Modifier.isPrivate(mod)) {
            sb.append("private").append(splitter);
        }
        if (Modifier.isProtected(mod)) {
            sb.append("protected").append(splitter);
        }
        if (Modifier.isPublic(mod)) {
            sb.append("public").append(splitter);
        }
        if (Modifier.isStatic(mod)) {
            sb.append("static").append(splitter);
        }
        if (Modifier.isStrict(mod)) {
            sb.append("strict").append(splitter);
        }
        if (Modifier.isSynchronized(mod)) {
            sb.append("synchronized").append(splitter);
        }
        if (Modifier.isTransient(mod)) {
            sb.append("transient").append(splitter);
        }
        if (Modifier.isVolatile(mod)) {
            sb.append("volatile").append(splitter);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 自动换行
     *
     * @param string 字符串
     * @param width  行宽
     * @return 换行后的字符串
     */
    public static String wrap(String string, int width) {
        final StringBuilder sb = new StringBuilder();
        final char[] buffer = string.toCharArray();
        int count = 0;
        for (char c : buffer) {

            if (count == width) {
                count = 0;
                sb.append('\n');
                if (c == '\n') {
                    continue;
                }
            }

            if (c == '\n') {
                count = 0;
            } else {
                count++;
            }

            sb.append(c);
        }
        return sb.toString();
    }

    /** 重复填充时的最大展开长度上限。 */

    private static final int PAD_LIMIT = 8192;

    /** 表示 indexOf 类搜索未找到时的返回值。 */

    public static final int INDEX_NOT_FOUND = -1;

    /** 工具类不可实例化。 */
    public StringUtils() {
    }

    /** 判断对象是否为 null 或空字符串。 */
    public static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

    /** 字符序列非 null 且长度大于 0。 */
    public static boolean hasLength(CharSequence str) {
        return str != null && str.length() > 0;
    }

    /** {@link #hasLength(CharSequence)} 的 String 重载。 */
    public static boolean hasLength(String str) {
        return hasLength((CharSequence)str);
    }

    /** 字符序列包含至少一个非空白字符。 */
    public static boolean hasText(CharSequence str) {
        if(!hasLength(str)) {
            return false;
        } else {
            int strLen = str.length();

            for(int i = 0; i < strLen; ++i) {
                if(!Character.isWhitespace(str.charAt(i))) {
                    return true;
                }
            }

            return false;
        }
    }

    /** {@link #hasText(CharSequence)} 的 String 重载。 */
    public static boolean hasText(String str) {
        return hasText((CharSequence)str);
    }

    /** 字符序列中是否包含空白字符。 */
    public static boolean containsWhitespace(CharSequence str) {
        if(!hasLength(str)) {
            return false;
        } else {
            int strLen = str.length();

            for(int i = 0; i < strLen; ++i) {
                if(Character.isWhitespace(str.charAt(i))) {
                    return true;
                }
            }

            return false;
        }
    }

    /** {@link #containsWhitespace(CharSequence)} 的 String 重载。 */
    public static boolean containsWhitespace(String str) {
        return containsWhitespace((CharSequence)str);
    }

    /** 去除首尾空白字符。 */
    public static String trimWhitespace(String str) {
        if(!hasLength(str)) {
            return str;
        } else {
            StringBuilder sb = new StringBuilder(str);

            while(sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
                sb.deleteCharAt(0);
            }

            while(sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
                sb.deleteCharAt(sb.length() - 1);
            }

            return sb.toString();
        }
    }

    /** 去除字符串中全部空白字符。 */
    public static String trimAllWhitespace(String str) {
        if(!hasLength(str)) {
            return str;
        } else {
            StringBuilder sb = new StringBuilder(str);
            int index = 0;

            while(sb.length() > index) {
                if(Character.isWhitespace(sb.charAt(index))) {
                    sb.deleteCharAt(index);
                } else {
                    ++index;
                }
            }

            return sb.toString();
        }
    }

    /** 仅去除首部空白。 */
    public static String trimLeadingWhitespace(String str) {
        if(!hasLength(str)) {
            return str;
        } else {
            StringBuilder sb = new StringBuilder(str);

            while(sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
                sb.deleteCharAt(0);
            }

            return sb.toString();
        }
    }

    /** 仅去除尾部空白。 */
    public static String trimTrailingWhitespace(String str) {
        if(!hasLength(str)) {
            return str;
        } else {
            StringBuilder sb = new StringBuilder(str);

            while(sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
                sb.deleteCharAt(sb.length() - 1);
            }

            return sb.toString();
        }
    }

    /** 去除首部指定字符。 */
    public static String trimLeadingCharacter(String str, char leadingCharacter) {
        if(!hasLength(str)) {
            return str;
        } else {
            StringBuilder sb = new StringBuilder(str);

            while(sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
                sb.deleteCharAt(0);
            }

            return sb.toString();
        }
    }

    /** 去除尾部指定字符。 */
    public static String trimTrailingCharacter(String str, char trailingCharacter) {
        if(!hasLength(str)) {
            return str;
        } else {
            StringBuilder sb = new StringBuilder(str);

            while(sb.length() > 0 && sb.charAt(sb.length() - 1) == trailingCharacter) {
                sb.deleteCharAt(sb.length() - 1);
            }

            return sb.toString();
        }
    }

    /** 忽略大小写判断前缀。 */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if(str != null && prefix != null) {
            if(str.startsWith(prefix)) {
                return true;
            } else if(str.length() < prefix.length()) {
                return false;
            } else {
                String lcStr = str.substring(0, prefix.length()).toLowerCase();
                String lcPrefix = prefix.toLowerCase();
                return lcStr.equals(lcPrefix);
            }
        } else {
            return false;
        }
    }

    /** 忽略大小写判断后缀。 */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if(str != null && suffix != null) {
            if(str.endsWith(suffix)) {
                return true;
            } else if(str.length() < suffix.length()) {
                return false;
            } else {
                String lcStr = str.substring(str.length() - suffix.length()).toLowerCase();
                String lcSuffix = suffix.toLowerCase();
                return lcStr.equals(lcSuffix);
            }
        } else {
            return false;
        }
    }

    /** 从指定下标起是否与子串逐字符相等。 */
    public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
        for(int j = 0; j < substring.length(); ++j) {
            int i = index + j;
            if(i >= str.length() || str.charAt(i) != substring.charAt(j)) {
                return false;
            }
        }

        return true;
    }

    /** 返回分隔符之后的首段子串；未找到时返回空串。 */
    public static String substringAfter(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        } else if (separator == null) {
            return "";
        } else {
            int pos = str.indexOf(separator);
            return pos == -1 ? "" : str.substring(pos + separator.length());
        }
    }

    /** 返回最后一次出现分隔符之前的子串。 */
    public static String substringBeforeLast(String str, String separator) {
        if (!isEmpty(str) && !isEmpty(separator)) {
            int pos = str.lastIndexOf(separator);
            return pos == -1 ? str : str.substring(0, pos);
        } else {
            return str;
        }
    }

    /** 返回首次出现分隔符之前的子串。 */
    public static String substringBefore(final String str, final String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return Constants.EMPTY_STRING;
        }
        final int pos = str.indexOf(separator);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    /** 返回最后一次出现分隔符之后的子串。 */
    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        } else if (isEmpty(separator)) {
            return "";
        } else {
            int pos = str.lastIndexOf(separator);
            return pos != -1 && pos != str.length() - separator.length() ? str.substring(pos + separator.length()) : "";
        }
    }

    /** 统计子串在源串中的出现次数。 */
    public static int countOccurrencesOf(String str, String sub) {
        if(str != null && sub != null && str.length() != 0 && sub.length() != 0) {
            int count = 0;

            int idx;
            for(int pos = 0; (idx = str.indexOf(sub, pos)) != -1; pos = idx + sub.length()) {
                ++count;
            }

            return count;
        } else {
            return 0;
        }
    }

    /** 将源串中所有 oldPattern 替换为 newPattern。 */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if(hasLength(inString) && hasLength(oldPattern) && newPattern != null) {
            int pos = 0;
            int index = inString.indexOf(oldPattern);
            if (index < 0) {
                // 无匹配项，直接返回原串
                return inString;
            }

            StringBuilder sb = new StringBuilder();
            for(int patLen = oldPattern.length(); index >= 0; index = inString.indexOf(oldPattern, pos)) {
                sb.append(inString, pos, index);
                sb.append(newPattern);
                pos = index + patLen;
            }

            sb.append(inString.substring(pos));
            return sb.toString();
        } else {
            return inString;
        }
    }

    /** 删除源串中所有 pattern 片段（等价于替换为空串）。 */
    public static String delete(String inString, String pattern) {
        return replace(inString, pattern, "");
    }

    /** 删除源串中出现在 charsToDelete 内的任意字符。 */
    public static String deleteAny(String inString, String charsToDelete) {
        if(hasLength(inString) && hasLength(charsToDelete)) {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < inString.length(); ++i) {
                char c = inString.charAt(i);
                if(charsToDelete.indexOf(c) == -1) {
                    sb.append(c);
                }
            }

            return sb.toString();
        } else {
            return inString;
        }
    }

    /** 用单引号包裹字符串。 */
    public static String quote(String str) {
        return str != null?"\'" + str + "\'":null;
    }

    /** 若为 String 则加引号，否则原样返回。 */
    public static Object quoteIfString(Object obj) {
        return obj instanceof String?quote((String)obj):obj;
    }

    /** 取最后一个 {@code .} 之后的简单名。 */
    public static String unqualify(String qualifiedName) {
        return unqualify(qualifiedName, '.');
    }

    /** 按自定义分隔符取最后一段简单名。 */
    public static String unqualify(String qualifiedName, char separator) {
        return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
    }

    /** 首字母大写。 */
    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    /** 首字母小写。 */
    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    /** 变更首字符大小写。 */
    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if(str != null && str.length() != 0) {
            StringBuilder sb = new StringBuilder(str.length());
            if(capitalize) {
                sb.append(Character.toUpperCase(str.charAt(0)));
            } else {
                sb.append(Character.toLowerCase(str.charAt(0)));
            }

            sb.append(str.substring(1));
            return sb.toString();
        } else {
            return str;
        }
    }

    /** 集合转 String 数组，null 输入返回 null。 */
    public static String[] toStringArray(Collection<String> collection) {
        return collection == null?null:(String[])collection.toArray(new String[0]);
    }

    /** 按分隔符拆成两段，无法拆分返回 null。 */
    public static String[] split(String toSplit, String delimiter) {
        if(hasLength(toSplit) && hasLength(delimiter)) {
            int offset = toSplit.indexOf(delimiter);
            if(offset < 0) {
                return null;
            } else {
                String beforeDelimiter = toSplit.substring(0, offset);
                String afterDelimiter = toSplit.substring(offset + delimiter.length());
                return new String[]{beforeDelimiter, afterDelimiter};
            }
        } else {
            return null;
        }
    }

    /** 将 {@code key=value} 形式数组解析为 Properties。 */
    public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter) {
        return splitArrayElementsIntoProperties(array, delimiter, (String)null);
    }

    /** 解析前先删除指定字符的 Properties 构造。 */
    public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter, String charsToDelete) {
        if(ObjectUtils.isEmpty(array)) {
            return null;
        } else {
            Properties result = new Properties();
            String[] var4 = array;
            int var5 = array.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String element = var4[var6];
                if(charsToDelete != null) {
                    element = deleteAny(element, charsToDelete);
                }

                String[] splittedElement = split(element, delimiter);
                if(splittedElement != null) {
                    result.setProperty(splittedElement[0].trim(), splittedElement[1].trim());
                }
            }

            return result;
        }
    }

    /** 按分隔符集拆分并 trim、忽略空 token。 */
    public static String[] tokenizeToStringArray(String str, String delimiters) {
        return tokenizeToStringArray(str, delimiters, true, true);
    }

    /** 可配置 trim 与是否忽略空 token 的分词。 */
    public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {
        if(str == null) {
            return null;
        } else {
            StringTokenizer st = new StringTokenizer(str, delimiters);
            ArrayList<String> tokens = new ArrayList<String>();

            while(true) {
                String token;
                do {
                    if(!st.hasMoreTokens()) {
                        return toStringArray(tokens);
                    }

                    token = st.nextToken();
                    if(trimTokens) {
                        token = token.trim();
                    }
                } while(ignoreEmptyTokens && token.length() <= 0);

                tokens.add(token);
            }
        }
    }

    /** 按分隔符拆成字符串数组。 */
    public static String[] delimitedListToStringArray(String str, String delimiter) {
        return delimitedListToStringArray(str, delimiter, (String)null);
    }

    /** 拆分前删除指定字符的分隔列表解析。 */
    public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
        if(str == null) {
            return new String[0];
        } else if(delimiter == null) {
            return new String[]{str};
        } else {
            ArrayList<String> result = new ArrayList<String>();
            int pos;
            if("".equals(delimiter)) {
                for(pos = 0; pos < str.length(); ++pos) {
                    result.add(deleteAny(str.substring(pos, pos + 1), charsToDelete));
                }
            } else {
                int delPos;
                for(pos = 0; (delPos = str.indexOf(delimiter, pos)) != -1; pos = delPos + delimiter.length()) {
                    result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                }

                if(str.length() > 0 && pos <= str.length()) {
                    result.add(deleteAny(str.substring(pos), charsToDelete));
                }
            }

            return toStringArray(result);
        }
    }

    /** 逗号分隔列表转数组。 */
    public static String[] commaDelimitedListToStringArray(String str) {
        return delimitedListToStringArray(str, ",");
    }

    /** 逗号分隔列表转有序 Set。 */
    public static Set<String> commaDelimitedListToSet(String str) {
        String[] tokens = commaDelimitedListToStringArray(str);
        return new TreeSet<String>(Arrays.asList(tokens));
    }

    /**
     * 以分隔符连接数组元素为单个字符串。
     * <p>null 分隔符视为空串；列表首尾不额外添加分隔符。</p>
     *
     * @param array 待连接元素
     * @param separator 分隔符
     * @return 连接后的字符串
     */
    public static String join(Object[] array, String separator) {
        if (separator == null) {
            separator = "";
        }
        int arraySize = array.length;
        int bufSize = (arraySize == 0 ? 0 : (array[0].toString().length() + separator.length()) * arraySize);
        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(separator);
            }
            buf.append(array[i]);
        }
        return buf.toString();
    }

    /**
     * 判断字符序列为 null、空串或仅含空白。
     *
     * @param cs 待检测字符序列，可为 null
     * @return 为 null、空或全空白时返回 true
     * @since 2.0
     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
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

    /** 将单个字符重复 repeat 次组成字符串（不支持增补字符）。
     *
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     *
     * <pre>
     * StringUtils.repeat('e', 0)  = ""
     * StringUtils.repeat('e', 3)  = "eee"
     * StringUtils.repeat('e', -2) = ""
     * </pre>
     *
     * <p>Note: this method doesn't not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of {@code char}s to be represented.
     * If you are needing to support full I18N of your applications
     * consider using {@link #repeat(String, int)} instead.
     * </p>
     *
     * @param ch  character to repeat
     * @param repeat  number of times to repeat char, negative treated as zero
     * @return String with repeated character
     * @see #repeat(String, int)
     */
    public static String repeat(final char ch, final int repeat) {
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    /** 将字符串重复 repeat 次拼接（null 输入返回 null）。
     *
     * <p>Repeat a String {@code repeat} times to form a
     * new String.</p>
     *
     * <pre>
     * StringUtils.repeat(null, 2) = null
     * StringUtils.repeat("", 0)   = ""
     * StringUtils.repeat("", 2)   = ""
     * StringUtils.repeat("a", 3)  = "aaa"
     * StringUtils.repeat("ab", 2) = "abab"
     * StringUtils.repeat("a", -2) = ""
     * </pre>
     *
     * @param str  the String to repeat, may be null
     * @param repeat  number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     *  {@code null} if null String input
     */
    public static String repeat(final String str, final int repeat) {
        // Performance tuned for 2.0 (JDK1.4)

        if (str == null) {
            return null;
        }
        if (repeat <= 0) {
            return Constants.EMPTY_STRING;
        }
        final int inputLength = str.length();
        if (repeat == 1 || inputLength == 0) {
            return str;
        }
        if (inputLength == 1 && repeat <= PAD_LIMIT) {
            return repeat(str.charAt(0), repeat);
        }

        final int outputLength = inputLength * repeat;
        switch (inputLength) {
            case 1 :
                return repeat(str.charAt(0), repeat);
            case 2 :
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char[] output2 = new char[outputLength];
                for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
                    output2[i] = ch0;
                    output2[i + 1] = ch1;
                }
                return new String(output2);
            default :
                final StringBuilder buf = new StringBuilder(outputLength);
                for (int i = 0; i < repeat; i++) {
                    buf.append(str);
                }
                return buf.toString();
        }
    }

    /**
     * 安全获取字符序列长度，null 时返回 0。
     *
     * @param cs 字符序列，可为 null
     * @return 长度或 0
     * @since 2.4
     * @since 3.0 Changed signature from length(String) to length(CharSequence)
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /** 从字符串末尾剥离指定字符集（stripChars 为 null 时按空白处理）。
     *
     * <p>Strips any of a set of characters from the end of a String.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is {@code null}, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripEnd(null, *)          = null
     * StringUtils.stripEnd("", *)            = ""
     * StringUtils.stripEnd("abc", "")        = "abc"
     * StringUtils.stripEnd("abc", null)      = "abc"
     * StringUtils.stripEnd("  abc", null)    = "  abc"
     * StringUtils.stripEnd("abc  ", null)    = "abc"
     * StringUtils.stripEnd(" abc ", null)    = " abc"
     * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
     * StringUtils.stripEnd("120.00", ".0")   = "12"
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the set of characters to remove, null treated as whitespace
     * @return the stripped String, {@code null} if null String input
     */
    public static String stripEnd(final String str, final String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (stripChars == null) {
            while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    /** 返回类所属 ClassLoader 的十六进制 hash，无 loader 时返回 {@code null} 字面量。 */
    public static String classLoaderHash(Class<?> clazz) {
        if (clazz == null || clazz.getClassLoader() == null) {
            return "null";
        }
        return Integer.toHexString(clazz.getClassLoader().hashCode());
    }

    /**
     * 将字节数格式化为 KiB/MiB 等人类可读单位。
     *
     * @param bytes 字节数
     * @return 带单位的可读字符串
     */
    public static String humanReadableByteCount(long bytes) {
        return bytes < 1024L ? bytes + " B"
                : bytes < 0xfffccccccccccccL >> 40 ? String.format("%.1f KiB", bytes / 0x1p10)
                : bytes < 0xfffccccccccccccL >> 30 ? String.format("%.1f MiB", bytes / 0x1p20)
                : bytes < 0xfffccccccccccccL >> 20 ? String.format("%.1f GiB", bytes / 0x1p30)
                : bytes < 0xfffccccccccccccL >> 10 ? String.format("%.1f TiB", bytes / 0x1p40)
                : bytes < 0xfffccccccccccccL ? String.format("%.1f PiB", (bytes >> 10) / 0x1p40)
                : String.format("%.1f EiB", (bytes >> 20) / 0x1p40);
    }

    /** 按行拆分文本为 List，IO 异常静默忽略。 */
    public static List<String> toLines(String text) {
        List<String> result = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new StringReader(text));
        try {
            String line = reader.readLine();
            while (line != null) {
                result.add(line);
                line = reader.readLine();
            }
        } catch (IOException exc) {
            // quit
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return result;
    }

    /** 生成长度为 length 的随机字母数字串。 */
    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(AB.charAt(ThreadLocalRandom.current().nextInt(AB.length())));
        return sb.toString();
    }

    /**
     * 返回 token 首次出现之前的子串。
     *
     * @param text 源文本
     * @param before 分隔 token
     * @return token 前子串；未找到时返回 null
     */
    public static String before(String text, String before) {
        int pos = text.indexOf(before);
        return pos == -1 ? null : text.substring(0, pos);
    }

    /**
     * 返回 token 首次出现之后的子串。
     *
     * @param text 源文本
     * @param after 分隔 token
     * @return token 后子串；未找到时返回 null
     */
    public static String after(String text, String after) {
        int pos = text.indexOf(after);
        if (pos == -1) {
            return null;
        }
        return text.substring(pos + after.length());
    }

    // print|(ILjava/util/List;)V
    /** 拆分 {@code 方法名|描述符} 形式的方法信息。 */
    public static String[] splitMethodInfo(String methodInfo) {
        int index = methodInfo.indexOf('|');
        return new String[] { methodInfo.substring(0, index), methodInfo.substring(index + 1) };
    }

    // demo/MathGame|primeFactors|(I)Ljava/util/List;|24
    /** 拆分 watch/trace 调用信息：类|方法|描述符|行号。 */
    public static String[] splitInvokeInfo(String invokeInfo) {
        int index1 = invokeInfo.indexOf('|');
        int index2 = invokeInfo.indexOf('|', index1 + 1);
        int index3 = invokeInfo.indexOf('|', index2 + 1);
        return new String[] { invokeInfo.substring(0, index1), invokeInfo.substring(index1 + 1, index2),
                invokeInfo.substring(index2 + 1, index3), invokeInfo.substring(index3 + 1) };
    }

    /** 将空格替换为下划线并转小写，便于文件名化。 */
    public static String beautifyName(String name) {
        return name.replace(' ', '_').toLowerCase();
    }

    /** URL 数组转字符串列表，null 输入返回空列表。 */
    public static List<String> toStringList(URL[] urls) {
        if (urls != null) {
            List<String> result = new ArrayList<String>(urls.length);
            for (URL url : urls) {
                result.add(url.toString());
            }
            return result;
        }
        return Collections.emptyList();
    }
}
