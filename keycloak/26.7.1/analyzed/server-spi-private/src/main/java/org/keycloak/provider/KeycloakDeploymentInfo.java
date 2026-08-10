package org.keycloak.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keycloak 部署信息：描述 provider JAR 中包含的服务、主题与 SPI 工厂。
 * <p>由 {@link ProviderLoaderFactory} 用于加载扩展模块中的提供者。</p>
 */
public class KeycloakDeploymentInfo {

    /** 部署单元名称。 */
    private String name;
    /** 是否通过 {@link java.util.ServiceLoader} 发现服务。 */
    private boolean services;
    /** 是否包含嵌入式主题。 */
    private boolean themes;
    /** 是否包含嵌入式 theme-resources。 */
    private boolean themeResources;
    /** 按 SPI 类型分组的提供者工厂列表。 */
    private Map<Class<? extends Spi>, List<ProviderFactory>> providers = new HashMap<>();

    /** @return 是否包含任意类型的提供者内容 */
    public boolean isProvider() {
        return services || themes || themeResources || !providers.isEmpty();
    }

    /** @return 是否启用 ServiceLoader 服务发现 */
    public boolean hasServices() {
        return services;
    }

    /** 创建空的部署信息构建器。 */
    public static KeycloakDeploymentInfo create() {
        return new KeycloakDeploymentInfo();
    }

    private KeycloakDeploymentInfo() {
    }

    /** 设置部署名称。 */
    public KeycloakDeploymentInfo name(String name) {
        this.name = name;
        return this;
    }

    /** @return 部署名称 */
    public String getName() {
        return name;
    }

    /**
     * 启用通过 {@link java.util.ServiceLoader} 发现服务。
     * Enables discovery of services via {@link java.util.ServiceLoader}.
     * @return
     */
    public KeycloakDeploymentInfo services() {
        this.services = true;
        return this;
    }

    /** @return 是否包含嵌入式主题 */
    public boolean hasThemes() {
        return themes;
    }

    /**
     * 启用嵌入式主题发现。
     * Enables discovery embedded themes.
     * @return
     */
    public KeycloakDeploymentInfo themes() {
        this.themes = true;
        return this;
    }

    /** @return 是否包含嵌入式 theme-resources */
    public boolean hasThemeResources() {
        return themeResources;
    }

    /**
     * 启用嵌入式 theme-resources 发现。
     * Enables discovery of embedded theme-resources.
     * @return
     */
    public KeycloakDeploymentInfo themeResources() {
        themeResources = true;
        return this;
    }

    /** 为指定 SPI 注册提供者工厂。 */
    public void addProvider(Class<? extends Spi> spi, ProviderFactory factory) {
        providers.computeIfAbsent(spi, key -> new ArrayList<>()).add(factory);
    }

    /** @return 按 SPI 分组的提供者工厂映射 */
    public Map<Class<? extends Spi>, List<ProviderFactory>> getProviders() {
        return providers;
    }
}
