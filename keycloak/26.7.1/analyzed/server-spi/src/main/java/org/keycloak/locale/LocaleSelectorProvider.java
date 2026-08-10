/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.locale;

import java.util.Locale;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;
import org.keycloak.theme.Theme;

/**
 * 区域（Locale）选择提供者：根据领域、用户及请求上下文解析应使用的语言区域。
 * <p>扩展 {@link Provider}，可通过 SPI 注册自定义选择策略。</p>
 */
public interface LocaleSelectorProvider extends Provider {

    /** 请求参数名：用户显式指定区域时使用。 */
    String KC_LOCALE_PARAM = "kc_locale";

    /** 会话 note 键：客户端请求的区域。 */
    String CLIENT_REQUEST_LOCALE = "locale_client_requested";
    /** 会话 note 键：用户请求的区域。 */
    String USER_REQUEST_LOCALE = "locale_user_requested";

    /**
     * 解析当前请求应使用的区域。
     * Resolve the locale which should be used for the request
     * @param user
     * @return 解析后的 {@link Locale}
     */
    Locale resolveLocale(RealmModel realm, UserModel user);

    /** 按主题类型解析区域（默认委托 {@link #resolveLocale(RealmModel, UserModel)}）。 */
    default Locale resolveLocale(RealmModel realm, UserModel user, Theme.Type themeType) {
        return resolveLocale(realm, user);
    }

    /** 解析区域，可选忽略 Accept-Language 请求头。 */
    default Locale resolveLocale(RealmModel realm, UserModel user, boolean ignoreAcceptLanguageHeader) {
        return resolveLocale(realm, user);
    }

}
