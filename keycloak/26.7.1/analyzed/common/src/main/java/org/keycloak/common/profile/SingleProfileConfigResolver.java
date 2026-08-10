package org.keycloak.common.profile;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.keycloak.common.Profile;

/**
 * 基于 {@code feature-<name>} 选项映射的特性配置解析器。
 *
 * <p>暂不支持 Profile 名称解析，参见
 * <a href="https://github.com/keycloak/keycloak/issues/44003">keycloak#44003</a>。</p>
 */
public class SingleProfileConfigResolver implements ProfileConfigResolver {
    private final Map<String, Boolean> features;

    public SingleProfileConfigResolver(Map<String, Boolean> features) {
        this.features = Optional.ofNullable(features).orElseGet(Collections::emptyMap);
    }

    @Override
    public Profile.ProfileName getProfileName() {
        // not supporting profiles yet - see https://github.com/keycloak/keycloak/issues/44003
        return null;
    }

    @Override
    public FeatureConfig getFeatureConfig(String feature) {
        Boolean state = features.get(feature);
        if (state == null) {
            return FeatureConfig.UNCONFIGURED;
        }

        return state ? FeatureConfig.ENABLED : FeatureConfig.DISABLED;
    }
}
