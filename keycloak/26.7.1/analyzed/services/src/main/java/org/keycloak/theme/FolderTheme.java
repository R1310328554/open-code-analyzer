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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.keycloak.models.RealmModel;
import org.keycloak.services.util.LocaleUtil;

/**
 * 文件夹主题实现。
 * <p>从本地目录读取 {@code theme.properties}、模板与资源文件。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class FolderTheme extends FileBasedTheme {

    private String parentName;
    private String importName;
    private File themeDir;
    private File resourcesDir;
    private String name;
    private Type type;
    private final Properties properties;

    /** 从主题目录加载属性、父主题与 import 配置。 */
    public FolderTheme(File themeDir, String name, Type type) throws IOException {
        this.themeDir = themeDir;
        this.name = name;
        this.type = type;
        this.properties = new Properties();

        File propertiesFile = new File(themeDir, "theme.properties");
        if (propertiesFile.isFile()) {
            try (InputStream stream = Files.newInputStream(propertiesFile.toPath())) {
                PropertiesUtil.readCharsetAware(properties, stream);
            }
            parentName = properties.getProperty("parent");
            importName = properties.getProperty("import");
        }

        resourcesDir = new File(themeDir, "resources");
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

    @Override
    public URL getTemplate(String name) throws IOException {
        File file = ResourceLoader.getFile(themeDir, name);
        return file != null && file.isFile() ? file.toURI().toURL() : null;
    }

    @Override
    public boolean hasResource(String path) throws IOException {
        var file = ResourceLoader.getFile(resourcesDir, path);
        return file != null && file.isFile();
    }

    @Override
    public InputStream getResourceAsStream(String path) throws IOException {
        return ResourceLoader.getFileAsStream(resourcesDir, path);
    }

    @Override
    public Properties getMessages(Locale locale) throws IOException {
        return getMessages("messages", locale);
    }

    /** 合法 locale/bundle 名字符校验。 */
    private static final Pattern LEGAL_LOCALE = Pattern.compile("[a-zA-Z0-9-_#]*");

    /** 从 {@code messages/} 子目录加载指定 bundle。 */
    @Override
    protected void loadBundle(String baseBundlename, Locale locale, Properties m) throws IOException {
        String filename = toBundleName(baseBundlename, locale);

        if (!LEGAL_LOCALE.matcher(filename).matches()) {
            throw new RuntimeException("Found illegal characters in locale or bundle name: " + filename);
        }

        File file = new File(themeDir, "messages" + File.separator + filename + ".properties");
        if (file.isFile()) {
            try (InputStream stream = Files.newInputStream(file.toPath())) {
                PropertiesUtil.readCharsetAware(m, stream);
            }
        }
    }

    /** 合并 Realm 本地化文本增强消息属性。 */
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
