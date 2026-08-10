package org.keycloak.quarkus.runtime.themes;

import java.io.File;
import java.util.Optional;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.theme.FolderThemeProvider;
import org.keycloak.theme.ThemeProvider;
import org.keycloak.theme.ThemeProviderFactory;

/**
 * Quarkus 文件夹主题提供方工厂：从配置目录或默认 {@link Environment#getDefaultThemeRootDir()} 加载主题。
 */
public class QuarkusFolderThemeProviderFactory implements ThemeProviderFactory {

    private static final String CONFIG_DIR_KEY = "dir";
    private FolderThemeProvider themeProvider;

    @Override
    public ThemeProvider create(KeycloakSession sessions) {
        return themeProvider;
    }

    @Override
    public void init(Config.Scope config) {
        String configDir = config.get(CONFIG_DIR_KEY);
        File rootDir = getThemeRootDirWithFallback(configDir);
        themeProvider = new FolderThemeProvider(rootDir);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "folder";
    }

    /**
     * 解析主题根目录：优先使用 {@link Config} 中的 {@code dir}，不存在则回退到 Quarkus 默认主题目录。
     *
     * @param rootDirFromConfig {@link Config} 中的目录字符串
     * @return 存在的主题根目录；均不可用则返回 {@code null}
     * @throws RuntimeException 路径不可访问时
     */
    private File getThemeRootDirWithFallback(String rootDirFromConfig) {
        return Optional.ofNullable(rootDirFromConfig).or(Environment::getDefaultThemeRootDir).map(File::new)
                .filter(File::exists).orElse(null);
    }
}
