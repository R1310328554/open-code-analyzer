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
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.keycloak.models.RealmModel;
import org.keycloak.services.util.LocaleUtil;

/**
 * 基于 ClassLoader 的主题实现。
 * <p>从 classpath 的 {@code theme/<name>/<type>/} 目录加载模板、资源与消息 bundle。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClassLoaderTheme extends FileBasedTheme {

    /** 主题名称。 */
    private String name;

    /** 父主题名称（来自 theme.properties）。 */
    private String parentName;

    /** 导入主题名称（来自 theme.properties）。 */
    private String importName;

    /** 主题类型（login、account、email 等）。 */
    private Type type;

    /** 加载主题资源的 ClassLoader。 */
    private ClassLoader classLoader;

    private String templateRoot;

    private String resourceRoot;

    private String messageRoot;

    private Properties properties;

    /** 构造并初始化 ClassLoader 主题。 */
    public ClassLoaderTheme(String name, Type type, ClassLoader classLoader) throws IOException {
        init(name, type, classLoader);
    }

    /** 解析 theme.properties 并设置模板/资源/消息根路径。 */
    public void init(String name, Type type, ClassLoader classLoader) throws IOException {
        this.name = name;
        this.type = type;
        this.classLoader = classLoader;

        String themeRoot = "theme/" + name + "/" + type.toString().toLowerCase() + "/";

        this.templateRoot = themeRoot;
        this.resourceRoot = themeRoot + "resources/";
        this.messageRoot = themeRoot + "messages/";
        this.properties = new Properties();

        URL p = classLoader.getResource(themeRoot + "theme.properties");
        if (p != null) {
            try (InputStream stream = p.openStream()) {
                PropertiesUtil.readCharsetAware(properties, stream);
            }
            this.parentName = properties.getProperty("parent");
            this.importName = properties.getProperty("import");
        } else {
            this.parentName = null;
            this.importName = null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getParentName() {
        return parentName;
    }

    @Override
    public String getImportName() {
        return importName;
    }

    @Override
    public Type getType() {
        return type;
    }

    /** 从 classpath 获取 FreeMarker 模板 URL。 */
    @Override
    public URL getTemplate(String name) {
        return classLoader.getResource(templateRoot + name);
    }

    /** 从主题 resources 目录读取静态资源流。 */
    @Override
    public InputStream getResourceAsStream(String path) throws IOException {
        return ResourceLoader.getResourceAsStream(resourceRoot, path);
    }

    @Override
    public Properties getMessages(Locale locale) throws IOException {
        return getMessages("messages", locale);
    }

    @Override
    protected void loadBundle(String baseBundlename, Locale locale, Properties m) throws IOException {
        URL url = classLoader.getResource(this.messageRoot + toBundleName(baseBundlename, locale) + ".properties");
        if (url != null) {
            try (InputStream stream = url.openStream()) {
                PropertiesUtil.readCharsetAware(m, stream);
            }
        }
    }

    /** 合并 realm 本地化文本与主题消息 bundle。 */
    @Override
    public Properties getEnhancedMessages(RealmModel realm, Locale locale) throws IOException {
        if (locale == null){
            return null;
        }

        Map<Locale, Properties> localeMessages = Collections.singletonMap(locale, getMessages(locale));
        return LocaleUtil.enhancePropertiesWithRealmLocalizationTexts(realm, locale, localeMessages);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

}
