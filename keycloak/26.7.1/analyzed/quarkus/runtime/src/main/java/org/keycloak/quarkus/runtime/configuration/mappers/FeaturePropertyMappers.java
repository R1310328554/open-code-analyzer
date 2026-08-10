package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.config.FeatureOptions;
import org.keycloak.quarkus.runtime.cli.PropertyException;
import org.keycloak.quarkus.runtime.configuration.SimilarityUtil;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

/**
 * Keycloak 特性（Profile Feature）开关相关 {@link PropertyMapper} 分组：
 * 支持 {@code features}、{@code features-disabled} 及通配 {@code feature-<name>} 配置。
 */
public final class FeaturePropertyMappers implements PropertyMapperGrouping {
    /** 匹配 {@code vN} 版本后缀的正则。 */
    private static final Pattern VERSION_SUFFIX_PATTERN = Pattern.compile("^v(\\d+)$");
    /** 匹配 {@code feature:vN} 带版本特性名的正则。 */
    private static final Pattern VERSIONED_PATTERN = Pattern.compile("([^:]+):v(\\d+)");

    @Override
    public List<PropertyMapper<?>> getPropertyMappers() {
        return List.of(
                fromOption(FeatureOptions.FEATURES)
                        .paramLabel("feature")
                        .validator(FeaturePropertyMappers::validateEnabledFeature)
                        .build(),
                fromOption(FeatureOptions.FEATURES_DISABLED)
                        .paramLabel("feature")
                        .build(),
                fromOption(FeatureOptions.FEATURE)
                        .paramLabel("enabled|disabled|vX(X is version)")
                        .wildcardKeysValidator(FeaturePropertyMappers::validateSingleFeature)
                        .build()
        );
    }

    /** 校验单个通配特性键的值（enabled/disabled 或 vN 版本号）。 */
    public static void validateSingleFeature(String feature, String value) {
        if (!Profile.getAllUnversionedFeatureNames().contains(feature)) {
            throw new PropertyException(unrecognizedFeatureMessage(feature, FeatureOptions.getFeatureValues(false, false)));
        }

        Matcher matcher = VERSION_SUFFIX_PATTERN.matcher(value);
        if (matcher.matches()) {
            int version = Integer.parseInt(matcher.group(1));
            validateFeatureVersions(feature, version);
        } else if (!value.equals("enabled") && !value.equals("disabled")) {
            throw new PropertyException("Wrong value for feature '%s': %s. You can specify either 'enabled', 'disabled', or specific version (lowercase) that will be enabled".formatted(feature, value));
        }
    }

    /** 校验 {@code features} 列表中的启用项格式与版本。 */
    public static void validateEnabledFeature(String feature) {
        if (!Profile.getFeatureVersions(feature).isEmpty()) {
            return;
        }
        if (feature.equals(Profile.Feature.Type.PREVIEW.name().toLowerCase())) {
            return;
        }
        Matcher matcher = VERSIONED_PATTERN.matcher(feature);
        if (!matcher.matches()) {
            if (feature.contains(":")) {
                throw new PropertyException(String.format(
                        "%s has an invalid format for enabling a feature, expected format is feature:v{version}, e.g. docker:v1",
                        feature));
            }
            throw new PropertyException(unrecognizedFeatureMessage(feature, FeatureOptions.getFeatureValues(false)));
        }
        String unversionedFeature = matcher.group(1);
        int version = Integer.parseInt(matcher.group(2));
        validateFeatureVersions(unversionedFeature, version);
    }

    /** 生成未识别特性名的错误消息，必要时附带相似建议。 */
    private static String unrecognizedFeatureMessage(String feature, List<String> validFeatures) {
        List<String> suggestions = SimilarityUtil.findSimilar(feature, validFeatures);
        if (!suggestions.isEmpty()) {
            return "'%s' is an unrecognized feature. Did you mean: %s?".formatted(feature, String.join(", ", suggestions));
        }
        return "'%s' is an unrecognized feature, it should be one of %s".formatted(feature, validFeatures);
    }

    /** 校验指定特性版本号是否在 Profile 已知版本集合中。 */
    private static void validateFeatureVersions(String feature, int version) {
        Set<Feature> featureVersions = Profile.getFeatureVersions(feature);
        if (featureVersions.isEmpty() || featureVersions.stream().noneMatch(f -> f.getVersion() == version)) {
            throw new PropertyException(
                    String.format("Feature '%s' has an unrecognized feature version, it should be one of %s", feature,
                            featureVersions.stream().map(Feature::getVersion).map(String::valueOf).collect(Collectors.toList())));
        }
    }
}
