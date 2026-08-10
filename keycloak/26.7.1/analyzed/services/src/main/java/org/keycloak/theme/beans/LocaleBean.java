/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.theme.beans;

import java.text.Bidi;
import java.text.Collator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.models.RealmModel;

/**
 * 登录页区域切换 Bean。
 * <p>封装当前语言、RTL 方向及 realm 支持的语言列表，注入 FreeMarker 上下文供语言选择器渲染。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LocaleBean {

    private final String current;
    private final String currentLanguageTag;
    /** 当前语言是否为从右到左书写。 */
    private final boolean rtl; // right-to-left language
    private final List<Locale> supported;
    private static final ConcurrentHashMap<String, Boolean> bidiMap = new ConcurrentHashMap<>();

    /** 根据 realm 支持语言与消息 bundle 构建区域 Bean。 */
    public LocaleBean(RealmModel realm, java.util.Locale current, UriBuilder uriBuilder, Properties messages) {
        this.currentLanguageTag = current.toLanguageTag();
        this.current = messages.getProperty("locale_" + this.currentLanguageTag, this.currentLanguageTag);
        this.rtl = !isLeftToRight(this.current);

        Collator collator = Collator.getInstance(current);
        collator.setStrength(Collator.PRIMARY); // ignore case and accents

        supported = realm.getSupportedLocalesStream()
                .map(l -> {
                    String label = messages.getProperty("locale_" + l, l);
                    String url = uriBuilder.replaceQueryParam("kc_locale", l).build().toString();
                    return new Locale(l, label, url);
                })
                .sorted((o1, o2) -> collator.compare(o1.label, o2.label))
                .collect(Collectors.toList());
    }

    /** 判断显示名称对应的书写方向是否为 LTR。 */
    protected static boolean isLeftToRight(String current) {
        // Some languages that are RTL have an English name in Java locales, like 'dv' aka Divehi as stated in
        // https://github.com/keycloak/keycloak/issues/33833#issuecomment-2446965307.
        // Still, this solution seems to be good enough for now. Any exceptions would be added when those translations arise,
        // as each localization file can contain a `locale_xx' property with the wanted translation.
        //
        // Adding the ICU library was discarded at the time to avoid an additional dependency and due to its special license.
        // This might be reconsidered in the future if there are more scenarios.
        //
        // As the most likely alternative, a translation could in the future define RTL, its language name, and then this can be used instead.

        return bidiMap.computeIfAbsent(current, l -> new Bidi(l, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isLeftToRight());
    }

    /** 返回当前语言的本地化显示名。 */
    public String getCurrent() {
        return current;
    }

    /** 返回当前 BCP 47 语言标签。 */
    public String getCurrentLanguageTag() {
        return currentLanguageTag;
    }

    /** 当前语言是否为 RTL（从右到左）。 */
    public boolean isRtl() {
        return rtl;
    }

    /** 返回 realm 支持的语言列表（含标签、显示名与切换 URL）。 */
    public List<Locale> getSupported() {
        return supported;
    }

    /** 单个可选语言的标签、显示名与切换链接。 */
    public static class Locale {

        private final String languageTag;
        private final String label;
        private final String url;

        public Locale(String languageTag, String label, String url) {
            this.languageTag = languageTag;
            this.label = label;
            this.url = url;
        }

        /** 返回 BCP 47 语言标签。 */
        public String getLanguageTag() {
            return languageTag;
        }

        /** 返回切换至该语言的 URL（含 kc_locale 参数）。 */
        public String getUrl() {
            return url;
        }

        /** 返回本地化显示名。 */
        public String getLabel() {
            return label;
        }

    }

}
