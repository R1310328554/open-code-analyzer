package org.keycloak.representations.info;

import java.util.Set;

/**
 * Keycloak 服务器功能开关的 REST 表示，用于 {@link ServerInfoRepresentation} 中描述各特性的启用状态与依赖关系。
 */
public class FeatureRepresentation {
    /** 功能内部名称（配置键）。 */
    private String name;
    /** 面向管理员/UI 的显示标签。 */
    private String label;
    /** 功能类型，见 {@link FeatureType}。 */
    private FeatureType type;
    /** 当前是否已启用。 */
    private boolean isEnabled;
    /** 是否已标记为废弃；非 {@code true} 时在 JSON 中省略。 */
    private Boolean deprecated;
    /** 启用本功能前须先启用的其他功能名称集合。 */
    private Set<String> dependencies;

    /** 默认构造函数，供 JSON 反序列化使用。 */
    public FeatureRepresentation() {
    }

    /** @return 功能名称 */
    public String getName() {
        return name;
    }

    /** @param name 功能名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 显示标签 */
    public String getLabel() {
        return label;
    }

    /** @param label 显示标签 */
    public void setLabel(String label) {
        this.label = label;
    }

    /** @return 功能类型 */
    public FeatureType getType() {
        return type;
    }

    /** @param type 功能类型 */
    public void setType(FeatureType type) {
        this.type = type;
    }

    /** @return 是否已废弃 */
    public Boolean isDeprecated() {
        return deprecated;
    }

    /**
     * 设置废弃标记；仅 {@code true} 会保留，否则置为 {@code null} 以省略序列化。
     *
     * @param deprecated 是否废弃
     */
    public void setDeprecated(Boolean deprecated) {
        this.deprecated = Boolean.TRUE.equals(deprecated) ? deprecated : null;
    }

    /** @return 是否已启用 */
    public boolean isEnabled() {
        return isEnabled;
    }

    /** @param enabled 是否启用 */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    /** @return 依赖的功能名称集合 */
    public Set<String> getDependencies() {
        return dependencies;
    }

    /** @param dependencies 依赖的功能名称集合 */
    public void setDependencies(Set<String> dependencies) {
        this.dependencies = dependencies;
    }
}
