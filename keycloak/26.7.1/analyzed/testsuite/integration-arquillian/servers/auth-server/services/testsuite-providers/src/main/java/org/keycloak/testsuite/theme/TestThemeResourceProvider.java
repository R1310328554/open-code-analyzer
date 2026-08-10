package org.keycloak.testsuite.theme;

import org.keycloak.Config;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.theme.ClasspathThemeResourceProviderFactory;

import io.quarkus.runtime.Application;

/**
 * 测试套件主题资源提供者：在 Undertow 嵌入式环境中从 classpath 加载主题资源。
 * Quarkus 会自动检测主题资源，因此此提供者仅在 Undertow 平台上启用。
 */
public class TestThemeResourceProvider extends ClasspathThemeResourceProviderFactory implements EnvironmentDependentProviderFactory {

    /** 使用 {@code test-resources} 作为主题 ID 并绑定当前类加载器。 */
    public TestThemeResourceProvider() {
        super("test-resources", TestThemeResourceProvider.class.getClassLoader());
    }

    /**
     * Quarkus 会自动检测主题资源，因此此提供者应仅在 Undertow 上启用。
     *
     * @return 若当前平台为 Undertow（无 Quarkus 应用上下文）则返回 true
     */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Application.currentApplication() == null;
    }
}
