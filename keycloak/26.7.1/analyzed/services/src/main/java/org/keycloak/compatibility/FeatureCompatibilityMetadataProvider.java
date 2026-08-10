package org.keycloak.compatibility;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.common.Profile;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 特性兼容性元数据提供者：导出各 {@link Profile.Feature} 的启用状态、
 * 版本与更新策略，用于滚动升级/集群节点兼容性校验。
 */
public class FeatureCompatibilityMetadataProvider implements CompatibilityMetadataProvider {

    /** 兼容性 provider id。 */
    public static final String ID = "feature-compatibility";

    /** 收集全部特性快照 JSON，同键保留最新启用或更高版本。 */
    @Override
    public Map<String, String> metadata() {
        Set<Profile.Feature> features = Profile.getInstance().getAllFeatures();
        Map<String, String> metadata = new HashMap<>(features.size());
        for (Profile.Feature f : features) {
            Feature feature = Feature.from(f);
            metadata.compute(f.getUnversionedKey(), (k, v) -> {
                if (v == null) {
                    return toJson(feature);
                }
                Feature existing = fromJson(v);
                // 同键冲突时保留已启用或版本号更高者
                if (!existing.enabled || feature.version > existing.version)
                    return toJson(feature);
                return v;
            });
        }
        return metadata;
    }

    /** 比对远端特性元数据：移除、版本不可滚动升级或启停违反 SHUTDOWN 策略时判不兼容。 */
    @Override
    public CompatibilityResult isCompatible(Map<String, String> other) {
        Map<String, String> currentMeta = metadata();
        // 逐项检查对端元数据中的特性条目
        for (Map.Entry<String, String> entry : other.entrySet()) {
            String featureKey = entry.getKey();
            String otherJson = entry.getValue();
            Feature otherFeature = fromJson(otherJson);

            // 当前版本已移除该特性
            if (!currentMeta.containsKey(featureKey)) {
                // 对端曾启用且策略为 SHUTDOWN 时拒绝加入集群
                if (otherFeature.enabled && otherFeature.updatePolicy == Profile.FeatureUpdatePolicy.SHUTDOWN)
                    return CompatibilityResult.incompatibleAttribute(ID, featureKey, otherJson, null);
                else
                    continue;
            }

            String json = currentMeta.get(featureKey);
            Feature feature = fromJson(json);
            // 版本不同且策略禁止滚动升级
            if (feature.version != otherFeature.version && feature.updatePolicy == Profile.FeatureUpdatePolicy.ROLLING_NO_UPGRADE)
                return CompatibilityResult.incompatibleAttribute(ID, featureKey, otherJson, json);

            // 启停状态变化且策略为 SHUTDOWN
            if (feature.enabled != otherFeature.enabled && feature.updatePolicy == Profile.FeatureUpdatePolicy.SHUTDOWN)
                return CompatibilityResult.incompatibleAttribute(ID, featureKey, otherJson, json);
        }

        // 检查本端新增且对端缺失的特性
        Map<String, String> distinct = currentMeta.entrySet().stream()
              .filter(e -> !other.containsKey(e.getKey()))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, String> entry : distinct.entrySet()) {
            String json = entry.getValue();
            Feature feature = fromJson(json);
            if (feature.enabled && feature.updatePolicy == Profile.FeatureUpdatePolicy.SHUTDOWN)
                return CompatibilityResult.incompatibleAttribute(ID, entry.getKey(), null, json);
        }
        return CompatibilityResult.providerCompatible(ID);
    }

    /** @return {@value #ID} */
    @Override
    public String getId() {
        return ID;
    }

    /** 将特性快照序列化为 JSON。 */
    static String toJson(Feature feature) {
        try {
            return JsonSerialization.mapper.writeValueAsString(feature);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Should never happen!", e);
        }
    }

    /** 从 JSON 反序列化特性快照。 */
    static Feature fromJson(String json) {
        try {
            return JsonSerialization.mapper.readValue(json, Feature.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Should never happen!", e);
        }
    }

    /** 特性兼容性快照：启用状态、版本与更新策略。 */
    record Feature(boolean enabled, int version, Profile.FeatureUpdatePolicy updatePolicy) {
        /** 从运行时 Profile 特性构建快照。 */
        static Feature from(Profile.Feature feature) {
            return new Feature(
                  Profile.isFeatureEnabled(feature),
                  feature.getVersion(),
                  feature.getUpdatePolicy()
            );
        }
    }
}
