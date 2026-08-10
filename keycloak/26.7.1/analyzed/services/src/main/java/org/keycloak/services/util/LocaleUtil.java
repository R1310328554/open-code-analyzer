/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.services.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.keycloak.locale.LocaleSelectorProvider;
import org.keycloak.locale.LocaleUpdaterProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 国际化（i18n）locale 处理工具类。
 * <p>解析请求中的 kc_locale 参数、计算 locale 继承链、
 * 合并主题与领域本地化消息。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:daniel.fesenmeyer@bosch.com">Daniel Fesenmeyer</a>
 */
public class LocaleUtil {

    private LocaleUtil() {
        // 工具类禁止实例化
    }

    /**
     * 处理请求 URL 中的 kc_locale 参数：写入认证会话 note 或 session 属性，并更新 locale Cookie。
     *
     * @param authSession 可为 null（如 info/error 页无认证会话时写入 session 属性）
     */
    public static void processLocaleParam(KeycloakSession session, RealmModel realm, AuthenticationSessionModel authSession) {
        if (realm.isInternationalizationEnabled()) {
            String locale = session.getContext().getUri().getQueryParameters().getFirst(LocaleSelectorProvider.KC_LOCALE_PARAM);
            if (locale != null) {
                if (authSession != null) {
                    authSession.setAuthNote(LocaleSelectorProvider.USER_REQUEST_LOCALE, locale);
                } else {
                    // info/error 页等无 authenticationSession 的场景
                    session.setAttribute(LocaleSelectorProvider.USER_REQUEST_LOCALE, locale);
                }

                LocaleUpdaterProvider localeUpdater = session.getProvider(LocaleUpdaterProvider.class);
                localeUpdater.updateLocaleCookie(locale);
            }
        }
    }

    /**
     * 返回给定 locale 的父 locale。
     * <p>仅含语言时回退为 "en"；"en" 无父 locale 返回 {@code null}。</p>
     *
     * @param locale 目标 locale
     * @return 父 locale，可能为 {@code null}
     * @deprecated 请使用 {@link LocaleUtil#getParentLocale(Locale, RealmModel)}
     */
    @Deprecated(since = "26.5", forRemoval = true)
    public static Locale getParentLocale(Locale locale) {
        return getParentLocale(locale, null);
    }

    /**
     * 返回给定 locale 的父 locale，考虑领域默认语言设置。
     * <p>variant → language+country → language → 领域默认 locale → en。</p>
     *
     * @return 父 locale，可能为 {@code null}
     */
    public static Locale getParentLocale(Locale locale, RealmModel realm) {
        if (Locale.ENGLISH.equals(locale)) {
            return null;
        }

        if (locale.getVariant() != null && !locale.getVariant().isEmpty()) {
            return new Locale(locale.getLanguage(), locale.getCountry());
        }

        if (locale.getCountry() != null && !locale.getCountry().isEmpty()) {
            return new Locale(locale.getLanguage());
        }

        if (realm != null
                && realm.isInternationalizationEnabled()
                && realm.getDefaultLocale() != null
                && Locale.forLanguageTag(realm.getDefaultLocale()).getLanguage().equals(locale.getLanguage())) {
            return Locale.ENGLISH;
        }

        if (realm != null
                && realm.isInternationalizationEnabled()
                && realm.getDefaultLocale() != null) {
            return Locale.forLanguageTag(realm.getDefaultLocale());
        }

        return Locale.ENGLISH;
    }

    /**
     * 获取 locale 的适用 locale 链（由具体到抽象）。
     * <p>示例："de-CH" → ["de-CH", "de", "en"]。</p>
     *
     * @param locale 目标 locale
     * @return 适用 locale 列表（顺序从具体到抽象）
     */
    static List<Locale> getApplicableLocales(Locale locale, RealmModel realm) {
        List<Locale> applicableLocales = new ArrayList<>();

        for (Locale currentLocale = locale; currentLocale != null; currentLocale = getParentLocale(currentLocale, realm)) {
            applicableLocales.add(currentLocale);
        }

        return applicableLocales;
    }

    /**
     * 将按 locale 分组的消息合并为适用于指定 locale 的 {@link Properties}。
     *
     * @param locale 目标 locale
     * @param messages 按 locale 分组的消息
     * @return 合并后的属性集
     * @see #mergeGroupedMessages(RealmModel, Locale, Map, Map)
     */
    public static Properties mergeGroupedMessages(RealmModel realm, Locale locale, Map<Locale, Properties> messages) {
        return mergeGroupedMessages(realm, locale, messages, null);
    }

    /**
     * 合并两组按 locale 分组的消息，firstMessages 优先级高于 secondMessages。
     * <p>优先级（F=firstMessages，S=secondMessages）：</p>
     * <ol>
     * <li>F &lt;language-region-variant&gt;</li>
     * <li>S &lt;language-region-variant&gt;</li>
     * <li>F &lt;language-region&gt;</li>
     * <li>S &lt;language-region&gt;</li>
     * <li>F &lt;language&gt;</li>
     * <li>S &lt;language&gt;</li>
     * <li>F en</li>
     * <li>S en</li>
     * </ol>
     *
     * @param locale 目标 locale
     * @param firstMessages 高优先级消息组
     * @param secondMessages 低优先级消息组，可为 {@code null}
     * @return 合并后的属性集
     * @see #mergeGroupedMessages(RealmModel, Locale, Map)
     */
    public static Properties mergeGroupedMessages(RealmModel realm, Locale locale, Map<Locale, Properties> firstMessages,
            Map<Locale, Properties> secondMessages) {
        List<Locale> applicableLocales = getApplicableLocales(locale, realm);

        Properties mergedProperties = new Properties();

        /*
         * 从列表末尾向前迭代，先写入低优先级消息，再由高优先级覆盖
         */
        ListIterator<Locale> itr = applicableLocales.listIterator(applicableLocales.size());
        while (itr.hasPrevious()) {
            Locale currentLocale = itr.previous();

            // 先写入 secondMessages（可被 firstMessages 覆盖）
            if (secondMessages != null) {
                Properties currentLocaleSecondMessages = secondMessages.get(currentLocale);
                if (currentLocaleSecondMessages != null) {
                    mergedProperties.putAll(currentLocaleSecondMessages);
                }
            }

            // 写入 firstMessages，覆盖 secondMessages
            Properties currentLocaleFirstMessages = firstMessages.get(currentLocale);
            if (currentLocaleFirstMessages != null) {
                mergedProperties.putAll(currentLocaleFirstMessages);
            }
        }

        return mergedProperties;
    }

    /**
     * 用领域本地化文本增强主题消息；同 locale 下领域文本优先，更具体的 locale 优先于更抽象的。
     * <p>实现细节见 {@link #mergeGroupedMessages(RealmModel, Locale, Map, Map)}。</p>
     *
     * @param realm 领域
     * @param locale 目标 locale
     * @param themeMessages 主题消息（作为低优先级组）
     * @return 增强后的属性集
     */
    public static Properties enhancePropertiesWithRealmLocalizationTexts(RealmModel realm, Locale locale,
            Map<Locale, Properties> themeMessages) {
        Map<Locale, Properties> realmLocalizationMessages = getRealmLocalizationTexts(realm, locale);

        return mergeGroupedMessages(realm, locale, realmLocalizationMessages, themeMessages);
    }

    /** 获取领域在 locale 链上各级的本地化文本，按 locale 分组。 */
    public static Map<Locale, Properties> getRealmLocalizationTexts(RealmModel realm, Locale locale) {
        LinkedHashMap<Locale, Properties> groupedMessages = new LinkedHashMap<>();

        List<Locale> applicableLocales = getApplicableLocales(locale, realm);
        for (Locale applicableLocale : applicableLocales) {
            Map<String, String> currentRealmLocalizationTexts =
                    realm.getRealmLocalizationTextsByLocale(applicableLocale.toLanguageTag());
            Properties currentMessages = new Properties();
            currentMessages.putAll(currentRealmLocalizationTexts);

            groupedMessages.put(applicableLocale, currentMessages);
        }

        return groupedMessages;
    }
    
}
