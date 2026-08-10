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

package org.keycloak.theme;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.keycloak.models.RealmModel;

/**
 * 主题接口：提供登录、账户、管理、邮件等 UI 模板、资源与国际化消息。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface Theme {

    /** 账户资源 Provider 配置键。 */
    String ACCOUNT_RESOURCE_PROVIDER_KEY = "accountResourceProvider";
    /** 内容哈希路径匹配模式配置键。 */
    String CONTENT_HASH_PATTERN = "contentHashPattern";
    /** 抽象主题标志属性名。 */
    String ABSTRACT_PROPERTY = "abstract";

    /** 主题类型：登录、账户、管理、邮件、欢迎页、通用。 */
    enum Type { LOGIN, ACCOUNT, ADMIN, EMAIL, WELCOME, COMMON };

    /** 主题名称。 */
    String getName();

    /** 父主题名称（继承链）。 */
    String getParentName();

    /** 导入主题名称。 */
    String getImportName();

    /** 主题类型。 */
    Type getType();

    /** 获取命名模板的 URL。
     * @param name 模板名
     * @throws IOException 读取失败 */
    URL getTemplate(String name) throws IOException;

    /** 以流形式读取主题资源。
     * @param path 资源路径
     * @throws IOException 读取失败 */
    InputStream getResourceAsStream(String path) throws IOException;

    /**
     * 使用默认 bundle 名（如 messages）获取指定 locale 的消息。
     * Same as getMessages(baseBundlename, locale), but uses a default baseBundlename
     * such as "messages".
     *
     * @param locale The locale of the desired message bundle.
     * @return The localized messages from the bundle.
     * @throws IOException If bundle can not be read.
     */
    Properties getMessages(Locale locale) throws IOException;

    /**
     * 从指定 baseBundlename 与 locale 读取国际化消息 bundle。
     * Retrieve localized messages from a message bundle.
     *
     * @param baseBundlename The base name of the bundle, such as "messages" in
     * messages_en.properties.
     * @param locale The locale of the desired message bundle.
     * @return The localized messages from the bundle.
     * @throws IOException If bundle can not be read.
     */
    Properties getMessages(String baseBundlename, Locale locale) throws IOException;

    /**
     * 读取 messages bundle 并用 realm 本地化翻译增强（realm 翻译优先）。
     * Retrieve localized messages from a message bundle named "messages" and enhance those messages with messages from
     * realm localization.
     * <p>
     * In general, the translation for the most specific applicable language is used. If a translation exists both in the message bundle and realm localization, the realm localization translation is used.
     * </p>
     *
     * @param realm The realm from which the localization should be retrieved
     * @param locale The locale of the desired message bundle.
     * @return The localized messages from the bundle, enhanced with realm localization
     * @throws IOException If bundle can not be read.
     */
    Properties getEnhancedMessages(RealmModel realm, Locale locale) throws IOException;

    /** 读取主题属性配置。
     * @throws IOException 读取失败 */
    Properties getProperties() throws IOException;

    /**
     * 检查主题中是否存在指定路径的资源。
     * Check if a resource exists in the theme.
     * @param path path of the resource
     * @return true if the resource exists
     */
    default boolean hasResource(String path) throws IOException {
        try (InputStream is = getResourceAsStream(path)) {
            return is != null;
        }
    }

    /**
     * 判断路径是否含内容哈希（保证跨版本内容一致，便于 CDN 缓存）。
     * Check if the given path contains a content hash.
     * If a resource is requested from this path, and it has a content hash, this guarantees that if the file
     * exists in two versions of the theme, it will contain the same contents.
     * With this guarantee, a different version of Keycloak can return the same contents even if a caller asks for
     * a different version of Keycloak.
     *
     * @param path path to check for a content hash
     */
    default boolean hasContentHash(String path) throws IOException {
        Object contentHashPattern = getProperties().get(CONTENT_HASH_PATTERN);
        if (contentHashPattern != null) {
            return path.matches(contentHashPattern.toString());
        } else {
            return false;
        }
    }

    /**
     * 判断是否为仅用于被继承的抽象主题（读取 abstract 属性）。
     * Method to know if the theme is just an abstract theme (only to be extended by
     * other themes). By default it just checks the <em>abstract</em> property.
     * @return true if abstract, false if not
     * @throws IOException Some error reading the properties
     */
    default boolean isAbstract() throws IOException {
        return Boolean.parseBoolean(getProperties().getProperty(ABSTRACT_PROPERTY));
    }

}
