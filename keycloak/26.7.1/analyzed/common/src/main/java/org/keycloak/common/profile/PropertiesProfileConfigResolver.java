package org.keycloak.common.profile;

import java.util.Properties;
import java.util.function.UnaryOperator;

import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;

/**
 * 基于 {@link Properties} 或属性读取函数的 Profile 配置解析器。
 *
 * <p>读取 {@code keycloak.profile} 与 {@code keycloak.profile.feature.<feature>} 等属性，
 * 将 {@code enabled}/{@code disabled} 字符串映射为特性开关。</p>
 */
public class PropertiesProfileConfigResolver implements ProfileConfigResolver {

    private UnaryOperator<String> getter;

    public PropertiesProfileConfigResolver(Properties properties) {
        this(properties::getProperty);
    }

    public PropertiesProfileConfigResolver(UnaryOperator<String> getter) {
        this.getter = getter;
    }

    @Override
    public Profile.ProfileName getProfileName() {
        String profile = getter.apply("keycloak.profile");

        if (profile != null) {
            try {
                return Profile.ProfileName.valueOf(profile.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ProfileException(String.format("Invalid profile '%s' specified via 'keycloak.profile' property", profile));
            }
        }
        return null;
    }

    @Override
    public FeatureConfig getFeatureConfig(String feature) {
        String key = getPropertyKey(feature);
        String config = getter.apply(key);
        if (config != null) {
            switch (config) {
                case "enabled":
                    return FeatureConfig.ENABLED;
                case "disabled":
                    return FeatureConfig.DISABLED;
                default:
                    throw new ProfileException("Invalid config value '" + config + "' for feature key " + key);
            }
        }
        return FeatureConfig.UNCONFIGURED;
    }

    /** 根据 {@link Feature} 生成对应的配置属性键。 */
    public static String getPropertyKey(Feature feature) {
        return getPropertyKey(feature.getKey());
    }

    /** 将特性键转换为 {@code keycloak.profile.feature.*} 属性名（{@code -}、{@code :} 替换为 {@code _}）。 */
    public static String getPropertyKey(String feature) {
        return "keycloak.profile.feature." + feature.replaceAll("[-:]", "_");
    }
}
