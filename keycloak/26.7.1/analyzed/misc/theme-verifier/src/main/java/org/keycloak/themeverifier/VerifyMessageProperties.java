/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.themeverifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.owasp.html.PolicyFactory;

/**
 * 主题 {@code messages_*.properties} 文件的静态校验器。
 * <p>
 * 检查重复键、HTML 安全、MessageFormat 引号/占位符、首尾空白、禁用词汇等，
 * 供 {@link ThemeVerifierMojo} 在构建阶段批量调用。
 */
public class VerifyMessageProperties {

    /** 待校验的属性文件。 */
    private final File file;
    /** 累积的校验错误消息。 */
    private List<String> messages;
    /** 是否按后端 MessageFormat 规则校验引号与占位符。 */
    private boolean validateMessageFormatQuotes;

    /**
     * @param file 目标 properties 文件
     */
    public VerifyMessageProperties(File file) {
        this.file = file;
    }

    /**
     * 执行全部校验规则并返回错误列表（空列表表示通过）。
     *
     * @return 校验错误描述列表
     */
    public List<String> verify() throws MojoExecutionException {
        messages = new ArrayList<>();
        try {
            String contents = Files.readString(file.toPath());
            verifyNoDuplicateKeys(contents);
            verifySafeHtml();
            verifyNoHtmlEntities();
            verifyProblematicBlanks();
            verifyNoDiscouragedWords();
            if (validateMessageFormatQuotes) {
                verifyMessageFormatQuotes();
                verifyMessageFormatPlaceholders();
            } else {
                verifyNotMessageFormatQuotes();
                verifyNotMessageFormatPlaceholders();
            }
            verifyUnbalancedCurlyBraces();
        } catch (IOException e) {
            throw new MojoExecutionException("Can not read file " + file, e);
        }
        return messages;
    }

    /** HTML 实体（如 {@code &nbsp;}）检测模式。 */
    private final static Pattern HTML_ENTITIES = Pattern.compile("&[a-zA-Z]+;");

    /** 禁止直接使用 HTML 实体，应改用 UTF-8 字符。 */
    private void verifyNoHtmlEntities() {
        PropertyResourceBundle bundle = getPropertyResourceBundle();

        bundle.getKeys().asIterator().forEachRemaining(key -> {
            String value = bundle.getString(key);

            if (HTML_ENTITIES.matcher(value).find()) {
                messages.add("HTML entities should not be used, as UTF-8 can be used instead '" + key + "' for file " + file + ": " + value);
            }

        });
    }

    /** 英文源文件中不推荐使用的词汇（whitelist/blacklist 等）。 */
    private final static Pattern DISCOURAGED_WORDS = Pattern.compile("(whitelist|blacklist)", Pattern.CASE_INSENSITIVE);

    /** 仅在 {@code _en.properties} 中检查禁用词汇。 */
    private void verifyNoDiscouragedWords() {
        PropertyResourceBundle bundle = getPropertyResourceBundle();

        if (!file.getName().endsWith("_en.properties")) {
            // 禁用词规则仅适用于英文源文件
            return;
        }

        bundle.getKeys().asIterator().forEachRemaining(key -> {
            String value = bundle.getString(key);

            Matcher matcher = DISCOURAGED_WORDS.matcher(value);
            if (matcher.find()) {
                messages.add("Discouraged word found in '" + key + "' for file " + file + ": '" + matcher.group(0) + "' in '" + value + "'");
            }

        });
    }

    /** MessageFormat 中表示字面量单引号的 {@code ''} 序列。 */
    private final static Pattern DOUBLE_SINGLE_QUOTES = Pattern.compile("''");

    /** 前端展示模式下不允许出现 MessageFormat 双单引号转义。 */
    private void verifyNotMessageFormatQuotes() {
        PropertyResourceBundle bundle = getPropertyResourceBundle();

        bundle.getKeys().asIterator().forEachRemaining(key -> {
            String value = bundle.getString(key);

            if (DOUBLE_SINGLE_QUOTES.matcher(value).find()) {
                messages.add("Double single quotes are not allowed in message formats as they might be shown in frontends as-is in '" + key + "' for file " + file + ": " + value);
            }

        });
    }

    /** 字符串中间、末尾、开头的孤立单引号（MessageFormat 后端模式）。 */
    private static final Pattern SINGLE_QUOTE_MIDDLE = Pattern.compile("[^']'[^']");
    private static final Pattern SINGLE_QUOTE_END = Pattern.compile("[^']'$");
    private static final Pattern SINGLE_QUOTE_START = Pattern.compile("^'[^']");

    /** 后端 MessageFormat 模式下禁止未转义的单引号。 */
    private void verifyMessageFormatQuotes() {
        PropertyResourceBundle bundle = getPropertyResourceBundle();

        bundle.getKeys().asIterator().forEachRemaining(key -> {
            String value = bundle.getString(key);

            if (SINGLE_QUOTE_START.matcher(value).find()
            || SINGLE_QUOTE_MIDDLE.matcher(value).find()
            || SINGLE_QUOTE_END.matcher(value).find()) {
                messages.add("Single quotes are not allowed in message formats due to unexpected behaviors in '" + key + "' for file " + file + ": " + value);
            }

        });
    }

    /** 后端 MessageFormat 双花括号占位符 {@code {{0}}} 的起止片段。 */
    private static final Pattern DOUBLE_CURLY_BRACES_START = Pattern.compile("\\{\\{[0-9]");
    private static final Pattern DOUBLE_CURLY_BRACES_END = Pattern.compile("[0-9]}}");

    /** 后端模式下占位符必须使用双花括号，禁止单花括号形式。 */
    private void verifyMessageFormatPlaceholders() {
        PropertyResourceBundle bundle = getPropertyResourceBundle();

        bundle.getKeys().asIterator().forEachRemaining(key -> {
            String value = bundle.getString(key);

            if (DOUBLE_CURLY_BRACES_START.matcher(value).find()
                    || DOUBLE_CURLY_BRACES_END.matcher(value).find()) {
                messages.add("Double curly braces are not allowed in message formats in the backend for in '" + key + "' for file " + file + ": " + value);
            }

        });
    }

    /** 前端展示模式下检测误用的单花括号占位符 {@code {0}}。 */
    private static final Pattern SINGLE_CURLY_BRACE_MIDDLE = Pattern.compile("[^{]\\{[0-9]");
    private static final Pattern SINGLE_CURLY_BRACE_END = Pattern.compile("[0-9]}$");
    private static final Pattern SINGLE_CURLY_BRACE_START = Pattern.compile("^\\{[0-9]");

    /** 前端模式下不允许 MessageFormat 风格数字占位符。 */
    private void verifyNotMessageFormatPlaceholders() {
        PropertyResourceBundle bundle = getPropertyResourceBundle();

        bundle.getKeys().asIterator().forEachRemaining(key -> {
            String value = bundle.getString(key);

            if (SINGLE_CURLY_BRACE_START.matcher(value).find()
                    || SINGLE_CURLY_BRACE_MIDDLE.matcher(value).find()
                    || SINGLE_CURLY_BRACE_END.matcher(value).find()) {
                messages.add("Single curly quotes are not supported as placeholders for the frontend in '" + key + "' for file " + file + ": " + value);
            }

        });
    }

    /** 检测不成对的花括号组合。 */
    private static final Pattern UNBALANCED_ONE = Pattern.compile("\\{\\{[^{}]*}[^}]");
    private static final Pattern UNBALANCED_ONE_END = Pattern.compile("\\{\\{[^{}]*}$");
    private static final Pattern UNBALANCED_TWO = Pattern.compile("[^{]\\{[^{}]*}}");
    private static final Pattern UNBALANCED_TWO_START = Pattern.compile("^\\{[^{}]*}}");

    /** 校验各键值中花括号是否成对、格式是否一致。 */
    private void verifyUnbalancedCurlyBraces() {
        PropertyResourceBundle bundle = getPropertyResourceBundle();

        bundle.getKeys().asIterator().forEachRemaining(key -> {
            String value = bundle.getString(key);

            if (UNBALANCED_ONE.matcher(value).find() || UNBALANCED_ONE_END.matcher(value).find()
                || UNBALANCED_TWO.matcher(value).find() || UNBALANCED_TWO_START.matcher(value).find()) {
                messages.add("Unbalanced curly braces in key '" + key + "' for file " + file + ": " + value);
            }

        });
    }

    /** 加载 properties 为 {@link PropertyResourceBundle}。 */
    private PropertyResourceBundle getPropertyResourceBundle() {
        PropertyResourceBundle bundle;
        try (FileInputStream fis = new FileInputStream(file)) {
            bundle = new PropertyResourceBundle(fis);
        } catch (IOException e) {
            throw new RuntimeException("unable to read file " + file, e);
        }
        return bundle;
    }

    /** 允许少量安全 HTML 标签的策略（与英文源对齐时使用）。 */
    PolicyFactory POLICY_SOME_HTML = new org.owasp.html.HtmlPolicyBuilder()
            .allowElements(
                    "br", "p", "strong", "b", "formattedLink"
            ).toFactory();

    /** 禁止一切 HTML 的严格策略。 */
    PolicyFactory POLICY_NO_HTML = new org.owasp.html.HtmlPolicyBuilder().toFactory();

    /**
     * 对照英文源字符串，用 OWASP HTML 策略校验翻译值是否含非法 markup。
     * 仅当英文原文含 HTML 时才允许对应键使用有限 HTML 标签。
     */
    private void verifySafeHtml() {
        PropertyResourceBundle bundle = getPropertyResourceBundle();

        PropertyResourceBundle bundleEnglish;
        String englishFile = file.getAbsolutePath().replaceAll("resources-community", "resources")
                .replaceAll("_[a-zA-Z-_]*\\.properties", "_en.properties");
        try (FileInputStream fis = new FileInputStream(englishFile)) {
            bundleEnglish = new PropertyResourceBundle(fis);
        } catch (IOException e) {
            throw new RuntimeException("unable to read file " + englishFile, e);
        }

        bundle.getKeys().asIterator().forEachRemaining(key -> {
            String value = bundle.getString(key);
            value = normalizeValue(key, value);
            String englishValue = getEnglishValue(key, bundleEnglish);
            englishValue = normalizeValue(key, englishValue);

            value = santizeAnchors(key, value, englishValue);

            // 英文源含 HTML 时，翻译也允许相同级别的 HTML
            PolicyFactory policy = containsHtml(englishValue) ? POLICY_SOME_HTML : POLICY_NO_HTML;
            String sanitized = policy.sanitize(value);

            //  sanitizer 会转义引号及类似 '<1>' 的数字标签
            sanitized = org.apache.commons.text.StringEscapeUtils.unescapeHtml4(sanitized);
            // 双花括号场景下 sanitizer 可能插入占位注释
            sanitized = sanitized.replace("<!-- -->", "");

            if (!Objects.equals(sanitized, value)) {

                // 剥去首尾相同片段，便于在错误信息中定位差异区间
                int start = 0;
                while (start < sanitized.length() && start < value.length() && value.charAt(start) == sanitized.charAt(start)) {
                    start++;
                }
                int end = 0;
                while (end < sanitized.length() - start && end < value.length() - start && value.charAt(value.length() - end - 1) == sanitized.charAt(sanitized.length() - end - 1)) {
                    end++;
                }

                messages.add("Illegal HTML in key " + key + " for file " + file + ": '" + value.substring(start, value.length() - end) + "' vs. '" + sanitized.substring(start, sanitized.length() - end) + "'");
            }

        });
    }

    /**
     * 双空格及字符串首尾空白难以在翻译工具中维护；UI 所需空白应放在 HTML 模板中。
     * Double blanks and blanks at the beginning of end of the string are difficult to translation in the translation tools and
     * are easily missed. If a blank before or after the string is needed in the UI, add it in the HTML template.
     */
    private void verifyProblematicBlanks() {
        if (!file.getName().endsWith("_en.properties")) {
            // 仅检查英文源文件；其它语言由翻译平台校验
            return;
        }
        PropertyResourceBundle bundle = getPropertyResourceBundle();

        bundle.getKeys().asIterator().forEachRemaining(key -> {
            String value = bundle.getString(key);

            if (value.contains("  ")) {
                messages.add("Duplicate blanks in '" + key + "' for file " + file + ": '" + value);
            }

            if (value.startsWith(" ")) {
                messages.add(key + " starts with a blank in file " + file + ": '" + value);
            }

            if (value.endsWith(" ")) {
                messages.add(key + " ends with a blank in file " + file + ": '" + value);
            }
        });
    }

    /** 对已知特殊键剥离模板占位片段，避免 HTML 校验误报。 */
    private String normalizeValue(String key, String value) {
        if (key.equals("templateHelp")) {
            // 允许 "CLAIM.<NAME>" 占位
            value = value.replaceAll("CLAIM\\.<[A-Z]*>", "");
        } else if (key.equals("optimizeLookupHelp")) {
            // 允许 "<Extensions>" 占位
            value = value.replaceAll("<Extensions>", "");
        } else if (key.startsWith("linkExpirationFormatter.timePeriodUnit") || key.equals("error-invalid-multivalued-size")) {
            // choice 格式中的 "<" 会导致误判
            value = value.replaceAll("\\{[0-9]+,choice,[^}]*}", "...");
        }

        // 与 sanitized 值一致，先反转义 HTML 实体
        value = org.apache.commons.text.StringEscapeUtils.unescapeHtml4(value);

        return value;
    }

    /** 检测字符串是否包含简单 HTML 开标签。 */
    Pattern HTML_TAGS = Pattern.compile("<[a-z]+[^>]*>");

    private boolean containsHtml(String englishValue) {
        return HTML_TAGS.matcher(englishValue).find();
    }

    /** {@code <a>} 锚点标签匹配模式。 */
    private static final Pattern ANCHOR_PATTERN = Pattern.compile("</?a[^>]*>");

    /**
     * 翻译中仅允许出现与英文源完全一致的 {@code <a>} 标签。
     * Allow only those anchor tags from the source key to also appear in the target key.
     */
    private String santizeAnchors(String key, String value, String englishValue) {
        Matcher matcher = ANCHOR_PATTERN.matcher(value);
        Matcher englishMatcher = ANCHOR_PATTERN.matcher(englishValue);
        while (matcher.find()) {
            if (englishMatcher.find() && Objects.equals(matcher.group(), englishMatcher.group())) {
                value = value.replaceFirst(Pattern.quote(englishMatcher.group()), "");
            } else {
                messages.add("Didn't find anchor tag " + matcher.group() + " in original string");
                break;
            }
        }
        return value;
    }

    /** 读取英文 bundle 中同键值，缺失时返回空串。 */
    private static String getEnglishValue(String key, PropertyResourceBundle bundleEnglish) {
        String englishValue;
        try {
            englishValue = bundleEnglish.getString(key);
        } catch (MissingResourceException ex) {
            englishValue = "";
        }
        return englishValue;
    }

    /** 按行扫描原始文件文本，检测重复的 property 键。 */
    private void verifyNoDuplicateKeys(String contents) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(contents));
        String line;
        HashSet<String> seenKeys = new HashSet<>();
        HashSet<String> duplicateKeys = new HashSet<>();
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }
            int split = line.indexOf("=");
            if (split != -1) {
                String key = line.substring(0, split).trim();
                if (seenKeys.contains(key)) {
                    duplicateKeys.add(key);
                } else {
                    seenKeys.add(key);
                }
            }
        }
        if (!duplicateKeys.isEmpty()) {
            messages.add("Duplicate keys in file '" + file.getAbsolutePath() + "': " + duplicateKeys);
        }
    }

    /**
     * 链式设置 MessageFormat 校验模式。
     *
     * @param validateMessageFormatQuotes 是否启用后端 MessageFormat 规则
     * @return 当前实例
     */
    public VerifyMessageProperties withValidateMessageFormatQuotes(boolean validateMessageFormatQuotes) {
        this.validateMessageFormatQuotes = validateMessageFormatQuotes;
        return this;
    }

}
