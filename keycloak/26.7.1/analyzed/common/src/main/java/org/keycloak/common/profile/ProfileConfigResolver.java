package org.keycloak.common.profile;

import org.keycloak.common.Profile;

/**
 * Keycloak Profile 配置解析 SPI。
 *
 * <p>负责解析当前 Profile 名称（如 preview）以及各特性（feature）的启用/禁用/未配置状态。</p>
 */
public interface ProfileConfigResolver {

    /** 返回显式配置的 Profile 名称；未配置时返回 {@code null}。 */
    Profile.ProfileName getProfileName();

    /** 返回指定特性键的配置状态。 */
    FeatureConfig getFeatureConfig(String feature);

    /** 特性开关配置状态。 */
    public enum FeatureConfig {
        /** 显式启用。 */
        ENABLED,
        /** 显式禁用。 */
        DISABLED,
        /** 未配置，由 Profile 默认值决定。 */
        UNCONFIGURED
    }

}
