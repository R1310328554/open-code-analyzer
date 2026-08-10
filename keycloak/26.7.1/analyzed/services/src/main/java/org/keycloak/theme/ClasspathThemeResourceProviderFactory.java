package org.keycloak.theme;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Classpath 主题资源提供者工厂。
 * <p>从 {@code theme-resources/} 目录提供跨主题共享的模板、静态资源与消息。</p>
 */
public class ClasspathThemeResourceProviderFactory implements ThemeResourceProviderFactory, ThemeResourceProvider {

    /** 共享主题资源根目录名。 */
    public static final String THEME_RESOURCES = "theme-resources";
    /** 共享 FreeMarker 模板目录。 */
    public static final String THEME_RESOURCES_TEMPLATES = THEME_RESOURCES + "/templates/";
    /** 共享静态资源目录。 */
    public static final String THEME_RESOURCES_RESOURCES = THEME_RESOURCES + "/resources/";
    /** 共享国际化消息 bundle 目录。 */
    public static final String THEME_RESOURCES_MESSAGES = THEME_RESOURCES + "/messages/";

    private final String id;
    protected final ClassLoader classLoader;

    /** 使用默认 id {@code classpath} 与当前线程上下文 ClassLoader。 */
    public ClasspathThemeResourceProviderFactory() {
        this("classpath", Thread.currentThread().getContextClassLoader());
    }

    /** 指定工厂 id 与 ClassLoader。 */
    public ClasspathThemeResourceProviderFactory(String id, ClassLoader classLoader) {
        this.id = id;
        this.classLoader = classLoader;
    }

    /** 工厂自身即 ThemeResourceProvider 实例。 */
    @Override
    public ThemeResourceProvider create(KeycloakSession session) {
        return this;
    }

    /** 从 theme-resources/templates 获取模板 URL。 */
    @Override
    public URL getTemplate(String name) throws IOException {
        return classLoader.getResource(THEME_RESOURCES_TEMPLATES + name);
    }

    /** 从 theme-resources/resources 读取静态资源。 */
    @Override
    public InputStream getResourceAsStream(String path) throws IOException {
        return ResourceLoader.getResourceAsStream(THEME_RESOURCES_RESOURCES, path);
    }

    /** 加载指定 locale 的消息 properties 文件。 */
    @Override
    public Properties getMessages(String baseBundlename, Locale locale) throws IOException {
        Properties messages = new Properties();
        URL resource = classLoader.getResource(THEME_RESOURCES_MESSAGES + baseBundlename + "_" + locale.toString() + ".properties");
        loadMessages(messages, resource);
        return messages;
    }

    /** 从 URL 读取 charset-aware 消息 properties 并合并。 */
    protected void loadMessages(Properties messages, URL resource) throws IOException {
        if (resource != null) {
            try (InputStream stream = resource.openStream()) {
                PropertiesUtil.readCharsetAware(messages, stream);
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

}
