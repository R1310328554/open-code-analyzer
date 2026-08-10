package org.keycloak.common.profile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.keycloak.common.Profile;

/**
 * 基于逗号分隔特性列表的 {@link ProfileConfigResolver} 实现。
 *
 * <p>通过启用/禁用特性名称集合解析 {@link Profile.ProfileName} 与各特性开关状态；
 * 同一特性同时出现在两个列表中将抛出 {@link ProfileException}。</p>
 */
public class CommaSeparatedListProfileConfigResolver implements ProfileConfigResolver {

    private Set<String> enabledFeatures;
    private Set<String> disabledFeatures;

    /**
     * @param enabledFeatures 逗号分隔的启用特性名，可为 {@code null}
     * @param disabledFeatures 逗号分隔的禁用特性名，可为 {@code null}
     */
    public CommaSeparatedListProfileConfigResolver(String enabledFeatures, String disabledFeatures) {
        if (enabledFeatures != null) {
            this.enabledFeatures = new HashSet<>(Arrays.asList(enabledFeatures.split(",")));
        }
        if (disabledFeatures != null) {
            this.disabledFeatures = new HashSet<>(Arrays.asList(disabledFeatures.split(",")));
        }
    }

    @Override
    public Profile.ProfileName getProfileName() {
        if (enabledFeatures != null && enabledFeatures.contains(Profile.ProfileName.PREVIEW.name().toLowerCase())) {
            return Profile.ProfileName.PREVIEW;
        }
        return null;
    }

    @Override
    public FeatureConfig getFeatureConfig(String feature) {
        if (enabledFeatures != null && enabledFeatures.contains(feature)) {
            if (disabledFeatures != null && disabledFeatures.contains(feature)) {
                throw new ProfileException(feature + " is in both the enabled and disabled feature lists.");
            }
            return FeatureConfig.ENABLED;
        }
        if (disabledFeatures != null && disabledFeatures.contains(feature)) {
            return FeatureConfig.DISABLED;
        }
        return FeatureConfig.UNCONFIGURED;
    }
}
